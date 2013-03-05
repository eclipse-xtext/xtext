/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.jvmmodel;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.core.xtend.CreateExtensionInfo;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.compiler.TypeReferenceSerializer;
import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.jvmmodel.ILogicalContainerProvider;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Procedures;
import org.eclipse.xtext.xbase.typing.ITypeProvider;

import com.google.inject.Inject;

@SuppressWarnings("deprecation")
public class CacheMethodCompileStrategy implements Procedures.Procedure1<ITreeAppendable> {
	@Inject
	private TypeReferences typeReferences;

	@Inject
	private ITypeProvider typeProvider;

	@Inject
	private TypeReferenceSerializer typeReferenceSerializer;

	@Inject
	private ILogicalContainerProvider logicalContainerProvider;

	@Inject
	private XbaseCompiler compiler;
	
	private CreateExtensionInfo createExtensionInfo;

	private JvmOperation initializerMethod;

	private JvmField cacheField;

	protected void init(CreateExtensionInfo createExtensionInfo, JvmField cacheField, JvmOperation initializerMethod) {
		this.createExtensionInfo = createExtensionInfo;
		this.initializerMethod = initializerMethod;
		this.cacheField = cacheField; 
	}

	public void apply(ITreeAppendable appendable) {
		JvmOperation cacheMethod = (JvmOperation) logicalContainerProvider
				.getLogicalContainer(createExtensionInfo.getCreateExpression());
		JvmDeclaredType containerType = cacheMethod.getDeclaringType();
		JvmTypeReference listType = typeReferences.getTypeForName(ArrayList.class, containerType);
		JvmTypeReference collectonLiterals = typeReferences.getTypeForName(CollectionLiterals.class,
				containerType);
		String cacheVarName = cacheField.getSimpleName();
		String cacheKeyVarName = appendable.declareSyntheticVariable("CacheKey", "_cacheKey");
		appendable.append("final ");
		typeReferenceSerializer.serialize(listType, containerType, appendable);
		appendable.append(cacheKeyVarName).append(" = ");
		typeReferenceSerializer.serialize(collectonLiterals, containerType, appendable);
		appendable.append(".newArrayList(");
		EList<JvmFormalParameter> list = cacheMethod.getParameters();
		for (Iterator<JvmFormalParameter> iterator = list.iterator(); iterator.hasNext();) {
			JvmFormalParameter jvmFormalParameter = iterator.next();
			appendable.append(getVarName(jvmFormalParameter));
			if (iterator.hasNext()) {
				appendable.append(", ");
			}
		}
		appendable.append(");");
		// declare result variable
		JvmTypeReference returnType = typeProvider.getType(createExtensionInfo.getCreateExpression());
		appendable.newLine().append("final ");
		typeReferenceSerializer.serialize(returnType, containerType, appendable);
		String resultVarName = "_result";
		appendable.append(" ").append(resultVarName).append(";");
		// open synchronize block
		appendable.newLine().append("synchronized (").append(cacheVarName).append(") {");
		appendable.increaseIndentation();
		// if the cache contains the key return the previously created object.
		appendable.newLine().append("if (").append(cacheVarName).append(".containsKey(").append(cacheKeyVarName)
				.append(")) {");
		appendable.increaseIndentation();
		appendable.newLine().append("return ").append(cacheVarName).append(".get(").append(cacheKeyVarName).append(");");
		appendable.decreaseIndentation().newLine().append("}");
		
		// execute the creation
		compiler.toJavaStatement(createExtensionInfo.getCreateExpression(), appendable, true);
		appendable.newLine();
		appendable.append(resultVarName).append(" = ");
		compiler.toJavaExpression(createExtensionInfo.getCreateExpression(), appendable);
		appendable.append(";");

		// store the newly created object in the cache
		appendable.newLine().append(cacheVarName).append(".put(").append(cacheKeyVarName).append(", ")
				.append(resultVarName).append(");");

		// close synchronize block
		appendable.decreaseIndentation();
		appendable.newLine().append("}");
		appendable.newLine().append(initializerMethod.getSimpleName()).append("(").append(resultVarName);
		for (JvmFormalParameter parameter : cacheMethod.getParameters()) {
			appendable.append(", ").append(parameter.getName());
		}
		appendable.append(");");
		// return the result
		appendable.newLine().append("return ");
		appendable.append(resultVarName).append(";");
	}

	protected String getVarName(JvmIdentifiableElement ex) {
		return ex.getSimpleName();
	}
}