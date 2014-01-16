package org.eclipse.xtend.core.tests.macro

import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.EnumerationTypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclarator
import org.eclipse.xtext.xbase.lib.Pair
import org.junit.Test

import static org.junit.Assert.*
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference
import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration

abstract class AbstractReusableActiveAnnotationTests {
	
	@Test def void testAnnotationValueSetting_1() {
		assertProcessing(
			'myannotation/ConfigurableAnnotation.xtend' -> '''
				package myannotation

				import java.util.List
				import org.eclipse.xtend.lib.macro.*
				import org.eclipse.xtend.lib.macro.declaration.*
				
				import static com.google.common.base.Preconditions.*
				
				@Active(ConfigurableAnnotationProcessor)
				annotation ConfigurableAnnotation {
					BlackOrWhite color
					BlackOrWhite[] colors
					Class<?> type
					Class<?>[] types
				}
				
				enum BlackOrWhite {
					BLACK, WHITE
				}
				
				class ConfigurableAnnotationProcessor extends AbstractClassProcessor {
				
					override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
						val anno = annotatedClass.annotations.head
						val enumType = findTypeGlobally('myannotation.BlackOrWhite') as EnumerationTypeDeclaration
						val white = enumType.findDeclaredValue('WHITE')
						val black = enumType.findDeclaredValue('BLACK')
						
						val existingValue = anno.getValue('color')
						if (existingValue !=  white)
							throw new AssertionError("color")
						anno.setEnumValue('color', black)
							
						val existingColorsValue = anno.getValue('colors') as Object[]
						if (existingColorsValue.get(0) !=  white && existingColorsValue.get(1) != black && existingColorsValue.length != 2)
							throw new AssertionError("colors")
							
						anno.setEnumValue('colors', black, white)
						
						val existingType = anno.getValue('type')
						if (existingType != string)
							throw new AssertionError("type")
						anno.setClassValue('type', annotatedClass.newTypeReference)
							
						val existingTypes = anno.getValue('types') as Object[]
						if (existingTypes.get(0) !=  primitiveInt && existingTypes.get(1) != annotatedClass.newTypeReference && existingTypes.length != 2)
							throw new AssertionError("types")
							
						anno.setClassValue('types', primitiveBoolean)
					}
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode

				import myannotation.*
				
				@ConfigurableAnnotation(color=BlackOrWhite.WHITE, colors=#[BlackOrWhite.WHITE, BlackOrWhite.BLACK], type = String, types=#[Integer, MyClass])
				class MyClass {
				}
			'''
		) [
			val clazz = typeLookup.findClass("myusercode.MyClass")
			val colorEnum = typeLookup.findTypeGlobally("myannotation.BlackOrWhite") as EnumerationTypeDeclaration
			val annotation = clazz.annotations.head
			
			assertEquals(colorEnum.findDeclaredValue('BLACK'), annotation.getValue('color'))
			
			val colors =  annotation.getValue('colors') as Object[]
			assertEquals(2, colors.length)
			assertEquals(colorEnum.findDeclaredValue('BLACK'), colors.get(0))
			assertEquals(colorEnum.findDeclaredValue('WHITE'), colors.get(1))
			
			assertEquals(typeReferenceProvider.newTypeReference(clazz), annotation.getValue('type'))
			
			val types =  annotation.getClassArrayValue('types')
			assertEquals(1, types.length)
			assertEquals(typeReferenceProvider.primitiveBoolean, types.get(0))
			
		]
	}
	
	@Test def void testAnnotationValueSetting_2() {
		assertProcessing(
			'myannotation/ConfigurableAnnotation.xtend' -> '''
				package myannotation

				import org.eclipse.xtend.lib.macro.*
				import org.eclipse.xtend.lib.macro.declaration.*
				
				@Active(ConfigurableAnnotationProcessor)
				annotation ConfigurableAnnotation {
					int someValue
				}
				
				class Constants {
					public static val int MYCONSTANT = Integer.MAX_VALUE - 42
				}
				
				class ConfigurableAnnotationProcessor extends AbstractClassProcessor {
				
					override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
						val anno = annotatedClass.annotations.head
						
						val existingValue = anno.getValue('someValue')
						
						annotatedClass.docComment = ''+existingValue
					}
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode

				import myannotation.*
				
				@ConfigurableAnnotation(someValue=MoreConstants.MY_CONSTANT * 1)
				class MyClass {
				}
				
				class MoreConstants {
					public static val int MY_CONSTANT = myannotation.Constants.MYCONSTANT - Integer.MAX_VALUE + 42 * 2
				}
			'''
		) [
			val clazz = typeLookup.findClass("myusercode.MyClass")
			assertEquals("42",clazz.docComment)
		]
	}
	
