/*******************************************************************************
 * Copyright (c) 2012, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.validation;

import static com.google.common.collect.Lists.*;
import static org.eclipse.xtext.xbase.validation.IssueCodes.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;
import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.eclipse.xtext.common.types.util.jdt.IJavaElementFinder;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.validation.IssueCodes;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XtypePackage;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * @author Holger Schill - Initial contribution and API
 * @since 2.4
 */
@SuppressWarnings("restriction")
public class XbaseUIValidator extends AbstractDeclarativeValidator {

	@Inject
	private IJavaProjectProvider projectProvider;

	@Inject
	private IJavaElementFinder javaElementFinder;
	
	@Override
	protected List<EPackage> getEPackages() {
		return newArrayList(TypesPackage.eINSTANCE, XtypePackage.eINSTANCE, XbasePackage.eINSTANCE);
	}
	
	@Check
	public void checkRestrictedType(XImportDeclaration importDeclaration){
		if (isRestrictionCheckIgnored())
			return;
		JvmType importedType = importDeclaration.getImportedType();
		if(importedType instanceof JvmDeclaredType)
			checkRestrictedType(importDeclaration, XtypePackage.Literals.XIMPORT_DECLARATION__IMPORTED_TYPE, (JvmDeclaredType) importedType);
	}

	@Check
	public void checkRestrictedType(XConstructorCall constructorCall) {
		if (isRestrictionCheckIgnored())
			return;
		JvmConstructor constructor = constructorCall.getConstructor();
		if (constructor == null)
			return;
		JvmDeclaredType declaringType = constructor.getDeclaringType();
		checkRestrictedType(constructorCall, XbasePackage.Literals.XCONSTRUCTOR_CALL__CONSTRUCTOR, declaringType);
	}

	@Check
	public void checkRestrictedType(XAbstractFeatureCall featureCall) {
		if (isRestrictionCheckIgnored())
			return;
		if (!featureCall.isTypeLiteral())
			return;
		JvmIdentifiableElement feature = featureCall.getFeature();
		if (feature instanceof JvmDeclaredType) {
			JvmDeclaredType referencedType = (JvmDeclaredType) feature;
			if (featureCall instanceof XMemberFeatureCall) {
				checkRestrictedType(featureCall, null, referencedType); // mark everything
			} else {
				checkRestrictedType(featureCall, XbasePackage.Literals.XABSTRACT_FEATURE_CALL__FEATURE, referencedType);
			}
			
		}
	}

	@Check
	public void checkRestrictedType(JvmTypeReference typeReference) {
		if (isRestrictionCheckIgnored())
			return;
		if (typeReference != null && typeReference.eResource() != null
				&& typeReference.eResource().getResourceSet() != null) {
			JvmType type = typeReference.getType();
			if (type instanceof JvmDeclaredType) {
				if (typeReference instanceof JvmParameterizedTypeReference)
					checkRestrictedType(typeReference, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE,
							(JvmDeclaredType) type);
				else
					checkRestrictedType(typeReference, null, (JvmDeclaredType) type);
			}
		}
	}

	protected boolean isRestrictionCheckIgnored() {
		return isIgnored(DISCOURAGED_REFERENCE) && isIgnored(FORBIDDEN_REFERENCE);
	}

	private static enum RestrictionKind {
		FORBIDDEN, DISCOURAGED, VALID
	}

