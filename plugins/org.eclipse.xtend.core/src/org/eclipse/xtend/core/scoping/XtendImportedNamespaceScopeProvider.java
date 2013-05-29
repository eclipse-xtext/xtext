/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.scoping;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmDeclaredType;
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
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.scoping.AbstractNestedTypeAwareImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;
import org.eclipse.xtext.xbase.scoping.batch.ConstructorTypeScopeWrapper;
import org.eclipse.xtext.xbase.typesystem.util.IVisibilityHelper;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Jan Koehnlein - Initial contribution and API
 * @author Sebastian Zarnekow - Improved support for nested types in connection with imports
 */
public class XtendImportedNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {

	public static final QualifiedName XTEND_LIB = QualifiedName.create("org","eclipse","xtend","lib");
	public static final QualifiedName XBASE_LIB = XImportSectionNamespaceScopeProvider.XBASE_LIB;
	public static final QualifiedName JAVA_LANG = XImportSectionNamespaceScopeProvider.JAVA_LANG;
	
	@Inject
	private AbstractTypeScopeProvider typeScopeProvider;
	
	@Inject
	private IQualifiedNameConverter qualifiedNameConverter;
	
	@Inject
	private IResourceScopeCache resourceScopeCache;
	
	@Override
	public IScope getScope(final EObject context, final EReference reference) {
		EClass referenceType = reference.getEReferenceType();
		if (TypesPackage.Literals.JVM_TYPE.isSuperTypeOf(referenceType)) {
			if (context instanceof XImportDeclaration) {
				Resource resource = context.eResource();
				IJvmTypeProvider typeProvider = typeScopeProvider.getTypeProvider(resource.getResourceSet());
				AbstractTypeScope typeScope = typeScopeProvider.createTypeScope(typeProvider, null);
				return new RecordingTypeScope(typeScope, getImportedNamesSet(resource));
			}
			final XtendFile xtendFile = getXtendFile(context);
			final Resource resource = xtendFile.eResource();
			AbstractScope result = resourceScopeCache.get("type.scope", xtendFile.eResource(), new Provider<AbstractScope>() {
				public AbstractScope get() {
					IJvmTypeProvider typeProvider = typeScopeProvider.getTypeProvider(resource.getResourceSet());
					AbstractTypeScope typeScope = typeScopeProvider.createTypeScope(typeProvider, null);
					RecordingTypeScope recordingTypeScope = new RecordingTypeScope(typeScope, getImportedNamesSet(resource));
					AbstractScope rootTypeScope = getRootTypeScope(xtendFile, recordingTypeScope);
					AbstractScope importScope = getImportScope(xtendFile.getImportSection(), rootTypeScope, recordingTypeScope);
					AbstractScope localTypes = getLocalTypeScope(xtendFile.eResource(), xtendFile.getPackage(), importScope);
					AbstractScope primitiveAware = new PrimitiveAwareScope(localTypes, typeScope);
					AbstractScope caching = new CachingTypeScope(primitiveAware);
					return caching;
				}
			});
			XtendMember syntacticContainer = EcoreUtil2.getContainerOfType(context, XtendMember.class);
			if (syntacticContainer != null) {
				result = getContainerScope(syntacticContainer, result);
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
		ImportedNamesAdapter adapter = ImportedNamesAdapter.find(resource);
		if (adapter!=null)
			return adapter;
		ImportedNamesAdapter importedNamesAdapter = new ImportedNamesAdapter();
		resource.eAdapters().add(importedNamesAdapter);
		return importedNamesAdapter;
	}

	protected XtendFile getXtendFile(final EObject context) {
		Resource resource = context.eResource();
		XtendFile result = (XtendFile) resource.getContents().get(0);
		return result;
	}
	
	private AbstractScope getContainerScope(XtendMember syntacticContainer, AbstractScope result) {
		List<List<JvmTypeParameter>> typeParameters = null;
		while(syntacticContainer != null) {
			Set<EObject> elements = getAssociations().getJvmElements(syntacticContainer);
			for (EObject derivedJvmElement : elements) {
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
			}
			EObject container = syntacticContainer.eContainer();
			if (container instanceof XtendMember) {
				syntacticContainer = (XtendMember) container;
			} else {
				if (typeParameters == null)
					return result;
				return new TypeParameterScope(typeParameters, result);
			}
		}
		return result;
	}

	private AbstractScope getLocalTypeScope(Resource resource, String packageName, AbstractScope parent) {
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
					JvmDeclaredType importedType = importDeclaration.getImportedType();
					if (!importedType.eIsProxy()) {
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
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(XTEND_LIB, true, false),
				}
			};
		} else {
			implicitImports = new ImportNormalizer[][] {
				{
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(JAVA_LANG, true, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(XBASE_LIB, true, false),
					AbstractNestedTypeAwareImportNormalizer.createNestedTypeAwareImportNormalizer(XTEND_LIB, true, false),
				}
			};
		}
		return new TypeScopeWithWildcardImports(implicitImports, typeScope);
	}

}
