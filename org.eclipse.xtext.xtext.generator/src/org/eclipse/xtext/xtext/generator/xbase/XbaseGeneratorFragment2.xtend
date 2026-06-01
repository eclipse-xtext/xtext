/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.xbase

import com.google.inject.Inject
import com.google.inject.name.Names
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.GrammarUtil
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.ILocationInFileProvider
import org.eclipse.xtext.scoping.IGlobalScopeProvider
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess
import org.eclipse.xtext.xtext.generator.model.TypeReference

import static extension org.eclipse.xtext.xtext.generator.model.TypeReference.*
import static extension org.eclipse.xtext.xtext.generator.util.GenModelUtil2.*

class XbaseGeneratorFragment2 extends AbstractXtextGeneratorFragment {

	@Accessors(PUBLIC_SETTER)
	boolean generateXtendInferrer = true
	
	@Accessors(PUBLIC_SETTER)
	boolean useInferredJvmModel = true
	
	@Accessors(PUBLIC_SETTER)
	boolean jdtTypeHierarchy = true
	
	@Accessors(PUBLIC_SETTER)
	boolean jdtCallHierarchy = true
	
	@Accessors(PUBLIC_SETTER)
	boolean skipExportedPackage = false
	
	@Inject FileAccessFactory fileAccessFactory
	
	@Inject extension XtextGeneratorNaming
	
	@Inject extension XbaseUsageDetector
	
	protected def TypeReference getJvmModelInferrer() {
		new TypeReference(grammar.runtimeBasePackage + '.jvmmodel.' + GrammarUtil.getSimpleName(grammar) + 'JvmModelInferrer')
	}
	
