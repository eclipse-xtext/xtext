/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.xtext.xtext.generator.builder.BuilderIntegrationFragment2;
import org.eclipse.xtext.xtext.generator.ecore.EMFGeneratorFragment2;
import org.eclipse.xtext.xtext.generator.exporting.QualifiedNamesFragment2;
import org.eclipse.xtext.xtext.generator.formatting.Formatter2Fragment2;
import org.eclipse.xtext.xtext.generator.generator.GeneratorFragment2;
import org.eclipse.xtext.xtext.generator.grammarAccess.GrammarAccessFragment2;
import org.eclipse.xtext.xtext.generator.index.ResourceDescriptionStrategyFragment;
import org.eclipse.xtext.xtext.generator.junit.JUnitFragment;
import org.eclipse.xtext.xtext.generator.parser.antlr.XtextAntlrGeneratorFragment2;
import org.eclipse.xtext.xtext.generator.resourceFactory.ResourceFactoryFragment2;
import org.eclipse.xtext.xtext.generator.scoping.ImportNamespacesScopingFragment2;
import org.eclipse.xtext.xtext.generator.serializer.SerializerFragment2;
import org.eclipse.xtext.xtext.generator.types.TypesGeneratorFragment2;
import org.eclipse.xtext.xtext.generator.ui.compare.CompareFragment2;
import org.eclipse.xtext.xtext.generator.ui.contentAssist.ContentAssistFragment2;
import org.eclipse.xtext.xtext.generator.ui.fileWizard.TemplateFileWizardFragment;
import org.eclipse.xtext.xtext.generator.ui.labeling.LabelProviderFragment2;
import org.eclipse.xtext.xtext.generator.ui.outline.OutlineTreeProviderFragment2;
import org.eclipse.xtext.xtext.generator.ui.outline.QuickOutlineFragment2;
import org.eclipse.xtext.xtext.generator.ui.projectWizard.TemplateProjectWizardFragment;
import org.eclipse.xtext.xtext.generator.ui.quickfix.QuickfixProviderFragment2;
import org.eclipse.xtext.xtext.generator.ui.refactoring.RefactorElementNameFragment2;
import org.eclipse.xtext.xtext.generator.ui.templates.CodetemplatesGeneratorFragment2;
import org.eclipse.xtext.xtext.generator.validation.ValidatorFragment2;
import org.eclipse.xtext.xtext.generator.xbase.XbaseGeneratorFragment2;
import org.eclipse.xtext.xtext.generator.xbase.XtypeGeneratorFragment2;

import com.google.inject.Injector;

/**
 * This specialization of the {@link XtextGeneratorLanguage} adds all the
 * standard generator fragments that are commonly used for Xtext languages. This
 * eliminates the need to list all these fragments explicitly in the workflow
 * file. More fragments can be added as required, but the standard fragments
 * cannot be removed. However, most of these fragments disable themselves
 * automatically if they are not applicable.
 * 
 * <p>
 * The configuration for individual fragments can be made with the corresponding
 * properties. For example, write
 * 
 * <pre>
 * formatter = formatting.Formatter2Fragment2 {
 *     generateStub = true
 * }
 * </pre>
 * 
 * to enable stub generation for the formatter.
 * </p>
 * 
 * @author Sven Efftinge - Initial contribution and API
 * @author Holger Schill
 * @author Arne Deutsch - Add projectWizard and fileWizard, deprecate
 * 'newProjectWizardForEclipse'
 * @noextend This class should not be extended by clients.
 */
public class StandardLanguage extends XtextGeneratorLanguage {

	private static final Logger LOG = Logger.getLogger(StandardLanguage.class);

	private GrammarAccessFragment2 grammarAccess = new GrammarAccessFragment2();

	private EMFGeneratorFragment2 emfGenerator = new EMFGeneratorFragment2();

	private SerializerFragment2 serializer = new SerializerFragment2();

	private ResourceFactoryFragment2 resourceFactoryFragment = new ResourceFactoryFragment2();

