/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.macro

import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.xtend.core.xtend.XtendAnnotationTarget
import org.eclipse.xtend.core.xtend.XtendAnnotationType
import org.eclipse.xtend.core.xtend.XtendClass
import org.eclipse.xtend.core.xtend.XtendConstructor
import org.eclipse.xtend.core.xtend.XtendField
import org.eclipse.xtend.core.xtend.XtendFunction
import org.eclipse.xtend.core.xtend.XtendParameter
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtext.common.types.JvmAnnotationType
import org.eclipse.xtext.common.types.JvmTypeAnnotationValue
import org.eclipse.xtext.linking.ILinkingService
import org.eclipse.xtext.linking.lazy.LazyURIEncoder
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation

import static org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage$Literals.*
import org.eclipse.xtext.common.types.JvmCustomAnnotationValue
import org.eclipse.xtext.xbase.XTypeLiteral
import org.eclipse.xtext.common.types.JvmType
import org.eclipse.xtend.core.xtend.XtendInterface
import org.eclipse.xtend.core.xtend.XtendEnum
import org.eclipse.xtend.core.xtend.XtendEnumLiteral

class XAnnotationExtensions {
	
	@Inject LazyURIEncoder encoder
	@Inject ILinkingService linkingService
	
	def XtendAnnotationTarget getAnnotatedTarget(XAnnotation annotation) {
		// ignore synthetic containers
		switch container : annotation.eContainer {
			XtendAnnotationType : container
			XtendClass : container
			XtendInterface : container
			XtendEnum : container
			XtendField : container
			XtendFunction : container
			XtendConstructor : container
			XtendEnumLiteral : container
			XtendParameter : container
			XtendAnnotationTarget 	: container.eContainer as XtendAnnotationTarget
			XAnnotation 			: getAnnotatedTarget(container)
			default 				: null
		}
	}
	
	/**
	 * Checks whether this annotation is pointing to a processed annotation, without resolving the proxy
	 */
	def isProcessed(XAnnotation it) {
		switch proxy : it.eGet(XANNOTATION__ANNOTATION_TYPE, false) {
			EObject case proxy.eIsProxy: {
				val uri = (proxy as InternalEObject).eProxyURI
				if (encoder.isCrossLinkFragment(eResource, uri.fragment)) {
					val triple = encoder.decode(eResource, uri.fragment)
					val candidates = linkingService.getLinkedObjects(triple.first, triple.second, triple.third)
					if (!candidates.isEmpty) {
						val head = candidates.head
						if (head instanceof JvmAnnotationType)
							return (head as JvmAnnotationType).isActiveAnnotation
					}
				}
			}
			JvmAnnotationType : {
				return proxy.isActiveAnnotation
			}
		}
		return false
	}
	
	def JvmType getProcessorType(JvmAnnotationType it) {
		val activeAnnotation = it.annotations.findFirst [ 
			annotation?.identifier == typeof(Active).name
		]
		val annoVal = activeAnnotation.values.findFirst [
			operation.simpleName == 'value'
		]
		switch annoVal {
			JvmTypeAnnotationValue : {
				return annoVal.values.head.type
			}
			JvmCustomAnnotationValue : {
				return (annoVal.values.head as XTypeLiteral).type				
			}
			default :
				return null				
		}
	}
	
	def getProcessorType(XAnnotation it) {
		return tryFindAnnotationType.processorType
	}
	
	def tryFindAnnotationType(XAnnotation it) {
		switch proxy : it.eGet(XANNOTATION__ANNOTATION_TYPE) {
			EObject case proxy.eIsProxy: {
				val uri = (proxy as InternalEObject).eProxyURI
				// TODO get these services from the resource service provider to make sure they are the ones used in Xtend
				if (encoder.isCrossLinkFragment(eResource, uri.fragment)) {
					val triple = encoder.decode(eResource, uri.fragment)
					val candidates = linkingService.getLinkedObjects(triple.first, triple.second, triple.third)
					if (!candidates.isEmpty) {
						return (candidates.head as JvmAnnotationType)
					}
				}
			}
			JvmAnnotationType : {
				return proxy
			}
		}
	}
	
	def protected boolean isActiveAnnotation(JvmAnnotationType annotationType) {
		for (anno : annotationType.annotations) {
			if (anno.annotation.identifier == typeof(Active).name)
				return true
		}
		return false
	}
}