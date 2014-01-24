/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.binary.asm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.common.types.JvmAnnotationTarget;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeParameterDeclarator;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.access.binary.BinaryClass;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class JvmDeclaredTypeBuilder extends JvmAnnotationTargetBuilder implements ClassVisitor {

    protected JvmDeclaredType result;
    
    protected int offset = 0;
    
    private Map<String, JvmTypeParameter> typeParameters;

	private final ClassLoader classLoader;

	private final BinaryClass binaryClass;

	private final ClassFileBytesAccess bytesAccess;

    public JvmDeclaredTypeBuilder(BinaryClass binaryClass, ClassFileBytesAccess bytesAccess, ClassLoader classLoader) {
		this(binaryClass, bytesAccess, classLoader, null, new Proxies());
	}
    
    @Override
    protected JvmAnnotationTarget getInstance() {
    	return result;
    }
    
    public void visitAttribute(Attribute attr) {
    	// ignore
    }
    
    protected JvmDeclaredTypeBuilder(BinaryClass binaryClass, ClassFileBytesAccess bytesAccess, ClassLoader classLoader, Map<String, JvmTypeParameter> typeParameters, Proxies proxies) {
    	super(proxies);
		this.binaryClass = binaryClass;
		this.bytesAccess = bytesAccess;
		this.classLoader = classLoader;
		this.typeParameters = typeParameters;
    }

    public JvmDeclaredType buildType() {
    	byte[] bytes = bytesAccess.getBytes(binaryClass);
		ClassReader reader = new ClassReader(bytes);
		reader.accept(this, 
			  ClassReader.SKIP_DEBUG 
			| ClassReader.SKIP_FRAMES 
			| ClassReader.SKIP_CODE
		);
		return result;
	}
    
    // ------------------------------------------------------------------------
    // Implementation of the ClassVisitor interface
    // ------------------------------------------------------------------------

    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces)
    {
    	if ((access & ACC_SYNTHETIC) != 0)
			throw new IllegalStateException("Cannot create type for anonymous or synthetic classes");
    	if ((ACC_ENUM & access) != 0) {
    		result = TypesFactory.eINSTANCE.createJvmEnumerationType();
    		offset = 2;
    	} else if ((ACC_ANNOTATION & access) != 0) {
    		result = TypesFactory.eINSTANCE.createJvmAnnotationType();
    	} else {
    		JvmGenericType generic = TypesFactory.eINSTANCE.createJvmGenericType(); 
    		result = generic;

    		generic.setInterface((access & ACC_INTERFACE) != 0);
    		generic.setStrictFloatingPoint((access & ACC_STRICT) != 0);
    	}
    	setTypeModifiers(access);
    	
    	setVisibility(access, result);
    	
    	setNameAndPackage(name);

    	BinarySuperTypeSignature genericSignature = null;
		if (signature != null) {
			if ((access & (ACC_STATIC | ACC_INTERFACE)) != 0) {
				typeParameters = Collections.emptyMap();
			}
			genericSignature = BinarySignatures.createSuperTypeSignature(signature);
			if (((ACC_ENUM | ACC_ANNOTATION) & access) == 0) {
				typeParameters = createTypeParameters(genericSignature, (JvmTypeParameterDeclarator) result, typeParameters);
			}
		}
		setSuperTypes(name, genericSignature, superName, interfaces);
    }
    
    private void setSuperTypes(String name, BinarySuperTypeSignature signature, String superName, String[] interfaces) {
    	InternalEList<JvmTypeReference> superTypes = (InternalEList<JvmTypeReference>) result.getSuperTypes();
		if (signature != null) {
			List<BinaryGenericTypeSignature> superTypeSignatures = signature.getSuperTypes();
			if (result.eClass() == TypesPackage.Literals.JVM_GENERIC_TYPE && ((JvmGenericType) result).isInterface()) {
				if (superTypeSignatures.size() > 1) {
					superTypeSignatures = superTypeSignatures.subList(1, superTypeSignatures.size());
				}
			}
			for (int i = 0; i < superTypeSignatures.size(); i++) {
				superTypes.addUnique(createTypeReference(superTypeSignatures.get(i), typeParameters));
			}
		} else {
			if (superName != null && result.eClass() != TypesPackage.Literals.JVM_ANNOTATION_TYPE) {
				superTypes.addUnique(createTypeReference(BinarySignatures.createTypeSignature(superName), typeParameters));
			}
			setInterfaces(interfaces, typeParameters, superTypes);
			if (superTypes.isEmpty() && !Proxies.JAVA_LANG_OBJECT.equals(name)) {
				superTypes.addUnique(createObjectTypeReference());
			}
		}
	}
    
    protected void setInterfaces(String[] interfaces, Map<String, JvmTypeParameter> typeParameters,
			InternalEList<JvmTypeReference> result) {
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				String interfaceName = interfaces[i];
				result.addUnique(createTypeReference(BinarySignatures.createTypeSignature(interfaceName),
						typeParameters));
			}
		}
	}
    
    protected void setNameAndPackage(String binaryName) {
    	int slash = binaryName.lastIndexOf('/');
    	if (slash == -1) { // no package
    		result.setSimpleName(binaryName);
    		result.internalSetIdentifier(binaryName);
    	} else {
    		String identifier = binaryName.replace('/', '.');
    		String simpleName = identifier.substring(slash + 1);
    		result.setSimpleName(simpleName);
    		String packageName = identifier.substring(0, slash);
    		result.setPackageName(packageName);
    		result.internalSetIdentifier(identifier);
    	}
	}
    
    protected void setTypeModifiers(int accessFlags) {
		result.setAbstract((accessFlags & ACC_ABSTRACT) != 0);
		if (result.eClass() != TypesPackage.Literals.JVM_ENUMERATION_TYPE) {
			result.setStatic((accessFlags & ACC_STATIC) != 0);
			if (result.isStatic()) {
				offset = 0;
			}
			result.setFinal((accessFlags & ACC_FINAL) != 0);
		} else {
			result.setStatic(true);
		}
	}

    public void visitSource(final String file, final String debug) {
        // ignore
    }

    public void visitOuterClass(
        final String owner,
        final String name,
        final String desc)
    {
    	throw new IllegalStateException("Expected top-level type");
    }

    public void visitInnerClass(
        final String name,
        final String outerName,
        final String innerName,
        final int access)
    {
    	if (outerName != null && innerName != null && outerName.replace('/', '.').equals(result.getIdentifier())) {
    		BinaryClass binaryClass = new BinaryClass(name, classLoader);
    		NestedJvmDeclaredTypeBuilder builder = new NestedJvmDeclaredTypeBuilder(
    				innerName,
    				((access & ACC_STATIC) == 0)? 1 : 0, 
    				binaryClass,
    				bytesAccess,
    				classLoader,
    				typeParameters,
    				proxies);
    		JvmDeclaredType nestedType = builder.buildType();
    		setVisibility(access, nestedType);
    		result.getMembers().add(nestedType);
    	}
    }
    
    static class NestedJvmDeclaredTypeBuilder extends JvmDeclaredTypeBuilder {

		private final String innerName;

		protected NestedJvmDeclaredTypeBuilder(
				String innerName,
				int offset,
				BinaryClass binaryClass,
				ClassFileBytesAccess bytesAccess,
				ClassLoader classLoader,
				Map<String, JvmTypeParameter> typeParameters,
				Proxies proxies) {
			super(binaryClass, bytesAccess, classLoader, typeParameters, proxies);
			this.innerName = innerName;
			this.offset = offset;
		}
		
		@Override
		public void visitOuterClass(String owner, String name, String desc) {
			// ignore
		}
		
		@Override
		protected void setNameAndPackage(String binaryName) {
    		String identifier = binaryName.replace('/', '.');
    		result.setSimpleName(innerName);
			result.internalSetIdentifier(identifier);
		}
    	
    }
    
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value)
    {
    	if ((access & ACC_SYNTHETIC) == 0) {
	        JvmFieldBuilder fieldBuilder = new JvmFieldBuilder(
	        		result,
	        		(access & ACC_STATIC) == 0 ? typeParameters : null,
	        		proxies,
    				access,
	        		name,
	        		desc,
	        		signature,
	        		value);
	        return fieldBuilder;
    	}
    	return null;
    }

    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
    	if ((access & (ACC_SYNTHETIC | ACC_BRIDGE)) == 0 && !isClinit(name)) {
    		JvmExecutableBuilder builder = new JvmExecutableBuilder(
    				result,
    				offset,
    				(access & ACC_STATIC) == 0 ? typeParameters : null,
					proxies,
    				access,
                    name,
                    desc,
                    signature,
                    exceptions);
            return builder;
    	}
    	return null;
    }

}