	private XtextAntlrGeneratorFragment2 parserGenerator = new XtextAntlrGeneratorFragment2();

	private ValidatorFragment2 validator = new ValidatorFragment2();

	private ImportNamespacesScopingFragment2 scopeProvider = new ImportNamespacesScopingFragment2();

	/**
	 * @since 2.14
	 */
	private ResourceDescriptionStrategyFragment resourceDescriptionStrategy = new ResourceDescriptionStrategyFragment();

	private QualifiedNamesFragment2 qualifiedNamesProvider = new QualifiedNamesFragment2();

	private BuilderIntegrationFragment2 builder = new BuilderIntegrationFragment2();

	private GeneratorFragment2 generator = new GeneratorFragment2();

	private Formatter2Fragment2 formatter = new Formatter2Fragment2();

	private LabelProviderFragment2 labelProvider = new LabelProviderFragment2();

	private QuickOutlineFragment2 quickOutline = new QuickOutlineFragment2();

	private OutlineTreeProviderFragment2 outline = new OutlineTreeProviderFragment2();

	private QuickfixProviderFragment2 quickFixProvider = new QuickfixProviderFragment2();

	private ContentAssistFragment2 contentAssist = new ContentAssistFragment2();

	private JUnitFragment junitSupport = new JUnitFragment();

	private RefactorElementNameFragment2 renameRefactoring = new RefactorElementNameFragment2();

	private TypesGeneratorFragment2 commonTypesSupport = new TypesGeneratorFragment2();

	private XbaseGeneratorFragment2 xbaseSupport = new XbaseGeneratorFragment2();

	private XtypeGeneratorFragment2 xtypeSupport = new XtypeGeneratorFragment2();

	private CodetemplatesGeneratorFragment2 codeTemplates = new CodetemplatesGeneratorFragment2();

	private CompareFragment2 compareEditor = new CompareFragment2();

	@Deprecated
	private org.eclipse.xtext.xtext.generator.ui.projectWizard.SimpleProjectWizardFragment2 newProjectWizardForEclipse = new org.eclipse.xtext.xtext.generator.ui.projectWizard.SimpleProjectWizardFragment2();

	private TemplateProjectWizardFragment projectWizard = new TemplateProjectWizardFragment();

	private TemplateFileWizardFragment fileWizard = new TemplateFileWizardFragment();

	public StandardLanguage() {
		try {
			getClass().getClassLoader().loadClass("org.eclipse.xtext.xbase.XbaseRuntimeModule");
			addReferencedResource("platform:/resource/org.eclipse.xtext.xbase/model/Xbase.genmodel");
		} catch (ClassNotFoundException e) {
			StandardLanguage.LOG.info("Skipping registration of Xbase genmodel. Xbase is not on the classpath.");
		}
	}

	@Override
	public void initialize(Injector injector) {
		if (!formatter.getGenerateStub().isSet()) {
			formatter.setGenerateStub(false);
		}
		if (!commonTypesSupport.getOnlyEnabledIfGrammarIsUsed().isSet()) {
			commonTypesSupport.setOnlyEnabledIfGrammarIsUsed(true);
		}
		super.initialize(injector);
	}

	@Override
	protected List<? extends IXtextGeneratorFragment> getImplicitFragments() {
		List<IXtextGeneratorFragment> fragments = new ArrayList<>();
		for (IXtextGeneratorFragment fragment : super.getImplicitFragments()) {
			fragments.add(fragment);
		}
		nullSafeAdd(fragments, grammarAccess);
		nullSafeAdd(fragments, emfGenerator);
		nullSafeAdd(fragments, serializer);
		nullSafeAdd(fragments, resourceFactoryFragment);
		nullSafeAdd(fragments, parserGenerator);
		nullSafeAdd(fragments, validator);
		nullSafeAdd(fragments, scopeProvider);
		nullSafeAdd(fragments, resourceDescriptionStrategy);
		nullSafeAdd(fragments, qualifiedNamesProvider);
		nullSafeAdd(fragments, builder);
		nullSafeAdd(fragments, generator);
		nullSafeAdd(fragments, formatter);
		nullSafeAdd(fragments, labelProvider);
		nullSafeAdd(fragments, quickOutline);
		nullSafeAdd(fragments, outline);
		nullSafeAdd(fragments, quickFixProvider);
		nullSafeAdd(fragments, contentAssist);
		nullSafeAdd(fragments, junitSupport);
		nullSafeAdd(fragments, renameRefactoring);
		nullSafeAdd(fragments, commonTypesSupport);
		nullSafeAdd(fragments, xbaseSupport);
		nullSafeAdd(fragments, xtypeSupport);
		nullSafeAdd(fragments, codeTemplates);
		nullSafeAdd(fragments, compareEditor);
		nullSafeAdd(fragments, newProjectWizardForEclipse);
		nullSafeAdd(fragments, projectWizard);
		nullSafeAdd(fragments, fileWizard);
		return fragments;
	}

