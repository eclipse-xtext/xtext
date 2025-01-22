/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.parsetree.reconstr.impl;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EnumRule;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parsetree.reconstr.IHiddenTokenHelper;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class TokenUtil {
	@Inject
	protected IHiddenTokenHelper hiddenTokenHelper;

	public Pair<List<ILeafNode>, List<ILeafNode>> getLeadingAndTrailingHiddenTokens(INode node) {
		Pair<List<ILeafNode>, List<ILeafNode>> result = Tuples.<List<ILeafNode>, List<ILeafNode>> create(Lists
				.<ILeafNode> newArrayList(), Lists.<ILeafNode> newArrayList());
		List<ILeafNode> leafNodes = Lists.newArrayList(node.getLeafNodes());
		int begin = 0, end = leafNodes.size() - 1;
		while (begin <= end && leafNodes.get(begin).isHidden())
			result.getFirst().add(leafNodes.get(begin++));
		while (begin <= end && leafNodes.get(end).isHidden())
			result.getSecond().add(0, leafNodes.get(end--));
		return result;
	}

	public EObject getTokenOwner(INode node) {
		if (node.hasDirectSemanticElement())
			return node.getSemanticElement();
		if (node.getParent() != null) {
			if (node.getParent().hasDirectSemanticElement())
				return node.getParent().getSemanticElement();
			EObject parentGrammarElement = node.getParent().getGrammarElement();
			boolean isParser = GrammarUtil.isEObjectRule(parentGrammarElement) || GrammarUtil.isEObjectRuleCall(parentGrammarElement);
			for (INode sibling : node.getParent().getChildren())
				if (sibling.hasDirectSemanticElement() && (isParser || sibling.getGrammarElement() instanceof Action))
					return sibling.getSemanticElement();
		}
		return node.getSemanticElement();
	}

	public AbstractRule getTokenRule(INode node) {
		if (node == null)
			return null;
		EObject element = node.getGrammarElement();
		if (element instanceof AbstractRule)
			return (AbstractRule) element;
		if (element instanceof CrossReference)
			element = ((CrossReference) element).getTerminal();
		if (element instanceof RuleCall)
			return ((RuleCall) element).getRule();
		return null;
	}

	public boolean isCommentNode(INode node) {
		if (node instanceof ILeafNode && ((ILeafNode) node).isHidden()
				&& node.getGrammarElement() instanceof AbstractRule rule)
			return hiddenTokenHelper.isComment(rule);
		return false;
	}

	public boolean isToken(INode node) {
		if (node instanceof ILeafNode)
			return !((ILeafNode) node).isHidden();
		AbstractRule rule;
		EObject grammarElement = node.getGrammarElement();
		if (grammarElement instanceof AbstractRule)
			rule = (AbstractRule) grammarElement;
		else if (grammarElement instanceof RuleCall)
			rule = ((RuleCall) grammarElement).getRule();
		else if (grammarElement instanceof CrossReference
				&& ((CrossReference) grammarElement).getTerminal() instanceof RuleCall)
			rule = ((RuleCall) ((CrossReference) grammarElement).getTerminal()).getRule();
		else
			rule = null;
		if (rule instanceof ParserRule)
			return GrammarUtil.isDatatypeRule((ParserRule) rule);
		if (rule instanceof EnumRule)
			return true;
		return false;
	}

	public boolean isWhitespaceNode(INode node) {
		if (node instanceof ILeafNode && ((ILeafNode) node).isHidden()
				&& node.getGrammarElement() instanceof AbstractRule rule)
			return hiddenTokenHelper.isWhitespace(rule);
		return false;
	}
	
	public boolean isWhitespaceOrCommentNode(INode node) {
		return isWhitespaceNode(node) || isCommentNode(node);
	}

	public String serializeNode(INode node) {
		if (node instanceof ILeafNode)
			return ((ILeafNode) node).getText();
		List<ILeafNode> leafNodes = Lists.newArrayList(node.getLeafNodes());
		int begin = 0, end = leafNodes.size() - 1;
		while (begin <= end && isWhitespaceOrCommentNode(leafNodes.get(begin)))
			begin++;
		while (begin <= end && isWhitespaceOrCommentNode(leafNodes.get(end)))
			end--;
		if (begin == end)
			return isWhitespaceOrCommentNode(leafNodes.get(begin)) ? "" : leafNodes.get(begin).getText();
		StringBuilder b = new StringBuilder();
		for (int i = begin; i <= end; i++)
			b.append(leafNodes.get(i).getText());
		return b.toString();
	}
}
