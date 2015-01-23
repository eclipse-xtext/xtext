/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.idea.generator.parser.antlr

import com.google.inject.Singleton
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.AbstractElement
import org.eclipse.xtext.AbstractRule
import org.eclipse.xtext.Action
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.EnumRule
import org.eclipse.xtext.Grammar
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ParserRule
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.TerminalRule
import org.eclipse.xtext.UnorderedGroup
import org.eclipse.xtext.generator.parser.antlr.AntlrOptions

import static extension org.eclipse.xtext.EcoreUtil2.*
import static extension org.eclipse.xtext.GrammarUtil.*

@Singleton
class AntlrGrammarGenerator extends DefaultAntlrGrammarGenerator {
	
	protected override getGrammarFileName(Grammar it) {
		getGrammarFileName('')
	}
	
	protected override compileOptions(Grammar it, AntlrOptions options) '''
		
		options {
			superClass=AbstractInternalAntlrParser;
		�IF options.backtrack || options.memoize || options.k >= 0�
			�IF options.backtrack�
			backtrack=true
			�ENDIF�
			�IF options.memoize�
			memoize=true
			�ENDIF�
			�IF options.k >= 0�
			memoize=�options.k�
			�ENDIF�
		�ENDIF�
		}
	''' 
	
	protected override compileParserImports(Grammar it, AntlrOptions options) '''

		import org.eclipse.xtext.*;
		import org.eclipse.xtext.parser.*;
		import org.eclipse.xtext.parser.impl.*;
		import org.eclipse.emf.ecore.util.EcoreUtil;
		import org.eclipse.emf.ecore.EObject;
		�IF !allEnumRules.empty�
		import org.eclipse.emf.common.util.Enumerator;
		�ENDIF�
		import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
		import org.eclipse.xtext.parser.antlr.XtextTokenStream;
		import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
		�IF !allParserRules.exists[!eAllContentsAsList.filter(UnorderedGroup).empty] && options.backtrack�
		import org.eclipse.xtext.parser.antlr.IUnorderedGroupHelper.UnorderedGroupState;
		�ENDIF�
		import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
		import �gaFQName�;

	'''
	
	protected override compileParserMembers(Grammar it, AntlrOptions options) '''
		
		@parser::members {
		
		�IF options.backtrack�
		/*
		  This grammar contains a lot of empty actions to work around a bug in ANTLR.
		  Otherwise the ANTLR tool will create synpreds that cannot be compiled in some rare cases.
		*/

		�ENDIF�
		 	private �gaSimpleName� grammarAccess;

		    public �internalParserClassName.toSimpleName�(TokenStream input, �gaSimpleName� grammarAccess) {
		        this(input);
		        this.grammarAccess = grammarAccess;
		        registerRules(grammarAccess.getGrammar());
		    }
		
		    @Override
		    protected String getFirstRuleName() {
		    	return "�allParserRules.head.name�";
		   	}
		
		   	@Override
		   	protected �gaSimpleName� getGrammarAccess() {
		   		return grammarAccess;
		   	}
		
		}
	'''
	
	protected override compileRuleCatch(Grammar it, AntlrOptions options) '''
		
		@rulecatch {
		    catch (RecognitionException re) {
		        recover(input,re);
		        appendSkippedTokens();
		    }
		}
	''' 
	
	override protected shouldBeSkipped(TerminalRule it, Grammar grammar) {
		false
	}
	
	protected override _compileRule(ParserRule it, Grammar grammar, AntlrOptions options) '''
		�compileEntryRule(grammar, options)�
		
		�compileEBNF(options)�
	'''
	
	protected def String compileEntryRule(ParserRule it, Grammar grammar, AntlrOptions options) '''
		// Entry rule �entryRuleName�
		�entryRuleName��compileEntryInit(options)�:
			{ �newCompositeNode� }
			iv_�ruleName�=�ruleName�
			{ $current=$iv_�ruleName�.current�IF datatypeRule�.getText()�ENDIF�; }
			EOF;
		�compileEntryFinally(options)�
	'''
	
