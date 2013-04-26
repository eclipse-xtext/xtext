package org.eclipse.xtext.xbase.ui.labeling

import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmOperation

import static org.eclipse.jdt.ui.JavaElementImageDescriptor.*
import org.eclipse.xtext.xbase.typesystem.^override.OverrideHelper
import javax.inject.Inject
import static extension org.eclipse.xtext.common.types.util.DeprecationUtil.*
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmIdentifiableElement
import org.eclipse.xtext.common.types.JvmExecutable

class XbaseImageAdornments {
	
	@Inject extension OverrideHelper
	  
	def dispatch get(JvmDeclaredType it) {
		0	
			.or(abstract, ABSTRACT)
			.or(final, FINAL) 
			.or(static, STATIC)
			.or(deprecated, DEPRECATED)
	}
	
	def dispatch get(JvmField it) {
		0	
			.or(final, FINAL) 
			.or(static, STATIC) 
			.or(deprecated, DEPRECATED)
			.or(volatile, VOLATILE)
			.or(transient, TRANSIENT)
	}
	
	def dispatch get(JvmConstructor it) {
		CONSTRUCTOR	
			.or(deprecated, DEPRECATED)
	}
	
	def dispatch get(JvmOperation it) {
		val adornment = 0	
			.or(abstract, ABSTRACT)
			.or(final, FINAL) 
			.or(synchronized, SYNCHRONIZED) 
			.or(static, STATIC) 
			.or(deprecated, DEPRECATED)
			.or(native, NATIVE)
			
		val overriddenOperation = findOverriddenOperation
		if(overriddenOperation != null) 
			adornment.bitwiseOr(getOverrideAdornment(overriddenOperation))
		else 
			adornment
	}
	
	def dispatch get(Void it) {
		0
	}

	def getOverrideAdornment(JvmExecutable overriddenOperation) {
		switch overriddenOperation {
			JvmOperation: if(overriddenOperation.abstract) IMPLEMENTS else OVERRIDES
			default: 0
		}
	}
	
	def dispatch get(JvmIdentifiableElement it) {
		0
	}
	
	protected def or(int adornment, boolean condition, int oredValue) {
		if(condition) 
			adornment.bitwiseOr(oredValue)
		else
			adornment
	} 
}