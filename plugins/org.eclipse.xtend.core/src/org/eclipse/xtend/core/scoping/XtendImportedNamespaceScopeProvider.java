/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.scoping;

import static java.util.Collections.*;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.jvmmodel.AnonymousClassUtil;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.xtend.AnonymousClass;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeParameterDeclarator;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.xtext.AbstractTypeScope;
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider;
import org.eclipse.xtext.linking.impl.ImportedNamesAdapter;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.CompilerPhases;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.scoping.impl.MultimapBasedSelectable;
import org.eclipse.xtext.scoping.impl.SelectableBasedScope;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.scoping.AbstractNestedTypeAwareImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;
import org.eclipse.xtext.xbase.scoping.batch.ConstructorTypeScopeWrapper;
import org.eclipse.xtext.xbase.typesystem.util.IVisibilityHelper;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;
import org.eclipse.xtext.xtype.XtypePackage;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Jan Koehnlein - Initial contribution and API
 * @author Sebastian Zarnekow - Improved support for nested types in connection with imports
 */
public class XtendImportedNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {

	public static final QualifiedName OLD_DATA_ANNOTATION = QualifiedName.create("org","eclipse","xtend","lib", "Data");
	public static final QualifiedName OLD_PROPERTY_ANNOTATION = QualifiedName.create("org","eclipse","xtend","lib", "Property");
	public static final QualifiedName XBASE_LIB = XImportSectionNamespaceScopeProvider.XBASE_LIB;
	public static final QualifiedName JAVA_LANG = XImportSectionNamespaceScopeProvider.JAVA_LANG;
	
	@Inject
	private AbstractTypeScopeProvider typeScopeProvider;
	
	@Inject
	private IQualifiedNameConverter qualifiedNameConverter;
	
	@Inject
	private IResourceScopeCache resourceScopeCache;
	
	@Inject
	private CompilerPhases compilerPhases;
	
	@Inject 
	private IXtendJvmAssociations associations;
	
	@Inject
	private AnonymousClassUtil anonymousClassUtil;
	
	@Inject
	private ResourceDescriptionsProvider resourceDescriptionsProvider;
	
	@Override
	public IScope getScope(final EObject context, final EReference reference) {
		EClass referenceType = reference.getEReferenceType();
		if (TypesPackage.Literals.JVM_TYPE.isSuperTypeOf(referenceType)) {
			if (context instanceof XImportDeclaration) {
				Resource resource = context.eResource();
				IJvmTypeProvider typeProvider = typeScopeProvider.getTypeProvider(resource.getResourceSet());
				AbstractTypeScope typeScope = typeScopeProvider.createTypeScope(typeProvider, null);
				IResourceDescriptions descriptions = resourceDescriptionsProvider.getResourceDescriptions(context.eResource());
				IResourceDescription resourceDescription = descriptions.getResourceDescription(resource.getURI());
				if (resourceDescription != null) {
					typeScope = new LocalResourceFilteringTypeScope(typeScope, resourceDescription);
				}
				RecordingTypeScope recordingTypeScope = new RecordingTypeScope(typeScope, getImportedNamesSet(resource));
				//TODO this scope doesn't support binary syntax for inner types. It should be a KnownTypes scope which doesn't allow simple names
				// Unfortunately I cannot use a RecordingTypeScope as a parent as it is not compatible...
				IScope scope = SelectableBasedScope.createScope(recordingTypeScope, getAllDescriptions(resource), reference.getEReferenceType(), false);
				return scope;
			}
			final XtendFile xtendFile = getXtendFile(context);
			final Resource resource = xtendFile.eResource();
			AbstractScope result = resourceScopeCache.get("type.scope", xtendFile.eResource(), new Provider<AbstractScope>() {
				@Override
				public AbstractScope get() {
					IJvmTypeProvider typeProvider = typeScopeProvider.getTypeProvider(resource.getResourceSet());
					AbstractTypeScope typeScope = typeScopeProvider.createTypeScope(typeProvider, null);
					IResourceDescriptions descriptions = resourceDescriptionsProvider.getResourceDescriptions(context.eResource());
					IResourceDescription resourceDescription = descriptions.getResourceDescription(resource.getURI());
					if (resourceDescription != null) {
						typeScope = new LocalResourceFilteringTypeScope(typeScope, resourceDescription);
					}
					RecordingTypeScope recordingTypeScope = new RecordingTypeScope(typeScope, getImportedNamesSet(resource));
					AbstractScope rootTypeScope = getRootTypeScope(xtendFile, recordingTypeScope);
					AbstractScope importScope = getImportScope(xtendFile.getImportSection(), rootTypeScope, recordingTypeScope);
					AbstractScope localTypes = getResourceTypeScope(xtendFile.eResource(), xtendFile.getPackage(), importScope);
					AbstractScope primitiveAware = new PrimitiveAwareScope(localTypes, typeScope);
					AbstractScope caching = new CachingTypeScope(primitiveAware);
					return caching;
				}
			});
			if(context instanceof AnonymousClass) {
				// necessary for the super type of an anonymous class expression
				JvmDeclaredType inferredAnonymousType = associations.getInferredType((AnonymousClass) context);
				if(inferredAnonymousType != null)
					result = new LocalTypeScope(singletonList(inferredAnonymousType), result);
			}
			XtendMember syntacticContainer = EcoreUtil2.getContainerOfType(context, XtendMember.class);
			if (syntacticContainer != null) {
				IScope containerScope = getContainerScope(syntacticContainer, result);
				return containerScope;
			}
			return result;
		} else if (TypesPackage.Literals.JVM_CONSTRUCTOR.isSuperTypeOf(referenceType)) {
			IScope typeScope = getScope(context, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE);
			// this is not called from the type resolution where we want to allow constructors to link to interfaces
			// in order to improve the error message, therefore we use a strict wrapper here
			IScope result = new ConstructorTypeScopeWrapper(context, IVisibilityHelper.ALL, typeScope, true /* strict scope */);
			return result;
		} else {
			throw new IllegalArgumentException("Unexpected global request for " + reference);
		}
	}
	
