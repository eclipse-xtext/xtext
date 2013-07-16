/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.refactoring;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static java.util.Collections.*;
import static org.eclipse.ltk.core.refactoring.RefactoringStatus.*;
import static org.eclipse.xtext.util.Strings.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.xtend.XtendClass;
import org.eclipse.xtend.core.xtend.XtendFunction;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.refactoring.impl.DisplayChangeWrapper;
import org.eclipse.xtext.ui.refactoring.impl.StatusWrapper;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.ReplaceRegion;
import org.eclipse.xtext.util.TextRegion;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.compiler.IAppendable;
import org.eclipse.xtext.xbase.compiler.StringBuilderBasedAppendable;
import org.eclipse.xtext.xbase.compiler.output.XtypeTypeReferenceSerializer;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.legacy.StandardTypeReferenceOwner;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.util.CommonTypeComputationServices;
import org.eclipse.xtext.xbase.ui.document.DocumentRewriter;
import org.eclipse.xtext.xbase.ui.document.DocumentRewriter.Section;
import org.eclipse.xtext.xbase.ui.imports.ReplaceConverter;
import org.eclipse.xtext.xbase.ui.refactoring.ExpressionUtil;
import org.eclipse.xtext.xbase.ui.refactoring.NewFeatureNameUtil;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class ExtractMethodRefactoring extends Refactoring {

	public static final Logger LOG = Logger.getLogger(ExtractMethodRefactoring.class);
	
	@Inject
	private Provider<StatusWrapper> statusProvider;
	
	@Inject
	private ILocationInFileProvider locationInFileProvider;
	
	@Inject
	private IBatchTypeResolver typeResolver;
	
	@Inject
	private CommonTypeComputationServices typeComputationServices;
	
	@Inject
	private ExpressionUtil expressionUtil;
	
	@Inject
	private NewFeatureNameUtil nameUtil;
	
	@Inject
	private IXtendJvmAssociations associations;
	
	@Inject 
	private DocumentRewriter.Factory rewriterFactory;

	@Inject
	private ReplaceConverter replaceConverter;
	
	@Inject
	private XtypeTypeReferenceSerializer typeReferenceSerializer;
	
	private IDocument document;

	private List<XExpression> expressions;

	private String methodName = "";

	private JvmVisibility visibility;

	private boolean isStatic;
	
	private List<ParameterInfo> parameterInfos = newArrayList();
	
	private boolean isExplicitlyDeclareReturnType;

	
	private XExpression firstExpression;
	
	private XExpression lastExpression; 

	private URI resourceURI;

	private XtendClass xtendClass;

	private XtendFunction originalMethod;

	private TextEdit textEdit;

	private List<String> localFeatureNames = newArrayList();
	
	private Multimap<String, XFeatureCall> externalFeatureCalls = HashMultimap.create();

	private XExpression returnExpression;
	
	private LightweightTypeReference returnType;
	
	private Set<JvmTypeParameter> neededTypeParameters = newHashSet();

	private DocumentRewriter rewriter;
	

	public boolean initialize(IXtextDocument document, List<XExpression> expressions) {
		if(expressions.isEmpty() || document == null)
			return false;
		this.document = document;
		this.expressions = expressions;
		this.firstExpression = expressions.get(0);
		this.lastExpression = expressions.get(expressions.size()-1);
		this.resourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(firstExpression).trimFragment();
		this.xtendClass = EcoreUtil2.getContainerOfType(firstExpression, XtendClass.class);
		this.originalMethod = EcoreUtil2.getContainerOfType(firstExpression, XtendFunction.class);
		if (xtendClass == null || originalMethod == null)
			return false;
		this.visibility = originalMethod.getVisibility();
		this.isStatic = originalMethod.isStatic();
		XExpression successorExpression = expressionUtil
				.findSuccessorExpressionForVariableDeclaration(lastExpression);
		nameUtil.setFeatureScopeContext(successorExpression);
		rewriter = rewriterFactory.create(document, (XtextResource) firstExpression.eResource());
		return true;
	}

	@Override
	public String getName() {
		return "Extract Method Refactoring";
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public RefactoringStatus validateMethodName(String newMethodName) {
		RefactoringStatus status = new RefactoringStatus();
		nameUtil.checkNewFeatureName(newMethodName, true, status);
		return status;
	}

	public JvmVisibility getVisibility() {
		return visibility;
	}

	public void setVisibility(JvmVisibility visibility) {
		this.visibility = visibility;
	}

	public void setExplicitlyDeclareReturnType(boolean isExplicitlyDeclareReturnType) {
		this.isExplicitlyDeclareReturnType = isExplicitlyDeclareReturnType;
	}
	
	public boolean isExplicitlyDeclareReturnType() {
		return isExplicitlyDeclareReturnType;
	}

	public XtendClass getXtendClass() {
		return xtendClass;
	}

	public List<ParameterInfo> getParameterInfos() {
		return parameterInfos;
	}

	public RefactoringStatus validateParameters() {
		RefactoringStatus status = new RefactoringStatus();
		Set<String> namesSoFar = newHashSet();
		for (ParameterInfo parameterInfo : parameterInfos) {
			String newName = parameterInfo.getNewName();
			if (namesSoFar.contains(newName))
				status.addError("Duplicate parameter name '" + newName + "'");
			if (!equal(newName, parameterInfo.getOldName()) && localFeatureNames.contains(newName))
				status.addError("'" + newName + "' is already used as a name in the selected code");
			nameUtil.checkNewFeatureName(newName, false, status);
			namesSoFar.add(newName);
		}
		// TODO: vararg order
		return status;
	}

	public String getMethodSignature() {
		StringBuilderBasedAppendable appendable = new StringBuilderBasedAppendable();
		appendMethodSignature(appendable);
		return appendable.toString();
	}
	
	protected void appendMethodSignature(IAppendable appendable) {
		if (visibility != JvmVisibility.PUBLIC)
			appendable.append(getVisibility().getName().toLowerCase()).append(" ");
		appendable.append("def ");
		if (isStatic)
			appendable.append("static ");
		if(!neededTypeParameters.isEmpty()) {
			appendable.append("<");
			boolean isFirst = true;
			for(JvmTypeParameter typeParameter: associations.getDirectlyInferredOperation(originalMethod).getTypeParameters()) {
				if(neededTypeParameters.contains(typeParameter)) {
					if(!isFirst)
						appendable.append(", ");
					isFirst = false;
					appendable.append(typeParameter);
				}
			}
			appendable.append("> ");
		}
		if (isExplicitlyDeclareReturnType) {
			typeReferenceSerializer.serialize(returnType.toTypeReference(), firstExpression, appendable);
			appendable.append(" ");
		}
		appendable.append(methodName).append("(");
		boolean isFirst = true;
		for (ParameterInfo parameterInfo : getParameterInfos()) {
			if (!isFirst)
				appendable.append(", ");
			isFirst = false;
			appendable.append(parameterInfo.getOldTypeName()).append(" ").append(parameterInfo.getNewName());
		}
		appendable.append(")");
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		StatusWrapper status = statusProvider.get();
		IResolvedTypes resolvedTypes = typeResolver.resolveTypes(firstExpression);
		try {
			Set<String> calledExternalFeatureNames = newHashSet();
			returnType = calculateReturnType(resolvedTypes);
			if (!equal("void", returnType.getIdentifier()))
				returnExpression = lastExpression;
			boolean isReturnAllowed = isEndOfOriginalMethod(); 
			for (EObject element : EcoreUtil2.eAllContents(originalMethod.getExpression())) {
				boolean isLocalExpression = EcoreUtil.isAncestor(expressions, element);
				if (element instanceof XFeatureCall) {
					XFeatureCall featureCall = (XFeatureCall) element;
					JvmIdentifiableElement feature = featureCall.getFeature();
					LightweightTypeReference featureType = resolvedTypes.getActualType(featureCall);
					boolean isLocalFeature = EcoreUtil.isAncestor(expressions, feature);
					if (!isLocalFeature && isLocalExpression) {
						// call-out
						if (feature instanceof JvmFormalParameter || feature instanceof XVariableDeclaration) {
							if (!calledExternalFeatureNames.contains(feature.getSimpleName())) {
								calledExternalFeatureNames.add(feature.getSimpleName());
								parameterInfos.add(
										new ParameterInfo(featureType.getIdentifier(), 
										feature.getSimpleName(), 
										parameterInfos.size()));
							}
							externalFeatureCalls.put(feature.getSimpleName(), featureCall);
						}
					} else if (isLocalFeature && !isLocalExpression) {
						// call-in
						if (returnExpression != null) {
							status.add(RefactoringStatus.FATAL,
									"Ambiguous return value: Multiple local variables are accesed in subsequent code.");
							break;
						}
						returnExpression = featureCall;
						returnType = featureType;
					}
				} else if(isLocalExpression) {
					if(element instanceof XReturnExpression && !isReturnAllowed) {
						status.add(RefactoringStatus.FATAL,
							"Extracting method would break control flow due to return statements.");
						break;
					}  else if(element instanceof JvmTypeReference) {
						JvmType type = ((JvmTypeReference) element).getType();
						if(type instanceof JvmTypeParameter) {
							List<JvmTypeParameter> typeParameters = 
									associations.getDirectlyInferredOperation(originalMethod).getTypeParameters();
							if(typeParameters.contains(type)) 
								neededTypeParameters.add((JvmTypeParameter) type);
						}
					} else if (element instanceof JvmFormalParameter)
						localFeatureNames.add(((JvmFormalParameter) element).getName());
					else if (element instanceof XVariableDeclaration)
						localFeatureNames.add(((XVariableDeclaration) element).getIdentifier());
				}
			}
		} catch (Exception exc) {
			handleException(exc, status);
		}
		return status.getRefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		StatusWrapper status = statusProvider.get();
		try {
			status.merge(validateMethodName(methodName));
			status.merge(validateParameters());
			
			ITextRegion expressionsRegion = getExpressionsRegion();
			ITextRegion predecessorRegion = locationInFileProvider.getFullTextRegion(originalMethod);
			
			Section expressionSection = rewriter.newSection(expressionsRegion.getOffset(), expressionsRegion.getLength());
			Section declarationSection = rewriter.newSection(predecessorRegion.getOffset() + predecessorRegion.getLength(), 0);
			createMethodCallEdit(expressionSection, expressionsRegion);
			createMethodDeclarationEdit(declarationSection, expressionSection.getBaseIndentationLevel(), expressionsRegion);
			textEdit = replaceConverter.convertToTextEdit(rewriter.getChanges());
		} catch (Exception exc) {
			handleException(exc, status);
		}
		return status.getRefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		DocumentChange change = new DocumentChange("Extract Method", document);
		change.setEdit(textEdit);
		change.setTextType(resourceURI.fileExtension());
		return new DisplayChangeWrapper(change);
	}

	protected void handleException(Exception exc, StatusWrapper status) {
		status.add(FATAL, "Error during refactoring: {0}", exc, LOG);
	}

	protected LightweightTypeReference calculateReturnType(IResolvedTypes resolvedTypes) {
		List<LightweightTypeReference> returnTypes = newArrayList();
		for(XExpression expression: expressions) {
			LightweightTypeReference expressionReturnType = resolvedTypes.getReturnType(expression);
			if(expressionReturnType != null)
				returnTypes.add(expressionReturnType);
		}		
		returnTypes.add(resolvedTypes.getActualType(lastExpression));
		StandardTypeReferenceOwner owner = new StandardTypeReferenceOwner(typeComputationServices, xtendClass);
		return typeComputationServices.getTypeConformanceComputer().getCommonSuperType(returnTypes, owner);
	}

	protected ITextRegion getExpressionsRegion() {
		ITextRegion firstRegion = locationInFileProvider.getFullTextRegion(firstExpression);
		ITextRegion lastRegion = locationInFileProvider.getFullTextRegion(lastExpression);
		ITextRegion expressionRegion = new TextRegion(firstRegion.getOffset(), lastRegion.getOffset()
				+ lastRegion.getLength() - firstRegion.getOffset());
		return expressionRegion;
	}
	
	protected void createMethodDeclarationEdit(DocumentRewriter.Section declarationSection, int expressionIndentLevel, ITextRegion expressionsRegion) throws BadLocationException {
		String expressionsAsString = getExtractedMethodBody(expressionsRegion);
		declarationSection
				.newLine()
				.newLine();
		appendMethodSignature(declarationSection);
		declarationSection
				.append(" {")
				.increaseIndentation()
				.newLine();
		declarationSection.append(expressionsAsString, Math.min(0, -expressionIndentLevel));
		if (isNeedsReturnExpression())
			declarationSection
				.newLine()
				.append(((XFeatureCall) returnExpression).getFeature().getSimpleName());
		declarationSection
			.decreaseIndentation()
			.newLine()
			.append("}");
	}

	protected String getExtractedMethodBody(ITextRegion expressionsRegion) throws BadLocationException {
		String methodBody = getMethodBodyWithRenamedParameters(expressionsRegion);
		if(expressions.size() == 1 
				&& firstExpression instanceof XClosure 
				&& (!methodBody.startsWith("[") || !methodBody.endsWith("]"))) {
			// legacy closure argument syntax
			return "[" + methodBody + "]";
		}
		return methodBody;
	}

	protected String getMethodBodyWithRenamedParameters(ITextRegion expressionsRegion) throws BadLocationException {
		String expressionsAsString = document.get(expressionsRegion.getOffset(), expressionsRegion.getLength());
		List<ReplaceRegion> parameterRenames = newArrayList();
		for(final String parameterName: externalFeatureCalls.keySet()) {
			ParameterInfo parameter = find(parameterInfos, new Predicate<ParameterInfo>() {
				public boolean apply(ParameterInfo info) {
					return equal(info.getOldName(), parameterName);
				}
			});
			if(parameter.isRenamed()) {
				for(XFeatureCall featureCall: externalFeatureCalls.get(parameterName)) {
					ITextRegion textRegion = locationInFileProvider.getSignificantTextRegion(featureCall, XbasePackage.Literals.XABSTRACT_FEATURE_CALL__FEATURE, -1);
					parameterRenames.add(new ReplaceRegion(textRegion, parameter.getNewName()));
				}
			}
		}
		sort(parameterRenames, new Comparator<ReplaceRegion>() {
			public int compare(ReplaceRegion o1, ReplaceRegion o2) {
				return o2.getOffset() - o1.getOffset();
			}
		});
		StringBuffer buffer = new StringBuffer(expressionsAsString);
		for(ReplaceRegion parameterRename: parameterRenames) {
			buffer.replace(parameterRename.getOffset() - expressionsRegion.getOffset(), 
					parameterRename.getEndOffset() - expressionsRegion.getOffset(), 
					parameterRename.getText());
		}
		expressionsAsString = buffer.toString();
		return expressionsAsString;
	}

	protected void createMethodCallEdit(DocumentRewriter.Section methodCallSection, ITextRegion expressionRegion) throws BadLocationException {
		if (isNeedsReturnExpression()) {
			JvmIdentifiableElement returnFeature = ((XFeatureCall) returnExpression).getFeature();
			if (isFinalFeature(returnFeature))
				methodCallSection.append("val ");
			else
				methodCallSection.append("var ");
			methodCallSection.append(returnFeature.getSimpleName()).append(" = ");
		}
		boolean needsSurroundingParentheses = false;
		if(firstExpression.eContainer() instanceof XMemberFeatureCall) {
			if(((XMemberFeatureCall) firstExpression.eContainer()).getMemberCallArguments().size() == 1) {
				String expressionExpanded = document.get(expressionRegion.getOffset()-1, expressionRegion.getLength() + 2);
				if(!expressionExpanded.startsWith("(") || !expressionExpanded.endsWith(")")) {
					needsSurroundingParentheses = true;
					methodCallSection.append("(");
				}
			}
		}
		methodCallSection.append(methodName).append("(");
		boolean isFirst = true;
		for (ParameterInfo parameterInfo : getParameterInfos()) {
			if (!isFirst)
				methodCallSection.append(", ");
			isFirst = false;
			methodCallSection.append(parameterInfo.getOldName());
		}
		methodCallSection.append(")");
		if(needsSurroundingParentheses)
			methodCallSection.append(")");
	}

	protected boolean isEndOfOriginalMethod() {
		EObject eContainer = lastExpression.eContainer();
		if(eContainer instanceof XBlockExpression) {
			if(eContainer.eContainer() == originalMethod) {
				EList<XExpression> siblings = ((XBlockExpression)eContainer).getExpressions();
				return siblings.indexOf(lastExpression) == siblings.size()-1;
			}
		}
		return false;
	}

	protected boolean isNeedsReturnExpression() {
		return returnExpression != null && returnExpression != lastExpression;
	}

	protected boolean isFinalFeature(JvmIdentifiableElement returnFeature) {
		return returnFeature instanceof JvmFormalParameter
				|| (returnFeature instanceof XVariableDeclaration && !((XVariableDeclaration) returnFeature)
						.isWriteable());
	}
}
