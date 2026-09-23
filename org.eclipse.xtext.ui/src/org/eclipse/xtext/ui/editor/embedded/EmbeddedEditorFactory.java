/*******************************************************************************
 * Copyright (c) 2011, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.embedded;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;
import org.eclipse.xtext.ui.editor.bracketmatching.BracketMatchingPreferencesInitializer;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocumentUtil;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.IssueModificationContext;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolution;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionProvider;
import org.eclipse.xtext.ui.editor.syntaxcoloring.HighlightingHelper;
import org.eclipse.xtext.ui.editor.validation.AnnotationIssueProcessor;
import org.eclipse.xtext.ui.editor.validation.IValidationIssueProcessor;
import org.eclipse.xtext.ui.editor.validation.ValidationJob;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Factory to create embedded editors for arbitrary {@link Resource resources}.
 * A typical usage pattern looks like this:
 * <pre>
 * EmbeddedEditorFactory editorFactory;
 * 
 * EmbeddedEditor editor = editorFactory
 *    .newEditor(resourceProvider)
 *    .showErrorAndWarningAnnotations()
 *    .withParent(parentComposite);
 * EmbeddedEditorModelAccess model = editor.createPartialEditor();
 * // work with the model 
 * </pre>
 * 
 * @since 2.2
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Christian Dietrich - Line number support
 */
public class EmbeddedEditorFactory {

	@Inject
	private Provider<Builder> builderProvider;
	
	public static class Builder {
		
		/** The width of the vertical ruler. */
		protected static final int VERTICAL_RULER_WIDTH = 12;
		
		@Inject
		protected XtextSourceViewer.Factory sourceViewerFactory;

		@Inject
		protected Provider<XtextSourceViewerConfiguration> sourceViewerConfigurationProvider;

		@Inject
		protected Provider<XtextDocument> documentProvider;

		@Inject
		protected Provider<IDocumentPartitioner> documentPartitionerProvider;

		@Inject
		protected IResourceValidator resourceValidator;

		@Inject
		protected IPreferenceStoreAccess preferenceStoreAccess;
		
		@Inject
		protected IssueResolutionProvider issueResolutionProvider;
		
		@Inject
		protected ICharacterPairMatcher characterPairMatcher;
		
		@Inject
		protected EmbeddedEditorActions.Factory actionFactory;
		
		@Inject
		protected HighlightingHelper highlightingHelper;
		
		/**
		 * @since 2.19
		 */
		@Inject
		protected XtextDocumentUtil xtextDocumentUtil;

		protected IEditedResourceProvider resourceProvider;
		protected String[] annotationTypes;
		protected Boolean lineNumbers;

		/**
		 * @since 2.19
		 */
		protected Color lineNumbersForegroundColor;

		/**
		 * @since 2.19
		 */
		protected Color lineNumbersBackgroundColor;

//		protected Boolean folding;
		protected Boolean readonly;
		protected boolean editorBuild;
		protected IValidationIssueProcessor issueProcessor;
		
		protected IOverviewRuler overviewRuler;
		protected boolean showsAnnotationOverview;

		/**
		 * @since 2.13
		 */
		protected int style = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
		
		public Builder showErrorAndWarningAnnotations() {
			return showAnnotations("org.eclipse.xtext.ui.editor.error", "org.eclipse.xtext.ui.editor.warning");
		}
		
		public Builder showAnnotations(String... annotationTypes) {
			if (this.annotationTypes != null)
				throw new IllegalStateException();
			if (annotationTypes == null)
				throw new IllegalArgumentException();
			this.annotationTypes = annotationTypes;
			return this;
		}
		
		/**
		 * Enables the line number column in the embedded editor
		 * 
		 * @since 2.11
		 */
		public Builder showLineNumbers() {
			if (lineNumbers != null)
				throw new IllegalStateException();
			this.lineNumbers = true;
			return this;
		}

		/**
		 * Enables the line number column in the embedded editor with customized foreground/background color.
		 * Use null for the default color. 
		 * 
		 * @since 2.19
		 */
		public Builder showLineNumbers(Color foregroundColor, Color backgroundColor) {
			showLineNumbers();
			this.lineNumbersForegroundColor = foregroundColor;
			this.lineNumbersBackgroundColor = backgroundColor;
			return this;
		}

		/**
		 * Enables the overview ruler to display errors/warnings/info marker beside the code.
		 * 
		 * @since 2.45
		 */
		public Builder showOverviewRuler() {
			if (overviewRuler != null) {
				throw new IllegalStateException();
			}

			this.overviewRuler = new OverviewRuler(createAnnotationAccess(), VERTICAL_RULER_WIDTH, getSharedColors());
			showsAnnotationOverview = true;

			return this;
		}

