/*******************************************************************************
 * Copyright (c) 2009, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.contentassist.antlr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.EnumLiteralDeclaration;
import org.eclipse.xtext.EnumRule;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.Parameter;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.UnorderedGroup;
import org.eclipse.xtext.XtextFactory;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.ide.editor.contentassist.CompletionPrefixProvider;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.antlr.ITokenDefProvider;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.contentassist.AbstractContentAssistContextFactory;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext.Builder;
import org.eclipse.xtext.ui.editor.contentassist.IFollowElementAcceptor;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.util.XtextSwitch;
import org.eclipse.xtext.xtext.ConditionEvaluator;
import org.eclipse.xtext.xtext.ParameterConfigHelper;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * This class is effetively deprecated. It is strongly recommend to use the {@link ContentAssistContextFactory} instead.
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@Singleton
public class ParserBasedContentAssistContextFactory extends AbstractContentAssistContextFactory {
	
	private final static Logger log = Logger.getLogger(ParserBasedContentAssistContextFactory.class);

	@Inject
	private Provider<StatefulFactory> statefulFactoryProvider;

	private ExecutorService pool;
	
	public ParserBasedContentAssistContextFactory() {
		pool = Executors.newFixedThreadPool(3);
	}
	
	public Provider<StatefulFactory> getStatefulFactoryProvider() {
		return statefulFactoryProvider;
	}
	
	public void setStatefulFactoryProvider(Provider<StatefulFactory> statefulFactoryProvider) {
		this.statefulFactoryProvider = statefulFactoryProvider;
	}
	
	/**
	 * @since 2.7
	 */
	protected ExecutorService getPool() {
		return pool;
	}
	
	public static class StatefulFactory implements Function<ContentAssistContext.Builder, ContentAssistContext> {
		
		private ExecutorService pool;
		
		/**
		 * @since 2.5
		 */
		@Inject
		protected IContentAssistParser parser;
		
		/**
		 * @since 2.5
		 */
		@Inject
		@Named(LexerIdeBindings.CONTENT_ASSIST)
		protected Lexer lexer; 
		
		/**
		 * @since 2.5
		 */
		@Inject
		protected Provider<ContentAssistContext.Builder> contentAssistContextProvider;
		
		/**
		 * @since 2.5
		 */
		@Inject
		protected PrefixMatcher matcher;
		
		/**
		 * @since 2.5
		 */
		@Inject
		protected ITokenDefProvider tokenDefProvider;
		
		/**
		 * @since 2.13
		 */
		@Inject
		protected CompletionPrefixProvider completionPrefixProvider;
		
		/**
		 * @since 2.5
		 */
		protected ITextViewer viewer;

		/**
		 * @since 2.5
		 */
		protected XtextResource resource;

		/**
		 * @since 2.5
		 */
		protected ICompositeNode rootNode;

		/**
		 * @since 2.5
		 */
		protected INode lastCompleteNode;

		/**
		 * @since 2.5
		 */
		protected INode currentNode;

		/**
		 * @since 2.5
		 */
		protected INode lastVisibleNode;

		/**
		 * @since 2.5
		 */
		protected EObject currentModel;

		/**
		 * @since 2.5
		 */
		protected List<ContentAssistContext.Builder> contextBuilders;

		/**
		 * @since 2.5
		 */
		protected IParseResult parseResult;

		/**
		 * @since 2.5
		 */
		protected INode datatypeNode;

		/**
		 * @since 2.5
		 */
		protected int completionOffset;

		/**
		 * @since 2.5
		 */
		protected ITextSelection selection;

		@Override
		public ContentAssistContext apply(Builder from) {
			return from.toContext();
		}
		
		public ContentAssistContext[] create(ITextViewer viewer, int offset, XtextResource resource)
				throws BadLocationException {
			this.viewer = viewer;
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
		
		/**
		 * @since 2.5
		 */
		protected INode getCurrentNode() {
			return currentNode;
		}
		
		/**
		 * @since 2.7
		 */
		protected void setPool(ExecutorService pool) {
			this.pool = pool;
		}

		protected ContentAssistContext[] doCreateContexts(int offset) throws BadLocationException {
			initializeFromViewerAndResource(offset);
			List<Future<?>> futures = Lists.newArrayList();
			if (!datatypeNode.equals(lastCompleteNode)) {
				futures.add(pool.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
//						long time = System.currentTimeMillis();
						handleLastCompleteNodeAsPartOfDatatypeNode();
//						System.out.println("handleLastCompleteNodeAsPartOfDatatypeNode(); = "
//								+ (System.currentTimeMillis() - time) + "ms");
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
//						long time = System.currentTimeMillis();
						handleLastCompleteNodeIsAtEndOfDatatypeNode();
//						System.out.println("handleLastCompleteNodeIsAtEndOfDatatypeNode(); = "
//								+ (System.currentTimeMillis() - time) + "ms");
						return null;
					}
				}));
			}

			// 4th context: we assume, that the current position is perfectly ok to insert a new token, if the previous one was valid
			if (!(lastCompleteNode instanceof ILeafNode) || lastCompleteNode.getGrammarElement() != null) {
				futures.add(pool.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
//						long time = System.currentTimeMillis();
						handleLastCompleteNodeIsPartOfLookahead();
//						System.out.println("handleLastCompleteNodeIsPartOfLookahead(); = "
//								+ (System.currentTimeMillis() - time) + "ms");
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
			selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
			completionOffset = offset;
			if (selection.getOffset() + selection.getLength() == offset)
				completionOffset = selection.getOffset();
		}

		protected void handleLastCompleteNodeIsPartOfLookahead() throws BadLocationException {
			// do not calculate twice for the same input
			if (!(lastCompleteNode instanceof ILeafNode && ((ILeafNode) lastCompleteNode).isHidden())) {
				createContextsForLastCompleteNode(currentModel, true);
			}
		}

		protected void handleLastCompleteNodeIsAtEndOfDatatypeNode() throws BadLocationException {
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

		protected void handleLastCompleteNodeHasNoGrammarElement(List<Builder> contextBuilderToCheck, EObject previousModel) throws BadLocationException {
			List<ContentAssistContext> newContexts = Lists.transform(contextBuilderToCheck, this);
			boolean wasValid = isLikelyToBeValidProposal(lastCompleteNode, newContexts);
			if (wasValid && !(lastCompleteNode instanceof ILeafNode && ((ILeafNode) lastCompleteNode).isHidden())) {
				createContextsForLastCompleteNode(previousModel, false);
			}
		}

		protected void handleLastCompleteNodeAsPartOfDatatypeNode() throws BadLocationException {
			String prefix = getPrefix(datatypeNode);
			String completeInput = getInputToParse(datatypeNode);
			Collection<FollowElement> followElements = parser.getFollowElements(completeInput, false);
			INode lastCompleteNodeBeforeDatatype = getLastCompleteNodeByOffset(rootNode, datatypeNode.getTotalOffset());
			doCreateContexts(lastCompleteNodeBeforeDatatype, datatypeNode, prefix, currentModel, followElements);
		}
		
		/**
		 * @since 2.13
		 */
		protected String getInputToParse(INode node) throws BadLocationException {
			return getInputToParse(node.getOffset());
		}
		
		/**
		 * @since 2.13
		 */
		protected String getInputToParse(int offset) throws BadLocationException {
			return completionPrefixProvider.getInputToParse(viewer.getDocument().get(), offset, completionOffset);
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

		protected void createContextsForLastCompleteNode(EObject previousModel, boolean strict) throws BadLocationException {
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
			String completeInput = getInputToParse(completionOffset);
			Collection<FollowElement> followElements = parser.getFollowElements(completeInput, strict);
			doCreateContexts(lastCompleteNode, currentNode, prefix, previousModel, followElements);
		}
		
		/**
		 * Return <code>true</code> if the token should be considered to be an error token.
		 * If the token that is created from the prefix before the cursor position is an error
		 * token, no proposals shall be computed that don't use a prefix. 
		 * @return <code>true</true> if the token should be considered to be an error token.
		 * @since 2.0
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
			computeFollowElements(followElements, followElementAsAbstractElements);
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
		
		protected void computeFollowElements(Collection<FollowElement> followElements, final Collection<AbstractElement> result) {
			FollowElementCalculator calculator = new FollowElementCalculator();
			calculator.acceptor = new IFollowElementAcceptor(){
				@Override
				public void accept(AbstractElement element) {
					ParserRule rule = GrammarUtil.containingParserRule(element);
					if (rule == null || !GrammarUtil.isDatatypeRule(rule))
						result.add(element);
				}
			};
			for(FollowElement element: followElements) {
				computeFollowElements(calculator, element);
			}
		}

		protected void computeFollowElements(FollowElementCalculator calculator, FollowElement element) {
			Multimap<Integer, List<AbstractElement>> visited = HashMultimap.create();
			computeFollowElements(calculator, element, visited);
		}
		
		protected void computeFollowElements(FollowElementCalculator calculator, FollowElement element, Multimap<Integer, List<AbstractElement>> visited) {
			List<AbstractElement> currentState = Lists.newArrayList(element.getLocalTrace());
			currentState.add(element.getGrammarElement());
			if (!visited.put(element.getLookAhead(), currentState))
				return;
			if (element.getLookAhead() <= 1) {
				List<Integer> paramStack = element.getParamStack();
				int paramIndex = computeParamStackOffset(currentState, paramStack);
				for(AbstractElement abstractElement: currentState) {
					paramIndex = updateParameterConfig(calculator, paramStack, paramIndex, abstractElement);
					Assignment ass = EcoreUtil2.getContainerOfType(abstractElement, Assignment.class);
					if (ass != null)
						calculator.doSwitch(ass);
					else {
						if (abstractElement instanceof UnorderedGroup && abstractElement == element.getGrammarElement()) {
							calculator.doSwitch((UnorderedGroup) abstractElement, element.getHandledUnorderedGroupElements());
						} else {
							calculator.doSwitch(abstractElement);
							if (GrammarUtil.isOptionalCardinality(abstractElement)) {
								EObject container = abstractElement.eContainer();
								if (container instanceof Group) {
									Group group = (Group) container;
									int idx = group.getElements().indexOf(abstractElement);
									if (idx == group.getElements().size() - 1) {
										if (!currentState.contains(group) && GrammarUtil.isMultipleCardinality(group)) {
											calculator.doSwitch(group);
										}
									} else if (idx < group.getElements().size() - 1 && "?".equals(abstractElement.getCardinality())) { // loops are fine
										AbstractElement nextElement = group.getElements().get(idx + 1);
										if (!currentState.contains(nextElement)) {
											calculator.doSwitch(nextElement);
										}
									}
								}
							} else if (isAlternativeWithEmptyPath(abstractElement)) {
								EObject container = abstractElement.eContainer();
								if (container instanceof Group) {
									Group group = (Group) container;
									int idx = group.getElements().indexOf(abstractElement);
									if (!currentState.contains(group) && idx != group.getElements().size() - 1) {
										AbstractElement next = group.getElements().get(idx + 1);
										if (!currentState.contains(next)) {
											calculator.doSwitch(next);
										}
									}
								}
							}
						}
					}
				}
				// special case: entry rule, first abstract element
				// we need a synthetic rule call
				if (element.getTrace().equals(element.getLocalTrace())) {
					ParserRule parserRule = GrammarUtil.containingParserRule(element.getGrammarElement());
					if (parserRule != null) {
						RuleCall ruleCall = XtextFactory.eINSTANCE.createRuleCall();
						ruleCall.setRule(parserRule);
						calculator.doSwitch(ruleCall);
					}
				}
				return;
			}
			Collection<FollowElement> followElements = parser.getFollowElements(element);
			for(FollowElement newElement: followElements) {
				if (newElement.getLookAhead() != element.getLookAhead() || newElement.getGrammarElement() != element.getGrammarElement()) {
					if (newElement.getLookAhead() == element.getLookAhead()) {
						int originalTraceSize = element.getLocalTrace().size();
						List<AbstractElement> newTrace = newElement.getLocalTrace();
						if (newTrace.size() > originalTraceSize) {
							if (Collections.indexOfSubList(element.getLocalTrace(), newTrace.subList(originalTraceSize, newTrace.size())) != -1) {
								continue;
							}
						}
					}
					computeFollowElements(calculator, newElement, visited);
				}
			}
		}

		private boolean isAlternativeWithEmptyPath(AbstractElement abstractElement) {
			if (abstractElement instanceof Alternatives) {
				Alternatives alternatives = (Alternatives) abstractElement;
				for(AbstractElement path: alternatives.getElements()) {
					if (GrammarUtil.isOptionalCardinality(path))
						return true;
				}
			}
			return false;
		}
		
		protected int updateParameterConfig(FollowElementCalculator calculator, List<Integer> paramStack, int paramIndex,
				AbstractElement abstractElement) {
			if (paramIndex >= 0) {
				calculator.parameterConfig = paramStack.get(paramIndex);	
			} else {
				calculator.parameterConfig = 0;
			}
			if (abstractElement instanceof RuleCall) {
				RuleCall call = (RuleCall) abstractElement;
				if (!call.getArguments().isEmpty()) {
					paramIndex++;
				}
			}
			return paramIndex;
		}

		protected int computeParamStackOffset(List<AbstractElement> currentState, List<Integer> paramStack) {
			int paramIndex = paramStack.size() - 1;
			if (!paramStack.isEmpty()) {
				for(AbstractElement abstractElement: currentState) {
					if (abstractElement instanceof RuleCall) {
						RuleCall call = (RuleCall) abstractElement;
						if (!call.getArguments().isEmpty()) {
							paramIndex--;
						}
					}
				}
			}
			return paramIndex;
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

		/**
		 * @since 2.4
		 */
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
			context.setViewer(viewer);
			context.setPrefix(prefix);
			int regionLength = prefix.length();
			if (selection.getLength() > 0)
				regionLength = regionLength + selection.getLength();
			Region region = new Region(completionOffset - prefix.length(), regionLength);
			context.setReplaceRegion(region);
			context.setSelectedText(selection.getText());
			context.setMatcher(matcher);
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
			if (viewer.getDocument() != null /* testing */ && length >= 0) {
				try {
					String text = viewer.getDocument().get(startOffset, trimmedNodeText.length());
					if (trimmedNodeText.equals(text))
						return text;
					return viewer.getDocument().get(startOffset, length);
				} catch (BadLocationException e) {
					log.error(e.getMessage(), e);
				}
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

		protected ParserBasedContentAssistContextFactory.CallHierarchyHelper createCallHierachyHelper(
				final EObject previousGrammarElement,
				final String previousText,
				final EObject nextGrammarElement) {
			return new ParserBasedContentAssistContextFactory.CallHierarchyHelper(nextGrammarElement, previousText, previousGrammarElement);
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
	
	/**
	 * @since 2.7
	 */
	public static class PartialStatefulFactory extends StatefulFactory {
		@Override
		protected void createContextsForLastCompleteNode(EObject previousModel, boolean strict)
				throws BadLocationException {
			String currentNodePrefix = getPrefix(currentNode);
			if (!Strings.isEmpty(currentNodePrefix) && !currentNode.getText().equals(currentNodePrefix)) {
				lexer.setCharStream(new ANTLRStringStream(currentNodePrefix));
				Token token = lexer.nextToken();
				if (token.getType() == Token.EOF) {
					return;
				}
				while (token.getType() != Token.EOF) {
					if (isErrorToken(token)) {
						return;
					}
					token = lexer.nextToken();
				}
			}
			String prefix = "";
			Collection<FollowElement> followElements = parseFollowElements(completionOffset, strict);
			doCreateContexts(lastCompleteNode, currentNode, prefix, previousModel, followElements);
		}

		protected Collection<FollowElement> parseFollowElements(int offset, boolean strict) {
			return ((IPartialContentAssistParser) parser).getFollowElements(parseResult, offset, strict);
		}

		@Override
		protected void handleLastCompleteNodeAsPartOfDatatypeNode() throws BadLocationException {
			String prefix = getPrefix(datatypeNode);
			Collection<FollowElement> followElements = parseFollowElements(datatypeNode.getOffset(), false);
			INode lastCompleteNodeBeforeDatatype = getLastCompleteNodeByOffset(rootNode, datatypeNode.getTotalOffset());
			doCreateContexts(lastCompleteNodeBeforeDatatype, datatypeNode, prefix, currentModel, followElements);
		}

		@Override
		protected void handleLastCompleteNodeIsAtEndOfDatatypeNode() throws BadLocationException {
			String prefix = getPrefix(lastCompleteNode);
			INode previousNode = getLastCompleteNodeByOffset(rootNode, lastCompleteNode.getOffset());
			EObject previousModel = previousNode.getSemanticElement();
			INode currentDatatypeNode = getContainingDatatypeRuleNode(currentNode);
			Collection<FollowElement> followElements = parseFollowElements(lastCompleteNode.getOffset(), false);
			int prevSize = contextBuilders.size();
			doCreateContexts(previousNode, currentDatatypeNode, prefix, previousModel, followElements);

			if (lastCompleteNode instanceof ILeafNode && lastCompleteNode.getGrammarElement() == null
					&& contextBuilders.size() != prevSize) {
				handleLastCompleteNodeHasNoGrammarElement(contextBuilders.subList(prevSize, contextBuilders.size()),
						previousModel);
			}
		}
	}

	@Override
	public ContentAssistContext[] create(ITextViewer viewer, int offset, XtextResource resource) {
		try { 
			StatefulFactory factory = statefulFactoryProvider.get();
			factory.pool = pool;
			return factory.create(viewer, offset, resource);
		}
		catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	public static class LeafNodeFinder extends org.eclipse.xtext.ide.editor.contentassist.antlr.LeafNodeFinder {
		public LeafNodeFinder(int offset, boolean leading) {
			super(offset, leading);
		}
	}

	public static class FollowElementCalculator extends XtextSwitch<Boolean> {
		
		protected IFollowElementAcceptor acceptor;
		
		private List<AbstractElement> handledAlternatives;

		private UnorderedGroup group;
		
		private Collection<RuleCall> visitedRuleCalls = Sets.newHashSet();
		
		private int parameterConfig = 0;
		
		private Set<Parameter> currentConfig = null;
		
		public void doSwitch(UnorderedGroup group, List<AbstractElement> handledAlternatives) {
			this.group = group;
			this.handledAlternatives = handledAlternatives;
			try {
				doSwitch(group);
			} finally {
				this.handledAlternatives = null;
				this.group = null;
			}
		}
		
		@Override
		public Boolean caseAlternatives(Alternatives object) {
			boolean more = false;
			for(AbstractElement element: object.getElements()) {
				more = doSwitch(element) || more;
			}
			return more || isOptional(object);
		}
		
		@Override
		public Boolean caseUnorderedGroup(UnorderedGroup object) {
			if (object == this.group) {
				boolean more = true;
				for(AbstractElement element: object.getElements()) {
					if (handledAlternatives == null || !handledAlternatives.contains(element)) {
						this.group = null;
						more = doSwitch(element) && more;
						this.group = object;
					}
				}
				if (more && GrammarUtil.isMultipleCardinality(object)) {
					handledAlternatives = null;
					this.group = null;
					return caseUnorderedGroup(object);
				}
				return more || isOptional(object);
			} else {
				boolean more = true;
				for(AbstractElement element: object.getElements()) {
					more = doSwitch(element) && more;
				}
				return more || isOptional(object);
			}
		}
		
		@Override
		public Boolean caseGroup(Group object) {
			boolean more = true;
			if (object.getGuardCondition() != null) {
				Set<Parameter> parameterValues = getParameterValues(object);
				more = new ConditionEvaluator(parameterValues).evaluate(object.getGuardCondition());
			}
			if (more) {
				for(AbstractElement element: object.getElements()) {
					more = more && doSwitch(element);
				}
			}
			return more || isOptional(object);
		}

		private Set<Parameter> getParameterValues(AbstractElement object) {
			Set<Parameter> parameterValues = Collections.emptySet();
			if (currentConfig != null) {
				parameterValues = currentConfig;
			} else {
				parameterValues = ParameterConfigHelper.getAssignedParameters(object, parameterConfig);
			}
			return parameterValues;
		}
		
		@Override
		public Boolean caseAction(Action object) {
			return true;
		}
		
		@Override
		public Boolean caseAssignment(Assignment object) {
			acceptor.accept(object);
			return doSwitch(object.getTerminal()) || isOptional(object);
		}
		
		@Override
		public Boolean caseCrossReference(CrossReference object) {
			return Boolean.FALSE;
		}
		
		@Override
		public Boolean caseParserRule(ParserRule object) {
			if (GrammarUtil.isDatatypeRule(object))
				return Boolean.FALSE;
			return doSwitch(object.getAlternatives());
		}
		
		@Override
		public Boolean caseEnumRule(EnumRule object) {
			return doSwitch(object.getAlternatives());
		}
		
		@Override
		public Boolean caseEnumLiteralDeclaration(EnumLiteralDeclaration object) {
			return doSwitch(object.getLiteral());
		}
		
		@Override
		public Boolean caseRuleCall(RuleCall object) {
			if (!visitedRuleCalls.add(object))
				return isOptional(object);
			acceptor.accept(object);
			Set<Parameter> oldConfig = currentConfig;
			try {
				if (!object.getArguments().isEmpty()) {
					currentConfig = ParameterConfigHelper.getAssignedArguments(object, getParameterValues(object));
				} else {
					currentConfig = Collections.emptySet();
				}
				Boolean result = doSwitch(object.getRule()) || isOptional(object);
				visitedRuleCalls.remove(object);
				return result;
			} finally {
				currentConfig = oldConfig;
			}
		}
		
		@Override
		public Boolean caseTerminalRule(TerminalRule object) {
			return Boolean.FALSE;
		}
		
		@Override
		public Boolean caseKeyword(Keyword object) {
			acceptor.accept(object);
			return isOptional(object);
		}
		
		public boolean isOptional(AbstractElement element) {
			return GrammarUtil.isOptionalCardinality(element);
		}
	}

	/**
	 * @author Sebastian Zarnekow - Initial contribution and API
	 */
	public static class CallHierarchyHelper extends XtextSwitch<Boolean> {
		private final EObject nextGrammarElement;
		private Set<AbstractRule> visiting = new HashSet<AbstractRule>();
		private Map<AbstractRule, Boolean> visited = Maps.newHashMapWithExpectedSize(4);
		private EObject grammarElement;
		private EObject queuedGrammarElement;
		private Boolean result = Boolean.FALSE;
		private String expectedText;
	
		public CallHierarchyHelper(EObject nextGrammarElement, String previousText, EObject previousGrammarElement) {
			this.nextGrammarElement = nextGrammarElement;
			this.expectedText = previousText;
			grammarElement = previousGrammarElement;
			queuedGrammarElement = nextGrammarElement;
		}
	
		@Override
		public Boolean caseAbstractRule(AbstractRule object) {
			if (!checkFurther(object))
				return result;
			if (!visiting.add(object))
				return Boolean.FALSE;
			if (visited.containsKey(object)) {
				visiting.remove(object);
				return visited.get(object);
			}
			EObject wasGrammarElement = grammarElement;
			Boolean result = doSwitch(object.getAlternatives());
			visiting.remove(object);
			if (isExpectedGrammarElement(wasGrammarElement)) // we store the result per grammarElement for performance reasons
				visited.put(object, result);
			return result;
		}
	
		private boolean checkFurther(EObject object) {
			if (isExpectedGrammarElement(object)) {
				if (queuedGrammarElement != null) {
					grammarElement = queuedGrammarElement;
					queuedGrammarElement = null;
					expectedText = null;
					visited.clear();
					visiting.clear();
					return true;
				}
				result = Boolean.TRUE;
				return false;
			}
			return true;
		}

		protected boolean isExpectedGrammarElement(EObject object) {
			if (object == grammarElement)
				return true;
			if (grammarElement == null && expectedText != null) {
				if (object instanceof Keyword) {
					if (expectedText.equals(((Keyword) object).getValue()))
						return true;
				}
			}
			return false;
		}
	
		@Override
		public Boolean caseTerminalRule(TerminalRule object) {
			checkFurther(object);
			return result;
		}
	
		@Override
		public Boolean caseGroup(Group object) {
			if (!checkFurther(object))
				return result;
			for (AbstractElement token : object.getElements())
				if (doSwitch(token))
					return true;
			if (GrammarUtil.isMultipleCardinality(object)) {
				if (!checkFurther(object))
					return result;
				for (AbstractElement token : object.getElements())
					if (doSwitch(token))
						return true;
			}
			return false;
		}
	
		@Override
		public Boolean caseUnorderedGroup(UnorderedGroup object) {
			if (!checkFurther(object))
				return result;
			// elements may occur in any order - treat them as looped alternatives
			if (caseAlternatives(object.getElements()))
				return true;
			if (!checkFurther(object))
				return result;
			return caseAlternatives(object.getElements());
		}
	
		@Override
		public Boolean caseAlternatives(Alternatives object) {
			if (!checkFurther(object))
				return result;
			if (caseAlternatives(object.getElements()))
				return true;
			if (GrammarUtil.isMultipleCardinality(object)) {
				if (!checkFurther(object))
					return result;
				return caseAlternatives(object.getElements());
			}
			return Boolean.FALSE;
		}
	
		public Boolean caseAlternatives(List<AbstractElement> elements) {
			EObject wasGrammarElement = this.grammarElement;
			Set<AbstractRule> visiting = Sets.newHashSet(this.visiting);
			boolean foundSomething = false;
			for (AbstractElement group : elements) {
				this.grammarElement = wasGrammarElement;
				this.visiting = Sets.newHashSet(visiting);
				if (doSwitch(group))
					return true;
				if (wasGrammarElement != this.grammarElement) {
					foundSomething = true;
				}
			}
			if (foundSomething) {
				this.grammarElement = nextGrammarElement;
				this.visited.clear();
				this.visiting.clear();
			}
			return Boolean.FALSE;
		}
	
		@Override
		public Boolean caseAbstractElement(AbstractElement object) {
			if (!checkFurther(object))
				return result;
			if (GrammarUtil.isMultipleCardinality(object)) {
				if (!checkFurther(object))
					return result;
			}
			return Boolean.FALSE;
		}
	
		@Override
		public Boolean caseAssignment(Assignment object) {
			if (!checkFurther(object))
				return result;
			if (doSwitch(object.getTerminal()))
				return true;
			if (GrammarUtil.isMultipleCardinality(object)) {
				if (!checkFurther(object))
					return result;
				if (doSwitch(object.getTerminal()))
					return true;
			}
			return Boolean.FALSE;
		}
	
		@Override
		public Boolean caseCrossReference(CrossReference object) {
			if (!checkFurther(object))
				return result;
			if (doSwitch(object.getTerminal()))
				return true;
			if (GrammarUtil.isMultipleCardinality(object)) {
				if (!checkFurther(object))
					return result;
				if (doSwitch(object.getTerminal()))
					return true;
			}
			return Boolean.FALSE;
		}
	
		@Override
		public Boolean caseRuleCall(RuleCall object) {
			if (!checkFurther(object))
				return result;
			if (doSwitch(object.getRule()))
				return true;
			if (GrammarUtil.isMultipleCardinality(object)) {
				if (!checkFurther(object))
					return result;
				if (doSwitch(object.getRule()))
					return true;
			}
			return Boolean.FALSE;
		}
	
		@Override
		public Boolean caseEnumLiteralDeclaration(EnumLiteralDeclaration object) {
			if (!checkFurther(object))
				return result;
			Boolean result = doSwitch(object.getLiteral());
			return result;
		}
	}
}
