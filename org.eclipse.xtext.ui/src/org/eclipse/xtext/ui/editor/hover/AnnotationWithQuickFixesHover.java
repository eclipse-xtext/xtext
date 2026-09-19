/*******************************************************************************
 * Copyright (c) 2010, 2017 Christoph Kulla
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Christoph Kulla - Initial API and implementation
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.hover;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.xtext.ui.XtextUIMessages;
import org.eclipse.xtext.ui.editor.quickfix.QuickAssistInvocationContext;
import org.eclipse.xtext.ui.editor.quickfix.XtextQuickAssistProcessor;

import com.google.inject.Inject;

/**
 * Hover which shows annotation and quick fixes. 
 * 
 * Clone from JDTs  org.eclipse.jdt.internal.ui.text.java.hover.AbstractAnnotationHover. 
 *
 * @author Christoph Kulla
 */

// Following differences exist between AbstractAnnotationHover and AnnotationWithQuickFixesHover:
//
// 1) AnnotationInfo provides proposals directly, no need of subclassing
// 2) Configuration of the hover content via preferences in the ui is not supported yet
// 3) List of Quickfixes is retrieved from an XtextQuickAssistProcessor
// 4) Removed access to JavaHoverMessages and replaced with XtextUIMessages
// 5) Dropped support of FixCorrectionProposal and ICleanUp (JDT concept)
// 6) Removed field fAnnotationAccess, DefaultMarkerAnnotationAccesss is not supported as we have no preferences
// 7) Removed field fAllAnnotations, no filtering of only Java Annotations required
// 8) instead of being an abstract class this a non abstract class

// All comments starting with DIFF describes differences between the two versions

// If no comments appear the code does not differ from AbstractAnnotationHover.

// DIFF: AnnotationWithQuickFixesHover is is not abstract (8)

public class AnnotationWithQuickFixesHover extends AbstractProblemHover {
	
	// DIFF: not part of AbstractAnnotationHover (3)
	
	@Inject
	private XtextQuickAssistProcessor quickAssistProcessor;

	// public to give access in tests
	public static class AnnotationInfo {
		public final Annotation annotation;
		public final Position position;
		public final ITextViewer viewer;
		// DIFF: not part of AbstractAnnotationHover (1)
		public final ICompletionProposal[] proposals;
		
		public AnnotationInfo(Annotation annotation, Position position, ITextViewer textViewer, ICompletionProposal[] proposals) {
			this.annotation= annotation;
			this.position= position;
			this.viewer= textViewer;
			// DIFF: not part of AbstractAnnotationHover(1)
			Assert.isNotNull(proposals);
			this.proposals = proposals;
		}

		/**
		 * Create completion proposals which can resolve the given annotation at
		 * the given position. Returns an empty array if no such proposals exist.
		 *
		 * @return the proposals or an empty array
		 */
		public ICompletionProposal[] getCompletionProposals() {
			// DIFF: return proposals directly, no subclassing (1)
			return proposals;
		}

		/**
		 * Adds actions to the given toolbar.
		 *
		 * @param manager the toolbar manager to add actions to
		 * @param infoControl the information control
		 */
		public void fillToolBar(ToolBarManager manager, IInformationControl infoControl) {
			// DIFF: disabled as configuration is not supported yet (2)
//			ConfigureAnnotationsAction configureAnnotationsAction= new ConfigureAnnotationsAction(annotation, infoControl);
//			manager.add(configureAnnotationsAction);
		}
	}

	private static class AnnotationInformationControl extends AbstractInformationControl implements IInformationControlExtension2 {

		private final DefaultMarkerAnnotationAccess fMarkerAnnotationAccess;
		private Control fFocusControl;
		private AnnotationInfo fInput;
		private Composite fParent;

		public AnnotationInformationControl(Shell parentShell, String statusFieldText) {
			super(parentShell, statusFieldText);
			fMarkerAnnotationAccess= new DefaultMarkerAnnotationAccess();
			create();
		}