	protected def compileEntryInit(ParserRule it, AntlrOptions options) '''
		 returns �compileEntryReturns(options)�
		�IF definesHiddenTokens || definesUnorderedGroups(options)�
		@init {
			�IF definesHiddenTokens��compileInitHiddenTokens(options)��ENDIF�
			�IF definesUnorderedGroups(options)��compileInitUnorderedGroups(options)��ENDIF�
		}�ENDIF�'''
	
	protected def compileEntryReturns(ParserRule it, AntlrOptions options) {
		switch it {
			ParserRule case datatypeRule:
				'[String current=null]'
			ParserRule:
				'[EObject current=null]'
			default:
				throw new IllegalStateException("Unexpected rule: " + it)
		}
	}
		
	protected def compileInitUnorderedGroups(ParserRule it, AntlrOptions options) '''
		UnorderedGroupState myUnorderedGroupState = getUnorderedGroupHelper().snapShot(
		�FOR group:eAllContentsAsList.filter(UnorderedGroup) SEPARATOR ', '�
			grammarAccess.�group.gaRuleElementAccessor�
		�ENDFOR�
		);
	'''
		
	protected def compileInitHiddenTokens(ParserRule it, AntlrOptions options) '''
		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens(�FOR hidden:hiddenTokens SEPARATOR ', '�"�hidden.ruleName�"�ENDFOR�);
	'''
	
	protected def compileEntryFinally(ParserRule it, AntlrOptions options) '''
		�IF definesHiddenTokens || definesUnorderedGroups(options)�
		finally {
			�IF definesHiddenTokens�myHiddenTokenState.restore();�ENDIF�
			�IF definesUnorderedGroups(options)�myUnorderedGroupState.restore();�ENDIF�
		}�ENDIF�'''
	
	override protected compileInit(AbstractRule it, AntlrOptions options) '''
		 returns �compileReturns(options)�
		@init {
			enterRule();
		}
		@after {
			leaveRule();
		}'''
		
	protected def compileReturns(AbstractRule it, AntlrOptions options) {
		switch it {
			EnumRule:
				'returns [Enumerator current=null]'
			ParserRule case datatypeRule:
				'[AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()]'
			ParserRule:
				'[EObject current=null]'
			default:
				throw new IllegalStateException("Unexpected rule: " + it)
		}
	}
	
	protected override String _dataTypeEbnf2(UnorderedGroup it, boolean supportActions) {
		if (supportActions) {
			val mandatoryContent = elements.filter[!optionalCardinality].size
			'''
				(
					{ 
					  getUnorderedGroupHelper().enter(grammarAccess.�gaRuleElementAccessor�);
					}
					(
						(
				�FOR element:elements.indexed SEPARATOR '|'�
							(
								{getUnorderedGroupHelper().canSelect(grammarAccess.�gaRuleElementAccessor�, �element.key�)}?=>(
									{
										getUnorderedGroupHelper().select(grammarAccess.�gaRuleElementAccessor�, �element.key�);
									}
			���	Predicate {true}=> helps to workaround an issue in the Antlr grammar processing
									({true}?=>(�element.value.dataTypeEbnf2(supportActions)�))�IF element.value.multipleCardinality�+�ENDIF�
									{ 
										getUnorderedGroupHelper().returnFromSelection(grammarAccess.�gaRuleElementAccessor�);
									}
								)
							)
				�ENDFOR�
						)�IF mandatoryContent != 0�+
						{getUnorderedGroupHelper().canLeave(grammarAccess.�gaRuleElementAccessor�)}?�ELSE�*�ENDIF�
					)
				)
					{ 
					  getUnorderedGroupHelper().leave(grammarAccess.�gaRuleElementAccessor�);
					}
			'''
		} else {
			super._dataTypeEbnf2(it, supportActions)
		}
	}
	
	protected override String _dataTypeEbnf2(Keyword it, boolean supportActions) {
		if (supportActions) '''
			kw=�super._dataTypeEbnf2(it, supportActions)�
			{
				$current.merge(kw);
				�newLeafNode("kw")�
			}
		''' 
		else
			super._dataTypeEbnf2(it, supportActions) 
	}
	