	private boolean nullSafeAdd(List<IXtextGeneratorFragment> list, IXtextGeneratorFragment fragment) {
		if (fragment != null) {
			return list.add(fragment);
		}
		return false;
	}

	/**
	 * @deprecated Use 'projectWizard' instead
	 */
	@Deprecated
	public org.eclipse.xtext.xtext.generator.ui.projectWizard.SimpleProjectWizardFragment2 setNewProjectWizardForEclipse(
			org.eclipse.xtext.xtext.generator.ui.projectWizard.SimpleProjectWizardFragment2 fragment) {
		return newProjectWizardForEclipse = fragment;
	}

	/**
	 * Create a wizard able to create new projects with initial content based on
	 * template definitions. Supported options: 'generate',
	 * 'generateToolbarButton', 'pluginProject'.
	 */
	public TemplateProjectWizardFragment setProjectWizard(TemplateProjectWizardFragment fragment) {
		return projectWizard = fragment;
	}

	/**
	 * Create a wizard able to create new files with initial content based on
	 * template definitions. Supported options: 'generate',
	 * 'generateToolbarButton'.
	 */
	public TemplateFileWizardFragment setFileWizard(TemplateFileWizardFragment fragment) {
		return fileWizard = fragment;
	}

	protected GrammarAccessFragment2 getGrammarAccess() {
		return grammarAccess;
	}

	public void setGrammarAccess(GrammarAccessFragment2 grammarAccess) {
		this.grammarAccess = grammarAccess;
	}

	protected EMFGeneratorFragment2 getEmfGenerator() {
		return emfGenerator;
	}

	public void setEmfGenerator(EMFGeneratorFragment2 emfGenerator) {
		this.emfGenerator = emfGenerator;
	}

	protected SerializerFragment2 getSerializer() {
		return serializer;
	}

	public void setSerializer(SerializerFragment2 serializer) {
		this.serializer = serializer;
	}

	protected ResourceFactoryFragment2 getResourceFactoryFragment() {
		return resourceFactoryFragment;
	}

	public void setResourceFactoryFragment(ResourceFactoryFragment2 resourceFactoryFragment) {
		this.resourceFactoryFragment = resourceFactoryFragment;
	}

	protected XtextAntlrGeneratorFragment2 getParserGenerator() {
		return parserGenerator;
	}

	public void setParserGenerator(XtextAntlrGeneratorFragment2 parserGenerator) {
		this.parserGenerator = parserGenerator;
	}

	protected ValidatorFragment2 getValidator() {
		return validator;
	}

	public void setValidator(ValidatorFragment2 validator) {
		this.validator = validator;
	}

	protected ImportNamespacesScopingFragment2 getScopeProvider() {
		return scopeProvider;
	}

	public void setScopeProvider(ImportNamespacesScopingFragment2 scopeProvider) {
		this.scopeProvider = scopeProvider;
	}

	protected ResourceDescriptionStrategyFragment getResourceDescriptionStrategy() {
		return resourceDescriptionStrategy;
	}

	public void setResourceDescriptionStrategy(ResourceDescriptionStrategyFragment resourceDescriptionStrategy) {
		this.resourceDescriptionStrategy = resourceDescriptionStrategy;
	}