	protected void checkRestrictedType(final EObject context, final EStructuralFeature feature, final JvmDeclaredType typeToCheck) {
		@SuppressWarnings("unchecked")
		Map<JvmDeclaredType, Pair<RestrictionKind,String>> validationContext = (Map<JvmDeclaredType, Pair<RestrictionKind,String>>) getContext().get(RestrictionKind.class);
		if (validationContext == null) {
			validationContext = Maps.newHashMap();
			getContext().put(RestrictionKind.class, validationContext);
		}
		RestrictionKind restriction = null;
		String javaProjectName = null;
		Pair<RestrictionKind,String> cached = validationContext.get(typeToCheck);
		if (cached != null) {
			restriction = cached.getKey();
			javaProjectName = cached.getValue();
		}
		if (restriction == null) {
			final IJavaElement javaElement = javaElementFinder.findElementFor(typeToCheck);
			if(javaElement == null || !(javaElement instanceof IType)) {
				validationContext.put(typeToCheck, Pair.of(RestrictionKind.VALID, null));
				return;
			}
			IJavaProject javaProject = javaElement.getJavaProject();
			if (javaProject != null) {
				javaProjectName = javaProject.getElementName();
			}
			restriction = computeRestriction(projectProvider.getJavaProject(context.eResource().getResourceSet()), 
					(IType) javaElement);
			validationContext.put(typeToCheck, Pair.of(restriction, javaProjectName));
		}
		
		if (restriction == RestrictionKind.FORBIDDEN) {
			if (javaProjectName == null) {
				IJavaProject javaProject = projectProvider.getJavaProject(context.eResource().getResourceSet());
				if (javaProject != null) {
					javaProjectName = javaProject.getElementName();
				}
			}
			
			addIssue("Access restriction: The type " + typeToCheck.getSimpleName()
					+ " is not accessible due to restriction on required project "
					+ javaProjectName, context,
					feature, IssueCodes.FORBIDDEN_REFERENCE);
		} else if (restriction == RestrictionKind.DISCOURAGED) {
			if (javaProjectName == null) {
				IJavaProject javaProject = projectProvider.getJavaProject(context.eResource().getResourceSet());
				if (javaProject != null) {
					javaProjectName = javaProject.getElementName();
				}
			}
			addIssue("Discouraged access: The type " + typeToCheck.getSimpleName()
					+ " is not accessible due to restriction on required project "
					+ javaProjectName,
					context,
					feature, IssueCodes.DISCOURAGED_REFERENCE);
		}
	}

	protected RestrictionKind computeRestriction(IJavaProject project, IType type) {
		try {
			IPackageFragmentRoot root = (IPackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			if (root == null) {
				return RestrictionKind.VALID;
			}
			IClasspathEntry entry = getResolvedClasspathEntry(project, root);
			if (entry == null) {
				return RestrictionKind.VALID;
			}
			IAccessRule[] rules = entry.getAccessRules();
			String typePath = type.getFullyQualifiedName().replace('.', '/');
			char[] typePathAsArray = typePath.toCharArray();
			for(IAccessRule rule: rules) {
				char[] patternArray = ((ClasspathAccessRule)rule).pattern;
				if (CharOperation.pathMatch(patternArray, typePathAsArray, true, '/')) {
					if (rule.getKind() == IAccessRule.K_DISCOURAGED) {
						return RestrictionKind.DISCOURAGED;
					} else if (rule.getKind() == IAccessRule.K_NON_ACCESSIBLE) {
						return RestrictionKind.FORBIDDEN;
					}
					return RestrictionKind.VALID;
				}
			}
		} catch(JavaModelException jme) {
			// ignore
		}
		return RestrictionKind.VALID;
	}
	
	/* @Nullable */ 
	protected IClasspathEntry getResolvedClasspathEntry(IJavaProject javaProject, /* @NonNull */ IPackageFragmentRoot root) throws JavaModelException {
		IClasspathEntry result = null;
		JavaProject castedProject = (JavaProject) javaProject;
		castedProject.getResolvedClasspath(); // force the resolved entry cache to be populated
		Map<IPath, IClasspathEntry> rootPathToResolvedEntries = getRootPathToResolvedEntries(castedProject.getPerProjectInfo());
		if (rootPathToResolvedEntries != null) {
			result = rootPathToResolvedEntries.get(root.getPath());
			if (result == null)
				result = rootPathToResolvedEntries.get(root.getJavaProject().getPath());
		}
		
		return result;
	}
	
	private final static MethodHandle GET_ROOT_PATH_TO_RESOLVED_ENTRIES = findAccessor();
	
	private static MethodHandle findAccessor() {
		try {
			return MethodHandles.lookup().findVirtual(PerProjectInfo.class, "getRootPathToResolvedEntries", MethodType.methodType(Map.class));
		} catch (Exception e) {
			return MethodHandles.dropArguments(MethodHandles.constant(Map.class, Collections.emptyMap()), 0, PerProjectInfo.class);
		}
	}
	
	protected Map<IPath, IClasspathEntry> getRootPathToResolvedEntries(PerProjectInfo info) throws JavaModelException {
		try {
			return (Map<IPath, IClasspathEntry>) GET_ROOT_PATH_TO_RESOLVED_ENTRIES.invoke(info);
		} catch(Throwable t) {
			throw new JavaModelException(Status.error(t.getMessage(), t));
		}
	}

}
