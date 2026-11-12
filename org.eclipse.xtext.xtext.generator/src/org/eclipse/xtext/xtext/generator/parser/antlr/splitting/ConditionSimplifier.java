/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.parser.antlr.splitting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.AndExpression;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.BooleanLiteral;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.Comparison;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.Expression;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.IfCondition;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.MethodCall;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.NotExpression;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.NumberLiteral;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.OrExpression;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.SimpleExpressionsFactory;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.simpleExpressions.util.SimpleExpressionsSwitch;

public class ConditionSimplifier extends SimpleExpressionsSwitch<Expression>{

	public void simplify(IfCondition condition) {
		Expression expression = simplifyRecursive(condition.getCondition());
		condition.setCondition(sortOrOperands(expression));
	}

	// ANTLR orders disjunctions of predicates by hash code, which is the identity hash for && and ! terms.
	private Expression sortOrOperands(Expression expression) {
		if (expression instanceof OrExpression) {
			List<OrExpression> ors = new ArrayList<>();
			List<Expression> operands = new ArrayList<>();
			collectOrOperands(expression, ors, operands);
			if (operands.stream().anyMatch(o -> o instanceof AndExpression || o instanceof NotExpression)
					&& operands.stream().noneMatch(this::containsComparison)) {
				operands.sort(Comparator.comparing(this::toSortKey));
			}
			// Reuse the parsed OrExpressions so that the serializer keeps their whitespace.
			Expression result = operands.get(0);
			for (int i = 0; i < ors.size(); i++) {
				OrExpression or = ors.get(i);
				or.setLeft(result);
				or.setRight(operands.get(i + 1));
				result = or;
			}
			return result;
		}
		if (expression instanceof AndExpression and) {
			and.setLeft(sortOrOperands(and.getLeft()));
			and.setRight(sortOrOperands(and.getRight()));
		} else if (expression instanceof NotExpression not) {
			not.setExpression(sortOrOperands(not.getExpression()));
		}
		return expression;
	}

	private void collectOrOperands(Expression expression, List<OrExpression> ors, List<Expression> operands) {
		if (expression instanceof OrExpression or) {
			collectOrOperands(or.getLeft(), ors, operands);
			collectOrOperands(or.getRight(), ors, operands);
			ors.add(or);
		} else {
			operands.add(sortOrOperands(expression));
		}
	}

	private boolean containsComparison(Expression expression) {
		if (expression instanceof Comparison) {
			return true;
		}
		if (expression instanceof OrExpression or) {
			return containsComparison(or.getLeft()) || containsComparison(or.getRight());
		}
		if (expression instanceof AndExpression and) {
			return containsComparison(and.getLeft()) || containsComparison(and.getRight());
		}
		if (expression instanceof NotExpression not) {
			return containsComparison(not.getExpression());
		}
		return false;
	}

	private String toSortKey(Expression expression) {
		if (expression instanceof OrExpression or) {
			return "(" + toSortKey(or.getLeft()) + "||" + toSortKey(or.getRight()) + ")";
		}
		if (expression instanceof AndExpression and) {
			return "(" + toSortKey(and.getLeft()) + "&&" + toSortKey(and.getRight()) + ")";
		}
		if (expression instanceof NotExpression not) {
			return "!" + toSortKey(not.getExpression());
		}
		if (expression instanceof MethodCall methodCall) {
			return methodCall.getValue();
		}
		if (expression instanceof NumberLiteral number) {
			return Integer.toString(number.getValue());
		}
		if (expression instanceof BooleanLiteral bool) {
			return Boolean.toString(bool.isValue());
		}
		throw new IllegalStateException(String.valueOf(expression));
	}

	public Expression simplifyRecursive(Expression expression) {
		return doSwitch(expression);
	}

	@Override
	public Expression caseAndExpression(AndExpression object) {
		if (object.getRight() == null)
			return doSwitch(object.getLeft());
		if (object.getLeft() == null)
			return doSwitch(object.getRight());
		Expression left = doSwitch(object.getLeft());
		Expression right = doSwitch(object.getRight());
		if (areSemanticallyEqual(left, right))
			return left;
		if (left instanceof BooleanLiteral leftLiteral) {
			if (leftLiteral.isValue()) {
				return right;
			}
			return left;
		}
		if (right instanceof BooleanLiteral rightLiteral) {
			if (rightLiteral.isValue()) {
				return left;
			}
			return right;
		}
		if (left instanceof NotExpression leftNot) {
			if (areSemanticallyEqual(leftNot.getExpression(), right)) {
				BooleanLiteral result = SimpleExpressionsFactory.eINSTANCE.createBooleanLiteral();
				result.setValue(false);
				return result;
			}
		} else if (right instanceof NotExpression rightNot) {
			if (areSemanticallyEqual(left, rightNot.getExpression())) {
				BooleanLiteral result = SimpleExpressionsFactory.eINSTANCE.createBooleanLiteral();
				result.setValue(false);
				return result;
			}
		}
		object.setLeft(left);
		object.setRight(right);
		return object;
	}