		public AnnotationInformationControl(Shell parentShell, ToolBarManager toolBarManager) {
			super(parentShell, toolBarManager);
			
			fMarkerAnnotationAccess= new DefaultMarkerAnnotationAccess();
			create();
		}
		
		public AnnotationInformationControl(Shell parentShell, boolean resizeable) {
			super(parentShell, resizeable);
			
			fMarkerAnnotationAccess= new DefaultMarkerAnnotationAccess();
			create();
		}		

		/*
		 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
		 */
		@Override
		public void setInformation(String information) {
			//replaced by IInformationControlExtension2#setInput
		}
		
		@Override
		public void setInput(Object input) {
			Assert.isLegal(input instanceof AnnotationInfo);
			fInput= (AnnotationInfo)input;
			disposeDeferredCreatedContent();
			deferredCreateContent();
		}

		@Override
		public boolean hasContents() {
			return fInput != null;
		}

		private AnnotationInfo getAnnotationInfo() {
			return fInput;
		}

		@Override
		public void setFocus() {
			super.setFocus();
			if (fFocusControl != null)
				fFocusControl.setFocus();
		}

		@Override
		public final void setVisible(boolean visible) {
			if (!visible)
				disposeDeferredCreatedContent();
			super.setVisible(visible);
		}

		protected void disposeDeferredCreatedContent() {
			Control[] children= fParent.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].dispose();
			}
			ToolBarManager toolBarManager= getToolBarManager();
			if (toolBarManager != null)
				toolBarManager.removeAll();
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#createContent(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createContent(Composite parent) {
			fParent= parent;
			GridLayout layout= new GridLayout(1, false);
			layout.verticalSpacing= 0;
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			fParent.setLayout(layout);
		}

		@Override
		public Point computeSizeHint() {
			Point preferedSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

			Point constrains= getSizeConstraints();
			if (constrains == null)
				return preferedSize;

			Point constrainedSize= getShell().computeSize(constrains.x, SWT.DEFAULT, true);

			int width= Math.min(preferedSize.x, constrainedSize.x);
			int height= Math.max(preferedSize.y, constrainedSize.y);

			return new Point(width, height);
		}

		/**
		 * Fills the toolbar actions, if a toolbar is available. This
		 * is called after the input has been set.
		 */
		protected void fillToolbar() {
			ToolBarManager toolBarManager= getToolBarManager();
			if (toolBarManager == null)
				return;
			fInput.fillToolBar(toolBarManager, this);
			toolBarManager.update(true);
		}

		/**
		 * Create content of the hover. This is called after
		 * the input has been set.
		 */
		protected void deferredCreateContent() {
			fillToolbar();

			createAnnotationInformation(fParent, getAnnotationInfo().annotation);
			setColorAndFont(fParent, fParent.getForeground(), fParent.getBackground(), JFaceResources.getDialogFont());

			ICompletionProposal[] proposals= getAnnotationInfo().getCompletionProposals();
			if (proposals.length > 0)
				createCompletionProposalsControl(fParent, proposals);

			fParent.layout(true);
		}

		private void setColorAndFont(Control control, Color foreground, Color background, Font font) {
			control.setForeground(foreground);
			control.setBackground(background);
			control.setFont(font);

			if (control instanceof Composite) {
				Control[] children= ((Composite) control).getChildren();
				for (int i= 0; i < children.length; i++) {
					setColorAndFont(children[i], foreground, background, font);
				}
			}
		}

		private void createAnnotationInformation(Composite parent, final Annotation annotation) {
			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout= new GridLayout(2, false);
			layout.marginHeight= 2;
			layout.marginWidth= 2;
			layout.horizontalSpacing= 0;
			composite.setLayout(layout);

			final Canvas canvas= new Canvas(composite, SWT.NO_FOCUS);
			GridData gridData= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
			gridData.widthHint= 17;
			gridData.heightHint= 16;
			canvas.setLayoutData(gridData);
			canvas.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					e.gc.setFont(null);
					fMarkerAnnotationAccess.paint(annotation, e.gc, canvas, new Rectangle(0, 0, 16, 16));
				}
			});

			StyledText text = new StyledText(composite, SWT.V_SCROLL| SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);

			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			text.setLayoutData(data);
			String annotationText = annotation.getText();
			if (annotationText != null){
				text.setText(annotationText);
			}
		}

		private void createCompletionProposalsControl(Composite parent, ICompletionProposal[] proposals) {
			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			GridLayout layout2= new GridLayout(1, false);
			layout2.marginHeight= 0;
			layout2.marginWidth= 0;
			layout2.verticalSpacing= 2;
			composite.setLayout(layout2);

			Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridData gridData= new GridData(SWT.FILL, SWT.CENTER, true, false);
			separator.setLayoutData(gridData);

			Label quickFixLabel= new Label(composite, SWT.NONE);
			GridData layoutData= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.horizontalIndent= 4;
			quickFixLabel.setLayoutData(layoutData);
			String text;
			if (proposals.length == 1) {
				// DIFF: replaced JavaHoverMessages with XtextUIMessages (4)
				text= XtextUIMessages.AnnotationWithQuickFixesHover_message_singleQuickFix;
			} else {
				// DIFF: replaced JavaHoverMessages with XtextUIMessages (4)
				text= MessageFormat.format(XtextUIMessages.AnnotationWithQuickFixesHover_message_multipleQuickFix, new Object[] { String.valueOf(proposals.length) });
			}
			quickFixLabel.setText(text);

			setColorAndFont(composite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());
			createCompletionProposalsList(composite, proposals);
		}

		private void createCompletionProposalsList(Composite parent, ICompletionProposal[] proposals) {
			final ScrolledComposite scrolledComposite= new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
			scrolledComposite.setLayoutData(gridData);
			scrolledComposite.setExpandVertical(false);
			scrolledComposite.setExpandHorizontal(false);

			Composite composite= new Composite(scrolledComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout= new GridLayout(2, false);
			layout.marginLeft= 5;
			layout.verticalSpacing= 2;
			composite.setLayout(layout);
			
			List<Link> list= new ArrayList<Link>();
			for (int i= 0; i < proposals.length; i++) {
				list.add(createCompletionProposalLink(composite, proposals[i], 1));// Original link for single fix, hence pass 1 for count

				// DIFF: outcommented, no support of FixCorrectionProposal and ICleanUp (5)
//				if (proposals[i] instanceof FixCorrectionProposal) {
//					FixCorrectionProposal proposal= (FixCorrectionProposal)proposals[i];
//					int count= proposal.computeNumberOfFixesForCleanUp(proposal.getCleanUp());
//					if (count > 1) {
//						list.add(createCompletionProposalLink(composite, proposals[i], count));
//					}
//				}
			}
			final Link[] links= list.toArray(new Link[list.size()]);

			scrolledComposite.setContent(composite);
			setColorAndFont(scrolledComposite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());

			Point contentSize= composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			composite.setSize(contentSize);

			Point constraints= getSizeConstraints();
			if (constraints != null && contentSize.x < constraints.x) {
				ScrollBar horizontalBar= scrolledComposite.getHorizontalBar();

				int scrollBarHeight;
				if (horizontalBar == null) {
					Point scrollSize= scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					scrollBarHeight= scrollSize.y - contentSize.y;
				} else {
					scrollBarHeight= horizontalBar.getSize().y;
				}
				gridData.heightHint= contentSize.y - scrollBarHeight;
			}

			fFocusControl= links[0];
			for (int i= 0; i < links.length; i++) {
				final int index= i;
				final Link link= links[index];
				link.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
						switch (e.keyCode) {
							case SWT.ARROW_DOWN:
								if (index + 1 < links.length) {
									links[index + 1].setFocus();
								}
								break;
							case SWT.ARROW_UP:
								if (index > 0) {
									links[index - 1].setFocus();
								}
								break;
							default:
								break;
						}
					}

					@Override
					public void keyReleased(KeyEvent e) {
					}
				});

				link.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(FocusEvent e) {
						int currentPosition= scrolledComposite.getOrigin().y;
						int hight= scrolledComposite.getSize().y;
						int linkPosition= link.getLocation().y;

						if (linkPosition < currentPosition) {
							if (linkPosition < 10)
								linkPosition= 0;

							scrolledComposite.setOrigin(0, linkPosition);
						} else if (linkPosition + 20 > currentPosition + hight) {
							scrolledComposite.setOrigin(0, linkPosition - hight + link.getSize().y);
						}
					}

					@Override
					public void focusLost(FocusEvent e) {
					}
				});
			}
		}

		private Link createCompletionProposalLink(Composite parent, final ICompletionProposal proposal, int count) {
			final boolean isMultiFix= count > 1;
			if (isMultiFix) {
				new Label(parent, SWT.NONE); // spacer to fill image cell
				parent= new Composite(parent, SWT.NONE); // indented composite for multi-fix
				GridLayout layout= new GridLayout(2, false);
				layout.marginWidth= 0;
				layout.marginHeight= 0;
				parent.setLayout(layout);
			}
			
			Label proposalImage= new Label(parent, SWT.NONE);
			proposalImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			Image image= /*isMultiFix ? JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_MULTI_FIX) : */proposal.getImage();
			if (image != null) {
				proposalImage.setImage(image);

				proposalImage.addMouseListener(new MouseListener() {

					@Override
					public void mouseDoubleClick(MouseEvent e) {
					}

					@Override
					public void mouseDown(MouseEvent e) {
					}

					@Override
					public void mouseUp(MouseEvent e) {
						if (e.button == 1) {
							apply(proposal, fInput.viewer, fInput.position.offset, isMultiFix);
						}
					}

				});
			}

			Link proposalLink= new Link(parent, SWT.WRAP);
			GridData layoutData= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			String linkText;
			if (isMultiFix) {
				// DIFF: XtextUIMessages (4)
				linkText= MessageFormat.format(XtextUIMessages.AnnotationWithQuickFixesHover_message_multipleQuickFix, new Object[] { String.valueOf(count) });
			} else {
				linkText= proposal.getDisplayString();
			}
			proposalLink.setText("<a>" + linkText + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			proposalLink.setLayoutData(layoutData);
			proposalLink.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					apply(proposal, fInput.viewer, fInput.position.offset, isMultiFix);
				}
			});
			return proposalLink;
		}

		private void apply(ICompletionProposal p, ITextViewer viewer, int offset, boolean isMultiFix) {
			//Focus needs to be in the text viewer, otherwise linked mode does not work
			dispose();

			IRewriteTarget target= null;
			try {
				IDocument document= viewer.getDocument();

				if (viewer instanceof ITextViewerExtension) {
					ITextViewerExtension extension= (ITextViewerExtension) viewer;
					target= extension.getRewriteTarget();
				}

				if (target != null)
					target.beginCompoundChange();

				if (p instanceof ICompletionProposalExtension2) {
					ICompletionProposalExtension2 e= (ICompletionProposalExtension2) p;
					e.apply(viewer, (char) 0, isMultiFix ? SWT.CONTROL : SWT.NONE, offset);
				} else if (p instanceof ICompletionProposalExtension) {
					ICompletionProposalExtension e= (ICompletionProposalExtension) p;
					e.apply(document, (char) 0, offset);
				} else {
					p.apply(document);
				}

				Point selection= p.getSelection(document);
				if (selection != null) {
					viewer.setSelectedRange(selection.x, selection.y);
					viewer.revealRange(selection.x, selection.y);
				}
			} finally {
				if (target != null)
					target.endCompoundChange();
			}
		}
	}

	private static final class PresenterControlCreator extends AbstractReusableInformationControlCreator {

		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			// DIFF: do not show toolbar in hover, no configuration supported (2)
			// return new AnnotationInformationControl(parent, new ToolBarManager(SWT.FLAT));
			return new AnnotationInformationControl(parent, true);
		}
	}

	private static final class HoverControlCreator extends AbstractReusableInformationControlCreator {
		private final IInformationControlCreator fPresenterControlCreator;

		public HoverControlCreator(IInformationControlCreator presenterControlCreator) {
			fPresenterControlCreator= presenterControlCreator;
		}

		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			return new AnnotationInformationControl(parent, EditorsUI.getTooltipAffordanceString()) {

				@Override
				public IInformationControlCreator getInformationPresenterControlCreator() {
					return fPresenterControlCreator;
				}
			};
		}

		@Override
		public boolean canReuse(IInformationControl control) {
			if (!super.canReuse(control))
				return false;

			if (control instanceof IInformationControlExtension4)
				((IInformationControlExtension4) control).setStatusText(EditorsUI.getTooltipAffordanceString());

			return true;
		}
	}

	// DIFF: added this Runnable to execute quickAssistProposals to retrieve proposal list (3)
	
	private final class CompletionProposalRunnable implements Runnable {
		private final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
		ICompletionProposal[] proposals = NO_PROPOSALS;
		IQuickAssistInvocationContext invocationContext;
		
		public CompletionProposalRunnable(IQuickAssistInvocationContext invocationContext) {
			this.invocationContext = invocationContext;
		}

		@Override
		public void run() {
			ICompletionProposal[] p = quickAssistProcessor.computeQuickAssistProposals(invocationContext);
			proposals = p != null ? p : NO_PROPOSALS;
		}
	}
	
	// DIFF: outcommented, no configuration supported (3)
	
