/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.conversion

import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.Annotation
import org.eclipse.jdt.core.dom.Assignment
import org.eclipse.jdt.core.dom.Block
import org.eclipse.jdt.core.dom.ClassInstanceCreation
import org.eclipse.jdt.core.dom.FieldAccess
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.IBinding
import org.eclipse.jdt.core.dom.IExtendedModifier
import org.eclipse.jdt.core.dom.IMethodBinding
import org.eclipse.jdt.core.dom.ITypeBinding
import org.eclipse.jdt.core.dom.IVariableBinding
import org.eclipse.jdt.core.dom.InfixExpression
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.Modifier
import org.eclipse.jdt.core.dom.Name
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.ReturnStatement
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.Statement
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.Type
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.TypeDeclarationStatement
import org.eclipse.jdt.core.dom.VariableDeclarationExpression
import org.eclipse.jdt.core.dom.VariableDeclarationFragment
import org.eclipse.jdt.core.dom.VariableDeclarationStatement

/**
 * @author dhuebner - Initial contribution and API
 */
class ASTFlattenerUtils {

	def boolean isDummyType(AbstractTypeDeclaration it) {
		if (it instanceof TypeDeclaration) {
			return "MISSING".equals(getName().getIdentifier()) && !isInterface() && getModifiers() == 0
		}
		return false;
	}

	def boolean isOverrideMethode(MethodDeclaration declaration) {
		if (!declaration.modifiers().filter(Annotation).filter [
			"Override" == typeName.toString
		].empty) {
			return true
		}
		val iMethodBinding = declaration.resolveBinding
		if (iMethodBinding != null) {
			return findOverride(iMethodBinding, iMethodBinding.declaringClass) != null
		}
		return false
	}

	def IMethodBinding findOverride(IMethodBinding method, ITypeBinding type) {
		findOverride(method, type, false)
	}

	def IMethodBinding findOverride(IMethodBinding method, ITypeBinding type, boolean onlyPrimarylevel) {
		val superclass = type.superclass
		var IMethodBinding overridden = null
		if (superclass != null) {
			overridden = internalFindOverride(method, superclass, onlyPrimarylevel)
		}
		if (overridden == null) {
			for (ITypeBinding interfaze : type.interfaces) {
				overridden = internalFindOverride(method, interfaze, onlyPrimarylevel)
				if (overridden != null) {
					return overridden
				}
			}
		}
		return overridden
	}

	def private internalFindOverride(IMethodBinding method, ITypeBinding superType, boolean onlyPrimarylevel) {
		val superClassOverride = superType.declaredMethods.filter[method.overrides(it)]
		if (superClassOverride.size == 1) {
			return superClassOverride.get(0)
		} else if (!onlyPrimarylevel) {
			return findOverride(method, superType)
		} else {
			return null
		}
	}

	def handleVariableDeclaration(Iterable<? extends ASTNode> modifier) {
		if (modifier.filter(Modifier).isFinal) {
			return 'val'
		} else {
			return 'var'
		}
	}

	def boolean isNotSupportedInnerType(TypeDeclaration it) {
		!isInterface() && (parent instanceof TypeDeclaration || parent instanceof Block ) &&
			!modifiers().filter(Modifier).exists[isStatic]
	}

	def boolean isNotSupportedInnerType(TypeDeclarationStatement it) {
		( parent instanceof Block )
	}

	def boolean isFinal(Iterable<Modifier> modifiers) {
		modifiers.filter(Modifier).exists[isFinal]
	}

	def boolean isStatic(Iterable<IExtendedModifier> modifiers) {
		modifiers.filter(Modifier).exists[isStatic]
	}

	def boolean isStaticMemberCall(MethodInvocation methInv) {
		return methInv.resolveMethodBinding.isStaticBinding
	}

	def boolean isStaticMemberCall(QualifiedName expr) {
		return expr.resolveBinding.isStaticBinding
	}

	def boolean isStaticMemberCall(FieldAccess expr) {
		return expr.resolveFieldBinding.isStaticBinding
	}

	def private boolean isStaticBinding(IBinding binding) {
		if (binding != null) {
			return Modifier.isStatic(binding.modifiers)
		}
		return false
	}

	def boolean isPackageVisibility(Iterable<Modifier> modifier) {
		modifier.filter[public || private || protected].empty
	}

	def canHandleAnnotation(ASTNode node) {
		if (node instanceof VariableDeclarationStatement) {
			return false
		}
		return true
	}

	def shouldConvertName(SimpleName it) {
		return (parent instanceof FieldAccess ||
			( parent instanceof VariableDeclarationFragment && parent.parent instanceof FieldDeclaration))
	}

