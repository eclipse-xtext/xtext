/**
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.testing;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @noextend This class is not intended to be subclassed by clients.
 * @noreference This class is not intended to be referenced by clients.
 * 
 * @since 2.9
 */
@Singleton
public class OnTheFlyJavaCompiler2 {
	private static final Logger logger = Logger.getLogger(OnTheFlyJavaCompiler2.class);
	
	private InMemoryJavaCompiler inMemoryCompiler;

	private JavaVersion javaVersion;

	/**
	 * Creates a new OnTheFlyCompiler that accepts {@link JavaVersion#DEFAULT} compliant code.
	 */
	@Inject
	public OnTheFlyJavaCompiler2(ClassLoader scope) {
		this(scope, JavaVersion.DEFAULT);
	}

	public OnTheFlyJavaCompiler2(ClassLoader scope, JavaVersion version) {
		this.inMemoryCompiler = new InMemoryJavaCompiler(scope, version);
		this.javaVersion = version;
	}

	/**
	 * Sets the Java version that this compiler works with. Defaults to Java8 and attempts to set older versions are ignored. 
	 * 
	 * @since 2.11
	 */
	public void setJavaVersion(JavaVersion version) {
		if (JavaVersion.JAVA8.compareTo(version) > 0) {
			logger.error("Ignored attempt to set JavaVersion lower than 8", new IllegalArgumentException(version.toString()));
			version = JavaVersion.JAVA8;
		}
		inMemoryCompiler.setJavaVersion(version);
		javaVersion = version;
	}

	/**
	 * @since 2.11
	 */
	public JavaVersion getJavaVersion() {
		return javaVersion;
	}

	public Class<?> compileToClass(String classname, String code) {
		InMemoryJavaCompiler.Result result = inMemoryCompiler.compile(new JavaSource(toJavaFile(classname), code));
		try {
			if (Iterables.any(result.getCompilationProblems(), CategorizedProblem::isError)) {
				StringConcatenation message = new StringConcatenation();
				message.append("Java code compiled with errors:");
				message.newLine();
				message.append(Joiner.on("\n").join(
						Iterables.filter(result.getCompilationProblems(), CategorizedProblem::isError)));
				message.newLineIfNotEmpty();
				message.newLine();
				message.append("Code was:");
				message.newLine();
				message.append(code);
				message.newLineIfNotEmpty();
				throw new IllegalArgumentException(message.toString());
			}
			return result.getClassLoader().loadClass(classname);
		} catch (Throwable t) {
			if (t instanceof ClassNotFoundException) {
				ClassNotFoundException e = (ClassNotFoundException) t;
				StringConcatenation message = new StringConcatenation();
				message.append("Couldn\'t load \'");
				message.append(classname);
				message.append("\' ");
				message.newLineIfNotEmpty();
				message.append("source :");
				message.newLine();
				message.append("\t");
				message.append(code, "\t");
				message.newLineIfNotEmpty();
				message.newLine();
				message.append("PROBLEMS : ");
				message.newLine();
				message.append("\t");
				message.append(Joiner.on("\n").join(result.getCompilationProblems()), "\t");
				message.newLineIfNotEmpty();
				throw new IllegalStateException(message.toString(), e);
			} else {
				throw Exceptions.sneakyThrow(t);
			}
		}
	}

	private String toJavaFile(String string) {
		return string.replace('.', '/') + ".java";
	}

	public Map<String, Class<?>> compileToClasses(Map<String, String> sources) {
		InMemoryJavaCompiler.Result result = inMemoryCompiler.compile(((JavaSource[]) Conversions.unwrapArray(
				Iterables.transform(sources.entrySet(), e -> new JavaSource(toJavaFile(e.getKey()), e.getValue())),
				JavaSource.class)));
		try {
			if (Iterables.any(result.getCompilationProblems(), CategorizedProblem::isError)) {
				StringConcatenation message = new StringConcatenation();
				message.append("Java code compiled with errors:");
				message.newLine();
				message.append(Joiner.on("\n")
						.join(Iterables.filter(result.getCompilationProblems(), CategorizedProblem::isError)));
				message.newLineIfNotEmpty();
				message.newLine();
				message.append("Code was:");
				message.newLine();
				message.append("=========");
				message.newLine();
				message.append(Joiner.on("\n=========\n").join(sources.values()));
				message.newLineIfNotEmpty();
				message.append("=========");
				message.newLine();
				throw new IllegalArgumentException(message.toString());
			}
			ClassLoader classLoader = result.getClassLoader();
			return IterableExtensions.toMap(
					Iterables.transform(sources.keySet(), (String it) -> {
						try {
							return classLoader.loadClass(it);
						} catch (ClassNotFoundException e) {
							throw Exceptions.sneakyThrow(e);
						}
					}), Class::getName);
		} catch (Throwable t) {
			if (t instanceof ClassNotFoundException) {
				ClassNotFoundException e = (ClassNotFoundException) t;
				StringConcatenation message = new StringConcatenation();
				message.append(e.getMessage());
				message.append(" ");
				message.newLineIfNotEmpty();
				message.append("source :");
				message.newLine();
				message.append("\t");
				message.append(sources, "\t");
				message.newLineIfNotEmpty();
				message.newLine();
				message.append("PROBLEMS : ");
				message.newLine();
				message.append("\t");
				message.append(IterableExtensions.join(result.getCompilationProblems(), "\n"), "\t");
				message.newLineIfNotEmpty();
				throw new IllegalStateException(message.toString(), e);
			} else {
				throw Exceptions.sneakyThrow(t);
			}
		}
	}

	public <RT extends Object> Function0<RT> createFunction(String expression, Class<RT> returnType) {
		try {
			StringConcatenation sourceCode = new StringConcatenation();
			sourceCode.append("public class __Generated implements org.eclipse.xtext.xbase.lib.Functions.Function0<");
			sourceCode.append(returnType.getName());
			sourceCode.append("> {");
			sourceCode.newLineIfNotEmpty();
			sourceCode.append("\t");
			sourceCode.append("public ");
			sourceCode.append(returnType.getName(), "\t");
			sourceCode.append(" apply() {");
			sourceCode.newLineIfNotEmpty();
			sourceCode.append("\t\t");
			sourceCode.append(expression, "\t\t");
			sourceCode.newLineIfNotEmpty();
			sourceCode.append("\t");
			sourceCode.append("}");
			sourceCode.newLine();
			sourceCode.append("}");
			sourceCode.newLine();
			Class<?> clazz = compileToClass("__Generated", sourceCode.toString());
			@SuppressWarnings("unchecked")
			Function0<RT> result = (Function0<RT>) clazz.getDeclaredConstructor().newInstance();
			return result;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw Exceptions.sneakyThrow(e);
		}
	}
}