	protected override String _dataTypeEbnf2(RuleCall it, boolean supportActions) {
		if (supportActions)
			switch rule {
				EnumRule case assigned,
				ParserRule case assigned: 
					super._dataTypeEbnf2(it, supportActions)
				EnumRule, 
				ParserRule: '''
					{
						�newCompositeNode�
					}
					�localVar�=�super._dataTypeEbnf2(it, supportActions)�
					{
						$current.merge(�localVar�);
					}
					{
						afterParserOrEnumRuleCall();
					}
				'''
				TerminalRule: '''
					�localVar�=�super._dataTypeEbnf2(it, supportActions)�
					{
						$current.merge(�localVar�);
					}
					{
						�newLeafNode(localVar)�
					}
				'''
				default:
					super._dataTypeEbnf2(it, supportActions)
			}
		else
			super._dataTypeEbnf2(it, supportActions)
	}
	
	protected override String _ebnf2(UnorderedGroup it, AntlrOptions options, boolean supportActions) {
		if (supportActions) {
			val mandatoryContent = elements.filter[!optionalCardinality].size
			'''
				(
					{ 
					  getUnorderedGroupHelper().enter(grammarAccess.�gaRuleElementAccessor�);
					}
					(
						(
				�FOR element:elements.indexed SEPARATOR '|'�
							(
								{getUnorderedGroupHelper().canSelect(grammarAccess.�gaRuleElementAccessor�, �element.key�)}?=>(
									{
										getUnorderedGroupHelper().select(grammarAccess.�gaRuleElementAccessor�, �element.key�);
									}
			���	Predicate {true}=> helps to workaround an issue in the Antlr grammar processing
									({true}?=>(�element.value.ebnf(options, supportActions)�))�IF element.value.multipleCardinality�+�ENDIF�
									{ 
										getUnorderedGroupHelper().returnFromSelection(grammarAccess.�gaRuleElementAccessor�);
									}
								)
							)
				�ENDFOR�
						)�IF mandatoryContent != 0�+
						{getUnorderedGroupHelper().canLeave(grammarAccess.�gaRuleElementAccessor�)}?�ELSE�*�ENDIF�
					)
				)
					{ 
					  getUnorderedGroupHelper().leave(grammarAccess.�gaRuleElementAccessor�);
					}
				'''
		} else {
			super._ebnf2(it, options, supportActions)	
		}
	}
	
	protected override String _ebnf2(Action it, AntlrOptions options, boolean supportActions) {
		if (supportActions) '''
			�IF options.backtrack�
			{
				/* */
			}
			�ENDIF�
			{
				$current = forceCreateModelElement�IF feature != null�And�setOrAdd.toFirstUpper��ENDIF�(
					grammarAccess.�grammarElementAccess�,
					$current);
			}
		'''
		else 
			super._ebnf2(it, options, supportActions)
	}
	
	protected override String _ebnf2(Keyword it, AntlrOptions options, boolean supportActions) {
		if (!supportActions) 
			super._ebnf2(it, options, supportActions)
		else if (assigned) '''
			�super._ebnf2(it, options, supportActions)�
			{
				�newLeafNode(containingAssignment.localVar(it))�
			}
		'''
		else '''
			�localVar�=�super._ebnf2(it, options, supportActions)�
			{
				�newLeafNode(localVar)�
			}
		'''
	}
	
	protected override String _ebnf2(RuleCall it, AntlrOptions options, boolean supportActions) {
		if (!supportActions)
			super._ebnf2(it, options, supportActions)
		else
			switch rule : rule {
				EnumRule case assigned,
				ParserRule case assigned: 
					super._ebnf2(it, options, supportActions)
				EnumRule, 
				ParserRule case rule.datatypeRule: '''
					�IF options.backtrack�
					{
						/* */
					}
					�ENDIF�
					{
						�newCompositeNode�
					}
					�super._ebnf2(it, options, supportActions)�
					{
						afterParserOrEnumRuleCall();
					}
				'''
				ParserRule: '''
					�IF options.backtrack�
					{
						/* */
					}
					�ENDIF�
					{
						�newCompositeNode�
					}
					�localVar�=�super._ebnf2(it, options, supportActions)�
					{
						$current = $�localVar�.current;
						afterParserOrEnumRuleCall();
					}
				'''
				TerminalRule: '''
					�localVar�=�super._ebnf2(it, options, supportActions)�
					{
						�newLeafNode(localVar)�
					}
				'''
				default: 
					super._ebnf2(it, options, supportActions)
			}
	}
	