	protected Set<QualifiedName> getImportedNamesSet(Resource resource) {
		ImportedNamesAdapter adapter = getImportedNamesAdapter(resource);
		return adapter.getImportedNames();
	}
	
	protected ImportedNamesAdapter getImportedNamesAdapter(Resource resource) {
		return ImportedNamesAdapter.findOrInstall(resource);
	}

	protected XtendFile getXtendFile(final EObject context) {
		Resource resource = context.eResource();
		XtendFile result = (XtendFile) resource.getContents().get(0);
		return result;
	}
	
	protected JvmDeclaredType getSuperTypeOfLocalTypeNonResolving(JvmDeclaredType declarator) {
		AnonymousClass anonymousClass = associations.getAnonymousClass(declarator);
		if (anonymousClass != null) {
			IScope typeScope = getScope(anonymousClass, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE);
			JvmDeclaredType superType = anonymousClassUtil.getSuperTypeNonResolving(anonymousClass, typeScope);
			if (superType != null) {
				return superType;
			}
		}
		return null;
	}
	
	private AbstractScope getContainerScope(XtendMember syntacticContainer, AbstractScope result) {
		JvmDeclaredType innermost = null;
		List<List<JvmTypeParameter>> typeParameters = null;
		XtendMember workWithMe = syntacticContainer;
		while(workWithMe != null) {
			EObject derivedJvmElement = getAssociations().getPrimaryJvmElement(workWithMe);
			// scope for JvmTypeParameterDeclarator
			if (derivedJvmElement instanceof JvmTypeParameterDeclarator) {
				JvmTypeParameterDeclarator parameterDeclarator = (JvmTypeParameterDeclarator) derivedJvmElement;
				List<JvmTypeParameter> current = parameterDeclarator.getTypeParameters();
				if (!current.isEmpty()) {
					if (typeParameters == null) {
						typeParameters = Lists.newArrayListWithCapacity(3);
					}
					typeParameters.add(current);
				}
			}
			if (innermost == null && derivedJvmElement instanceof JvmDeclaredType) {
				if (syntacticContainer != workWithMe) { // prevent stackoverflow / cyclic resolution
					innermost = (JvmDeclaredType) derivedJvmElement;
				}
			}
			EObject container = workWithMe;
			do {
				container = container.eContainer();
				if (container == null) {
					if (typeParameters == null) {
						if (innermost == null)
							return result;
						return new NestedTypesScope(innermost, this, result);
					}
					TypeParameterScope typeParameterScope = new TypeParameterScope(typeParameters, result);
					if (innermost == null) {
						return typeParameterScope;
					}
					return new NestedTypesScope(innermost, this, typeParameterScope);
				}
			} while (!(container instanceof XtendMember));
			workWithMe = (XtendMember) container;
		}
		return result;
	}

	private AbstractScope getResourceTypeScope(Resource resource, String packageName, AbstractScope parent) {
		List<EObject> contents = resource.getContents();
		List<JvmType> knownTypes = Lists.newArrayListWithExpectedSize(contents.size() - 1);
		for(EObject content: contents) {
			if (content instanceof JvmType) {
				if (content instanceof JvmDeclaredType) {
					if (Strings.equal(packageName, ((JvmDeclaredType) content).getPackageName())) {
						knownTypes.add((JvmType) content);	
					}
				} else {
					knownTypes.add((JvmType) content);
				}
			}
		}
		if (knownTypes.isEmpty())
			return parent;
		return new KnownTypesScope(knownTypes, parent);
	}