	def isLambdaCase(ClassInstanceCreation creation) {
		val anonymousClazz = creation.anonymousClassDeclaration
		if (anonymousClazz != null && anonymousClazz.bodyDeclarations.size == 1) {
			val declaredMethod = anonymousClazz.bodyDeclarations.get(0)
			if (declaredMethod instanceof MethodDeclaration && creation.type.resolveBinding != null) {
				val methodBinding = (declaredMethod as MethodDeclaration).resolveBinding
				if (methodBinding != null) {
					val overrides = findOverride(methodBinding, methodBinding.declaringClass, true)
					return overrides != null && Modifier.isAbstract(overrides.modifiers)
				}
			}
		}
		return false
	}

	def boolean needsReturnValue(ASTNode node) {
		(node.parent != null) && (!(node.parent instanceof Statement) || (node.parent instanceof ReturnStatement))
	}

	def boolean canConvertToRichText(InfixExpression node) {
		val parentFieldDecl = node.findParentOfType(FieldDeclaration)
		if (parentFieldDecl != null) {
			val typeDeclr = parentFieldDecl.findParentOfType(TypeDeclaration)

			//Do not convert static final fields
			if (typeDeclr.isInterface || parentFieldDecl.modifiers().isFinal && parentFieldDecl.modifiers().isStatic)
				return false
		}
		val nodes = node.collectCompatibleNodes()
		return !nodes.empty && nodes.forall[canTranslate]
	}

	def <T extends ASTNode> T findParentOfType(ASTNode someNode, Class<T> parentType) {
		if (someNode.parent == null) {
			return null
		} else if (parentType.isInstance(someNode.parent)) {
			return parentType.cast(someNode.parent)
		} else {
			return someNode.parent.findParentOfType(parentType)
		}
	}

	def private Iterable<StringLiteral> collectCompatibleNodes(InfixExpression node) {
		val strings = newArrayList()
		if (node.getOperator() != InfixExpression.Operator.PLUS) {
			return strings
		}
		val left = node.leftOperand
		if (left instanceof StringLiteral) {
			strings.add(left)
		} else if (left instanceof InfixExpression) {
			strings.addAll(collectCompatibleNodes(left))
		}

		val right = node.rightOperand
		if (right instanceof StringLiteral) {
			strings.add(right)
		} else if (right instanceof InfixExpression) {
			strings.addAll(collectCompatibleNodes(right))
		}
		strings.addAll(node.extendedOperands().filter(StringLiteral))
		return strings
	}

	def private boolean canTranslate(StringLiteral literal) {
		val value = literal.escapedValue
		return !(value.contains("�") || value.contains("�") || value.contains("'''"))
	}

	def Type findDeclaredType(SimpleName simpleName) {
		var scope = simpleName.findParentOfType(Block)
		while (scope != null) {
			val type = scope.findDeclaredType(simpleName)
			if (type != null) {
				return type
			}
			scope = scope.findParentOfType(Block)
		}
		return null
	}

	def private Type findDeclaredType(Block scope, SimpleName simpleName) {
		val matchesFound = newArrayList
		scope.accept(
			new ASTVisitor() {
				override boolean visit(VariableDeclarationFragment node) {
					if (node.name.identifier.equals(simpleName.identifier)) {
						val parentNode = node.parent
						switch parentNode {
							VariableDeclarationStatement: {
								matchesFound.add(parentNode.type)
							}
							FieldDeclaration: {
								matchesFound.add(parentNode.type)
							}
							VariableDeclarationExpression: {
								matchesFound.add(parentNode.type)
							}
						}
					}
					return false
				}

				override preVisit2(ASTNode node) {
					matchesFound.empty
				}

				override boolean visit(SingleVariableDeclaration node) {
					if (node.name.equals(simpleName)) {
						matchesFound.add(node.type)
					}
					return false
				}

			})
		return matchesFound.head
	}

	def private Iterable<Assignment> findAssigmentsInBlock(Block scope, (Assignment)=>Boolean constraint) {
		val assigments = newHashSet()
		if (scope != null) {
			scope.accept(
				new ASTVisitor() {
					override visit(Assignment node) {
						if (constraint.apply(node)) {
							assigments.add(node)
						}
						return true
					}
				})
		}
		return assigments
	}

	def Boolean isAssignedInBody(Block scope, VariableDeclarationFragment fieldDeclFragment) {
		return !scope.findAssigmentsInBlock(
			[
				if (leftHandSide instanceof Name) {
					val simpleName = (leftHandSide as Name)
					val binding = simpleName.resolveBinding()
					if (binding instanceof IVariableBinding) {
						return binding.field && fieldDeclFragment.name.identifier.equals(simpleName.toSimpleName)
					}
				}
				return false
			]).empty
	}

	def Boolean isAssignedInBody(Block scope, SimpleName nameToLookFor) {
		return !scope.findAssigmentsInBlock(
			[
				if (leftHandSide  instanceof SimpleName) {
					return nameToLookFor.identifier.equals((leftHandSide as SimpleName).identifier)
				}
				return false
			]).empty
	}

	def dispatch String toSimpleName(SimpleName name) {
		name.identifier
	}

	def dispatch String toSimpleName(QualifiedName name) {
		name.name.identifier
	}

}
