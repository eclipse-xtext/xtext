/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.psi.impl;

import static java.util.Collections.emptyList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.idea.lang.IXtextLanguage;
import org.eclipse.xtext.idea.resource.IResourceSetProvider;
import org.eclipse.xtext.idea.resource.PsiToEcoreAdapter;
import org.eclipse.xtext.idea.resource.PsiToEcoreTransformator;
import org.eclipse.xtext.idea.resource.ResourceDescriptionAdapter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.impl.SyntheticCompositeNode;
import org.eclipse.xtext.psi.PsiEObject;
import org.eclipse.xtext.psi.stubs.ExportedObject;
import org.eclipse.xtext.psi.stubs.XtextFileStub;
import org.eclipse.xtext.psi.tree.IGrammarAwareElementType;
import org.eclipse.xtext.resource.CompilerPhases;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.ISynchronizable;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.IndexingDataKeys;

public abstract class BaseXtextFile extends PsiFileBase {
	
	@Inject
	private CompilerPhases compilerPhases;

    @Inject
    protected IResourceSetProvider resourceSetProvider;
    
    @Inject
    protected Provider<PsiToEcoreTransformator> psiToEcoreTransformatorProvider;
    
    protected final Object resourceCacheLock;
    
    protected final CachedValue<XtextResource> resourceCache;
    
	protected BaseXtextFile(@NotNull FileViewProvider viewProvider, @NotNull Language language) {
        super(viewProvider, language);
        if (language instanceof IXtextLanguage) {
        	((IXtextLanguage) language).injectMembers(this);
        } else {
        	throw new IllegalArgumentException("Expected an Xtext language but got " + language.getDisplayName());
        }
        resourceCacheLock = new Object();
        resourceCache = CachedValuesManager.getManager(getProject()).createCachedValue(new CachedValueProvider<XtextResource>() {

			@Override
			public Result<XtextResource> compute() {
				XtextResource resource = createResource();
				return Result.create(resource, BaseXtextFile.this);
			}
        	
        }, false);
    }
	
	public IXtextLanguage getXtextLanguage() {
		return (IXtextLanguage) getLanguage();
	}
	
	public XtextResource getResource() {
		XtextResource resource = doGetResource();
		installDerivedState(resource);
		return resource;
	}
    
    protected XtextResource doGetResource() {
    	synchronized(resourceCacheLock) {
    		return CachedValuesManager.getManager(getProject()).<XtextResource, BaseXtextFile>getCachedValue(this, new CachedValueProvider<XtextResource>() {

    			@Override
				public Result<XtextResource> compute() {
    				// check whether the cached resource is still up-to-date or will be computed
					boolean hasUpToDateValue = resourceCache.hasUpToDateValue();
					XtextResource resource = resourceCache.getValue();
					// if we reuse an existing resource, we have to relink it
					if (hasUpToDateValue) {
						resource.getResourceSet().getResources().retainAll(Collections.singletonList(resource));
						resource.relink();
					}
			        installResourceDescription(resource);
					return Result.create(resource, new Object[] {
							PsiModificationTracker.MODIFICATION_COUNT, 
							BaseXtextFile.this
					});
				}
    			
    		});
    	}
    }
	
	public INode getINode(ASTNode node) {
		return PsiToEcoreAdapter.get(getResource()).getNodesMapping().get(node);
	}
	
	public List<ASTNode> getASTNodes(INode node) {
		INode originalNode = findOriginalNode(node);
		List<ASTNode> astNodes = PsiToEcoreAdapter.get(getResource()).getReverseNodesMapping().get(originalNode);
		return filterASTNodes(node, astNodes);
	}

	protected INode findOriginalNode(INode node) {
		if (node instanceof SyntheticCompositeNode) {
			return findOriginalNode(node.getParent());
		}
		return node;
	}

	protected List<ASTNode> filterASTNodes(INode node, List<ASTNode> astNodes) {
		if (astNodes == null) {
			return emptyList();
		}
		if (node instanceof SyntheticCompositeNode) {
			List<ASTNode> result = new ArrayList<ASTNode>();
			for (ASTNode astNode : astNodes) {
				IElementType elementType = astNode.getElementType();
				if (elementType instanceof IGrammarAwareElementType) {
					IGrammarAwareElementType grammarAwareElementType = (IGrammarAwareElementType) elementType;
					if (grammarAwareElementType.getGrammarElement() == node.getGrammarElement()) {
						result.add(astNode);
					}
				}
			}
			return result;
		}
		return astNodes;
	}