	private AbstractScope getImportScope(XImportSection importSection, AbstractScope parent, RecordingTypeScope typeScope) {
		if (importSection == null)
			return parent;
		List<XImportDeclaration> importDeclarations = importSection.getImportDeclarations();
		if (importDeclarations.isEmpty()) {
			return parent;
		}
		List<ImportNormalizer> wildcardImports = null;
		List<JvmType> concreteImports = null;
		List<QualifiedName> importedNames = null;
		boolean hasLegacyImport = false;
		for(XImportDeclaration importDeclaration: importDeclarations) {
			if (!importDeclaration.isStatic()) {
				if (importDeclaration.isWildcard()) {
					if (wildcardImports == null) {
						wildcardImports = Lists.newArrayListWithCapacity(4);
					}
					String importedNamespace = importDeclaration.getImportedNamespace();
					importedNamespace = importedNamespace.substring(0, importedNamespace.length() - 2);
					QualifiedName qualifiedImportedNamespace = qualifiedNameConverter.toQualifiedName(importedNamespace);
					wildcardImports.add(AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(qualifiedImportedNamespace, true, false));
				} else {
					JvmDeclaredType importedType = null;
					if (compilerPhases.isIndexing(importSection)) {
						EObject proxy = (EObject) importDeclaration.eGet(XtypePackage.Literals.XIMPORT_DECLARATION__IMPORTED_TYPE, false);
						if (proxy.eIsProxy()) {
							URI uri = ((InternalEObject)proxy).eProxyURI();
							importedType = (JvmDeclaredType) importSection.eResource().getResourceSet().getEObject(uri, true);
						} 
					} else {
						importedType = importDeclaration.getImportedType();
					}
					if (importedType != null && !importedType.eIsProxy()) {
						if (concreteImports == null || importedNames == null /* to make JDT happy */) {
							concreteImports = Lists.newArrayListWithCapacity(10);
							importedNames = Lists.newArrayListWithCapacity(10);
						}
						concreteImports.add(importedType);
						if (importedType.eContainer() instanceof JvmDeclaredType) {
							String importSyntax = getImportsConfiguration().getLegacyImportSyntax(importDeclaration);
							if (importSyntax != null) {
								hasLegacyImport = true;
								importedNames.add(getQualifiedNameConverter().toQualifiedName(importSyntax));
							} else
								importedNames.add(null);
						} else {
							importedNames.add(null);
						}
					}
				}
			}
		}
		return getImportScope(wildcardImports, concreteImports, hasLegacyImport ? importedNames : null, parent, typeScope);
	}

	private AbstractScope getImportScope(List<ImportNormalizer> wildcardImports, List<JvmType> concreteImports, List<QualifiedName> importedNames,
			AbstractScope parent, RecordingTypeScope typeScope) {
		AbstractScope result = parent;
		if (wildcardImports != null) {
			result = new TypeScopeWithWildcardImports(wildcardImports, typeScope, result);
		}
		if (concreteImports != null) {
			if (importedNames != null)
				result = new LegacyKnownTypesScope(concreteImports, importedNames, result);
			else
				result = new KnownTypesScope(concreteImports, result);
		}
		return result;
	}
	
	protected AbstractScope getRootTypeScope(XtendFile rootContainer, RecordingTypeScope typeScope) {
		String packageName = rootContainer.getPackage();
		final ImportNormalizer[][] implicitImports;
		if (packageName != null && packageName.length() > 0) {
			QualifiedName packageQualifiedName = qualifiedNameConverter.toQualifiedName(packageName);
			implicitImports = new ImportNormalizer[][] {
				{
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(packageQualifiedName, true, false),
				},
				{
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(JAVA_LANG, true, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(XBASE_LIB, true, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(OLD_DATA_ANNOTATION, false, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(OLD_PROPERTY_ANNOTATION, false, false),
				}
			};
		} else {
			implicitImports = new ImportNormalizer[][] {
				{
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(JAVA_LANG, true, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(XBASE_LIB, true, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(OLD_DATA_ANNOTATION, false, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(OLD_PROPERTY_ANNOTATION, false, false),
				}
			};
		}
		return new TypeScopeWithWildcardImports(implicitImports, typeScope);
	}

	@Override
	protected ISelectable internalGetAllDescriptions(final Resource resource) {
		List<IEObjectDescription> descriptions = Lists.newArrayList();
		for(EObject content: resource.getContents()) {
			if (content instanceof JvmDeclaredType) {
				doGetAllDescriptions((JvmDeclaredType) content, descriptions);
			}
		}
		return new MultimapBasedSelectable(descriptions);
	}

	private void doGetAllDescriptions(JvmDeclaredType type, List<IEObjectDescription> descriptions) {
		descriptions.add(EObjectDescription.create(getQualifiedNameConverter().toQualifiedName(type.getIdentifier()), type));
		for(JvmMember member: type.getMembers()) {
			if (member instanceof JvmDeclaredType) {
				doGetAllDescriptions((JvmDeclaredType) member, descriptions);
			}
		}
	}
	
}
