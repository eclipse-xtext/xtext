/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist.antlr;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.ide.editor.contentassist.CompletionPrefixProvider;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext.Builder;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.Lexer;
import org.eclipse.xtext.ide.editor.partialEditing.IPartialEditingContentAssistParser;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.antlr.ITokenDefProvider;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.util.TextRegion;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * @since 2.9
 */
public class ContentAssistContextFactory implements Function<ContentAssistContext.Builder, ContentAssistContext> {
	
	private static final Logger log = Logger.getLogger(ContentAssistContextFactory.class);
	
	private ExecutorService pool;
	
	@Inject
	protected IContentAssistParser parser;
	
	@Inject
	@Named(LexerIdeBindings.CONTENT_ASSIST)
	protected Lexer lexer; 
	
	@Inject
	protected Provider<ContentAssistContext.Builder> contentAssistContextProvider;
	
	@Inject
	protected ITokenDefProvider tokenDefProvider;
	
	@Inject
	protected FollowElementComputer followElementComputer;
	
	/**
	 * @since 2.13
	 */
	@Inject
	protected CompletionPrefixProvider completionPrefixProvider;
	
	protected XtextResource resource;

	protected ICompositeNode rootNode;

	protected INode lastCompleteNode;

	protected INode currentNode;

	protected INode lastVisibleNode;

	protected EObject currentModel;

	protected List<ContentAssistContext.Builder> contextBuilders;

	protected IParseResult parseResult;

	protected INode datatypeNode;

	protected int completionOffset;

	protected ITextRegion selection;

	private String document;

	@Override
	public ContentAssistContext apply(Builder from) {
		return from.toContext();
	}
	
	public ContentAssistContext[] create(String document, ITextRegion selection, int offset, XtextResource resource) {
		this.document = document;
		this.selection = selection;
		this.resource = resource;
		//This is called to make sure late initialization is done.
		if (resource instanceof DerivedStateAwareResource) {
			resource.getContents();
		}
		this.parseResult = resource.getParseResult();
		if (parseResult == null)
			throw new NullPointerException("parseResult is null");
		return doCreateContexts(offset);
	}
	
	protected INode getCurrentNode() {
		return currentNode;
	}
	
	public void setPool(ExecutorService pool) {
		this.pool = pool;
	}