	override generate() {
		if (!grammar.inheritsXbase)
			return;
		
		if (!grammar.eclipsePluginEditor.equals(grammar.eclipsePluginXbaseEditor)) {
			contributeEditorStub()
		}
		
		contributeRuntimeGuiceBindings()
		contributeEclipsePluginGuiceBindings()
		if (projectConfig.eclipsePlugin.pluginXml !== null)
			contributeEclipsePluginExtensions()
		if (generateXtendInferrer)
			doGenerateXtendInferrer()
		
		if (projectConfig.runtime.manifest !== null) {
			projectConfig.runtime.manifest.requiredBundles.addAll(#[
				'org.eclipse.xtext.xbase', 'org.eclipse.xtext.xbase.lib;bundle-version="'+projectConfig.runtime.xbaseLibVersionLowerBound+'"'
			])
			if ((generateXtendInferrer || useInferredJvmModel) && !skipExportedPackage) {
				projectConfig.runtime.manifest.exportedPackages += jvmModelInferrer.packageName
			}
		}
		if (projectConfig.eclipsePlugin.manifest !== null) {
			projectConfig.eclipsePlugin.manifest.requiredBundles.addAll(#[
				'org.eclipse.xtext.xbase.ui', 'org.eclipse.jdt.debug.ui'
			])
		}
		
		language.ideGenModule.superClass = 'org.eclipse.xtext.xbase.ide.DefaultXbaseIdeModule'.typeRef
	}
	
	protected def contributeEditorStub() {
		if (projectConfig.eclipsePlugin?.srcGen !== null) {
			val file = fileAccessFactory.createGeneratedJavaFile(grammar.eclipsePluginEditor)
			
			file.content = '''
				/**
				 * This class was generated. Customizations should only happen in a newly
				 * introduced subclass.
				 */
				public class «grammar.eclipsePluginEditor.simpleName» extends «grammar.eclipsePluginXbaseEditor» {
				}
			'''
			file.writeTo(projectConfig.eclipsePlugin.srcGen)
		}

		if (projectConfig.eclipsePlugin.manifest !== null) {
			projectConfig.eclipsePlugin.manifest.exportedPackages += grammar.eclipsePluginEditor.packageName
		}
	}
	
	protected def contributeRuntimeGuiceBindings() {
		val bindingFactory = new GuiceModuleAccess.BindingFactory()
				// overrides binding from org.eclipse.xtext.generator.exporting.QualifiedNamesFragment
				.addTypeToType(IQualifiedNameProvider.typeRef,
						'org.eclipse.xtext.xbase.scoping.XbaseQualifiedNameProvider'.typeRef)
		if (!grammar.usesXImportSection) {
			bindingFactory.addConfiguredBinding("RewritableImportSectionEnablement", '''
					binder.bind(«Boolean».TYPE)
						.annotatedWith(«Names».named(«new TypeReference('org.eclipse.xtext.xbase.imports','RewritableImportSection.Factory')».REWRITABLEIMPORTSECTION_ENABLEMENT))
						.toInstance(«Boolean».FALSE);
				''')
		}
		if (useInferredJvmModel) {
			bindingFactory
				.addTypeToType(ILocationInFileProvider.typeRef,
						'org.eclipse.xtext.xbase.jvmmodel.JvmLocationInFileProvider'.typeRef)
				.addTypeToType(IGlobalScopeProvider.typeRef,
						'org.eclipse.xtext.common.types.xtext.TypesAwareDefaultGlobalScopeProvider'.typeRef)
				.addTypeToType('org.eclipse.xtext.xbase.validation.FeatureNameValidator'.typeRef,
						'org.eclipse.xtext.xbase.validation.LogicalContainerAwareFeatureNameValidator'.typeRef)
				.addTypeToType('org.eclipse.xtext.xbase.typesystem.internal.DefaultBatchTypeResolver'.typeRef,
						'org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareBatchTypeResolver'.typeRef)
				.addTypeToType('org.eclipse.xtext.xbase.typesystem.internal.DefaultReentrantTypeResolver'.typeRef,
						'org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareReentrantTypeResolver'.typeRef)
				.addTypeToType(IResourceValidator.typeRef,
						'org.eclipse.xtext.xbase.annotations.validation.DerivedStateAwareResourceValidator'.typeRef)
			if (generateXtendInferrer) {
				bindingFactory
					.addTypeToType('org.eclipse.xtext.xbase.jvmmodel.IJvmModelInferrer'.typeRef, jvmModelInferrer)
			}
		} else {
			bindingFactory
				.addTypeToType(ILocationInFileProvider.typeRef,
						'org.eclipse.xtext.xbase.resource.XbaseLocationInFileProvider'.typeRef)

		}
		bindingFactory.contributeTo(language.runtimeGenModule)
		
		if (language.grammar.inheritsXbaseWithAnnotations)
			language.runtimeGenModule.superClass = 'org.eclipse.xtext.xbase.annotations.DefaultXbaseWithAnnotationsRuntimeModule'.typeRef
		else
			language.runtimeGenModule.superClass = 'org.eclipse.xtext.xbase.DefaultXbaseRuntimeModule'.typeRef
	}
	
	protected def contributeEclipsePluginGuiceBindings() {
		val bindingFactory = new GuiceModuleAccess.BindingFactory()
		if (useInferredJvmModel) {
			val StringConcatenationClient statement = '''
				if («'org.eclipse.ui.PlatformUI'.typeRef».isWorkbenchRunning()) {
					binder.bind(«'org.eclipse.xtext.ui.editor.IURIEditorOpener'.typeRef».class).annotatedWith(«'org.eclipse.xtext.ui.LanguageSpecific'.typeRef».class).to(«'org.eclipse.xtext.xbase.ui.jvmmodel.navigation.DerivedMemberAwareEditorOpener'.typeRef».class);
					binder.bind(«'org.eclipse.xtext.common.types.ui.navigation.IDerivedMemberAwareEditorOpener'.typeRef».class).to(«'org.eclipse.xtext.xbase.ui.jvmmodel.navigation.DerivedMemberAwareEditorOpener'.typeRef».class);
				}
			'''
			// Rename refactoring
			bindingFactory
				.addTypeToType('org.eclipse.xtext.ui.editor.findrefs.FindReferencesHandler'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.findrefs.JvmModelFindReferenceHandler'.typeRef)
				.addTypeToType('org.eclipse.xtext.ui.editor.findrefs.ReferenceQueryExecutor'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.findrefs.JvmModelReferenceQueryExecutor'.typeRef)
				
				// overrides binding from org.eclipse.xtext.generator.exporting.QualifiedNamesFragment
				.addTypeToType('org.eclipse.xtext.ui.refactoring.IDependentElementsCalculator'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.JvmModelDependentElementsCalculator'.typeRef)
				// overrides binding from RefactorElementNameFragment
				.addTypeToType('org.eclipse.xtext.ui.refactoring.IRenameRefactoringProvider'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.jdt.CombinedJvmJdtRenameRefactoringProvider'.typeRef)
				// overrides binding from RefactorElementNameFragment
				.addTypeToType('org.eclipse.xtext.ui.refactoring.IReferenceUpdater'.typeRef,
						'org.eclipse.xtext.xbase.ui.refactoring.XbaseReferenceUpdater'.typeRef)
				// overrides binding from RefactorElementNameFragment
				.addfinalTypeToType('org.eclipse.xtext.ui.refactoring.ui.IRenameContextFactory'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.jdt.CombinedJvmJdtRenameContextFactory'.typeRef)
				// overrides binding from RefactorElementNameFragment
				.addTypeToType('org.eclipse.xtext.ui.refactoring.IRenameStrategy'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.DefaultJvmModelRenameStrategy'.typeRef)
				
				.addTypeToType(new TypeReference('org.eclipse.xtext.common.types.ui.refactoring.participant', 'JdtRenameParticipant.ContextFactory'),
						new TypeReference ('org.eclipse.xtext.xbase.ui.jvmmodel.refactoring', 'JvmModelJdtRenameParticipantContext.ContextFactory'))
				.addTypeToType('org.eclipse.xtext.ui.editor.outline.impl.OutlineNodeElementOpener'.typeRef,
						'org.eclipse.xtext.xbase.ui.jvmmodel.outline.JvmOutlineNodeElementOpener'.typeRef)
				.addTypeToType('org.eclipse.xtext.ui.editor.GlobalURIEditorOpener'.typeRef,
						'org.eclipse.xtext.common.types.ui.navigation.GlobalDerivedMemberAwareURIEditorOpener'.typeRef)
				.addTypeToType('org.eclipse.xtext.common.types.ui.query.IJavaSearchParticipation'.typeRef,
						new TypeReference('org.eclipse.xtext.common.types.ui.query', 'IJavaSearchParticipation.No'))
				// DerivedMemberAwareEditorOpener
				.addConfiguredBinding('LanguageSpecificURIEditorOpener', statement)
		} else {
			bindingFactory
				.addTypeToType('org.eclipse.xtext.ui.refactoring.IRenameStrategy'.typeRef,
						'org.eclipse.xtext.xbase.ui.refactoring.XbaseRenameStrategy'.typeRef)
		}
		if (language.grammar.usesXImportSection) {
			bindingFactory
				.addTypeToType('org.eclipse.xtext.xbase.imports.IUnresolvedTypeResolver'.typeRef,
						'org.eclipse.xtext.xbase.ui.imports.InteractiveUnresolvedTypeResolver'.typeRef)
				.addTypeToType('org.eclipse.xtext.common.types.xtext.ui.ITypesProposalProvider'.typeRef,
						'org.eclipse.xtext.xbase.ui.contentassist.ImportingTypesProposalProvider'.typeRef)
				.addTypeToType('org.eclipse.xtext.ui.editor.templates.XtextTemplateContextType'.typeRef,
						'org.eclipse.xtext.xbase.ui.templates.XbaseTemplateContextType'.typeRef)
		} else {
			bindingFactory
				.addTypeToType('org.eclipse.xtext.xbase.ui.quickfix.JavaTypeQuickfixes'.typeRef,
						'org.eclipse.xtext.xbase.ui.quickfix.JavaTypeQuickfixesNoImportSection'.typeRef)
		}
		bindingFactory.addTypeToType(grammar.eclipsePluginDefaultEditor, grammar.eclipsePluginEditor)
		
		bindingFactory.contributeTo(language.eclipsePluginGenModule)
		
		if (language.grammar.inheritsXbaseWithAnnotations)
			language.eclipsePluginGenModule.superClass = 'org.eclipse.xtext.xbase.annotations.ui.DefaultXbaseWithAnnotationsUiModule'.typeRef
		else
			language.eclipsePluginGenModule.superClass = 'org.eclipse.xtext.xbase.ui.DefaultXbaseUiModule'.typeRef
	}
	
	protected def doGenerateXtendInferrer() {
		val firstRuleType = language.grammar.rules.head.type.classifier.getJavaTypeName(language.grammar.eResource.resourceSet).typeRef
		fileAccessFactory.createXtendFile(jvmModelInferrer, '''
			/**
			 * <p>Infers a JVM model from the source model.</p>
			 *
			 * <p>The JVM model should contain all elements that would appear in the Java code
			 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>
			 */
			class «jvmModelInferrer.simpleName» extends «'org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer'.typeRef» {
			
				/**
				 * convenience API to build and initialize JVM types and their members.
				 */
				@«Inject» extension «'org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder'.typeRef»
			
				/**
				 * The dispatch method {@code infer} is called for each instance of the
				 * given element's type that is contained in a resource.
				 *
				 * @param element
				 *            the model to create one or more
				 *            {@link org.eclipse.xtext.common.types.JvmDeclaredType declared
				 *            types} from.
				 * @param acceptor
				 *            each created
				 *            {@link org.eclipse.xtext.common.types.JvmDeclaredType type}
				 *            without a container should be passed to the acceptor in order
				 *            get attached to the current resource. The acceptor's
				 *            {@link IJvmDeclaredTypeAcceptor#accept(org.eclipse.xtext.common.types.JvmDeclaredType)
				 *            accept(..)} method takes the constructed empty type for the
				 *            pre-indexing phase. This one is further initialized in the
				 *            indexing phase using the lambda you pass as the last argument.
				 * @param isPreIndexingPhase
				 *            whether the method is called in a pre-indexing phase, i.e.
				 *            when the global index is not yet fully updated. You must not
				 *            rely on linking using the index if isPreIndexingPhase is
				 *            <code>true</code>.
				 */
				def dispatch void infer(«firstRuleType» element, «'org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor'.typeRef» acceptor, boolean isPreIndexingPhase) {
					// Here you explain how your model is mapped to Java elements, by writing the actual translation code.
					
					// An implementation for the initial hello world example could look like this:
			//		acceptor.accept(element.toClass("my.company.greeting.MyGreetings")) [
			//			for (greeting : element.greetings) {
			//				members += greeting.toMethod("hello" + greeting.name, typeRef(String)) [
			//					body = «"'''"»
			//						return "Hello «'«'»greeting.name«'»'»";
			//					«"'''"»
			//				]
			//			}
			//		]
				}
			}
		''').writeTo(projectConfig.runtime.src)
	}
	
	protected def contributeEclipsePluginExtensions() {
		val name = language.grammar.name
		if (jdtTypeHierarchy) {
			projectConfig.eclipsePlugin.pluginXml.entries += '''
				<!-- Type Hierarchy  -->
				<extension point="org.eclipse.ui.handlers">
					<handler
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.xbase.ui.hierarchy.OpenTypeHierarchyHandler"
						commandId="org.eclipse.xtext.xbase.ui.hierarchy.OpenTypeHierarchy">
						<activeWhen>
							<reference
								definitionId="«name».Editor.opened">
							</reference>
						</activeWhen>
					</handler>
					<handler
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.xbase.ui.hierarchy.QuickTypeHierarchyHandler"
						commandId="org.eclipse.jdt.ui.edit.text.java.open.hierarchy">
						<activeWhen>
							<reference
								definitionId="«name».Editor.opened">
							</reference>
						</activeWhen>
					</handler>
					«IF language.grammar.usesXImportSection»
						<handler
							class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.xbase.ui.imports.OrganizeImportsHandler"
							commandId="org.eclipse.xtext.xbase.ui.organizeImports">
							<activeWhen>
								<reference
									definitionId="«name».Editor.opened">
								</reference>
							</activeWhen>
						</handler>
					«ENDIF»
				</extension>
				<extension point="org.eclipse.ui.menus">
					«IF language.grammar.usesXImportSection»
						<menuContribution
							locationURI="popup:#TextEditorContext?after=group.edit">
							<command
								commandId="org.eclipse.xtext.xbase.ui.organizeImports"
								style="push"
								tooltip="Organize Imports">
								<visibleWhen checkEnabled="false">
									<reference
										definitionId="«name».Editor.opened">
									</reference>
								</visibleWhen>
							</command>
						</menuContribution>
					«ENDIF»
					<menuContribution
						locationURI="popup:#TextEditorContext?after=group.open">
						<command commandId="org.eclipse.xtext.xbase.ui.hierarchy.OpenTypeHierarchy"
							style="push"
							tooltip="Open Type Hierarchy">
							<visibleWhen checkEnabled="false">
								<reference definitionId="«name».Editor.opened"/>
							</visibleWhen>
						</command>
					</menuContribution>
					<menuContribution
						locationURI="popup:#TextEditorContext?after=group.open">
						<command commandId="org.eclipse.jdt.ui.edit.text.java.open.hierarchy"
							style="push"
							tooltip="Quick Type Hierarchy">
							<visibleWhen checkEnabled="false">
								<reference definitionId="«name».Editor.opened"/>
							</visibleWhen>
						</command>
					</menuContribution>
				</extension>
			'''
		}
		if (jdtCallHierarchy) {
			projectConfig.eclipsePlugin.pluginXml.entries += '''
				<!-- Call Hierachy -->
				<extension point="org.eclipse.ui.handlers">
					<handler
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.xbase.ui.hierarchy.OpenCallHierachyHandler"
						commandId="org.eclipse.xtext.xbase.ui.hierarchy.OpenCallHierarchy">
						<activeWhen>
							<reference
								definitionId="«name».Editor.opened">
							</reference>
						</activeWhen>
					</handler>
				</extension>
				<extension point="org.eclipse.ui.menus">
					<menuContribution
						locationURI="popup:#TextEditorContext?after=group.open">
						<command commandId="org.eclipse.xtext.xbase.ui.hierarchy.OpenCallHierarchy"
							style="push"
							tooltip="Open Call Hierarchy">
							<visibleWhen checkEnabled="false">
								<reference definitionId="«name».Editor.opened"/>
							</visibleWhen>
						</command>
					</menuContribution>
				</extension>
			'''
		}
		projectConfig.eclipsePlugin.pluginXml.entries += '''
			<extension point="org.eclipse.core.runtime.adapters">
				<factory class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.builder.smap.StratumBreakpointAdapterFactory"
					adaptableType="«grammar.eclipsePluginEditor.name»">
					<adapter type="org.eclipse.debug.ui.actions.IToggleBreakpointsTarget"/>
				</factory>
			</extension>
			<extension point="org.eclipse.ui.editorActions">
				<editorContribution targetID="«name»"
					id="«name».rulerActions">
					<action
						label="Not Used"
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate"
						style="push"
						actionID="RulerDoubleClick"
						id="«name».doubleClickBreakpointAction"/>
				</editorContribution>
			</extension>
			<extension point="org.eclipse.ui.popupMenus">
				<viewerContribution
					targetID="«name».RulerContext"
					id="«name».RulerPopupActions">
					<action
						label="Toggle Breakpoint"
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate"
						menubarPath="debug"
						id="«name».rulerContextMenu.toggleBreakpointAction">
					</action>
					<action
						label="Not used"
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.debug.ui.actions.RulerEnableDisableBreakpointActionDelegate"
						menubarPath="debug"
						id="«name».rulerContextMenu.enableDisableBreakpointAction">
					</action>
					<action
						label="Breakpoint Properties"
						helpContextId="breakpoint_properties_action_context"
						class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.jdt.debug.ui.actions.JavaBreakpointPropertiesRulerActionDelegate"
						menubarPath="group.properties"
						id="«name».rulerContextMenu.openBreapointPropertiesAction">
					</action>
				</viewerContribution>
			</extension>
			<!-- Introduce Local Variable Refactoring -->
			<extension point="org.eclipse.ui.handlers">
				<handler
					class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.xbase.ui.refactoring.ExtractVariableHandler"
					commandId="org.eclipse.xtext.xbase.ui.refactoring.ExtractLocalVariable">
					<activeWhen>
						<reference
							definitionId="«name».Editor.opened">
						</reference>
					</activeWhen>
				</handler>
			</extension>
			<extension point="org.eclipse.ui.menus">
				<menuContribution
					locationURI="popup:#TextEditorContext?after=group.edit">
					<command commandId="org.eclipse.xtext.xbase.ui.refactoring.ExtractLocalVariable"
						style="push">
						<visibleWhen checkEnabled="false">
							<reference
								definitionId="«name».Editor.opened">
							</reference>
						</visibleWhen>
					</command>
				</menuContribution>
			</extension>
			<!-- Open implementation -->
			<extension point="org.eclipse.ui.handlers">
				<handler
					class="«grammar.eclipsePluginExecutableExtensionFactory»:org.eclipse.xtext.xbase.ui.navigation.OpenImplementationHandler"
					commandId="org.eclipse.xtext.xbase.ui.OpenImplementationCommand">
					<activeWhen>
						<reference
							definitionId="«name».Editor.opened">
						</reference>
					</activeWhen>
				</handler>
			</extension>
			<extension point="org.eclipse.ui.menus">
				<menuContribution
					locationURI="menu:navigate?after=open.ext4">
					<command commandId="org.eclipse.xtext.xbase.ui.OpenImplementationCommand">
						<visibleWhen checkEnabled="false">
							<reference
								definitionId="«name».Editor.opened">
							</reference>
						</visibleWhen>
					</command>
				</menuContribution>
			</extension>
		'''
	}
	
}