		public Builder processIssuesBy(IValidationIssueProcessor issueProcessor) {
			if (this.issueProcessor != null)
				throw new IllegalStateException();
			if (issueProcessor == null)
				throw new IllegalArgumentException();
			this.issueProcessor = issueProcessor;
			return this;
		}
//		public Builder enableFolding() {
//			if (folding != null)
//				throw new IllegalStateException();
//			this.folding = true;
//			return this;
//		}
		public Builder readOnly() {
			if (readonly != null)
				throw new IllegalStateException();
			this.readonly = true;
			return this;
		}
		public Builder withResourceValidator(IResourceValidator resourceValidator) {
			this.resourceValidator = resourceValidator;
			return this;
		}
		public EmbeddedEditor withParent(final Composite parent) {
			if (editorBuild)
				throw new IllegalStateException();
			editorBuild = true;
//			/*fProjectionSupport =*/installProjectionSupport(this.fSourceViewer);
			final CompositeRuler verticalRuler;
			if ((annotationTypes != null && annotationTypes.length != 0) || Boolean.TRUE.equals(lineNumbers)) {
				verticalRuler = new CompositeRuler();
			} else {
				verticalRuler = null;
			}
			final XtextSourceViewer viewer = this.sourceViewerFactory.createSourceViewer(
					parent, 
					verticalRuler, 
					overviewRuler,
					showsAnnotationOverview,
					style); // SWT styling
			final XtextSourceViewerConfiguration viewerConfiguration = this.sourceViewerConfigurationProvider.get();
			viewer.configure(viewerConfiguration);

			// squiggles for markers and other decorations
			final SourceViewerDecorationSupport viewerDecorationSupport = new SourceViewerDecorationSupport(
					viewer, 
					overviewRuler,
					createAnnotationAccess(),
					getSharedColors());
			MarkerAnnotationPreferences annotationPreferences = new MarkerAnnotationPreferences();
			Iterator<AnnotationPreference> e = Iterators.filter(annotationPreferences.getAnnotationPreferences().iterator(), AnnotationPreference.class);
			while (e.hasNext()) {
				viewerDecorationSupport.setAnnotationPreference(e.next());
			}
			if (characterPairMatcher != null) {
				viewerDecorationSupport.setCharacterPairMatcher(characterPairMatcher);
				viewerDecorationSupport.setMatchingCharacterPainterPreferenceKeys(BracketMatchingPreferencesInitializer.IS_ACTIVE_KEY,
						BracketMatchingPreferencesInitializer.COLOR_KEY);
			}
			viewerDecorationSupport.install(this.preferenceStoreAccess.getPreferenceStore());
			
			final XtextDocument document = this.documentProvider.get();
			IDocumentPartitioner partitioner = this.documentPartitionerProvider.get();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);

