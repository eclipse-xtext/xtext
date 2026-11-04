/**
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.testing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.lib.Exceptions;

import com.google.common.collect.Lists;

/**
 * @since 2.9
 */
public class InMemoryJavaCompiler {
	
	private static final Logger logger = Logger.getLogger(InMemoryJavaCompiler.class);
	
	private static class ClassLoaderBasedNameEnvironment implements INameEnvironment {
		private final ClassLoader classLoader;

		private Map<String, NameEnvironmentAnswer> cache = new HashMap<>();

		public ClassLoaderBasedNameEnvironment(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public void cleanup() {
			cache.clear();
		}

		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
			try {
				String fileName = new String(CharOperation.concatWith(compoundTypeName, '/')) + ".class";
				if (cache.containsKey(fileName)) {
					return cache.get(fileName);
				}
				URL url = classLoader.getResource(fileName);
				if (url == null) {
					cache.put(fileName, null);
					return null;
				}
				ClassFileReader reader = null;
				try (InputStream in = url.openStream()) {
					reader = ClassFileReader.read(in, fileName);
				}
				if (reader == null) {
					return null;
				}
				NameEnvironmentAnswer result = new NameEnvironmentAnswer(reader, null);
				cache.put(fileName, result);
				return result;
			} catch (ClassFormatException | IOException e) {
				throw Exceptions.sneakyThrow(e);
			}
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
			try {
				String fileName = new String(CharOperation.concatWith(packageName, '/')) + "/"
						+ String.valueOf(typeName) + ".class";
				if (cache.containsKey(fileName)) {
					return cache.get(fileName);
				}
				URL url = classLoader.getResource(fileName);
				if (url == null) {
					cache.put(fileName, null);
					return null;
				}
				ClassFileReader reader = null;
				try (InputStream in = url.openStream()) {
					reader = ClassFileReader.read(in, fileName);
				}
				if (reader == null) {
					return null;
				}
				NameEnvironmentAnswer result = new NameEnvironmentAnswer(reader, null);
				cache.put(fileName, result);
				return result;
			} catch (ClassFormatException | IOException e) {
				throw Exceptions.sneakyThrow(e);
			}
		}

		@Override
		public boolean isPackage(char[][] parentPackageName, char[] packageName) {
			return Character.isLowerCase(packageName[0]);
		}

	}

	static class ByteClassLoader extends ClassLoader {
		private Map<String, byte[]> classMap;

		public ByteClassLoader(Map<String, byte[]> classMap, ClassLoader parent) {
			super(parent);
			this.classMap = classMap;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			byte[] bytes = classMap.get(name);
			if (bytes == null) {
				return super.findClass(name);
			} else {
				return defineClass(name, bytes, 0, bytes.length);
			}
		}

		@Override
		protected URL findResource(String path) {
			try {
				if (path.endsWith(".class")) {
					byte[] bytes = classMap.get(path.substring(0, path.length() - 6).replace("/", "."));
					if (bytes != null) {
						return new URL("in-memory", null, -1, path, new URLStreamHandler() {
							@Override
							protected URLConnection openConnection(URL it) throws IOException {
								return new URLConnection(it) {
									@Override
									public void connect() {
									}

									@Override
									public InputStream getInputStream() {
										return new ByteArrayInputStream(bytes);
									}
								};
							}
						});
					}
				}
				return null;
			} catch (MalformedURLException e) {
				throw Exceptions.sneakyThrow(e);
			}
		}
	}

	public static class Result {
		private final Set<CategorizedProblem> compilationProblems = new LinkedHashSet<>();

		private final HashMap<String, byte[]> classMap = new HashMap<String, byte[]>();

		private final ClassLoader parentClassLoader;

		public Result(ClassLoader parentClassLoader) {
			this.parentClassLoader = parentClassLoader;
		}

		public ClassLoader getClassLoader() {
			return new ByteClassLoader(classMap, parentClassLoader);
		}

		public Set<CategorizedProblem> getCompilationProblems() {
			return compilationProblems;
		}
	}

	private final INameEnvironment nameEnv;

	private final ClassLoader parentClassLoader;

	private final CompilerOptions compilerOptions;

	public InMemoryJavaCompiler(ClassLoader parent, JavaVersion javaVersion) {
		this.nameEnv = new ClassLoaderBasedNameEnvironment(parent);
		this.parentClassLoader = parent;
		this.compilerOptions = new CompilerOptions();
		this.setJavaVersion(javaVersion);
		if (INLINE_JSR_BYTECODE != null) {
			try {
				INLINE_JSR_BYTECODE.invoke(this.compilerOptions, true);
			} catch (Throwable e) {
				// ignore
			}
		}
		this.compilerOptions.preserveAllLocalVariables = true;
	}
	
