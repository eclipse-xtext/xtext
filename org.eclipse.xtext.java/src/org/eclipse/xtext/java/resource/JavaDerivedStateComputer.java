/**
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.java.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.access.binary.BinaryClass;
import org.eclipse.xtext.common.types.access.binary.asm.ClassFileBytesAccess;
import org.eclipse.xtext.common.types.access.binary.asm.JvmDeclaredTypeBuilder;
import org.eclipse.xtext.common.types.descriptions.EObjectDescriptionBasedStubGenerator;
import org.eclipse.xtext.jdt.facade.JdtFacade;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.parser.antlr.IReferableElementsUnloader;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

import com.google.inject.Inject;

public class JavaDerivedStateComputer {

	private static final Logger LOG = Logger.getLogger(JavaDerivedStateComputer.class);

	@Inject
	private IReferableElementsUnloader unloader;

	@Inject
	private EObjectDescriptionBasedStubGenerator stubGenerator;

	@Inject
	private IResourceDescriptionsProvider resourceDescriptionsProvider;

	public void discardDerivedState(Resource resource) {
		EList<EObject> resourcesContentsList = resource.getContents();
		for (EObject eObject : resourcesContentsList) {
			unloader.unloadRoot(eObject);
		}
		resourcesContentsList.clear();
		if (isInfoFile(resource)) {
			return;
		}
		if (getCompilationUnit(resource) != null) {
			ClassFileCache classFileCache = findOrCreateClassFileCache(resource.getResourceSet());
			classFileCache.addResourceToCompile(resource);
		}
	}

	public void installStubs(Resource resource) {
		if (isInfoFile(resource)) {
			return;
		}
		CompilationUnit compilationUnit = getCompilationUnit(resource);
		ProblemReporter problemReporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				getCompilerOptions(resource), new DefaultProblemFactory());
		Parser parser = new Parser(problemReporter, true);
		CompilationResult compilationResult = new CompilationResult(compilationUnit, 0, 1, -1);
		CompilationUnitDeclaration result = parser.dietParse(compilationUnit, compilationResult);
		if (result.types != null) {
			for (TypeDeclaration type : result.types) {
				ImportReference currentPackage = result.currentPackage;
				String packageName = null;
				if (currentPackage != null) {
					char[][] importName = currentPackage.getImportName();
					if (importName != null) {
						packageName = CharOperation.toString(importName);
					}
				}
				JvmDeclaredType jvmType = createType(type, packageName);
				resource.getContents().add(jvmType);
			}
		}
	}

	public JvmDeclaredType createType(TypeDeclaration type, String packageName) {
		JvmDeclaredType jvmType = null;
		switch (TypeDeclaration.kind(type.modifiers)) {
		case TypeDeclaration.CLASS_DECL:
			jvmType = TypesFactory.eINSTANCE.createJvmGenericType();
			break;
		case TypeDeclaration.INTERFACE_DECL:
			jvmType = TypesFactory.eINSTANCE.createJvmGenericType();
			((JvmGenericType) jvmType).setInterface(true);
			break;
		case TypeDeclaration.ENUM_DECL:
			jvmType = TypesFactory.eINSTANCE.createJvmEnumerationType();
			break;
		case TypeDeclaration.ANNOTATION_TYPE_DECL:
			jvmType = TypesFactory.eINSTANCE.createJvmAnnotationType();
			break;
		case TypeDeclaration.RECORD_DECL:
			jvmType = TypesFactory.eINSTANCE.createJvmGenericType();
			break;
		default:
			throw new IllegalArgumentException("Cannot handle type " + type.toString());
		}
		jvmType.setPackageName(packageName);
		jvmType.setSimpleName(String.valueOf(type.name));
		if (jvmType instanceof JvmGenericType) {
			if (type.typeParameters != null) {
				for (TypeParameter typeParam : type.typeParameters) {
					JvmTypeParameter jvmTypeParam = TypesFactory.eINSTANCE.createJvmTypeParameter();
					jvmTypeParam.setName(String.valueOf(typeParam.name));
					((JvmGenericType) jvmType).getTypeParameters().add(jvmTypeParam);
				}
			}
		}
		if (type.memberTypes != null) {
			for (TypeDeclaration nestedType : type.memberTypes) {
				JvmDeclaredType nested = createType(nestedType, null);
				jvmType.getMembers().add(nested);
			}
		}
		return jvmType;
	}

	public CompilationUnit getCompilationUnit(Resource resource) {
		return ((JavaResource) resource).getCompilationUnit();
	}

	protected ClassFileCache findOrCreateClassFileCache(ResourceSet rs) {
		ClassFileCache cache = ClassFileCache.findInEmfObject(rs);
		if (cache == null) {
			cache = new ClassFileCache();
			cache.attachToEmfObject(rs);
		}
		return cache;
	}

	public void installFull(Resource resource) {
		if (isInfoFile(resource)) {
			return;
		}
		ClassFileCache classFileCache = findOrCreateClassFileCache(resource.getResourceSet());
		CompilationUnit compilationUnit = getCompilationUnit(resource);
		ClassLoader classLoader = getClassLoader(resource);
		IResourceDescriptions data = resourceDescriptionsProvider.getResourceDescriptions(resource.getResourceSet());
		if ((data == null)) {
			throw new IllegalStateException("No index installed");
		}
		Procedure2<? super List<String>, ? super Map<String, byte[]>> initializer = (List<String> topLevelTypes,
				Map<String, byte[]> classMap) -> {
			InMemoryClassLoader inMemClassLoader = new InMemoryClassLoader(classMap, classLoader);
			for (String topLevel : topLevelTypes) {
				try {
					BinaryClass binaryClass = new BinaryClass(topLevel, inMemClassLoader);
					ClassFileBytesAccess classFileBytesAccess = new ClassFileBytesAccess();
					JvmDeclaredTypeBuilder builder = new JvmDeclaredTypeBuilder(binaryClass, classFileBytesAccess,
							inMemClassLoader);
					JvmDeclaredType type = builder.buildType();
					resource.getContents().add(type);
				} catch (Throwable t) {
					throw new IllegalStateException("Could not load type '" + topLevel + "'", t);
				}
			}
		};
		boolean wasCached = classFileCache.popCompileResult(compilationUnit.fileName, initializer);
		if (!wasCached) {
			Set<CompilationUnit> unitsToCompile = classFileCache.drainResourcesToCompile().stream()
					.map((r) -> getCompilationUnit(r)).collect(Collectors.toSet());
			unitsToCompile.add(compilationUnit);
			IndexAwareNameEnvironment nameEnv = new IndexAwareNameEnvironment(resource, classLoader, data,
					stubGenerator, classFileCache);
			ICompilerRequestor compilerRequestor = new ICompilerRequestor() {

				@Override
				public void acceptResult(CompilationResult result) {
					for (ClassFile cls : result.getClassFiles()) {
						QualifiedName key = QualifiedName.create(CharOperation.toStrings(cls.getCompoundName()));
						classFileCache.computeIfAbsent(key, (QualifiedName name) -> {
							try {
								return new ClassFileReader(cls.getBytes(), cls.fileName());
							} catch (Throwable e) {
								throw Exceptions.sneakyThrow(e);
							}
						});
					}
					Map<String, byte[]> map = new HashMap<>();
					List<String> topLevelTypes = new ArrayList<>();
					for (ClassFile cf : result.getClassFiles()) {
						String className = CharOperation.toString(cf.getCompoundName());
						map.put(className, cf.getBytes());
						if (!cf.isNestedType) {
							topLevelTypes.add(className);
						}
					}
					if (Arrays.equals(result.fileName, compilationUnit.fileName)) {
						initializer.apply(topLevelTypes, map);
					} else {
						classFileCache.addCompileResult(result.fileName, topLevelTypes, map);
					}
				}

			};
			DefaultProblemFactory problemFactory = new DefaultProblemFactory();
			org.eclipse.jdt.internal.compiler.Compiler compiler = new org.eclipse.jdt.internal.compiler.Compiler(
					nameEnv, DefaultErrorHandlingPolicies.proceedWithAllProblems(), getCompilerOptions(resource),
					compilerRequestor, problemFactory);
			compiler.compile(unitsToCompile.toArray(new ICompilationUnit[0]));
		}
	}

	protected boolean isInfoFile(Resource resource) {
		String name = resource.getURI().trimFileExtension().lastSegment();
		return "package-info".equals(name) || "module-info".equals(name);
	}

	protected CompilerOptions getCompilerOptions() {
		return getCompilerOptions(((JavaConfig) null));
	}

	protected CompilerOptions getCompilerOptions(Resource resource) {
		if (resource != null) {
			ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet != null) {
				return getCompilerOptions(resourceSet);
			}
		}
		return null;
	}

	protected CompilerOptions getCompilerOptions(ResourceSet resourceSet) {
		return getCompilerOptions(JavaConfig.findInEmfObject(resourceSet));
	}

	protected CompilerOptions getCompilerOptions(JavaConfig javaConfig) {
		JavaVersion sourceVersion = JavaVersion.DEFAULT;
		JavaVersion targetVersion = JavaVersion.DEFAULT;
		if (javaConfig != null) {
			if (javaConfig.getJavaSourceLevel() != null) {
				sourceVersion = javaConfig.getJavaSourceLevel();
			}
			if (javaConfig.getJavaTargetLevel() != null) {
				targetVersion = javaConfig.getJavaTargetLevel();

			}
		}
		if (JavaVersion.JAVA7.equals(sourceVersion)) {
			JavaDerivedStateComputer.LOG.warn(
					"The java source language has been configured with Java 7. JDT will not produce signature information for generic @Override methods in this version, which might lead to follow up issues.");
		}
		long sourceLevel = toJdtVersion(sourceVersion);
		long targetLevel = toJdtVersion(targetVersion);
		CompilerOptions compilerOptions = new CompilerOptions();
		compilerOptions.targetJDK = targetLevel;
		compilerOptions.sourceLevel = sourceLevel;
		compilerOptions.produceMethodParameters = true;
		compilerOptions.produceReferenceInfo = true;
		compilerOptions.complianceLevel = sourceLevel;
		JdtFacade.setOriginalSourceLevel(compilerOptions, targetLevel);
		JdtFacade.setOriginalComplianceLevel(compilerOptions, targetLevel);
		JdtFacade.setInlineJsrBytecode(compilerOptions, true);
		return compilerOptions;
	}

	protected long toJdtVersion(JavaVersion version) {
		return version.toJdtClassFileConstant();
	}

	protected ClassLoader getClassLoader(Resource it) {
		ResourceSet resourceSet = it.getResourceSet();
		return ((ClassLoader) ((XtextResourceSet) resourceSet).getClasspathURIContext());
	}

}
