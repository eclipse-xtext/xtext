/*******************************************************************************
 * Copyright (c) 2009, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.impl;

import org.apache.log4j.Logger;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.access.binary.BinaryClass;
import org.eclipse.xtext.common.types.access.binary.asm.ClassFileBytesAccess;
import org.eclipse.xtext.common.types.access.binary.asm.JvmDeclaredTypeBuilder;
import org.eclipse.xtext.common.types.access.reflect.ReflectURIHelper;
import org.eclipse.xtext.common.types.access.reflect.ReflectionTypeFactory;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.util.internal.Stopwatches;
import org.eclipse.xtext.util.internal.Stopwatches.StoppedTask;
import org.objectweb.asm.Opcodes;

import com.google.inject.Inject;

/**
 * Internal implementation that allows to convert the bytes of Java top-level classes to {@link JvmType types}.
 * 
 * Clients are not supposed to use this class directly but the {@link IJvmTypeProvider} or {@link TypeReferences}
 * instead. Those will take care of types that are requested more than once and therefore should return the very same
 * {@link JvmType type} on subsequent queries.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class DeclaredTypeFactory implements ITypeFactory<BinaryClass, JvmDeclaredType> {

	private final StoppedTask createTypeTask = Stopwatches.forTask("AsmTypeFactory.createType");
	
	private static final Logger logger = Logger.getLogger(DeclaredTypeFactory.class);

	/**
	 * The classLoader that is used to find the class files for nested types.
	 */
	private final ClassLoader classLoader;

	/**
	 * The reader may cache the read structure of the class files.
	 */
	private final ClassFileBytesAccess bytesAccess;
	
	private static final boolean ASM_AVAILABLE = isAsm8Available();
	
	private final boolean useASM;

	@Inject
	public DeclaredTypeFactory(ClassFileBytesAccess bytesAccess, ClassLoader loader) {
		this(bytesAccess, loader, ASM_AVAILABLE);
	}
	
	public DeclaredTypeFactory(ClassFileBytesAccess bytesAccess, ClassLoader loader, boolean useASM) {
		this.bytesAccess = bytesAccess;
		this.classLoader = loader;
		this.useASM = useASM;
	}

	private static boolean isAsm8Available() {
		try {
			// don't forget to adapt the error message below
			if(Opcodes.class.getDeclaredField("ASM9") != null) 
				return true;
		} catch(NoClassDefFoundError e) {
			logger.warn("--- xtext.common.types ---------------------------------------------------");
			logger.warn("ASM library is not available. Falling back to java.lang.reflect API.");
		} catch(NoSuchFieldException e) {
			logger.warn("--- xtext.common.types ---------------------------------------------------");
			logger.warn("ASM library is too old. Falling back to java.lang.reflect API.");
		}
		logger.warn("Please note that no information about compile time constants is available.");
		logger.warn("It's recommended to use org.objectweb.asm 9.9.0 or better (Maven group id: org.ow2.asm).");
		logger.warn("--------------------------------------------------------------------------");
		return false;
	}

	/**
	 * Creates a new {@link JvmDeclaredType type} from the given binary class..
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public JvmDeclaredType createType(BinaryClass binaryClass) {
		if (useASM) {
			try {
				createTypeTask.start();
				return doCreateType(binaryClass);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				createTypeTask.stop();
			}
		} else {
			try {
				ReflectURIHelper uriHelper = new ReflectURIHelper();
				ReflectionTypeFactory reflectionBased = new ReflectionTypeFactory(uriHelper);
				Class<?> clazz = Class.forName(binaryClass.getName(), false, getClassLoader());
				return reflectionBased.createType(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected JvmDeclaredType doCreateType(BinaryClass binaryClass) {
		JvmDeclaredTypeBuilder builder = new JvmDeclaredTypeBuilder(binaryClass, getBytesAccess(), getClassLoader());
		return builder.buildType();
	}
	
	/**
	 * @since 2.35
	 */
	protected ClassFileBytesAccess getBytesAccess() {
		return bytesAccess;
	}
	
	/**
	 * @since 2.35
	 */
	protected ClassLoader getClassLoader() {
		return classLoader;
	}

}
