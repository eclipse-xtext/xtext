/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.validation;

import static com.google.common.collect.Lists.*;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XBasicForLoopExpression;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XDoWhileExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.controlflow.IEarlyExitComputer;
import org.eclipse.xtext.xbase.interpreter.ConstantExpressionsInterpreter;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class EarlyExitValidator extends AbstractDeclarativeValidator {

	private final Map<EReference,EarlyExitKind> disallowedEarylExitReferences = ImmutableMap.of(
		XbasePackage.Literals.XTRY_CATCH_FINALLY_EXPRESSION__FINALLY_EXPRESSION, EarlyExitKind.BOTH
	);
	
	/**
	 * @return map of references which 
	 */
	protected Map<EReference,EarlyExitKind> getDisallowedEarlyExitReferences() {
		return disallowedEarylExitReferences;
	}
	
	protected enum EarlyExitKind {
		RETURN, THROW, BOTH
	}
	
	@Inject
	private IEarlyExitComputer earlyExitComputer;
	
	@Inject
	private ConstantExpressionsInterpreter constantExpressionInterpreter;
	
	@Check
	public void checkInvalidReturnExpression(XExpression expression) {
		final EReference contFeature = (EReference) expression.eContainingFeature();
		final Map<EReference, EarlyExitKind> map = getDisallowedEarlyExitReferences();
		if (map.containsKey(contFeature)) {
			EarlyExitKind exitKind = map.get(contFeature);
			List<XExpression> returns = newArrayList();
			collectExits(expression, returns);
			for (XExpression expr : returns) {
				if (expr instanceof XReturnExpression && (exitKind == EarlyExitKind.RETURN || exitKind == EarlyExitKind.BOTH)) {
					error("A return expression is not allowed in this context.", expr, null, IssueCodes.INVALID_EARLY_EXIT);
				}
				if (expr instanceof XThrowExpression && (exitKind == EarlyExitKind.THROW || exitKind == EarlyExitKind.BOTH)) {
					error("A throw expression is not allowed in this context.", expr, null, IssueCodes.INVALID_EARLY_EXIT);
				}
			}
		}
	}

	protected void collectExits(EObject expr, List<XExpression> found) {
		if (expr instanceof XReturnExpression) {
			found.add((XExpression) expr);
		} else if (expr instanceof XThrowExpression) {
			found.add((XExpression) expr);
		} else if (expr instanceof XClosure) {
			return;
		}
		for (EObject child : expr.eContents()) {
			collectExits(child, found);
		}
	}
	
	@Check
	public void checkDeadCode(XBlockExpression block) {
		List<XExpression> expressions = block.getExpressions();
		for(int i = 0; i < expressions.size() - 1; i++) {
			XExpression expression = expressions.get(i);
			if (earlyExitComputer.isEarlyExit(expression)) {
				if (!(expression instanceof XAbstractFeatureCall)) {
					// XAbstractFeatureCall does already a decent job for its argument lists
					// no additional error necessary
					error("Unreachable expression.", expressions.get(i + 1), null, IssueCodes.UNREACHABLE_CODE);
				}
				return;
			}
		}
	}
	
	@Check
	public void checkDeadCode(XWhileExpression loop) {
		if (!earlyExitComputer.isEarlyExit(loop.getPredicate())) {
			if (isConstant(loop.getPredicate(), Boolean.FALSE)) {
				markAsDeadCode(loop.getBody());
			}
		} else {
			markAsDeadCode(loop.getBody());
		}
	}

	protected boolean markAsDeadCode(List<XExpression> expressions) {
		if (!expressions.isEmpty()) {
			markAsDeadCode(expressions.get(0));
			return true;
		}
		return false;
	}
	
	protected boolean markAsDeadCode(XExpression expression) {
		if (expression instanceof XBlockExpression) {
			List<XExpression> expressions = ((XBlockExpression) expression).getExpressions();
			if (markAsDeadCode(expressions)) {
				return true;
			}
		} 
		if (expression != null) {
			error("Unreachable expression.", expression, null, IssueCodes.UNREACHABLE_CODE);
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean markAsDeadCode(JvmTypeReference typeGuard) {
		if (typeGuard != null) {
			error("Unreachable expression.", typeGuard, null, IssueCodes.UNREACHABLE_CODE);
			return true;
		}
		return false;
	}
	
	@Check
	public void checkDeadCode(XDoWhileExpression loop) {
		if (earlyExitComputer.isEarlyExit(loop.getBody())) {
			markAsDeadCode(loop.getPredicate());
		}
	}
	
	@Check
	public void checkDeadCode(XIfExpression condition) {
		if (!earlyExitComputer.isEarlyExit(condition.getIf())) {
			if (isConstant(condition.getIf(), Boolean.TRUE)) {
				if (condition.getElse() != null) { 
					markAsDeadCode(condition.getElse());
				} else {
					error("Unnecessary condition. Expression evaluates always to true.", condition.getIf(), null, IssueCodes.UNREACHABLE_CODE);
				}
			} else if (isConstant(condition.getIf(), Boolean.FALSE)) {
				markAsDeadCode(condition.getThen());
			}
		} else {
			if (!markAsDeadCode(condition.getThen())) {
				markAsDeadCode(condition.getElse());
			}
		}
	}
	
	@Check
	public void checkDeadCode(XBasicForLoopExpression loop) {
		if (!earlyExitComputer.isEarlyExit(loop.getExpression())) {
			if (isConstant(loop.getExpression(), Boolean.FALSE)) {
				if (!markAsDeadCode(loop.getUpdateExpressions())) {
					markAsDeadCode(loop.getEachExpression());
				}
			}
		} else {
			if (!markAsDeadCode(loop.getUpdateExpressions())) {
				markAsDeadCode(loop.getEachExpression());
			}
		}
	}

	protected boolean isConstant(XExpression expression, Boolean constant) {
		return expression != null && constantExpressionInterpreter.isConstant(expression, constant);
	}
	
	@Check
	public void checkDeadCode(XSwitchExpression switchExpression) {
		List<XCasePart> cases = switchExpression.getCases();
		for(int i = 0, size = cases.size(); i < size; i++) {
			XCasePart casePart = cases.get(i);
			XExpression caseExpression = casePart.getCase();
			if (!earlyExitComputer.isEarlyExit(caseExpression)) {
				// TODO should we do cool stuff here?
//				if (constantExpressionInterpreter.isConstant(caseExpression, Boolean.FALSE)) {
//					// then part cannot be reached
//					markAsDeadCode(casePart.getThen());
//				} else if (casePart.getTypeGuard() == null && constantExpressionInterpreter.isConstant(caseExpression, Boolean.TRUE)) {
//					// then part will definitely be reached
//					// all subsequent cases are dead code, the first one shall be reported
//					if (casePart.isFallThrough()) {
//						int j = i + 1;
//						for(;j < size && cases.get(j).isFallThrough(); j++) {
//							XCasePart next = cases.get(j);
//							if (markAsDeadCode(next.getTypeGuard()) || markAsDeadCode(next.getCase())) {
//								i = j;
//								j = size;
//							}
//						}
//						if (j < size) {
//							XCasePart next = cases.get(j);
//							if (markAsDeadCode(next.getTypeGuard()) || markAsDeadCode(next.getCase())) {
//								i = j;
//								j = size;
//							}
//						}
//					}
//					i = markAsDeadCode(cases, casePart, i, size);
//				}
			} else if (!markAsDeadCode(casePart.getThen())) {
				if (casePart.getTypeGuard() == null) { 
					i = markAsDeadCode(cases, casePart, i, size);
				}
			}
		}
	}

	protected int markAsDeadCode(List<XCasePart> cases, XCasePart from, int idx, int size) {
		if (!markAsDeadCode(from.getThen())) {
			for(int j = idx + 1; j < size; j++) {
				XCasePart next = cases.get(j);
				if (markAsDeadCode(next.getTypeGuard()) || markAsDeadCode(next.getCase()) || markAsDeadCode(next.getThen())) {
					idx = j;
					j = size;
				}
			}
		}
		return idx;
	}
	
	@Override
	public void register(EValidatorRegistrar registrar) {
		// do nothing
	}
}