	protected override String _crossrefEbnf(AbstractRule it, CrossReference ref, boolean supportActions) {
		if (supportActions)
			switch it {
				EnumRule,
				ParserRule: '''
					{
						�ref.newCompositeNode�
					}
					�ruleName�
					{
						afterParserOrEnumRuleCall();
					}
				'''
				TerminalRule: '''
					�ref.containingAssignment.localVar�=�ruleName�
					{
						�ref.newLeafNode(ref.containingAssignment.localVar)�
					}
				'''
				default:
					throw new IllegalStateException("crossrefEbnf is not supported for " + it)
			}
		else
			super._crossrefEbnf(it, ref, supportActions)
	}
	
	override protected _assignmentEbnf(CrossReference it, Assignment assignment, AntlrOptions options, boolean supportActions) {
		if (supportActions) '''
			�IF options.backtrack�
			{
				/* */
			}
			�ENDIF�
			{
				if ($current==null) {
					$current = �assignment.createModelElement�;
				}
			}
			�super._assignmentEbnf(it, assignment, options, supportActions)�'''
		else
			super._assignmentEbnf(it, assignment, options, supportActions)
	}

	override protected _assignmentEbnf(AbstractElement it, Assignment assignment, AntlrOptions options, boolean supportActions) {
		if (supportActions) '''
			�assignment.localVar(it)�=�super._assignmentEbnf(it, assignment, options, supportActions)�
			{
				if ($current==null) {
					$current = �assignment.createModelElement�;
				}
				�assignment.setOrAdd�WithLastConsumed($current, "�assignment.feature�", �
	        		IF assignment.boolean�true�
	        		ELSE��assignment.localVar(it)��
	        		ENDIF�, �assignment.terminal.toStringLiteral�);
			}
		'''
		else
			super._assignmentEbnf(it, assignment, options, supportActions)
	}
	
	protected override String _assignmentEbnf(RuleCall it, Assignment assignment, AntlrOptions options, boolean supportActions) {
		if (supportActions)
			switch rule {
				EnumRule,
				ParserRule: '''
					{
						�newCompositeNode�
					}
					�assignment.localVar(it)�=�super._assignmentEbnf(it, assignment, options, supportActions)�
					{
						if ($current==null) {
							$current = �assignment.createModelElementForParent�;
						}
						�assignment.setOrAdd�(
							$current,
							"�assignment.feature�",
							�IF assignment.boolean�true�ELSE��assignment.localVar(it)��ENDIF�,
							�toStringLiteral�);
						afterParserOrEnumRuleCall();
					}
				'''
				TerminalRule: '''
					�assignment.localVar(it)�=�super._assignmentEbnf(it, assignment, options, supportActions)�
					{
						�newLeafNode(assignment.localVar(it))�
					}
					{
						if ($current==null) {
							$current = �assignment.createModelElement�;
						}
						�assignment.setOrAdd�WithLastConsumed(
							$current,
							"�assignment.feature�",
							�IF assignment.isBoolean�true�ELSE��assignment.localVar(it)��ENDIF�,
							�toStringLiteral�);
					}
				'''
				default: 
					throw new IllegalStateException("assignmentEbnf is not supported for " + it)
			}
		else
			super._assignmentEbnf(it, assignment, options, supportActions)
	}
	
	protected def createModelElement(EObject grammarElement) '''
		createModelElement(grammarAccess.�grammarElement.containingParserRule.grammarElementAccess�)'''
	
	protected def createModelElementForParent(EObject grammarElement) '''
		createModelElementForParent(grammarAccess.�grammarElement.containingParserRule.grammarElementAccess�)'''
	
	protected def newCompositeNode(EObject it) '''newCompositeNode(grammarAccess.�grammarElementAccess�);'''

	protected def newLeafNode(EObject it, String token) '''newLeafNode(�token�, grammarAccess.�grammarElementAccess�);'''

}