	protected XtextResource createResource() {    	
    	VirtualFile virtualFile = getViewProvider().getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        
        PsiToEcoreTransformator psiToEcoreTransformator = psiToEcoreTransformatorProvider.get();
        psiToEcoreTransformator.setXtextFile(this);
        
        ResourceSet resourceSet = resourceSetProvider.get(this);
        XtextResource resource = (XtextResource) resourceSet.createResource(getURI());
        resource.setParser(psiToEcoreTransformator);
        try {
            resource.load(new ByteArrayInputStream(new byte[0]), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        psiToEcoreTransformator.getAdapter().install(resource);
        
        return resource;
    }

	protected void installResourceDescription(Resource resource) {
		try {
			if (compilerPhases.isIndexing(resource)) {
				throw new IllegalStateException("Was already indexing resource set for " + resource.getURI());
			}
			compilerPhases.setIndexing(resource, true);
			/*
			 * Avoid deadlocks when a language accesses the index during indexing, 
			 * e.g. Xtend active annotations
			 */
			FileBasedIndexImpl.disableUpToDateCheckForCurrentThread();
			ResourceDescriptionAdapter.install(resource);
		} catch(OperationCanceledError e) {
			throw e.getWrapped();
		} finally {
			FileBasedIndexImpl.enableUpToDateCheckForCurrentThread();
			compilerPhases.setIndexing(resource, false);
		}
	}

	protected void installDerivedState(Resource resource) {
		if (resource instanceof DerivedStateAwareResource) {
			final DerivedStateAwareResource derivedStateAwareResource = (DerivedStateAwareResource) resource;
			// avoid synchronization of the resource
			if (!derivedStateAwareResource.isFullyInitialized()) {
				if (derivedStateAwareResource instanceof ISynchronizable<?>) {
					ISynchronizable<?> synchronizable = (ISynchronizable<?>) derivedStateAwareResource;
					try {
						synchronizable.execute(new IUnitOfWork<Void, Object>() {
	
							@Override
							public java.lang.Void exec(Object state) throws Exception {
								doInstallDerivedState(derivedStateAwareResource);
								return null;
							}
							
						});
					} catch (Exception e) {
						Exceptions.sneakyThrow(e);
					}
				} else {
					doInstallDerivedState(derivedStateAwareResource);
				}
			}
		}
	}
	
	protected void doInstallDerivedState(DerivedStateAwareResource resource) {
		try {
			boolean deliver = resource.eDeliver();
			try {
				resource.eSetDeliver(false);
				resource.installDerivedState(false);
			} finally {
				resource.eSetDeliver(deliver);
			}
		} catch (OperationCanceledError e) {
			throw e.getWrapped();
		}
	}

	public EObject getEObject(URI uri) {
    	Resource resource = getResource();
    	if (resource == null) {
    		return null;
    	}
    	return resource.getEObject(uri.fragment());
	}
    
    public IResourceDescription getResourceDescription() {
    	Resource resource = doGetResource();
    	return resource != null ? ResourceDescriptionAdapter.get(resource) : null;
    }
    
    public PsiEObject getRoot() {
    	PsiElement firstChild = getFirstChild();
    	if (firstChild instanceof PsiEObject) {
    		return (PsiEObject) firstChild;
    	}
    	return null;
    }
    
    public URI getURI() {
    	StubElement<?> stub = getStub();
    	if (stub instanceof XtextFileStub<?>) {
    		XtextFileStub<?> xtextFileStub = (XtextFileStub<?>) stub;
    		return xtextFileStub.getUri();
    	}
        return getPhysicalURI();
	}
    
    public URI getPhysicalURI() {
		PsiFile originalFile = getOriginalFile();
		if (originalFile != this && originalFile instanceof BaseXtextFile) {
			BaseXtextFile originalXtextFile = (BaseXtextFile) originalFile;
			return originalXtextFile.getURI();
		}
    	VirtualFile virtualFile = getUserData(IndexingDataKeys.VIRTUAL_FILE);
		if (virtualFile == null) {
    		virtualFile = getViewProvider().getVirtualFile();
    	}
		String url = virtualFile.getUrl();
        return URI.createURI(url);
    }

	public List<IEObjectDescription> getExportedObjects() {
		StubElement<?> stub = getStub();
		if (stub instanceof XtextFileStub<?>) {
			XtextFileStub<?> xtextFileStub = (XtextFileStub<?>) stub;
			List<IEObjectDescription> exportedObjects = new ArrayList<IEObjectDescription>();
			for (ExportedObject exportedObject : xtextFileStub.getExportedObjects()) {
				exportedObjects.add(createEObjectDescription(exportedObject.getEClass(), exportedObject.getEObjectURI(), exportedObject.getQualifiedName()));
			}
			return exportedObjects;
		}
		IResourceDescription resourceDescription = getResourceDescription();
		if (resourceDescription != null) {
			return IterableExtensions.toList(resourceDescription.getExportedObjects());
		}
		return Collections.emptyList();
	}

	protected IEObjectDescription createEObjectDescription(EClass eClass, URI eObjectURI, QualifiedName qualifiedName) {
		EFactory factory = eClass.getEPackage().getEFactoryInstance();
		InternalEObject element = (InternalEObject) factory.create(eClass);
		element.eSetProxyURI(eObjectURI);
		return EObjectDescription.create(qualifiedName, element);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getName();
	}

}