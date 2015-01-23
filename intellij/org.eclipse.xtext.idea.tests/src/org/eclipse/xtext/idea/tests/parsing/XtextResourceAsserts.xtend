/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.idea.tests.parsing

import com.google.inject.Inject
import com.intellij.lang.ASTNode
import org.eclipse.xtext.idea.resource.PsiToEcoreAdapter
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.nodemodel.impl.InvariantChecker
import org.eclipse.xtext.resource.XtextResource
import org.junit.Assert

import static extension org.eclipse.xtext.util.EmfFormatter.*
import com.intellij.psi.PsiErrorElement

class XtextResourceAsserts extends Assert {

	@Inject
	extension NodeModelPrinter nodeModelPrinter

	@Inject
	extension InvariantChecker invariantChecker

	def assertResource(XtextResource expectedResource, XtextResource actualResource) {
		val expectedRootNode = expectedResource.parseResult.rootNode
		val actualRootNode = actualResource.parseResult.rootNode
		assertEquals(expectedRootNode.print, actualRootNode.print)

		val expectedRootASTElement = expectedResource.parseResult.rootASTElement
		val actualRootASTElement = actualResource.parseResult.rootASTElement
		assertEquals(expectedRootASTElement.objToStr, actualRootASTElement.objToStr)

		actualRootNode.checkInvariant
		val psiToEcoreAdapter = PsiToEcoreAdapter.get(actualResource)
		for (child : psiToEcoreAdapter.xtextFile.node.getChildren(null)) {
			assertASTNode(child, actualRootNode, psiToEcoreAdapter)
		}
	}
	
	protected def void assertASTNode(ASTNode astNode, ICompositeNode rootNode, PsiToEcoreAdapter psiToEcoreAdapter) {
		if (!(astNode instanceof PsiErrorElement)) { 
			val node = psiToEcoreAdapter.nodesMapping.get(astNode)
			assertNotNull('''The node is null the ast node: �astNode�''', node)
			assertTrue('''The node �node� is not a part of the tree for the ast node: �astNode�''', node.belongsTo(rootNode))

			val reverseNodesMapping = psiToEcoreAdapter.reverseNodesMapping.get(node)
			assertNotNull(reverseNodesMapping)
			assertTrue(reverseNodesMapping.contains(astNode))
		}
		for (child : astNode.getChildren(null)) {
			assertASTNode(child, rootNode, psiToEcoreAdapter)
		}
	} 

	protected def belongsTo(INode node, ICompositeNode rootNode) {
		rootNode.asTreeIterable.exists[it == node]
	}

}