//	/**
//	 * Action to configure the annotation preferences.
//	 *
//	 * @since 3.4
//	 */
//	private static final class ConfigureAnnotationsAction extends Action {
//
//		private final Annotation fAnnotation;
//		private final IInformationControl fInfoControl;
//
//		public ConfigureAnnotationsAction(Annotation annotation, IInformationControl infoControl) {
//			super();
//			fAnnotation= annotation;
//			fInfoControl= infoControl;
//			setImageDescriptor(JavaPluginImages.DESC_ELCL_CONFIGURE_ANNOTATIONS);
//			setDisabledImageDescriptor(JavaPluginImages.DESC_DLCL_CONFIGURE_ANNOTATIONS);
//			setToolTipText(JavaHoverMessages.AbstractAnnotationHover_action_configureAnnotationPreferences);
//		}
//
//		/*
//		 * @see org.eclipse.jface.action.Action#run()
//		 */
//		@Override
//		public void run() {
//			Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//
//			Object data= null;
//			AnnotationPreference preference= getAnnotationPreference(fAnnotation);
//			if (preference != null)
//				data= preference.getPreferenceLabel();
//
//			fInfoControl.dispose(); //FIXME: should have protocol to hide, rather than dispose
//			PreferencesUtil.createPreferenceDialogOn(shell, "org.eclipse.ui.editors.preferencePages.Annotations", null, data).open(); //$NON-NLS-1$
//		}
//	}

