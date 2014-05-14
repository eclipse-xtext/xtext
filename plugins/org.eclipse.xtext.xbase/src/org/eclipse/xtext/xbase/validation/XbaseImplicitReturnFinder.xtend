/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.validation

import org.eclipse.xtext.xbase.XAbstractFeatureCall
import org.eclipse.xtext.xbase.XBlockExpression
import org.eclipse.xtext.xbase.XBooleanLiteral
import org.eclipse.xtext.xbase.XCastedExpression
import org.eclipse.xtext.xbase.XClosure
import org.eclipse.xtext.xbase.XCollectionLiteral
import org.eclipse.xtext.xbase.XConstructorCall
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XIfExpression
import org.eclipse.xtext.xbase.XInstanceOfExpression
import org.eclipse.xtext.xbase.XNullLiteral
import org.eclipse.xtext.xbase.XNumberLiteral
import org.eclipse.xtext.xbase.XStringLiteral
import org.eclipse.xtext.xbase.XSwitchExpression
import org.eclipse.xtext.xbase.XSynchronizedExpression
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression
import org.eclipse.xtext.xbase.XTypeLiteral
import org.eclipse.xtext.xbase.XAssignment

/**
 * @author Stefan Oehme - Initial contribution and API
 */
class XbaseImplicitReturnFinder implements ImplicitReturnFinder {

	def dispatch findImplicitReturns(Void expression, ImplicitReturnFinder.Acceptor acceptor) {
	}

	def dispatch findImplicitReturns(XExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
	}
	
	def dispatch findImplicitReturns(XAssignment expression, ImplicitReturnFinder.Acceptor acceptor) {
	}

	def dispatch findImplicitReturns(XNumberLiteral expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XBooleanLiteral expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XCollectionLiteral expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XStringLiteral expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XNullLiteral expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XTypeLiteral expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XClosure expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XConstructorCall expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XAbstractFeatureCall expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XInstanceOfExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XCastedExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		acceptor.accept(expression)
	}

	def dispatch findImplicitReturns(XBlockExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		findImplicitReturns(expression.expressions.last, acceptor)
	}

	def dispatch findImplicitReturns(XSynchronizedExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		findImplicitReturns(expression.expression, acceptor)
	}

	def dispatch findImplicitReturns(XIfExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		findImplicitReturns(expression.then, acceptor)

		//TODO this will not find the implicit "return null" when there is no "else"
		findImplicitReturns(expression.^else, acceptor)
	}

	def dispatch findImplicitReturns(XTryCatchFinallyExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		findImplicitReturns(expression.expression, acceptor)
		expression.catchClauses.forEach[findImplicitReturns(it.expression, acceptor)]
	}

	def dispatch findImplicitReturns(XSwitchExpression expression, ImplicitReturnFinder.Acceptor acceptor) {
		expression.cases.forEach[findImplicitReturns(it.then, acceptor)]
		findImplicitReturns(expression.^default, acceptor)
	}
}