	@Override
	public Expression caseOrExpression(OrExpression object) {
		if (object.getRight() == null)
			return doSwitch(object.getLeft());
		if (object.getLeft() == null)
			return doSwitch(object.getRight());
		Expression left = doSwitch(object.getLeft());
		Expression right = doSwitch(object.getRight());
		if (areSemanticallyEqual(left, right))
			return left;
		if (left instanceof BooleanLiteral leftLiteral) {
			if (leftLiteral.isValue()) {
				return left;
			}
			return right;
		}
		if (right instanceof BooleanLiteral rightLiteral) {
			if (rightLiteral.isValue()) {
				return right;
			}
			return left;
		}
		if (left instanceof AndExpression leftAsAnd) {
			if (areSemanticallyEqual(leftAsAnd.getLeft(), right)
				|| areSemanticallyEqual(leftAsAnd.getRight(), right))
				return right;
		} else if (right instanceof AndExpression rightAsAnd) {
			if (areSemanticallyEqual(rightAsAnd.getLeft(), left)
				|| areSemanticallyEqual(rightAsAnd.getRight(), left))
				return left;
		}
		if (left instanceof OrExpression leftAsOr) {
			if (areSemanticallyEqual(leftAsOr.getLeft(), right)
				|| areSemanticallyEqual(leftAsOr.getRight(), right))
				return left;
		} else if (right instanceof OrExpression rightAsOr) {
			if (areSemanticallyEqual(rightAsOr.getLeft(), left)
				|| areSemanticallyEqual(rightAsOr.getRight(), left))
				return right;
		}
		if (left instanceof NotExpression leftNot) {
			if (areSemanticallyEqual(leftNot.getExpression(), right)) {
				BooleanLiteral result = SimpleExpressionsFactory.eINSTANCE.createBooleanLiteral();
				result.setValue(true);
				return result;
			}
		} else if (right instanceof NotExpression rightNot) {
			if (areSemanticallyEqual(left, rightNot.getExpression())) {
				BooleanLiteral result = SimpleExpressionsFactory.eINSTANCE.createBooleanLiteral();
				result.setValue(true);
				return result;
			}
		}
		object.setLeft(left);
		object.setRight(right);
		return object;
	}

	@Override
	public Expression caseNotExpression(NotExpression object) {
		if (object.getExpression() instanceof NotExpression inner)
			return doSwitch(inner.getExpression());
		Expression result = doSwitch(object.getExpression());
		if (result instanceof BooleanLiteral literal) {
			literal.setValue(!literal.isValue());
			return literal;
		} else {
			object.setExpression(result);
			return object;
		}
	}

	@Override
	public Expression caseExpression(Expression object) {
		return object;
	}

	public boolean areSemanticallyEqual(Expression left, Expression right) {
		if (left.eClass() != right.eClass())
			return false;
		if (left instanceof OrExpression leftOrExpression && right instanceof OrExpression rightOrExpression) {
			return (areSemanticallyEqual(leftOrExpression.getLeft(), rightOrExpression.getLeft())
				&& areSemanticallyEqual(leftOrExpression.getRight(), rightOrExpression.getRight()))
				|| (areSemanticallyEqual(leftOrExpression.getRight(), rightOrExpression.getLeft())
				&& areSemanticallyEqual(leftOrExpression.getLeft(), rightOrExpression.getRight()));
		}
		if (left instanceof AndExpression leftAndExpression && right instanceof AndExpression rightAndExpression) {
			return (areSemanticallyEqual(leftAndExpression.getLeft(), rightAndExpression.getLeft())
				&& areSemanticallyEqual(leftAndExpression.getRight(), rightAndExpression.getRight()))
				|| (areSemanticallyEqual(leftAndExpression.getRight(), rightAndExpression.getLeft())
				&& areSemanticallyEqual(leftAndExpression.getLeft(), rightAndExpression.getRight()));
		}
		if (left instanceof NotExpression leftNot && right instanceof NotExpression rightNot) {
			return areSemanticallyEqual(leftNot.getExpression(), rightNot.getExpression());
		}
		if (left instanceof Comparison leftComparison && right instanceof Comparison rightComparison) {
			return leftComparison.getOperator().equals(rightComparison.getOperator())
				&& areSemanticallyEqual(leftComparison.getLeft(), rightComparison.getLeft())
				&& areSemanticallyEqual(leftComparison.getRight(), rightComparison.getRight());
		}
		if (left instanceof NumberLiteral leftNumber && right instanceof NumberLiteral rightNumber) {
			return leftNumber.getValue() == rightNumber.getValue();
		}
		if (left instanceof MethodCall leftMethod && right instanceof MethodCall rightMethod) {
			return leftMethod.getValue().equals(rightMethod.getValue());
		}
		throw new IllegalStateException(left + " - " + right);
	}

}