//	private final IPreferenceStore fStore= JavaPlugin.getDefault().getCombinedPreferenceStore();
	// DIFF: (6)
//	private final DefaultMarkerAnnotationAccess fAnnotationAccess= new DefaultMarkerAnnotationAccess();
	// DIFF: (7)
//	private final boolean fAllAnnotations;
	
	/**
	 * The hover control creator.
	 *
	 * @since 3.4
	 */
	private IInformationControlCreator fHoverControlCreator;
	/**
	 * The presentation control creator.
	 *
	 * @since 3.4
	 */
	private IInformationControlCreator fPresenterControlCreator;

	private volatile AnnotationInfo recentAnnotationInfo;
	
	// DIFF: this code is entirely different, as the hover subclasses from AbstractProblemHover and 
	// hooks different methods
	
	@Override
	protected Region getHoverRegionInternal(final int lineNumber, final int offset) {
		recentAnnotationInfo = null;
		List<Annotation> annotations = getAnnotations(lineNumber, offset);
		for (Annotation annotation : sortBySeverity(annotations)) {
			Position position = sourceViewer.getAnnotationModel().getPosition(annotation);
			if (position != null) {
				final int start = position.getOffset();
				return new Region(start, position.getLength());	
			}
		}
		return null;
	}

	// DIFF: this code is entirely different, as the hover subclasses from AbstractProblemHover and 
	// hooks different methods
	
	@Override
	protected Object getHoverInfoInternal(ITextViewer textViewer, final int lineNumber, final int offset) {
		AnnotationInfo result = recentAnnotationInfo;
		if (result != null)
			return result;
		List<Annotation> annotations = getAnnotations(lineNumber, offset);
		for (Annotation annotation : sortBySeverity(annotations)) {
			Position position = getAnnotationModel().getPosition(annotation);
			if (annotation.getText() != null && position != null) {
				final QuickAssistInvocationContext invocationContext = new QuickAssistInvocationContext(sourceViewer, position.getOffset(), position.getLength(), true);
				CompletionProposalRunnable runnable = new CompletionProposalRunnable(invocationContext);
				// Note: the resolutions have to be retrieved from the UI thread, otherwise
				// workbench.getActiveWorkbenchWindow() will return null in LanguageSpecificURIEditorOpener and
				// cause an exception
				Display.getDefault().syncExec(runnable);
				if (invocationContext.isMarkedCancelled()) {
					return null;
				}
				result = new AnnotationInfo(annotation, position, sourceViewer, runnable.proposals);
				recentAnnotationInfo = result;
				return result;
			}
		}
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fHoverControlCreator == null)
			fHoverControlCreator= new HoverControlCreator(getInformationPresenterControlCreator());
		return fHoverControlCreator;
	}

	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fPresenterControlCreator == null)
			fPresenterControlCreator= new PresenterControlCreator();
		return fPresenterControlCreator;
	}

	// DIFF: not required, get AnnotationModel via getAnnotationModel and not from the 
	// file buffer manager
	