			final EmbeddedEditorActions actions = initializeActions(viewer);
			parent.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					viewerDecorationSupport.dispose();
					highlightingHelper.uninstall();
				}
			});
			final EmbeddedEditor result = new EmbeddedEditor(
					document, viewer, viewerConfiguration, resourceProvider, new Runnable() {
						@Override
						public void run() {
							afterCreatePartialEditor(viewer, document, verticalRuler, actions);
							highlightingHelper.install(viewerConfiguration, viewer);
						}
					});
			result.setXtextDocumentUtil(xtextDocumentUtil);
			viewer.setEditable(!Boolean.TRUE.equals(readonly));
			viewer.getContentAssistantFacade().addCompletionListener(new ICompletionListener() {
				
				private Button defaultButton;

				@Override
				public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
				}
				
				@Override
				public void assistSessionStarted(ContentAssistEvent event) {
					defaultButton = parent.getShell().getDefaultButton();
					parent.getShell().setDefaultButton(null);
				}
				
				@Override
				public void assistSessionEnded(ContentAssistEvent event) {
					parent.getShell().setDefaultButton(defaultButton);
					defaultButton = null;
				}
			});
			ValidationJob job = new ValidationJob(this.resourceValidator, document, new IValidationIssueProcessor() {
				private AnnotationIssueProcessor annotationIssueProcessor;

				@Override
				public void processIssues(List<Issue> issues, IProgressMonitor monitor) {
					IValidationIssueProcessor issueProcessor = Builder.this.issueProcessor;
					if (issueProcessor != null) {
						issueProcessor.processIssues(issues, monitor);
					}
					IAnnotationModel annotationModel = viewer.getAnnotationModel();
					if (annotationModel != null) {
						if (this.annotationIssueProcessor == null) {
							this.annotationIssueProcessor = new AnnotationIssueProcessor(document, annotationModel, new IssueResolutionProvider() {
	
								@Override
								public boolean hasResolutionFor(String issueCode) {
									return issueResolutionProvider.hasResolutionFor(issueCode);
								}
	
								@Override
								public List<IssueResolution> getResolutions(Issue issue) {
									List<IssueResolution> resolutions = issueResolutionProvider.getResolutions(issue);
									List<IssueResolution> result = Lists.transform(resolutions, new Function<IssueResolution, IssueResolution>() {
	
										@Override
										public IssueResolution apply(final IssueResolution input) {
											IssueResolution result = new IssueResolution(
													input.getLabel(), 
													input.getDescription(), 
													input.getImage(), 
													new IModificationContext() {
														@Override
														public IXtextDocument getXtextDocument(URI uri) {
															if (uri.trimFragment().equals(document.getResourceURI()))
																return document;
															return input.getModificationContext().getXtextDocument(uri);
														}
														
														@Override
														public IXtextDocument getXtextDocument() {
															IModificationContext original = input.getModificationContext();
															if (original instanceof IssueModificationContext) {
																URI uri = ((IssueModificationContext) original).getIssue().getUriToProblem();
																return getXtextDocument(uri);
															}
															return original.getXtextDocument();
														}
													}, 
													input.getModification());
											return result;
										}
									});
									return result;
								}
								
							});
						}
						if (this.annotationIssueProcessor != null) {
							this.annotationIssueProcessor.processIssues(issues, monitor);
						}
					}
				}
			}, CheckMode.FAST_ONLY);
			document.setValidationJob(job);

			Control control = viewer.getControl();
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			control.setLayoutData(data);
			return result;
		}
		
		/**
		 * Configures the SWT style of the embedded editor.
		 * @since 2.13
		 */
		public Builder withStyle(int style){
			this.style = style;
			return this;
		}
		
		protected void afterCreatePartialEditor(XtextSourceViewer viewer, XtextDocument document, CompositeRuler verticalRuler, 
				final EmbeddedEditorActions actions) {
			int rulerColumnCounter = 1;
			if (verticalRuler != null && annotationTypes != null && annotationTypes.length > 0) {
				AnnotationRulerColumn annotationRulerColumn = new AnnotationRulerColumn(viewer.getAnnotationModel(), VERTICAL_RULER_WIDTH, createAnnotationAccess());
				for(String annotationType: annotationTypes)
					annotationRulerColumn.addAnnotationType(annotationType);
				verticalRuler.addDecorator(rulerColumnCounter++, annotationRulerColumn);
			}
			if (verticalRuler != null && Boolean.TRUE.equals(lineNumbers)) {
				LineNumberRulerColumn lineNumberRulerColumn = new LineNumberRulerColumn();
				if (lineNumbersForegroundColor != null) {
					lineNumberRulerColumn.setForeground(lineNumbersForegroundColor);
				}
				if (lineNumbersBackgroundColor != null) {
					lineNumberRulerColumn.setBackground(lineNumbersBackgroundColor);
				}
				verticalRuler.addDecorator(rulerColumnCounter++, lineNumberRulerColumn);
			}

			final OperationHistoryListener listener = installUndoRedoSupport(viewer, document, actions);
			viewer.getControl().addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					uninstallUndoRedoSupport(listener);
				}
			});
			viewer.addTextListener(new ITextListener() {
				@Override
				public void textChanged(TextEvent event) {
					if (event.getDocumentEvent() != null) {
						actions.updateAllActions();
					}
				}
			});
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					actions.updateSelectionDependentActions();
				}
			});
			viewer.getUndoManager().reset();
		}

		protected void updateUndoAction(EmbeddedEditorActions actions) {
			actions.updateAction(ITextEditorActionConstants.UNDO);
		}

		protected void uninstallUndoRedoSupport(OperationHistoryListener listener) {
			IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			operationHistory.removeOperationHistoryListener(listener);
		}

		protected OperationHistoryListener installUndoRedoSupport(SourceViewer viewer, IDocument document, final EmbeddedEditorActions actions) {
			IDocumentUndoManager undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
			final IUndoContext context = undoManager.getUndoContext();
			IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			OperationHistoryListener operationHistoryListener = new OperationHistoryListener(context, new IUpdate() {
				@Override
				public void update() {
					actions.updateAction(ITextEditorActionConstants.REDO);
					actions.updateAction(ITextEditorActionConstants.UNDO);
				}
			});
			operationHistory.addOperationHistoryListener(operationHistoryListener);
			return operationHistoryListener;
		}

		protected IAnnotationAccess createAnnotationAccess() {
			return new DefaultMarkerAnnotationAccess() {
				@Override
				public int getLayer(Annotation annotation) {
					if (annotation.isMarkedDeleted()) {
						return IAnnotationAccessExtension.DEFAULT_LAYER;
					}
					return super.getLayer(annotation);
				}
			};
		}

		protected void setResourceProvider(IEditedResourceProvider resourceProvider) {
			this.resourceProvider = resourceProvider;
		}
		
		protected ISharedTextColors getSharedColors() {
			return EditorsUI.getSharedTextColors();
		}

		protected EmbeddedEditorActions initializeActions(final SourceViewer viewer) {
			return actionFactory.createActions(viewer);
		}
		
	}
	
	public Builder newEditor(IEditedResourceProvider resourceProvider) {
		if (resourceProvider == null)
			throw new IllegalArgumentException("resourceProvider may not be null");
		Builder result = getBuilderProvider().get();
		result.setResourceProvider(resourceProvider);
		return result;
	}

	protected Provider<Builder> getBuilderProvider() {
		return builderProvider;
	}

	public void setBuilderProvider(Provider<Builder> builderProvider) {
		this.builderProvider = builderProvider;
	}

}
