package org.eclipse.xtend.core.tests.macro

import java.util.List
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.TransformationParticipant
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.junit.Test

import static org.junit.Assert.*
import org.junit.Ignore

class ImmutableAnnotationTest extends AbstractActiveAnnotationTest {
	
	@Ignore("We need to suppress the initialization check.") @Test def void defSimpleTest() {
		'''
			import org.eclipse.xtend.core.tests.macro.Immutable
			
			@Immutable class MyClass {
				String foo
			}
		'''.compile [
			val clazz = compiledClass
			val constr = clazz.declaredConstructors
			assertEquals(1, constr.length)
			assertEquals(1, constr.head.getParameterTypes.length)
		]
	}
}

@Active(typeof(ImmutableProcessor))
annotation Immutable {
}

class ImmutableProcessor implements TransformationParticipant<MutableClassDeclaration> {
	
	override doTransform(List<? extends MutableClassDeclaration> annotatedTargetElements, extension TransformationContext context) {
		for (clazz : annotatedTargetElements) {
			val fields = clazz.declaredFields
			clazz.addConstructor[
				for (f : fields) {
					addParameter(f.name, f.type)
				}
				body = ['''
					�FOR f : fields�
						this.�f.name� = �f.name�;
					�ENDFOR�
				''']
			]
			for (f : fields) {
				// make everything final
				f.final = true
				
				val getterName = 'get'+f.name.toFirstUpper
				
				clazz.tryAddMethod(getterName) [
					returnType = f.type
					body = ['''
						return �f.name�;
					''']
				]
			}
			
			clazz.tryAddMethod('hashCode') [
				returnType = primitiveInt
				addAnnotation(typeof(Override).findTypeGlobally)
				body = ['''
					final int prime = 31;
					�IF clazz.extendsSomethingWithProperHashCode�
						int result = super.hashCode();
					�ELSE�
						int result = 1;
					�ENDIF�
					�FOR f : fields�
						�IF f.type == primitiveBoolean�
							result = prime * result + (�f.name� ? 1231 : 1237);
						�ELSEIF #{primitiveInt, primitiveChar, primitiveByte, primitiveShort}.contains(f.type)�
							result = prime * result + �f.name�;
						�ELSEIF primitiveLong == f.type�
							result = prime * result + (int) (�f.name� ^ (�f.name� >>> 32));
						�ELSEIF primitiveFloat == f.type�
							result = prime * result + Float.floatToIntBits(�f.name�);
						�ELSEIF primitiveDouble == f.type�
							result = prime * result + (int) (Double.doubleToLongBits(�f.name�) ^ (Double.doubleToLongBits(�f.name�) >>> 32));
						�ELSE�
							result = prime * result + ((�f.name�== null) ? 0 : �f.name�.hashCode());
						�ENDIF�
					�ENDFOR�
					return result;
				''']
			]
		}
	}
	
	def boolean getExtendsSomethingWithProperHashCode(MutableClassDeclaration declaration) {
		return false
	}
	
	def MutableMethodDeclaration tryAddMethod(MutableClassDeclaration it, String name, (MutableMethodDeclaration)=>void initializer) {
		findMethod(name) ?: addMethod(name, initializer)
	}
}