//	private IPath getEditorInputPath() {
//		if (getEditor() == null)
//			return null;
//
//		IEditorInput input= getEditor().getEditorInput();
//		if (input instanceof IStorageEditorInput) {
//			try {
//				return ((IStorageEditorInput)input).getStorage().getFullPath();
//			} catch (CoreException ex) {
//				JavaPlugin.log(ex.getStatus());
//			}
//		}
//		return null;
//	}
//	private IAnnotationModel getAnnotationModel(IPath path) {
//		if (path == null)
//			return null;
//
//		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
//		try {
//			manager.connect(path, LocationKind.NORMALIZE, null);
//		} catch (CoreException ex) {
//			JavaPlugin.log(ex.getStatus());
//			return null;
//		}
//
//		IAnnotationModel model= null;
//		try {
//			model= manager.getTextFileBuffer(path, LocationKind.NORMALIZE).getAnnotationModel();
//			return model;
//		} finally {
//			if (model == null) {
//				try {
//					manager.disconnect(path, LocationKind.NORMALIZE, null);
//				} catch (CoreException ex) {
//					JavaPlugin.log(ex.getStatus());
//				}
//			}
//		}
//	}
	
	// DIFF: not required, as preferences are not supported (2)
//	/**
//	 * Returns the annotation preference for the given annotation.
//	 *
//	 * @param annotation the annotation
//	 * @return the annotation preference or <code>null</code> if none
//	 */
//	private static AnnotationPreference getAnnotationPreference(Annotation annotation) {
//
//		if (annotation.isMarkedDeleted())
//			return null;
//		return EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
//	}
}