	protected QualifiedNamesFragment2 getQualifiedNamesProvider() {
		return qualifiedNamesProvider;
	}

	public void setQualifiedNamesProvider(QualifiedNamesFragment2 qualifiedNamesProvider) {
		this.qualifiedNamesProvider = qualifiedNamesProvider;
	}

	protected BuilderIntegrationFragment2 getBuilder() {
		return builder;
	}

	public void setBuilder(BuilderIntegrationFragment2 builder) {
		this.builder = builder;
	}

	protected GeneratorFragment2 getGenerator() {
		return generator;
	}

	public void setGenerator(GeneratorFragment2 generator) {
		this.generator = generator;
	}

	protected Formatter2Fragment2 getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter2Fragment2 formatter) {
		this.formatter = formatter;
	}

	protected LabelProviderFragment2 getLabelProvider() {
		return labelProvider;
	}

	public void setLabelProvider(LabelProviderFragment2 labelProvider) {
		this.labelProvider = labelProvider;
	}

	protected QuickOutlineFragment2 getQuickOutline() {
		return quickOutline;
	}

	public void setQuickOutline(QuickOutlineFragment2 quickOutline) {
		this.quickOutline = quickOutline;
	}

	protected OutlineTreeProviderFragment2 getOutline() {
		return outline;
	}

	public void setOutline(OutlineTreeProviderFragment2 outline) {
		this.outline = outline;
	}

	protected QuickfixProviderFragment2 getQuickFixProvider() {
		return quickFixProvider;
	}

	public void setQuickFixProvider(QuickfixProviderFragment2 quickFixProvider) {
		this.quickFixProvider = quickFixProvider;
	}

	protected ContentAssistFragment2 getContentAssist() {
		return contentAssist;
	}

	public void setContentAssist(ContentAssistFragment2 contentAssist) {
		this.contentAssist = contentAssist;
	}

	protected JUnitFragment getJunitSupport() {
		return junitSupport;
	}

	public void setJunitSupport(JUnitFragment junitSupport) {
		this.junitSupport = junitSupport;
	}

	protected RefactorElementNameFragment2 getRenameRefactoring() {
		return renameRefactoring;
	}

	public void setRenameRefactoring(RefactorElementNameFragment2 renameRefactoring) {
		this.renameRefactoring = renameRefactoring;
	}

	protected TypesGeneratorFragment2 getCommonTypesSupport() {
		return commonTypesSupport;
	}

	public void setCommonTypesSupport(TypesGeneratorFragment2 commonTypesSupport) {
		this.commonTypesSupport = commonTypesSupport;
	}

	protected XbaseGeneratorFragment2 getXbaseSupport() {
		return xbaseSupport;
	}

	public void setXbaseSupport(XbaseGeneratorFragment2 xbaseSupport) {
		this.xbaseSupport = xbaseSupport;
	}

	protected XtypeGeneratorFragment2 getXtypeSupport() {
		return xtypeSupport;
	}

	public void setXtypeSupport(XtypeGeneratorFragment2 xtypeSupport) {
		this.xtypeSupport = xtypeSupport;
	}

	protected CodetemplatesGeneratorFragment2 getCodeTemplates() {
		return codeTemplates;
	}

	public void setCodeTemplates(CodetemplatesGeneratorFragment2 codeTemplates) {
		this.codeTemplates = codeTemplates;
	}

	protected CompareFragment2 getCompareEditor() {
		return compareEditor;
	}

	public void setCompareEditor(CompareFragment2 compareEditor) {
		this.compareEditor = compareEditor;
	}

	@SuppressWarnings("deprecation")
	protected org.eclipse.xtext.xtext.generator.ui.projectWizard.SimpleProjectWizardFragment2 getNewProjectWizardForEclipse() {
		return newProjectWizardForEclipse;
	}

	protected TemplateProjectWizardFragment getProjectWizard() {
		return projectWizard;
	}

	protected TemplateFileWizardFragment getFileWizard() {
		return fileWizard;
	}

}