	protected ContentAssistContext[] doCreateContexts(int offset) {
		initializeFromViewerAndResource(offset);
		List<Future<?>> futures = Lists.newArrayList();
		if (!datatypeNode.equals(lastCompleteNode)) {
			futures.add(pool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					handleLastCompleteNodeAsPartOfDatatypeNode();
					return null;
				}
			}));
		}

		// 2nd context: we assume, that the current token is incomplete and try to calculate
		// any valid grammar element by removing the current token and using it as prefix
		if (datatypeNode.equals(lastCompleteNode) && completionOffset != lastCompleteNode.getOffset()) {
			futures.add(pool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					handleLastCompleteNodeIsAtEndOfDatatypeNode();
					return null;
				}
			}));
		}

		// 4th context: we assume, that the current position is perfectly ok to insert a new token, if the previous one was valid
		if (!(lastCompleteNode instanceof ILeafNode) || lastCompleteNode.getGrammarElement() != null) {
			futures.add(pool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					handleLastCompleteNodeIsPartOfLookahead();
					return null;
				}
			}));
		}

		// wait for all requests to complete
		for(Future<?> f: futures) {
			try {
				f.get();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return Lists.transform(contextBuilders, this).toArray(new ContentAssistContext[contextBuilders.size()]);
	}

	protected void initializeFromViewerAndResource(int offset) {
		initializeAndAdjustCompletionOffset(offset);
		initializeNodeAndModelData();
		contextBuilders = Collections.synchronizedList(Lists.<ContentAssistContext.Builder>newArrayList());
	}

	protected void initializeNodeAndModelData() {
		rootNode = parseResult.getRootNode();
		if (parser instanceof IPartialEditingContentAssistParser) {
			((IPartialEditingContentAssistParser) parser).initializeFor(resource.getEntryPoint());
		}
		lastCompleteNode = new LeafNodeFinder(completionOffset, true).searchIn(rootNode);
		if (lastCompleteNode == null)
			lastCompleteNode = rootNode;
		currentNode = new LeafNodeFinder(completionOffset, false).searchIn(rootNode);
		if (currentNode == null)
			currentNode = lastCompleteNode;
		lastVisibleNode = getLastCompleteNodeByOffset(rootNode, completionOffset);
		datatypeNode = getContainingDatatypeRuleNode(lastCompleteNode);
		currentModel = NodeModelUtils.findActualSemanticObjectFor(lastVisibleNode);
	}

	protected void initializeAndAdjustCompletionOffset(int offset) {
		completionOffset = offset;
		if (selection.getEndOffset() == offset)
			completionOffset = selection.getOffset();
	}

	protected void handleLastCompleteNodeIsPartOfLookahead(){
		// do not calculate twice for the same input
		if (!(lastCompleteNode instanceof ILeafNode && ((ILeafNode) lastCompleteNode).isHidden())) {
			createContextsForLastCompleteNode(currentModel, true);
		}
	}

	protected void handleLastCompleteNodeIsAtEndOfDatatypeNode() {
		String prefix = getPrefix(lastCompleteNode);
		String completeInput = getInputToParse(lastCompleteNode);
		INode previousNode = getLastCompleteNodeByOffset(rootNode, lastCompleteNode.getOffset());
		EObject previousModel = previousNode.getSemanticElement();
		INode currentDatatypeNode = getContainingDatatypeRuleNode(currentNode);
		Collection<FollowElement> followElements = parser.getFollowElements(completeInput, false);
		int prevSize = contextBuilders.size();
		doCreateContexts(previousNode, currentDatatypeNode, prefix, previousModel, followElements);
		
		if (lastCompleteNode instanceof ILeafNode && lastCompleteNode.getGrammarElement() == null && contextBuilders.size() != prevSize) {
			handleLastCompleteNodeHasNoGrammarElement(contextBuilders.subList(prevSize, contextBuilders.size()), previousModel);
		}
	}

	/**
	 * @since 2.13
	 */
	protected String getInputToParse(INode node) {
		return getInputToParse(document, node.getOffset());
	}
	
	/**
	 * @since 2.13
	 */
	protected String getInputToParse(String completeInput, int offset) {
		return completionPrefixProvider.getInputToParse(completeInput, offset, completionOffset);
	}

	protected void handleLastCompleteNodeHasNoGrammarElement(List<Builder> contextBuilderToCheck, EObject previousModel) {
		List<ContentAssistContext> newContexts = Lists.transform(contextBuilderToCheck, this);
		boolean wasValid = isLikelyToBeValidProposal(lastCompleteNode, newContexts);
		if (wasValid && !(lastCompleteNode instanceof ILeafNode && ((ILeafNode) lastCompleteNode).isHidden())) {
			createContextsForLastCompleteNode(previousModel, false);
		}
	}

	protected void handleLastCompleteNodeAsPartOfDatatypeNode() {
		String prefix = getPrefix(datatypeNode);
		String completeInput = getInputToParse(datatypeNode);
		Collection<FollowElement> followElements = parser.getFollowElements(completeInput, false);
		INode lastCompleteNodeBeforeDatatype = getLastCompleteNodeByOffset(rootNode, datatypeNode.getTotalOffset());
		doCreateContexts(lastCompleteNodeBeforeDatatype, datatypeNode, prefix, currentModel, followElements);
	}
	
	protected boolean isLikelyToBeValidProposal(INode lastCompleteNode, Iterable<ContentAssistContext> contexts) {
		for(ContentAssistContext context: contexts) {
			for (AbstractElement element: context.getFirstSetGrammarElements()) {
				if (element instanceof Keyword) {
					String keywordValue = ((Keyword) element).getValue();
					String lastText = lastCompleteNode.getText();
					if (keywordValue.equals(lastText)) 
						return true;
				}
			}
		}
		return false;
	}

	protected void createContextsForLastCompleteNode(EObject previousModel, boolean strict) {
		String currentNodePrefix = getPrefix(currentNode);
		if (!Strings.isEmpty(currentNodePrefix) && !currentNode.getText().equals(currentNodePrefix)) {
			lexer.setCharStream(new ANTLRStringStream(currentNodePrefix));
			Token token = lexer.nextToken();
			if (token.getType() == Token.EOF) { // error case - nothing could be parsed
				return;
			}
			while(token.getType() != Token.EOF) {
				if (isErrorToken(token))
					return;
				token = lexer.nextToken();
			}
		}
		String prefix = "";
		String completeInput = getInputToParse(document, completionOffset);
		Collection<FollowElement> followElements = parser.getFollowElements(completeInput, strict);
		doCreateContexts(lastCompleteNode, currentNode, prefix, previousModel, followElements);
	}
	
	/**
	 * Return <code>true</code> if the token should be considered to be an error token.
	 * If the token that is created from the prefix before the cursor position is an error
	 * token, no proposals shall be computed that don't use a prefix. 
	 * @return <code>true</true> if the token should be considered to be an error token.
	 */
	protected boolean isErrorToken(Token token) {
		String tokenTypeName = tokenDefProvider.getTokenDefMap().get(token.getType());
		return "RULE_ANY_OTHER".equals(tokenTypeName);			
	}

	protected void doCreateContexts(
			INode lastCompleteNode, INode currentNode,
			String prefix, EObject previousModel, 
			Collection<FollowElement> followElements) {
		Set<AbstractElement> followElementAsAbstractElements = Sets.newLinkedHashSet();
		followElementComputer.computeFollowElements(followElements, followElementAsAbstractElements);
		Multimap<EObject, AbstractElement> contextMap = computeCurrentModel(previousModel, lastCompleteNode, followElementAsAbstractElements);
		currentNode = getContainingDatatypeRuleNode(currentNode);
		for (Entry<EObject, Collection<AbstractElement>> entry : contextMap.asMap().entrySet()) {
			ContentAssistContext.Builder contextBuilder = doCreateContext(lastCompleteNode, entry.getKey(), previousModel, currentNode, prefix);
			for(AbstractElement element: entry.getValue()) {
				contextBuilder.accept(element);
			}
			contextBuilders.add(contextBuilder);
		}
	}

	protected Multimap<EObject, AbstractElement> computeCurrentModel(EObject currentModel, INode lastCompleteNode,
			Collection<AbstractElement> followElements) {
		Multimap<EObject, AbstractElement> result = LinkedHashMultimap.create();
		ICompositeNode currentParserNode = NodeModelUtils.getNode(currentModel);
		if (currentParserNode == null) {
			result.putAll(currentModel, followElements);
			return result;
		}
		EObject currentGrammarElement = currentParserNode.getGrammarElement();
		AbstractRule currentRule = getRule(currentGrammarElement);
		for (AbstractElement grammarElement : followElements) {
			AbstractRule rule = currentRule;
			ICompositeNode loopParserNode = currentParserNode;
			EObject loopLastGrammarElement = lastCompleteNode.getGrammarElement();
			while (!canBeCalledAfter(rule, loopLastGrammarElement, lastCompleteNode.getText(), grammarElement) && loopParserNode.getParent() != null) {
				loopLastGrammarElement = loopParserNode.getGrammarElement();
				loopParserNode = loopParserNode.getParent();
				while (loopParserNode.getGrammarElement() == null && loopParserNode.getParent() != null)
					loopParserNode = loopParserNode.getParent();
				EObject loopGrammarElement = loopParserNode.getGrammarElement();
				rule = getRule(loopGrammarElement);
			}
			EObject context = loopParserNode.getSemanticElement();
			result.put(context, grammarElement);
		}
		return result;
	}
	
	

	public INode getContainingDatatypeRuleNode(INode node) {
		INode result = node;
		EObject grammarElement = result.getGrammarElement();
		if (grammarElement != null) {
			ParserRule parserRule = GrammarUtil.containingParserRule(grammarElement);
			while (parserRule != null && GrammarUtil.isDatatypeRule(parserRule)) {
				result = result.getParent();
				grammarElement = result.getGrammarElement();
				parserRule = GrammarUtil.containingParserRule(grammarElement);
			}
		}
		return result;
	}

	protected int getCompletionOffset(){
		return completionOffset;
	}

	public ContentAssistContext.Builder doCreateContext(
			INode lastCompleteNode, 
			EObject currentModel, EObject previousModel,
			INode currentNode, String prefix) {
		ContentAssistContext.Builder context = contentAssistContextProvider.get();

		context.setRootNode(rootNode);
		context.setLastCompleteNode(lastCompleteNode);
		context.setCurrentNode(currentNode);

		context.setRootModel(parseResult.getRootASTElement());
		context.setCurrentModel(currentModel);
		context.setPreviousModel(previousModel);
		context.setOffset(completionOffset);
		context.setPrefix(prefix);
		int regionLength = prefix.length();
		if (selection.getLength() > 0) {
			regionLength = regionLength + selection.getLength();
		}
		ITextRegion region = new TextRegion(completionOffset - prefix.length(), regionLength);
		if (selection.getOffset() >= 0 && selection.getLength() >= 0) {
			context.setSelectedText(document.substring(selection.getOffset(), selection.getOffset() + selection.getLength()));
		}
		context.setReplaceRegion(region);
		context.setResource(resource);
		return context;
	}

	// TODO: match the node string with the widget string to compute the correct
	// replace region length
	public String getPrefix(INode prefixNode) {
		if (prefixNode instanceof ILeafNode) {
			if (((ILeafNode) prefixNode).isHidden() && prefixNode.getGrammarElement() != null)
				return "";
			return getNodeTextUpToCompletionOffset(prefixNode);
		}
		StringBuilder result = new StringBuilder(prefixNode.getTotalLength());
		doComputePrefix((ICompositeNode) prefixNode, result);
		return result.toString();
	}

	public String getNodeTextUpToCompletionOffset(INode currentNode) {
		int startOffset = currentNode.getOffset();
		int length = completionOffset - startOffset;
		String nodeText = currentNode.getText();
		String trimmedNodeText = length > nodeText.length() ? nodeText : nodeText.substring(0, length);
		try {
			String text = document.substring(startOffset, startOffset + trimmedNodeText.length());
			if (trimmedNodeText.equals(text))
				return text;
			return document.substring(startOffset, startOffset + length);
		} catch (IndexOutOfBoundsException e) {
			log.error(e.getMessage(), e);
		}
		return trimmedNodeText;
	}

	public boolean doComputePrefix(ICompositeNode node, StringBuilder result) {
		List<ILeafNode> hiddens = Lists.newArrayListWithCapacity(2);
		for (INode child : node.getChildren()) {
			if (child instanceof ICompositeNode) {
				if (!doComputePrefix((ICompositeNode) child, result))
					return false;
			}
			else {
				ILeafNode leaf = (ILeafNode) child;
				ITextRegion leafRegion = leaf.getTextRegion();
				if (leafRegion.getOffset() > completionOffset)
					return false;
				if (leaf.isHidden()) {
					if (result.length() != 0)
						hiddens.add((ILeafNode) child);
				}
				else {
					Iterator<ILeafNode> iter = hiddens.iterator();
					while (iter.hasNext()) {
						result.append(iter.next().getText());
					}
					hiddens.clear();
					result.append(getNodeTextUpToCompletionOffset(leaf));
					if (leafRegion.getEndOffset() > completionOffset)
						return false;
				}
			}
		}
		return true;
	}
	
	public void setParser(IContentAssistParser parser) {
		this.parser = parser;
	}

	public IContentAssistParser getParser() {
		return parser;
	}

	protected boolean canBeCalledAfter(AbstractRule rule, final EObject previousGrammarElement, String previousText, final EObject nextGrammarElement) {
		Boolean result = createCallHierachyHelper(previousGrammarElement, previousText, nextGrammarElement).doSwitch(rule);
		return result;
	}

	protected CallHierarchyHelper createCallHierachyHelper(
			final EObject previousGrammarElement,
			final String previousText,
			final EObject nextGrammarElement) {
		return new CallHierarchyHelper(nextGrammarElement, previousText, previousGrammarElement);
	}

	protected AbstractRule getRule(EObject currentGrammarElement) {
		AbstractRule rule = null;
		if (currentGrammarElement instanceof RuleCall)
			rule = ((RuleCall) currentGrammarElement).getRule();
		if (currentGrammarElement instanceof AbstractRule)
			rule = (AbstractRule) currentGrammarElement;
		if (currentGrammarElement instanceof Action)
			rule = EcoreUtil2.getContainerOfType(currentGrammarElement, AbstractRule.class);
		if (rule == null)
			throw new IllegalStateException();
		return rule;
	}
	
	protected INode getLastCompleteNodeByOffset(INode node, int offsetPosition) {
		return completionPrefixProvider.getLastCompleteNodeByOffset(node, offsetPosition, completionOffset);
	}
}