	private final static MethodHandle INLINE_JSR_BYTECODE = findInlineJsrBytecode();
	private static MethodHandle findInlineJsrBytecode() {
		try {
			return MethodHandles.lookup().findSetter(CompilerOptions.class, "inlineJsrBytecode", boolean.class);
		} catch (Exception e) {
			return null;
		}
	}

	private final static MethodHandle ORIGINAL_SOURCE_LEVEL = findOriginalSourceLevel();
	private static MethodHandle findOriginalSourceLevel() {
		try {
			return MethodHandles.lookup().findSetter(CompilerOptions.class, "originalSourceLevel", long.class);
		} catch (Exception e) {
			return null;
		}
	}

	private final static MethodHandle ORIGINAL_COMPLIANCE_LEVEL = findOriginalComplianceLevel();
	private static MethodHandle findOriginalComplianceLevel() {
		try {
			return MethodHandles.lookup().findSetter(CompilerOptions.class, "originalComplianceLevel", long.class);
		} catch (Exception e) {
			return null;
		}
	}

	public InMemoryJavaCompiler(ClassLoader parent, CompilerOptions compilerOptions) {
		this.nameEnv = new ClassLoaderBasedNameEnvironment(parent);
		this.parentClassLoader = parent;
		this.compilerOptions = new CompilerOptions(compilerOptions.getMap());
	}

	/**
	 * Sets the Java version that this compiler works with. Attempts to set versions older than Java 8 are ignored.
	 * 
	 * @since 2.11
	 */
	public long setJavaVersion(JavaVersion javaVersion) {
		long classFmt = toClassFmt(javaVersion);
		setSourceLevel(classFmt);
		setComplianceLevel(classFmt);
		return compilerOptions.targetJDK = classFmt;
	}

	private long toClassFmt(JavaVersion version) {
		if (JavaVersion.JAVA8.compareTo(version) > 0) {
			logger.error("Ignored attempt to set JavaVersion lower than 8", new IllegalArgumentException(version.toString()));
			version = JavaVersion.JAVA8;
		}
		return version.toJdtClassFileConstant();
	}

	/**
	 * sets the source level
	 * (see @link(org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants))
	 */
	private void setSourceLevel(long jdkVersion) {
		compilerOptions.sourceLevel = jdkVersion;
		if (ORIGINAL_SOURCE_LEVEL != null) {
			try {
				ORIGINAL_SOURCE_LEVEL.invoke(compilerOptions, jdkVersion);
			} catch (Throwable e) {
				// ignore
			}
		}	
	}

	/**
	 * sets the compliance level
	 * (see @link(org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants))
	 */
	private void setComplianceLevel(long jdkVersion) {
		compilerOptions.complianceLevel = jdkVersion;
		if (ORIGINAL_COMPLIANCE_LEVEL != null) {
			try {
				ORIGINAL_COMPLIANCE_LEVEL.invoke(compilerOptions, jdkVersion);
			} catch (Throwable e) {
				// ignore
			}
		}
	}

	public Result compile(JavaSource... sources) {
		Result result = new Result(parentClassLoader);
		ICompilerRequestor requestor = (CompilationResult it) -> {
			for (ClassFile cf : it.getClassFiles()) {
				result.classMap.put(CharOperation.toString(cf.getCompoundName()), cf.getBytes());
			}
		};
		org.eclipse.jdt.internal.compiler.Compiler compiler = new org.eclipse.jdt.internal.compiler.Compiler(nameEnv,
				DefaultErrorHandlingPolicies.proceedWithAllProblems(), compilerOptions, requestor,
				new DefaultProblemFactory() {
					@Override
					public CategorizedProblem createProblem(char[] originatingFileName, int problemId,
							String[] problemArguments, int elaborationId, String[] messageArguments, int severity,
							int startPosition, int endPosition, int lineNumber, int columnNumber) {
						CategorizedProblem problem = super.createProblem(originatingFileName, problemId,
								problemArguments, elaborationId, messageArguments, severity, startPosition, endPosition,
								lineNumber, columnNumber);
						result.compilationProblems.add(problem);
						return problem;
					}

					@Override
					public CategorizedProblem createProblem(char[] originatingFileName, int problemId,
							String[] problemArguments, String[] messageArguments, int severity, int startPosition,
							int endPosition, int lineNumber, int columnNumber) {
						CategorizedProblem problem = super.createProblem(originatingFileName, problemId,
								problemArguments, messageArguments, severity, startPosition, endPosition, lineNumber,
								columnNumber);
						result.compilationProblems.add(problem);
						return problem;
					}
				});
		List<CompilationUnit> unitsLists = Lists.transform(Arrays.asList(sources),
				(JavaSource it) -> new CompilationUnit(it.getCode().toCharArray(), it.getFileName(), null));
		ICompilationUnit[] units = unitsLists.toArray(new ICompilationUnit[unitsLists.size()]);
		compiler.compile(units);
		return result;
	}
}
