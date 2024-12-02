/*******************************************************************************
 * Copyright (c) 2012, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.hover;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.HTML2TextReader;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IDelayedInputChangeProvider;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInputChangedListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
import org.eclipse.xtext.ui.editor.hover.html.IXtextBrowserInformationControl;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;
import org.eclipse.xtext.ui.editor.validation.IValidationIssueProcessor;
import org.eclipse.xtext.validation.Issue;

/**
 * @author Holger Schill - Initial contribution and API most of the code is copied from
 *{@link org.eclipse.jface.internal.text.html.BrowserInformationControl} due to visibility problems. The methods and
 *                                                                      fields that are modified are marked
 * @since 2.3
 */
@SuppressWarnings("restriction")
public class XbaseInformationControl extends AbstractInformationControl implements IInformationControlExtension2,
		IDelayedInputChangeProvider, IXtextBrowserInformationControl {

	/**
	 * Xbase Modifications
	 */
	private XtextBrowserInformationControlInput fInput;
	private Composite detailPaneComposite;
	private XbaseHoverConfiguration xbaseHoverConfiguration;
	private EmbeddedEditorModelAccess embeddedEditorAccess;
	private HoverEditedResourceProvider resourceProvider;
	private EmbeddedEditor embeddedEditor;
	private SashForm fSashForm;

	/**
	 * Xbase - modification Added XbaseHoverConfiguration because we do not have DI here
	 */
	public XbaseInformationControl(Shell parent, String symbolicFontName, String statusFieldText,
			XbaseHoverConfiguration xbaseHoverConfiguration) {
		super(parent, statusFieldText);
		fSymbolicFontName = symbolicFontName;
		this.xbaseHoverConfiguration = xbaseHoverConfiguration;
		create();
	}

	/**
	 * Xbase - modification Added XbaseHoverConfiguration because we do not have DI here
	 */
	public XbaseInformationControl(Shell parent, String symbolicFontName, ToolBarManager toolBarManager,
			XbaseHoverConfiguration xbaseHoverConfiguration) {
		super(parent, toolBarManager);
		fSymbolicFontName = symbolicFontName;
		this.xbaseHoverConfiguration = xbaseHoverConfiguration;
		create();
	}

	/**
	 * Xbase - modification+ added detailPane
	 */
	@Override
	protected void createContent(Composite parent) {
		fSashForm = new SashForm(parent, parent.getStyle());
		fSashForm.setOrientation(SWT.VERTICAL);
		fBrowser = new Browser(fSashForm, SWT.NONE);
		fBrowser.setJavascriptEnabled(false);
		Display display = getShell().getDisplay();
		ColorRegistry registry = JFaceResources.getColorRegistry();
		Color foreground= registry.get("org.eclipse.ui.workbench.HOVER_FOREGROUND"); //$NON-NLS-1$
		Color background= registry.get("org.eclipse.ui.workbench.HOVER_BACKGROUND"); //$NON-NLS-1$
		if (background != null && foreground != null) {
			fBrowser.setForeground(foreground);
			fBrowser.setBackground(background);
		} else {
			fBrowser.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			fBrowser.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		}
		fBrowser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				fCompleted = true;
			}
		});
		fBrowser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				event.required = true; // Cancel opening of new windows
			}
		}); 
		// Replace browser's built-in context menu with none
		fSashForm.setMenu(new Menu(getShell(), SWT.NONE));
		detailPaneComposite = createComposite(fSashForm, 1, 1, GridData.FILL_BOTH);
		Layout layout = detailPaneComposite.getLayout();
		if (layout instanceof GridLayout) {
			GridLayout gl = (GridLayout) layout;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			gl.numColumns = 1;
		}
		if (background != null && foreground != null) {
			detailPaneComposite.setForeground(foreground);
			detailPaneComposite.setBackground(background);
		} else {
			detailPaneComposite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			detailPaneComposite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		}
		resourceProvider = new HoverEditedResourceProvider();
		embeddedEditor = xbaseHoverConfiguration.getEditorFactory().newEditor(resourceProvider).readOnly()
				.processIssuesBy(new IValidationIssueProcessor() {
					@Override
					public void processIssues(List<Issue> issues, IProgressMonitor monitor) {
					}
				}).withParent(detailPaneComposite);
		Control viewerControl = embeddedEditor.getViewer().getControl();

		if (background != null && foreground != null) {
			viewerControl.setForeground(foreground);
			viewerControl.setBackground(background);
		} else {
			viewerControl.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			viewerControl.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		}
		embeddedEditor.getDocument().setValidationJob(null);
		createTextLayout();
		parent.addDisposeListener(e -> {
			if (fTextLayout != null && !fTextLayout.isDisposed()) {
				fTextLayout.dispose();
			}
		});
	}
	
	protected Composite createComposite(Composite parent, int columns, int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = true;
		g.setLayoutData(gd);
		return g;
	}

	/**
	 * Creates the synthetic resource for the embedded editor Allows to set the ClasspathContext afterwards
	 */
	public class HoverEditedResourceProvider implements IEditedResourceProvider {
		private XtextResourceSet xtextResourceSet;

		@Override
		public XtextResource createResource() {
			final String SYNTHETIC_SCHEME = "synthetic";
			xtextResourceSet = xbaseHoverConfiguration.createResourceSet();
			if (fInput != null) {
				XtextResourceSet resourceSet = (XtextResourceSet) fInput.getElement().eResource().getResourceSet();
				xtextResourceSet.setClasspathURIContext(resourceSet.getClasspathURIContext());
			}
			URI uri = URI.createURI(SYNTHETIC_SCHEME + ":/unsugaredExpression." + xbaseHoverConfiguration.getFileExtension());
			XtextResource resource = (XtextResource) xtextResourceSet.createResource(uri);
			xtextResourceSet.getResources().add(resource);
			return resource;

		}

		public void setContext(Object object) {
			xtextResourceSet.setClasspathURIContext(object);
		}
	}

	/**
	 * Xbase - modification added detailPane
	 */
	@Override
	public void setInput(Object input) {
		Assert.isLegal(input == null || input instanceof String || input instanceof XtextBrowserInformationControlInput, String.valueOf(input));

		if (input instanceof String) {
			setInformation((String) input);
			return;
		}
		if (input instanceof XtextBrowserInformationControlInput)
			fInput = (XtextBrowserInformationControlInput) input;

		String content = null;
		if (fInput != null)
			content = fInput.getHtml();

		fBrowserHasContent = content != null && content.length() > 0;

		if (!fBrowserHasContent)
			content = "<html><body ></html>"; //$NON-NLS-1$

		boolean RTL = (getShell().getStyle() & SWT.RIGHT_TO_LEFT) != 0;
		boolean resizable = isResizable();

		// The default "overflow:auto" would not result in a predictable width for the client area
		// and the re-wrapping would cause visual noise
		String[] styles = null;
		if (RTL && resizable)
			styles = new String[] { "direction:rtl;", "overflow:scroll;", "word-wrap:break-word;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else if (RTL && !resizable)
			styles = new String[] { "direction:rtl;", "overflow:hidden;", "word-wrap:break-word;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else if (!resizable)
			//XXX: In IE, "word-wrap: break-word;" causes bogus wrapping even in non-broken words :-(see e.g. Javadoc of String).
			// Re-check whether we really still need this now that the Javadoc Hover header already sets this style.
			styles = new String[] { "overflow:hidden;"/*, "word-wrap: break-word;"*/}; //$NON-NLS-1$
		else
			styles = new String[] { "overflow:scroll;" }; //$NON-NLS-1$

		StringBuilder builder = new StringBuilder(content);
		HTMLPrinter.insertStyles(builder, styles);
		content = builder.toString();

		/*
		 * XXX: Should add some JavaScript here that shows something like
		 * "(continued...)" or "..." at the end of the visible area when the page overflowed
		 * with "overflow:hidden;".
		 */

		fCompleted = false;
		fBrowser.setText(content);
		String unsugaredExpression = "";
		if (fInput != null && fInput instanceof XbaseInformationControlInput) {
			XbaseInformationControlInput castedInput = (XbaseInformationControlInput) fInput;
			unsugaredExpression = castedInput.getUnsugaredExpression();
			if(unsugaredExpression != null && unsugaredExpression.length() > 0){
				EObject element = fInput.getElement();
				if(element != null && element.eResource() != null && element.eResource().getResourceSet() != null){
					// FIXME: No arguments need when https://bugs.eclipse.org/bugs/show_bug.cgi?id=368827 is solved
					// THEN move to createContent as it was before
					if(embeddedEditorAccess == null)
						embeddedEditorAccess = embeddedEditor.createPartialEditor("", "INITIAL CONTENT", "", false);
					resourceProvider.setContext(((XtextResourceSet) element.eResource().getResourceSet()).getClasspathURIContext());
				} else
					return;
				embeddedEditorAccess.updateModel(castedInput.getPrefix() , unsugaredExpression ,castedInput.getSuffix());
			}
		}

		if (unsugaredExpression != null && unsugaredExpression.length() > 0)
			fSashForm.setWeights(new int[] { 7, 3 });
		else
			fSashForm.setWeights(new int[] { 10, 0 });
		Object[] listeners = fInputChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++)
			((IInputChangedListener) listeners[i]).inputChanged(fInput);
		
	}

	/**
	 * Xbase - modification added some space for the detailPane
	 */
	@Override
	public Point computeSizeHint() {
		Point sizeConstraints = getSizeConstraints();
		Rectangle trim = computeTrim();
		int height = trim.height;

		//FIXME: The HTML2TextReader does not render <p> like a browser.
		// Instead of inserting an empty line, it just adds a single line break.
		// Furthermore, the indentation of <dl><dd> elements is too small (e.g with a long @see line)
		TextPresentation presentation = new TextPresentation();
		HTML2TextReader reader = new HTML2TextReader(new StringReader(fInput.getHtml()), presentation);
		String text;
		try {
			text = reader.getString();
		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		}

		Rectangle bounds = computeBounds(sizeConstraints, trim, presentation, text);

		int minWidth = bounds.width;
		height = height + bounds.height;

		if(fInput instanceof XbaseInformationControlInput){
			String unsugaredExpression = ((XbaseInformationControlInput)fInput).getUnsugaredExpression();
			if(unsugaredExpression != null && unsugaredExpression.length() > 0){
				Rectangle detailBounds = computeBounds(sizeConstraints, trim, null, unsugaredExpression);
				minWidth = Math.max(minWidth, detailBounds.width);
				height += detailBounds.height + 30;
			}
		}

		// Add some air to accommodate for different browser renderings
		height += 15;
		minWidth += 70;
		
		
		// Apply max size constraints
		if (sizeConstraints != null) {
			if (sizeConstraints.x != SWT.DEFAULT)
				minWidth = Math.min(sizeConstraints.x, minWidth + trim.width);
			if (sizeConstraints.y != SWT.DEFAULT)
				height = Math.min(sizeConstraints.y, height);
		}

		// Ensure minimal size
		int width = Math.max(MIN_WIDTH, minWidth);
		height = Math.max(MIN_HEIGHT, height);
		return new Point(width, height);
	}
	/**
	 * Xbase - modification
	 * Reuse of bounds calculation
	 */
	private Rectangle computeBounds(Point sizeConstraints, Rectangle trim, TextPresentation presentation, String text) {
		fTextLayout.setText(text);
		fTextLayout.setWidth(sizeConstraints == null ? SWT.DEFAULT : sizeConstraints.x - trim.width);
		if (presentation != null) {
			@SuppressWarnings("rawtypes")
			Iterator iter = presentation.getAllStyleRangeIterator();
			while (iter.hasNext()) {
				StyleRange sr = (StyleRange) iter.next();
				if (sr.fontStyle == SWT.BOLD)
					fTextLayout.setStyle(fBoldStyle, sr.start, sr.start + sr.length - 1);
			}
		}
		Rectangle bounds = fTextLayout.getBounds(); // does not return minimum width, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217446
		int lineCount = fTextLayout.getLineCount();
		int textWidth = 0;
		for (int i = 0; i < lineCount; i++) {
			Rectangle rect = fTextLayout.getLineBounds(i);
			int lineWidth = rect.x + rect.width;
			if (i == 0)
				lineWidth += fInput.getLeadingImageWidth();
			textWidth = Math.max(textWidth, lineWidth);
		}
		bounds.width = textWidth;
		fTextLayout.setText(""); //$NON-NLS-1$
		return bounds;
	}

	/**
	 * Xbase - modification
	 */
	@Override
	public XtextBrowserInformationControlInput getInput() {
		return fInput;
	}

	/**
	 * Xbase - modification
	 */
	@Override
	public Point computeSizeConstraints(int widthInChars, int heightInChars) {
		if (fSymbolicFontName == null)
			return null;

		GC gc = new GC(fBrowser);
		Font font = JFaceResources.getFont(fSymbolicFontName);
		gc.setFont(font);
		double width = gc.getFontMetrics().getAverageCharacterWidth();
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();

		return new Point((int) (widthInChars * width), heightInChars * height);
	}
	//------------------ Original implementation
	/**
	 * Tells whether the SWT Browser widget and hence this information control is available.
	 * 
	 * @param parent
	 *            the parent component used for checking or <code>null</code> if none
	 * @return <code>true</code> if this control is available
	 */
	public static boolean isAvailable(Composite parent) {
		if (!fgAvailabilityChecked) {
			try {
				Browser browser = new Browser(parent, SWT.NONE);
				browser.dispose();

				fgIsAvailable = true;

				Slider sliderV = new Slider(parent, SWT.VERTICAL);
				Slider sliderH = new Slider(parent, SWT.HORIZONTAL);
				int width = sliderV.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				int height = sliderH.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
				fgScrollBarSize = new Point(width, height);
				sliderV.dispose();
				sliderH.dispose();
			} catch (SWTError er) {
				fgIsAvailable = false;
			} finally {
				fgAvailabilityChecked = true;
			}
		}

		return fgIsAvailable;
	}

	/**
	 * Minimal size constraints.
	 * 
	 * @since 3.2
	 */
	private static final int MIN_WIDTH = 80;

	private static final int MIN_HEIGHT = 50;

	/**
	 * Availability checking cache.
	 */
	private static boolean fgIsAvailable = false;

	private static boolean fgAvailabilityChecked = false;

	/**
	 * Cached scroll bar width and height
	 * 
	 * @since 3.4
	 */
	private static Point fgScrollBarSize;

	/** The control's browser widget */
	private Browser fBrowser;

	/** Tells whether the browser has content */
	private boolean fBrowserHasContent;

	/** Text layout used to approximate size of content when rendered in browser */
	private TextLayout fTextLayout;

	/** Bold text style */
	private TextStyle fBoldStyle;

	/**
	 * <code>true</code> if the browser has completed loading of the last input set via {@link #setInformation(String)}
	 * .
	 * 
	 * @since 3.4
	 */
	private boolean fCompleted = false;

	/**
	 * The listener to be notified when a delayed location changing event happened.
	 * 
	 * @since 3.4
	 */
	private IInputChangedListener fDelayedInputChangeListener;

	/**
	 * The listeners to be notified when the input changed.
	 * 
	 * @since 3.4
	 */
	private ListenerList<IInputChangedListener> fInputChangeListeners = new ListenerList<IInputChangedListener>(ListenerList.IDENTITY);

	/**
	 * The symbolic name of the font used for size computations, or <code>null</code> to use dialog font.
	 * 
	 * @since 3.4
	 */
	private final String fSymbolicFontName;
	
	private int fDisposeTimeout;

	private Runnable fDisposeTask;

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		Shell shell = getShell();

		if (!visible) {
			super.setVisible(false);
			setInput(null);
			startDisposeTimeout(shell.getDisplay());
			return;
		}
		
		if (shell.isVisible() == visible)
			return;

		/*
		 * The Browser widget flickers when made visible while it is not completely loaded.
		 * The fix is to delay the call to setVisible until either loading is completed
		 * (see ProgressListener in constructor), or a timeout has been reached.
		 */
		final Display display = shell.getDisplay();

		// Make sure the display wakes from sleep after timeout:
		display.timerExec(100, new Runnable() {
			@Override
			public void run() {
				fCompleted = true;
			}
		});

		while (!fCompleted) {
			// Drive the event loop to process the events required to load the browser widget's contents:
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell = getShell();
		if (shell == null || shell.isDisposed())
			return;

		/*
		 * Avoids flickering when replacing hovers, especially on Vista in ON_CLICK mode.
		 * Causes flickering on GTK. Carbon does not care.
		 */
		if ("win32".equals(SWT.getPlatform())) //$NON-NLS-1$
			shell.moveAbove(null);

		super.setVisible(true);
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControl#setSize(int, int)
	 */
	@Override
	public void setSize(int width, int height) {
		fSashForm.setRedraw(false); // avoid flickering
		try {
			super.setSize(width, height);
		} finally {
			fSashForm.setRedraw(true);
		}
	}

	/**
	 * Creates and initializes the text layout used to compute the size hint.
	 * 
	 * @since 3.2
	 */
	private void createTextLayout() {
		fTextLayout = new TextLayout(fSashForm.getDisplay());

		// Initialize fonts
		String symbolicFontName = fSymbolicFontName == null ? JFaceResources.DIALOG_FONT : fSymbolicFontName;
		Font font = JFaceResources.getFont(symbolicFontName);
		fTextLayout.setFont(font);
		fTextLayout.setWidth(-1);
		font = JFaceResources.getFontRegistry().getBold(symbolicFontName);
		fBoldStyle = new TextStyle(font, null, null);

		// Compute and set tab width
		fTextLayout.setText("    "); //$NON-NLS-1$
		int tabWidth = fTextLayout.getBounds().width;
		fTextLayout.setTabs(new int[] { tabWidth });
		fTextLayout.setText(""); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
	 */
	@Override
	public Rectangle computeTrim() {
		Rectangle trim = super.computeTrim();
		if (isResizable()) {
			boolean RTL = (getShell().getStyle() & SWT.RIGHT_TO_LEFT) != 0;
			if (RTL) {
				trim.x -= fgScrollBarSize.x;
			}
			trim.width += fgScrollBarSize.x;
			trim.height += fgScrollBarSize.y;
		}
		return trim;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the current location has changed or is
	 * about to change.
	 * 
	 * @param listener
	 *            the location listener
	 * @since 3.4
	 */
	@Override
	public void addLocationListener(LocationListener listener) {
		fBrowser.addLocationListener(listener);
	}

	/*
	 * @see IInformationControlExtension#hasContents()
	 */
	@Override
	public boolean hasContents() {
		return fBrowserHasContent;
	}

	/**
	 * Adds a listener for input changes to this input change provider. Has no effect if an identical listener is
	 * already registered.
	 * 
	 * @param inputChangeListener
	 *            the listener to add
	 * @since 3.4
	 */
	@Override
	public void addInputChangeListener(IInputChangedListener inputChangeListener) {
		Assert.isNotNull(inputChangeListener);
		fInputChangeListeners.add(inputChangeListener);
	}

	/**
	 * Removes the given input change listener from this input change provider. Has no effect if an identical listener
	 * is not registered.
	 * 
	 * @param inputChangeListener
	 *            the listener to remove
	 * @since 3.4
	 */
	public void removeInputChangeListener(IInputChangedListener inputChangeListener) {
		fInputChangeListeners.remove(inputChangeListener);
	}

	/*
	 * @see org.eclipse.jface.text.IDelayedInputChangeProvider#setDelayedInputChangeListener(org.eclipse.jface.text.IInputChangedListener)
	 * @since 3.4
	 */
	@Override
	public void setDelayedInputChangeListener(IInputChangedListener inputChangeListener) {
		fDelayedInputChangeListener = inputChangeListener;
	}

	/**
	 * Tells whether a delayed input change listener is registered.
	 * 
	 * @return <code>true</code> if a delayed input change listener is currently registered
	 * @since 3.4
	 */
	@Override
	public boolean hasDelayedInputChangeListener() {
		return fDelayedInputChangeListener != null;
	}

	/**
	 * Notifies listeners of a delayed input change.
	 * 
	 * @param newInput
	 *            the new input, or <code>null</code> to request cancellation
	 * @since 3.4
	 */
	@Override
	public void notifyDelayedInputChange(Object newInput) {
		if (fDelayedInputChangeListener != null)
			fDelayedInputChangeListener.inputChanged(newInput);
	}

	/*
	 * @see java.lang.Object#toString()
	 * @since 3.4
	 */
	@Override
	public String toString() {
		String style = (getShell().getStyle() & SWT.RESIZE) == 0 ? "fixed" : "resizeable"; //$NON-NLS-1$ //$NON-NLS-2$
		return super.toString() + " -  style: " + style; //$NON-NLS-1$
	}
	
	/**
	 * Sets a timeout, after which a not visible control will be disposed.
	 *
	 * @param disposeTimeout The timeout in milliseconds. Non-positive values result in no timeout,
	 *            i.e. don't dispose non-visible controls.
	 */
	@Override
	public void setDisposeTimeout(int disposeTimeout) {
		fDisposeTimeout= disposeTimeout;
	}

	private void startDisposeTimeout(Display display) {
		class DisposeTask implements Runnable {
			@Override
			public void run() {
				fDisposeTask= null;
				if (!isVisible()) {
					dispose();
				}
			}
		}
		if (fDisposeTimeout > 0 && fDisposeTask == null) {
			fDisposeTask= new DisposeTask();
			display.timerExec(fDisposeTimeout, fDisposeTask);
		}
	}

}