	@Test def void testAnnotationValueSetting_3() {
		assertProcessing(
			'myannotation/MoveValues.xtend' -> '''
				package myannotation
				
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				import org.eclipse.xtend.lib.macro.declaration.TypeReference
				import org.eclipse.xtend.lib.macro.Active
				
				@Active(MoveAnnotationValuesProcessor)
				annotation MoveValues {}
				
				class MoveAnnotationValuesProcessor extends AbstractClassProcessor {
					
					override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
						val classAnnotation = annotatedClass.annotations.head
						val fieldAnnotation = annotatedClass.declaredFields.head.annotations.head
						
						fieldAnnotation.set('booleanValue', classAnnotation.getValue('booleanValue'))
						fieldAnnotation.set('intValue', classAnnotation.getValue('intValue'))
						fieldAnnotation.set('longValue', classAnnotation.getValue('longValue'))
						fieldAnnotation.set('stringValue', classAnnotation.getValue('stringValue'))
						fieldAnnotation.set('booleanArrayValue', classAnnotation.getValue('booleanArrayValue'))
						fieldAnnotation.set('intArrayValue', classAnnotation.getValue('intArrayValue'))
						fieldAnnotation.set('longArrayValue', classAnnotation.getValue('longArrayValue'))
						fieldAnnotation.set('stringArrayValue', classAnnotation.getValue('stringArrayValue'))
						fieldAnnotation.set('typeValue', classAnnotation.getValue('typeValue'))
						fieldAnnotation.set('typeArrayValue', classAnnotation.getValue('typeArrayValue'))
				  		fieldAnnotation.set('annotation2Value', classAnnotation.getValue('annotation2Value'))
				  		fieldAnnotation.set('annotation2ArrayValue', classAnnotation.getExpression('annotation2ArrayValue'))
						fieldAnnotation.set('enumValue', classAnnotation.getValue('enumValue'))
						fieldAnnotation.set('enumArrayValue', classAnnotation.getValue('enumArrayValue'))
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				import test.Annotation
				import test.Annotation2
				import myannotation.MoveValues
				
				@Annotation(
					intValue = 2 / 2 + 2 * 3 - 4 % 1,
					longValue = 42 + 4 + 6 * 42 - 4 / 45,
					stringValue = 'foo' + 'baz',
					booleanArrayValue = #[true, false],
					intArrayValue = #[ -1, 34 + 45, 2 - 6 ],
					longArrayValue = #[42, 5 * -3],
					stringArrayValue = #['foo', 'bla' + 'buzz'],
					typeValue = String,
					typeArrayValue = #[String, Integer],
					annotation2Value = @Annotation2('foo' + 'wuppa'),
					annotation2ArrayValue = #[@Annotation2, @Annotation2('foo'+'wuppa')],
					enumValue = test.Enum1.YELLOW,
					enumArrayValue = #[test.Enum1.YELLOW, test.Enum1.RED]
				)
				@MoveValues 
				class UserCode {
					@Annotation() String foo
				}
			'''
		) [
			val clazz = typeLookup.findClass("myusercode.UserCode")
			val annoRef = clazz.declaredFields.head.annotations.head
			
			assertEquals(2 / 2 + 2 * 3 - 4 % 1, annoRef.getValue("intValue"))
			assertEquals((42 + 4 + 6 * 42 - 4 / 45) as long, annoRef.getValue("longValue"))
			assertEquals('foobaz', annoRef.getValue("stringValue"))
			
			val bools = annoRef.getValue("booleanArrayValue") as boolean[]
			assertTrue(bools.get(0))
			assertFalse(bools.get(1))
			
			assertArrayEquals(#[ -1, 34 + 45, 2 - 6 ], annoRef.getValue("intArrayValue") as int[])
			
			val type = annoRef.getValue('typeArrayValue') as TypeReference[]
			
			assertEquals(typeReferenceProvider.newTypeReference(Integer), type.get(1)) 
			
			val anno = annoRef.getValue('annotation2Value') as AnnotationReference
			assertEquals('foowuppa', anno.getValue('value'))
			
			val annoArray = annoRef.getValue('annotation2ArrayValue') as AnnotationReference[]
			assertEquals("HUBBA BUBBA!", annoArray.get(0).getValue('value'))
			
			val enum1 = annoRef.getValue('enumValue') as EnumerationValueDeclaration
			assertEquals('YELLOW', enum1.simpleName)
			
			val enumArray = annoRef.getValue('enumArrayValue') as EnumerationValueDeclaration[]
			assertEquals("YELLOW", enumArray.head.simpleName)
		]
	}
	
	@Test def void testAnnotationValueSetting_AsExpression() {
		assertProcessing(
			'myannotation/MoveValues.xtend' -> '''
				package myannotation
				
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				import org.eclipse.xtend.lib.macro.declaration.TypeReference
				import org.eclipse.xtend.lib.macro.Active
				
				@Active(MoveAnnotationValuesProcessor)
				annotation MoveValues {}
				
				class MoveAnnotationValuesProcessor extends AbstractClassProcessor {
					
					override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
						val classAnnotation = annotatedClass.annotations.head
						val fieldAnnotation = annotatedClass.declaredFields.head.annotations.head
						
						fieldAnnotation.set('booleanValue', classAnnotation.getExpression('booleanValue'))
						fieldAnnotation.set('intValue', classAnnotation.getExpression('intValue'))
						fieldAnnotation.set('longValue', classAnnotation.getExpression('longValue'))
						fieldAnnotation.set('stringValue', classAnnotation.getExpression('stringValue'))
						fieldAnnotation.set('booleanArrayValue', classAnnotation.getExpression('booleanArrayValue'))
						fieldAnnotation.set('intArrayValue', classAnnotation.getExpression('intArrayValue'))
						fieldAnnotation.set('longArrayValue', classAnnotation.getExpression('longArrayValue'))
						fieldAnnotation.set('stringArrayValue', classAnnotation.getExpression('stringArrayValue'))
						fieldAnnotation.set('typeValue', classAnnotation.getExpression('typeValue'))
						fieldAnnotation.set('typeArrayValue', classAnnotation.getExpression('typeArrayValue'))
				  		fieldAnnotation.set('annotation2Value', classAnnotation.getExpression('annotation2Value'))
				  		fieldAnnotation.set('annotation2ArrayValue', classAnnotation.getExpression('annotation2ArrayValue'))
						fieldAnnotation.set('enumValue', classAnnotation.getExpression('enumValue'))
						fieldAnnotation.set('enumArrayValue', classAnnotation.getExpression('enumArrayValue'))
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				import test.Annotation
				import test.Annotation2
				import myannotation.MoveValues
				
				@Annotation(
					intValue = 2 / 2 + 2 * 3 - 4 % 1,
					longValue = 42 + 4 + 6 * 42 - 4 / 45,
					stringValue = 'foo' + 'baz',
					booleanValue = true,
					booleanArrayValue = #[true, false],
					intArrayValue = #[ -1, 34 + 45, 2 - 6 ],
					longArrayValue = #[42, 5 * -3],
					stringArrayValue = #['foo', 'bla' + 'buzz'],
					typeValue = String,
					typeArrayValue = #[String, Integer],
					annotation2Value = @Annotation2('foo' + 'wuppa'),
					annotation2ArrayValue = #[@Annotation2, @Annotation2('foo'+'wuppa')],
					enumValue = test.Enum1.YELLOW,
					enumArrayValue = #[test.Enum1.YELLOW, test.Enum1.RED]
				)
				@MoveValues 
				class UserCode {
					@Annotation() String foo
				}
			'''
		) [
			val clazz = typeLookup.findClass("myusercode.UserCode")
			val annoRef = clazz.declaredFields.head.annotations.head
			
			assertEquals(2 / 2 + 2 * 3 - 4 % 1, annoRef.getValue("intValue"))
			assertEquals((42 + 4 + 6 * 42 - 4 / 45) as long, annoRef.getValue("longValue"))
			assertEquals('foobaz', annoRef.getValue("stringValue"))
			
			assertTrue(annoRef.getValue("booleanValue") as Boolean)
			val bools = annoRef.getValue("booleanArrayValue") as boolean[]
			assertTrue(bools.get(0))
			assertFalse(bools.get(1))
			
			assertArrayEquals(#[ -1, 34 + 45, 2 - 6 ], annoRef.getValue("intArrayValue") as int[])
			
			val type = annoRef.getValue('typeArrayValue') as TypeReference[]
			
			assertEquals(typeReferenceProvider.newTypeReference(Integer), type.get(1)) 
			
			val anno = annoRef.getValue('annotation2Value') as AnnotationReference
			assertEquals('foowuppa', anno.getValue('value'))
			
			val annoArray = annoRef.getValue('annotation2ArrayValue') as AnnotationReference[]
			assertEquals("HUBBA BUBBA!", annoArray.get(0).getValue('value'))
			
			val enum1 = annoRef.getValue('enumValue') as EnumerationValueDeclaration
			assertEquals('YELLOW', enum1.simpleName)
			
			val enumArray = annoRef.getValue('enumArrayValue') as EnumerationValueDeclaration[]
			assertEquals("YELLOW", enumArray.head.simpleName)
		]
	}
	
	@Test def void testAccessAndModifyEnumerationValueDeclaration() {
		assertProcessing(
			'myannotation/MyAnnotation.xtend' -> '''
				package myannotation

				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.CodeGenerationContext
				import org.eclipse.xtend.lib.macro.CodeGenerationParticipant
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.EnumerationTypeDeclaration
				import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableEnumerationTypeDeclaration
				import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration
				import org.eclipse.xtend.lib.macro.declaration.Visibility
				
				import static com.google.common.base.Preconditions.*
				
				@Active(MyAnnotationProcessor)
				annotation MyAnnotation {
				}
				
				class MyAnnotationProcessor implements RegisterGlobalsParticipant<EnumerationTypeDeclaration>, TransformationParticipant<MutableEnumerationTypeDeclaration>, CodeGenerationParticipant<EnumerationTypeDeclaration> {
				
					override doGenerateCode(List<? extends EnumerationTypeDeclaration> annotatedSourceElements,
						extension CodeGenerationContext context) {
					}
				
					override doRegisterGlobals(List<? extends EnumerationTypeDeclaration> annotatedSourceElements,
						RegisterGlobalsContext context) {
						for (enumeration : annotatedSourceElements) {
							enumeration.checkState
						}
					}
				
					def checkState(EnumerationTypeDeclaration enumeration) {
						val values = enumeration.declaredValues
						checkState(values.size == 3, "enumeration.declaredValues.size != 3")
						enumeration.findDeclaredValue(values.get(0).simpleName).checkState("A", enumeration)
						enumeration.findDeclaredValue(values.get(1).simpleName).checkState("B", enumeration)
						enumeration.findDeclaredValue(values.get(2).simpleName).checkState("C", enumeration)
					}
				
					def checkState(EnumerationValueDeclaration value, String expectedSimpleName, TypeDeclaration expectedType) {
						checkState(value.simpleName == expectedSimpleName, "value.simpleName != expectedSimpleName")
						checkState(value.declaringType == expectedType, "value.declaringType != expectedType")
						checkState(value.visibility == Visibility.PUBLIC, "value.visibility != Visibility.PUBLIC")
						checkState(value.annotations.size == 0, "value.annotations.size != 0")
						checkState(value.findAnnotation(null) == null, "value.findAnnotation(null) != null")
					}

					override doTransform(List<? extends MutableEnumerationTypeDeclaration> annotatedTargetElements,
						extension TransformationContext context) {
						for (enumeration : annotatedTargetElements) {
							enumeration.checkState
							for (value : enumeration.declaredValues) {
								checkState(value.annotations.size == 0, value.annotations.size != 0)
								value.addAnnotation(Deprecated.newTypeReference.type)
								checkState(value.annotations.size == 1, value.annotations.size != 1)
							}
							enumeration.addValue("D") [
								addAnnotation(Deprecated.newTypeReference.type)
							]
						}
					}
				
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode

				import myannotation.MyAnnotation
				
				@MyAnnotation
				enum UserCode {
					A, 
					B,
					C
				}
			'''
		) [
			val enumerationType = typeLookup.findEnumerationType("myusercode.UserCode")
			assertEquals(4, enumerationType.declaredValues.size)
			
			val deprecatedAnnotationType = typeReferenceProvider.newTypeReference(Deprecated).type
			for (value : enumerationType.declaredValues) {
				assertEquals(1, value.annotations.size)
				assertNotNull(value.findAnnotation(deprecatedAnnotationType))
			}
			assertEquals("D", enumerationType.declaredValues.last.simpleName)
			assertNotNull(enumerationType.findDeclaredValue("D"))
		]
	}
	
	@Test def void testModifyTypeParameters() {
		assertProcessing(
			'myannotation/MyAnnotation.xtend' -> '''
				package myannotation

				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclarator
				
				@Active(MyAnnotationProcessor)
				annotation MyAnnotation {
				}
				
				class MyAnnotationProcessor implements TransformationParticipant<MutableTypeParameterDeclarator> {
				
					override doTransform(List<? extends MutableTypeParameterDeclarator> annotatedTargetElements,
						extension TransformationContext context) {
						for (annotatedTargetElement : annotatedTargetElements) {
							doTransform(annotatedTargetElement, context)
						}
					}
				
					def doTransform(MutableTypeParameterDeclarator it, extension TransformationContext context) {
						if (typeParameters.size != 0) {
							throw new IllegalStateException("Before: typeParameters.size != 0")
						}
						addTypeParameter("T", String.newTypeReference)
						if (typeParameters.size != 1) {
							throw new IllegalStateException("After: typeParameters.size != 1")
						}
						val typeParameter = typeParameters.head
						if (!typeParameter.simpleName.equals("T")) {
							throw new IllegalStateException("After: expected type name: 'T', actual type name: '" + typeParameter.simpleName + "'")
						}
					}
				
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode

				import myannotation.MyAnnotation
				
				@MyAnnotation
				class MyClass {
				
					@MyAnnotation
					new() {
					}
				
				}
				
				@MyAnnotation
				interface MyInterface {
				
					@MyAnnotation
					abstract def void foo();
				
				}
			'''
		) [
			val (MutableTypeParameterDeclarator)=>void assertMyAnnotationChanges = [
				assertEquals(1, typeParameters.size)
			
				val typeParameter = typeParameters.head
				assertEquals("T", typeParameter.simpleName)
				assertEquals(1, typeParameter.upperBounds.size)
				assertEquals("java.lang.String", typeParameter.upperBounds.head.name)
			]
			
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertMyAnnotationChanges.apply(clazz)
			assertMyAnnotationChanges.apply(clazz.declaredConstructors.head)
			
			val myInterface = typeLookup.findInterface('myusercode.MyInterface')
			assertMyAnnotationChanges.apply(myInterface)
			assertMyAnnotationChanges.apply(myInterface.declaredMethods.head)
		]
	}
	
	@Test def void testRemoveTypeParameters() {
		assertProcessing(
			'myannotation/MyAnnotation.xtend' -> '''
				package myannotation

				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclarator
				
				@Active(MyAnnotationProcessor)
				annotation MyAnnotation {
				}
				
				class MyAnnotationProcessor implements TransformationParticipant<MutableTypeParameterDeclarator> {
				
					override doTransform(List<? extends MutableTypeParameterDeclarator> annotatedTargetElements,
						extension TransformationContext context) {
						for (annotatedTargetElement : annotatedTargetElements) {
							doTransform(annotatedTargetElement, context)
						}
					}
				
					def doTransform(MutableTypeParameterDeclarator it, extension TransformationContext context) {
						typeParameters.head.remove
					}
				
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode

				import myannotation.MyAnnotation
				
				@MyAnnotation
				class MyClass<T extends String> {
				}
				
				@MyAnnotation
				interface MyInterface<T extends String> {
				
					@MyAnnotation
					abstract def <T extends String> void foo();
				
				}
			'''
		) [
			val (MutableTypeParameterDeclarator)=>void assertMyAnnotationChanges = [
				assertEquals(0, typeParameters.size)
			]
			
			assertMyAnnotationChanges.apply(typeLookup.findClass('myusercode.MyClass'))
			
			val myInterface = typeLookup.findInterface('myusercode.MyInterface')
			assertMyAnnotationChanges.apply(myInterface)
			assertMyAnnotationChanges.apply(myInterface.declaredMethods.head)
		]
	}
	
	@Test def void testSetUpperBoundsForMutableTypeParameterDeclaration() {
		assertProcessing(
			'myannotation/MyAnnotation.xtend' -> '''
				package myannotation

				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclarator
				
				@Active(MyAnnotationProcessor)
				annotation MyAnnotation {
				}
				
				class MyAnnotationProcessor implements TransformationParticipant<MutableTypeParameterDeclarator> {
				
					override doTransform(List<? extends MutableTypeParameterDeclarator> annotatedTargetElements,
						extension TransformationContext context) {
						for (annotatedTargetElement : annotatedTargetElements) {
							doTransform(annotatedTargetElement, context)
						}
					}
				
					def doTransform(MutableTypeParameterDeclarator it, extension TransformationContext context) {
						typeParameters.head.upperBounds = #[String.newTypeReference]
					}
				
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode

				import myannotation.MyAnnotation
				
				@MyAnnotation
				class MyClass<T extends CharSequence> {
				}
				
				@MyAnnotation
				interface MyInterface<T extends CharSequence> {
				
					@MyAnnotation
					abstract def <T extends CharSequence> void foo();
				
				}
			'''
		) [
			val (MutableTypeParameterDeclarator)=>void assertMyAnnotationChanges = [
				assertEquals(1, typeParameters.size)
				
				val typeParameter = typeParameters.head
				assertEquals(1, typeParameter.upperBounds.size)
				assertEquals("java.lang.String", typeParameter.upperBounds.head.name)
			]
			
			assertMyAnnotationChanges.apply(typeLookup.findClass('myusercode.MyClass'))
			
			val myInterface = typeLookup.findInterface('myusercode.MyInterface')
			assertMyAnnotationChanges.apply(myInterface)
			assertMyAnnotationChanges.apply(myInterface.declaredMethods.head)
		]
	}

	@Test def void testRemoveAnnotation() {
		assertProcessing(
			'myannotation/RemoveAnnotation.xtend' -> '''
				package myannotation
				
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				
				@Active(typeof(RemoveAnnotationProcessor))
				annotation RemoveAnnotation{ }
				class RemoveAnnotationProcessor extends AbstractClassProcessor {
				
					override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
						clazz.findAnnotation(RemoveAnnotation.newTypeReference.type).remove
					}
				
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				/**
				 * MAKE ME LOWER CASE!
				 */
				@myannotation.RemoveAnnotation class MyClass {
				}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertEquals(0, clazz.annotations.size)
		]
	}

	@Test def void testChangeJavaDoc() {
		assertProcessing(
			'myannotation/ChangeDocAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				
				@Active(typeof(ChangeDocProcessor))
				annotation ChangeDoc{ }
				class ChangeDocProcessor extends AbstractClassProcessor {
					
					override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
						clazz.docComment = clazz.docComment.toLowerCase
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				/**
				 * MAKE ME LOWER CASE!
				 */
				@myannotation.ChangeDoc class MyClass {
				}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertEquals("make me lower case!", clazz.docComment)
		]
	}

	@Test def void testAddConstructor() {
		assertProcessing(
			'myannotation/AddConstructorAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				
				@Active(typeof(AddConstructorProcessor))
				annotation AddConstructor{ }
				class AddConstructorProcessor extends AbstractClassProcessor {
					
					override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
						clazz.addConstructor [
							addParameter("foo", string)
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.AddConstructor class MyClass {
				}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertEquals(1, clazz.declaredConstructors.size)
			assertEquals('foo', clazz.declaredConstructors.head.parameters.head.simpleName)
		]
	}

	@Test def void testAddDefaultConstructor() {
		assertProcessing(
			'myannotation/AddConstructorAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				
				@Active(typeof(AddConstructorProcessor))
				annotation AddConstructor{ }
				class AddConstructorProcessor extends AbstractClassProcessor {
					
					override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
						clazz.addConstructor [
							body=['System.out.println("Hello World");']
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.AddConstructor class MyClass {
				}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertEquals(1, clazz.declaredConstructors.size)
			assertTrue(clazz.declaredConstructors.head.parameters.isEmpty)
		]
	}
	
	@Test def void testAddDefaultConstructorWithTemplate() {
		assertProcessing(
			'myannotation/AddConstructorAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				
				@Active(typeof(AddConstructorProcessor))
				annotation AddConstructor{ }
				class AddConstructorProcessor extends AbstractClassProcessor {
					
					override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
						clazz.addConstructor [
							body=�"'''"�System.out.println("Hello World");�"'''"�
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.AddConstructor class MyClass {
				}
			'''
		) [
			compilationUnit
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertEquals(1, clazz.declaredConstructors.size)
			assertTrue(clazz.declaredConstructors.head.parameters.isEmpty)
		]
	}

	@Test def void testSwapExpressions() {
		assertProcessing(
			'myannotation/SwapAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
				
				@Active(typeof(SwapProcessor))
				annotation Swap{ }
				class SwapProcessor implements TransformationParticipant<MutableMethodDeclaration> {
					
					override doTransform(List<? extends MutableMethodDeclaration> methods, extension TransformationContext context) {
						val b1 = methods.get(0).body
						val b2 = methods.get(1).body
						methods.get(0).body = b2
						methods.get(1).body = b1
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					@myannotation.Swap def foo(String a) {
						return b
					}
					@myannotation.Swap def bar(String b) {
						return a
					}
				}
			'''
		) [
			// no compile errors
		]
	}

	@Test def void testSwapExpressions_01() {
		assertProcessing(
			'myannotation/SwapAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
				
				@Active(typeof(SwapProcessor))
				annotation Swap{ }
				class SwapProcessor implements TransformationParticipant<MutableFieldDeclaration> {
					
					override doTransform(List<? extends MutableFieldDeclaration> fields, extension TransformationContext context) {
						val b1 = fields.get(0).initializer
						val b2 = fields.get(1).initializer
						fields.get(0).initializer = b2
						fields.get(1).initializer = b1
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					@myannotation.Swap String a = 42
					@myannotation.Swap int b = 'foo'
				}
			'''
		) [
			// no compile errors
		]
	}

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
					
					override doTransform(List<? extends MutableClassDeclaration> annotatedSourceClasses, extension TransformationContext context) {
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
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertTrue(clazz.isAbstract)
		]
	}

	@Test def void testParameterAnnotation() {
		assertProcessing(
			'myannotation/AbstractAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration
				
				@Active(typeof(ParamProcessor))
				annotation Param { }
				class ParamProcessor implements TransformationParticipant<MutableParameterDeclaration> {
					
					override doTransform(List<? extends MutableParameterDeclaration> params, extension TransformationContext context) {
						params.forEach[
							simpleName = simpleName+'foo'
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					def void foo(@myannotation.Param String a, @myannotation.Param String b) {
					}
				}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertTrue(clazz.declaredMethods.head.parameters.forall[simpleName.endsWith('foo')])
		]
	}
	
	@Test def void testConstantExpressionEvaluation() {
		assertProcessing(
			'annotations/MyAnnotation.xtend' -> '''
				package annotations
				
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				import org.eclipse.xtend.lib.macro.declaration.Visibility
				
				@Active(Processor)
				annotation MyAnnotation {
					String value
				}
				
				class Processor extends AbstractClassProcessor {
					
					override doRegisterGlobals(ClassDeclaration annotatedClass, extension RegisterGlobalsContext context) {
						val value = annotatedClass.annotations.head.getValue('value') as String
						registerClass(annotatedClass.qualifiedName+'.'+value)
					}
					
					override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
						val value = annotatedClass.annotations.head.getValue('value') as String
						val newClass = findClass(annotatedClass.qualifiedName+'.'+value)
						for (method : annotatedClass.declaredMethods) {
							method.addParameter(value, newClass.newTypeReference)
						}
						newClass.addField(value) [
							type = string
							visibility = Visibility.PUBLIC
							initializer = '��''"FOO"'��''
						]
					}
					
				}
			''',
			'application/MyAnnotation.xtend' -> '''
				package application
				
				import annotations.MyAnnotation
				
				@MyAnnotation("FOO") class MyClient {
					def myMethod() {
						FOO.FOO
					}  
				}
			'''
		) [
			val clazz = typeLookup.findClass('application.MyClient.FOO')
			assertEquals("FOO",clazz.declaredFields.head.simpleName)
			
			val clazz2 = typeLookup.findClass('application.MyClient')
			assertEquals("FOO",clazz2.declaredMethods.head.parameters.head.simpleName)
		]
	}

	@Test def void testSetDocumentation() {
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
					
					override doTransform(List<? extends MutableClassDeclaration> annotatedSourceClasses, extension TransformationContext context) {
						annotatedSourceClasses.forEach [
							docComment = docComment.toCharArray.reverse.join
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				/**
				 * dlroW olleH
				 */
				@myannotation.Abstract
				class MyClass {
					
				}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			assertEquals('Hello World', clazz.docComment)
		]
	}

	@Test def void testAddAnnotationValue() {
		assertProcessing(
			'myannotation/AddAnnotationValue.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTarget
				
				@Active(AddAnnotationValueProcessor)
				annotation AddAnnotationValue { }
				
				class AddAnnotationValueProcessor implements TransformationParticipant<MutableAnnotationTarget> {
					
					override doTransform(List<? extends MutableAnnotationTarget> annotationTargets, extension TransformationContext context) {
						annotationTargets.forEach [
							addAnnotation(typeof(MyAnnotation).findTypeGlobally) => [
								set('value', #['foo','bar','baz'] as String[])
								set('singleValue', 'foo')
								set('booleans', #[true, false, true] as boolean[])
								set('singleBoolean', true)
								set('numbers', #[1,2,3] as int[])
								set('singleNumber', 1)
							]
						]
					}
					
				}
				annotation MyAnnotation {
					String[] value
					String singleValue
					boolean[] booleans
					boolean singleBoolean
					int[] numbers
					int singleNumber
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.AddAnnotationValue
				class MyClass {}
			'''
		) [
			val clazz = typeLookup.findClass('myusercode.MyClass')
			val annotation = clazz.findAnnotation(
				typeReferenceProvider.newTypeReference('myannotation.MyAnnotation').type)
			assertArrayEquals(#['foo', 'bar', 'baz'] as String[], annotation.getValue('value') as String[])
			assertEquals('foo', annotation.getValue('singleValue'))
			val booleans = annotation.getValue('booleans') as boolean[]
			assertTrue(booleans.get(2))
			assertEquals(true, annotation.getValue('singleBoolean'))
			assertArrayEquals(#[1, 2, 3], annotation.getValue('numbers') as int[])
			assertEquals(1, annotation.getValue('singleNumber'))
		]
	}
	
	@Test def void testCreateTypeFromUsage() {
		assertProcessing(
			'myannotation/SomeAnnotation.xtend' -> '''
				package myannotation
				
				import java.lang.annotation.Documented
				import java.lang.annotation.ElementType
				import java.lang.annotation.Target
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant
				import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
				import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration
				
				@Active(typeof(SomeProcessor))
				@Documented
				@Target(ElementType::TYPE)
				annotation SomeAnnotation {}
				class SomeProcessor implements RegisterGlobalsParticipant<TypeDeclaration> {
					override doRegisterGlobals(List<? extends TypeDeclaration> types, RegisterGlobalsContext context) {
						types.forEach[
							declaredMethods.forEach[
								context.registerClass(parameterType)
							]
						]
					}
					private def getParameterType(MethodDeclaration it) {
						parameters.head.type.type.qualifiedName
					}
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.SomeAnnotation
				class MyClass {
					def void myMethod(DoesNotExist p) {}
				}
			'''
		) [
			assertTrue(typeLookup.findClass('myusercode.DoesNotExist').simpleName == 'DoesNotExist')
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
					
					override doTransform(List<? extends MutableFieldDeclaration> annotatedTargetFields, extension TransformationContext context) {
						annotatedTargetFields.forEach [ field |
							val declaringType = field.declaringType 
							declaringType.addMethod(field.getterName) [
								returnType = field.type
								body = ['''
									return this.�field.simpleName�;
								''']
							]
							declaringType.addMethod('set'+field.simpleName.toFirstUpper) [
								addParameter(field.simpleName, field.type)
								body = ['''
									this.�field.simpleName� = �field.simpleName�;
								''']
							]
						]
					}
					
					def private String getterName(MutableFieldDeclaration field) {
						return 'get'+field.simpleName.toFirstUpper
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
			val clazz = typeLookup.findClass('myusercode.MyClass')
			val getter = clazz.declaredMethods.head
			assertEquals('getMyField', getter.simpleName)
			assertEquals('String', getter.returnType.toString)
			val setter = clazz.findDeclaredMethod('setMyField', getter.returnType)
			assertEquals('setMyField', setter.simpleName)
			assertEquals('void', setter.returnType.toString)
			assertEquals('myField', setter.parameters.head.simpleName)
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
					
					override doTransform(List<? extends MutableMethodDeclaration> annotatedMethods, extension TransformationContext context) {
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
			val type = typeLookup.findClass('myusercode.MyClass')
			val method = type.declaredMethods.head
			assertEquals('A', method.typeParameters.head.simpleName)
			assertEquals('myParam', method.parameters.head.simpleName)
			assertSame(method.typeParameters.head, method.parameters.head.type.type)
			assertEquals(1, method.exceptions.size)
		]
	}
	
	@Test def void testMovingComputedTypes() {
		assertProcessing(
			'myannotation/MyAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
				
				@Active(Field2MethodProcessor)
				annotation Field2Method { }
				class Field2MethodProcessor implements TransformationParticipant<MutableFieldDeclaration> {
					
					override doTransform(List<? extends MutableFieldDeclaration> annotatedFields, extension TransformationContext context) {
						annotatedFields.forEach [ field |
							field.declaringType.addMethod(field.simpleName) [
								returnType = field.type
								body = field.initializer
							]
							field.remove
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					val foo = 'foo'
					@myannotation.Field2Method
					val x = foo
					@myannotation.Field2Method
					val y = x
				}
			'''
		) [
			val type = typeLookup.findClass('myusercode.MyClass')
			val method = type.declaredMethods.get(1)
			assertEquals('java.lang.String', method.returnType.name)
		]
	}
	
	@Test def void testMovingComputedTypes_02() {
		assertProcessing(
			'myannotation/MyAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
				
				@Active(Method2FieldProcessor)
				annotation Method2Field { }
				class Method2FieldProcessor implements TransformationParticipant<MutableMethodDeclaration> {
					
					override doTransform(List<? extends MutableMethodDeclaration> annotatedMethods, extension TransformationContext context) {
						annotatedMethods.forEach [ method |
							method.declaringType.addField(method.simpleName) [
								type = method.returnType
								initializer = method.body
							]
							method.remove
						]
					}
					
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				class MyClass {
					val foo = 'foo'
					@myannotation.Method2Field
					def x() { foo }
					@myannotation.Method2Field
					def y() { x }
				}
			'''
		) [
			val type = typeLookup.findClass('myusercode.MyClass')
			val field = type.declaredFields.get(1)
			assertEquals('java.lang.String', field.type.name)
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
					
					override doTransform(List<? extends MutableNamedElement> annotatedMethods, extension TransformationContext context) {
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
			val type = typeLookup.findClass('myusercode.MyClass')
			val method = type.declaredMethods.head
			val field = type.declaredFields.head
			assertEquals('field-warning', problemSupport.getProblems(field).head.message)
			assertEquals('warning', problemSupport.getProblems(method).head.message)
		]
	}

	@Test def void testIntroduceNewTypes() {
		assertProcessing(
			'myannotation/NewTypesAddingAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant
				import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
				
				@Active(typeof(NewTypesAddingAnnotationProcessor))
				annotation NewTypesAddingAnnotation { }
				class NewTypesAddingAnnotationProcessor implements RegisterGlobalsParticipant<ClassDeclaration> {
					
					override doRegisterGlobals(List<? extends ClassDeclaration> sourceClasses, RegisterGlobalsContext context) {
						for (clazz : sourceClasses) {
							context.registerClass(clazz.qualifiedName+".InnerClass")
							context.registerInterface(clazz.qualifiedName+"Interface")
							context.registerEnumerationType(clazz.qualifiedName+"Enum")
							context.registerAnnotationType(clazz.qualifiedName+"Annotation")
						}
					}
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.NewTypesAddingAnnotation
				class MyClass {
				}
			'''
		) [
			assertNotNull(typeLookup.findClass('myusercode.MyClass'))
			assertNotNull(typeLookup.findClass('myusercode.MyClass.InnerClass'))
			assertNotNull(typeLookup.findInterface('myusercode.MyClassInterface'))
			assertNotNull(typeLookup.findEnumerationType('myusercode.MyClassEnum'))
			assertNotNull(typeLookup.findAnnotationType('myusercode.MyClassAnnotation'))
		]
	}

	@Test def void testIntroduceNewTypeAndWorkWithIt() {
		assertProcessing(
			'myannotation/NewTypesAddingAnnotation.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant
				import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				
				@Active(typeof(NewTypesAddingAnnotationProcessor))
				annotation NewTypesAddingAnnotation { }
				class NewTypesAddingAnnotationProcessor implements RegisterGlobalsParticipant<ClassDeclaration>, TransformationParticipant<MutableClassDeclaration> {
					
					override doRegisterGlobals(List<? extends ClassDeclaration> sourceClasses, RegisterGlobalsContext context) {
						for (clazz : sourceClasses) {
							context.registerClass(clazz.qualifiedName+"Derived")
						}
					}
					
					override doTransform(List<? extends MutableClassDeclaration> classes, extension TransformationContext context) {
						classes.forEach [ ele |
							val cl = context.findClass(ele.qualifiedName+"Derived")
							cl.extendedClass = newTypeReference(ele)
						]
					}
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.NewTypesAddingAnnotation
				class MyClass {
				}
			'''
		) [
			val declaredClass = typeLookup.findClass('myusercode.MyClass')
			assertNotNull(declaredClass)
			val clazz = typeLookup.findClass('myusercode.MyClassDerived')
			assertNotNull(clazz)
			assertEquals(declaredClass.qualifiedName, clazz.extendedClass.type.qualifiedName)
		]
	}

	val THREE_ANNOTATIONS = 'myannotation/three.xtend' -> '''
		package myannotation
		
		import java.util.List
		import org.eclipse.xtend.lib.macro.Active
		import org.eclipse.xtend.lib.macro.TransformationContext
		import org.eclipse.xtend.lib.macro.TransformationParticipant
		import org.eclipse.xtend.lib.macro.declaration.MutableNamedElement
		
		@Active(typeof(Aprocessor))
		annotation _A {}
		class Aprocessor implements TransformationParticipant<MutableNamedElement> {
			
			override doTransform(List<? extends MutableNamedElement> annotatedTargetElements, extension TransformationContext context) {
				annotatedTargetElements.forEach[
					simpleName = simpleName + num()
				]
			}
		
			def num() {
				'_A'
			}
			
		}
		@Active(typeof(Bprocessor))
		annotation _B {}
		class Bprocessor extends Aprocessor {
			override num() { '_B' }
		}
		
		@Active(typeof(Cprocessor))
		annotation _C {}
		class Cprocessor extends Aprocessor {
			override num() { '_C' }
		}
	'''

	@Test def void testDeterministicExecutionOrder_01() {
		assertProcessing(
			THREE_ANNOTATIONS,
			'MyClass.xtend' -> '''
				import myannotation.*
				
				class MyClass {
					@_A @_B @_C String field
				}
			'''
		) [
			val myClass = typeLookup.findClass('MyClass')
			assertEquals('field_A_B_C', myClass.declaredFields.head.simpleName)
		]
	}

	@Test def void testDeterministicExecutionOrder_02() {

		// annotation processors are called in the order their annotations first occur in the file
		assertProcessing(
			THREE_ANNOTATIONS,
			'MyClass.xtend' -> '''
				import myannotation.*
				class MyClass {
					@_A @_B @_C String field1
					@_C @_B @_A String field2
				}
			'''
		) [
			val myClass = typeLookup.findClass('MyClass')
			assertEquals('field1_A_B_C', myClass.declaredFields.head.simpleName)
			assertEquals('field2_A_B_C', myClass.declaredFields.get(1).simpleName)
		]
	}

	def void assertProcessing(Pair<String, String> macroFile, Pair<String, String> clientFile,
		(CompilationUnitImpl)=>void expectations)
		
	@Test def void testFileSystemSupport_01() {

		assertProcessing(
			'myannotation/FileSystemSupportTest.xtend' -> "
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant
				import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				
				@Active(FileSystemUsingProcessor)
				annotation FileSystemSupportTest { }
				
				class FileSystemUsingProcessor extends AbstractClassProcessor {
	
					override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
						val path = annotatedClass.compilationUnit.filePath
						annotatedClass.docComment = '''
							Path '�path.toString�' {
								exists: �path.exists�
								isFolder: �path.isFolder�
								isFile: �path.isFile�
							}
							sourceFolder : �path.sourceFolder�
							targetFolder : �path.targetFolder�
							projectFolder: �path.projectFolder�
						'''
					}
					
				}
			",
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.FileSystemSupportTest
				class MyClass {
				}
			'''
		) [
			val declaredClass = typeLookup.findClass('myusercode.MyClass')
			assertEquals('''
				Path '/userProject/src/myusercode/UserCode.xtend' {
					exists: true
					isFolder: false
					isFile: true
				}
				sourceFolder : /userProject/src
				targetFolder : /userProject/xtend-gen
				projectFolder: /userProject
			'''.toString, declaredClass.docComment)
		]
	}
}

class FileSystemUsingProcessor extends AbstractClassProcessor {
	
	override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
		val path = annotatedClass.compilationUnit.filePath
		annotatedClass.docComment = '''
			Path "�path.toString�" {
				exists: �path.exists�
				isFolder: �path.isFolder�
				isFile: �path.isFile�
			}
			sourceFolder : �path.sourceFolder�
			targetFolder : �path.targetFolder�
			projectFolder: �path.projectFolder�
		'''
	}
	
}

