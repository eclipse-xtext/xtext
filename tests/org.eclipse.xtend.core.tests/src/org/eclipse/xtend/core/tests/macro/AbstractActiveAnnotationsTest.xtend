package org.eclipse.xtend.core.tests.macro

import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.eclipse.xtext.xbase.lib.Pair
import org.junit.Test

import static org.junit.Assert.*

abstract class AbstractActiveAnnotationsTest {
	
	@Test def void testSimpleModification() {
		assertProcessing(
			'myannotation/AbstractAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

				@Active(typeof(AbstractProcessor))
				annotation Abstract { }
				class AbstractProcessor implements TransformationParticipant<MutableClassDeclaration> {
					
					extension TransformationContext ctx
					
					override doTransform(List<? extends MutableClassDeclaration> annotatedSourceClasses, TransformationContext context) {
						ctx = context
						annotatedSourceClasses.forEach [
							^abstract = true
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.Abstract
				class MyClass {
					
				}
			'''
		) [
			val clazz = generatedClassDeclarations.head
			assertTrue(clazz.isAbstract)
		]
	}
	
	@Test def void testPropertyAnnotation() {
		assertProcessing(
			'myannotation/PropertyAnnotation.xtend' -> "
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

				@Active(typeof(PropertyProcessor))
				annotation Property2 { }
				class PropertyProcessor implements TransformationParticipant<MutableFieldDeclaration> {
					
					extension TransformationContext ctx
					
					override doTransform(List<? extends MutableFieldDeclaration> annotatedSourceFields, TransformationContext context) {
						ctx = context
						annotatedSourceFields.forEach [ field |
							val declaringType = field.declaringType as MutableClassDeclaration 
							declaringType.addMethod(field.getterName) [
								returnType = field.type
								body = ['''
									return this.�field.name�;
								''']
							]
							declaringType.addMethod('set'+field.name.toFirstUpper) [
								addParameter(field.name, field.type)
								body = ['''
									this.�field.name� = �field.name�;
								''']
							]
						]
					}
					
					def private String getterName(MutableFieldDeclaration field) {
						return 'get'+field.name.toFirstUpper
					}
				}
			",
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					
					@myannotation.Property2 String myField
				}
			'''
		) [
			val clazz = generatedClassDeclarations.head
			val getter = clazz.members.filter(typeof(MutableMethodDeclaration)).head
			assertEquals('getMyField', getter.name)
			assertEquals('String', getter.returnType.toString)
			
			val setter = clazz.members.filter(typeof(MutableMethodDeclaration)).drop(1).head
			assertEquals('setMyField', setter.name)
			assertEquals('void', setter.returnType.toString)
			assertEquals('myField', setter.parameters.head.name)
			assertEquals('String', setter.parameters.head.type.toString)
		]
	}
	
	@Test def void testThrowsAndTypeParam() {
		assertProcessing(
			'myannotation/AbstractAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration

				@Active(typeof(ThrowsAndTypeParamProcessor))
				annotation ThrowsAndTypeParam { }
				class ThrowsAndTypeParamProcessor implements TransformationParticipant<MutableMethodDeclaration> {
					
					extension TransformationContext ctx
					
					override doTransform(List<? extends MutableMethodDeclaration> annotatedMethods, TransformationContext context) {
						ctx = context
						annotatedMethods.forEach [
							val type = addTypeParameter('A')
							addParameter('myParam', newTypeReference(type))
							setExceptions(newTypeReference('java.lang.Exception'))
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					@myannotation.ThrowsAndTypeParam
					def void foo(){
					}
				}
			'''
		) [
			val method = generatedClassDeclarations.head.members.filter(typeof(MutableMethodDeclaration)).head
			assertEquals('A', method.typeParameters.head.name)
			assertEquals('myParam', method.parameters.head.name)
			assertSame(method.typeParameters.head, method.parameters.head.type.type)
			assertEquals(1, method.exceptions.size)
		]
	}
	
	@Test def void testValidation() {
		assertProcessing(
			'myannotation/AbstractAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableNamedElement
				import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration

				@Active(typeof(ValidatedProcessor))
				annotation Validated { }
				class ValidatedProcessor implements TransformationParticipant<MutableNamedElement> {
					
					extension TransformationContext ctx
					
					override doTransform(List<? extends MutableNamedElement> annotatedMethods, TransformationContext context) {
						ctx = context
						annotatedMethods.forEach [ ele |
							switch ele {
								MutableFieldDeclaration : ele.addWarning('field-warning')
								default : ele.addWarning('warning')
							}
						]
					}
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					@myannotation.Validated
					def void foo() {
					}
					
					@myannotation.Validated
					String name
				}
			'''
		) [
			val method = generatedClassDeclarations.head.members.filter(typeof(MutableMethodDeclaration)).head
			val field = generatedClassDeclarations.head.members.filter(typeof(MutableFieldDeclaration)).head
			assertEquals('field-warning', problemSupport.getProblems(field).head.message)
			assertEquals('warning', problemSupport.getProblems(method).head.message)
		]
	}
	
	def void assertProcessing(Pair<String,String> macroFile, Pair<String,String> clientFile, (CompilationUnitImpl)=>void expectations)
}