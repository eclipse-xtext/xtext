/*******************************************************************************
 * Copyright (c) 2023 Avaloq Evolution AG (http://www.avaloq.com).
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.parsetree.reconstr.impl.TokenUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * IdeContentProposalProvider that provides proposals by creating snippets from a language specific template file.
 * 
 * @author Andrew Lamb - Initial contribution and API
 * 
 * @since 2.31
 */
@Beta
public class TemplateIdeContentProposalProvider extends IdeContentProposalProvider {
	private static final String TEMPLATE_FILE = "templates.xml"; //$NON-NLS-1$
	private static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$
	private static final String CONTEXT_ATTRIBUTE = "context"; //$NON-NLS-1$
	private static final String COMMENT_CONTEXT = "org.eclipse.xtext.ide.CommentContext"; //$NON-NLS-1$

	@Inject
	private TokenUtil tokenUtil;

	@Inject
	@Named(Constants.LANGUAGE_NAME)
	private String languageName;

	@Inject
	private SnippetTemplateProposalProvider snippetProposalProvider;

	/**
	 * Simple class for holding the data for a template read from a template file.
	 */
	protected static class TemplateData {
		/** The name of this template. */
		private final String name;
		/** A description of this template. */
		private final String description;
		/** The name of the context type of this template. */
		private final String contextTypeId;
		/** The template pattern. */
		private final String pattern;

		/**
		 * Creates a new instance of {@link TemplateData}.
		 *
		 * @param name
		 *            the name of the template.
		 * @param description
		 *            a description of the template.
		 * @param contextTypeId
		 *            the name of the context type that the template applies to.
		 * @param pattern
		 *            the template pattern for the proposal.
		 */
		protected TemplateData(final String name, final String description, final String contextTypeId,
				final String pattern) {
			this.name = name;
			this.description = description;
			this.contextTypeId = contextTypeId;
			this.pattern = pattern;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getContextTypeId() {
			return contextTypeId;
		}

		public String getPattern() {
			return pattern;
		}
	}

	private final Map<String, List<TemplateData>> templates = new HashMap<>();
	private boolean loaded;

	/**
	 * Get the templates defined for the given context type.
	 *
	 * @param contextTypeId
	 *            the name of the context type to get the templates for.
	 * @return the list of templates that have been defined for the given context.
	 */
	protected List<TemplateData> getTemplatesForContextTypeId(final String contextTypeId) {
		if (!loaded) {
			loadTemplates();
		}

		return templates.getOrDefault(contextTypeId, Collections.emptyList());
	}

	/**
	 * Get the templates defined for the context determined by the given grammar element.
	 *
	 * @param element
	 *            the grammar element that specifies the current context.
	 * @return the list of templates that have been defined for the context that the element specifies.
	 */
	protected List<TemplateData> getTemplatesForElement(final AbstractElement element) {
		String contextTypeId = getContextTypeId(element);
		return getTemplatesForContextTypeId(contextTypeId);
	}

	/**
	 * Load the template data from the templates file into the templates map.
	 */
	protected synchronized void loadTemplates() {
		if (loaded) {
			return;
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			Document document;
			try (InputStream source = openTemplateFile()) {
				if (source == null) {
					// no templates for this dsl
					return;
				}
				document = parser.parse(source);
			}

			NodeList elements = document.getElementsByTagName(TEMPLATE_ELEMENT);

			int count = elements.getLength();
			for (int i = 0; i != count; i++) {
				Node node = elements.item(i);
				NamedNodeMap attributes = node.getAttributes();

				if (attributes == null) {
					continue;
				}

				String name = getStringValue(attributes, NAME_ATTRIBUTE);
				String context = getStringValue(attributes, CONTEXT_ATTRIBUTE);
				if (name == null || context == null) {
					throw new IOException("Required template attribute missing."); //$NON-NLS-1$
				}

				String description = getStringValue(attributes, DESCRIPTION_ATTRIBUTE, ""); //$NON-NLS-1$
				TemplateData template = makeTemplateData(name, description, context, node);
				templates.computeIfAbsent(context, c -> new ArrayList<>()).add(template);

			}
		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (SAXException | IOException e) {
			templates.clear();
			throw new WrappedException("Could not read template file", e); //$NON-NLS-1$
		} finally {
			loaded = true;
		}
	}

	private InputStream openTemplateFile() {
		String templatePlugin = getTemplateFilePlugin();
		if (templatePlugin == null) {
			return null;
		}
		try {
			for (Enumeration<URL> resources = getClass().getClassLoader().getResources(TEMPLATE_FILE); resources
					.hasMoreElements();) {
				URL url = resources.nextElement();
				if (url.getPath().contains(templatePlugin)) {
					return url.openStream();
				}
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	/**
	 * To support the possibility of there being multiple languages, get the name of the plugin containing the templates.xml
	 * file for the current language. Override if the template file is not in the ide plugin or the ide plugin does not have
	 * the default name.
	 * 
	 * @return the name of the plugin containing the templates file for the current language.
	 */
	protected String getTemplateFilePlugin() {
		// usually, template files would be kept with the ide plugin.
		if (languageName == null || !languageName.contains(".")) { //$NON-NLS-1$
			return null;
		}
		return languageName.substring(0, languageName.lastIndexOf('.')) + ".ide"; //$NON-NLS-1$
	}

	private String getStringValue(final NamedNodeMap attributes, final String name) throws SAXException {
		String val = getStringValue(attributes, name, null);
		if (val == null) {
			throw new SAXException("Missing attribute in template file"); //$NON-NLS-1$
		}
		return val;
	}

	private String getStringValue(final NamedNodeMap attributes, final String name, final String defaultValue) {
		Node node = attributes.getNamedItem(name);
		return node == null ? defaultValue : node.getNodeValue();
	}

	/**
	 * Construct a template data object for the specified xml node with the given attributes.
	 *
	 * @param name
	 *            the name attribute from the xml node.
	 * @param description
	 *            the description attribute from the xml node.
	 * @param context
	 *            the context attribute from the xml node.
	 * @param node
	 *            the xml node.
	 * @return the template data for the given xml node.
	 */
	protected TemplateData makeTemplateData(final String name, final String description, final String context,
			final Node node) {
		StringBuilder buffer = new StringBuilder();
		NodeList children = node.getChildNodes();
		for (int j = 0; j != children.getLength(); j++) {
			String value = children.item(j).getNodeValue();
			if (value != null) {
				buffer.append(value);
			}
		}
		String pattern = buffer.toString();

		return new TemplateData(name, description, context, pattern);
	}

	@Override
	public void createProposals(final Collection<ContentAssistContext> contexts,
			final IIdeContentProposalAcceptor acceptor) {
		for (ContentAssistContext context : getFilteredContexts(contexts)) {
			for (AbstractElement element : context.getFirstSetGrammarElements()) {
				if (!acceptor.canAcceptMoreProposals()) {
					return;
				}
				createElementProposals(element, context, acceptor);
			}
			createAdditionalProposals(context, acceptor);
		}
	}

	/**
	 * Get the name of the context type for the given grammar element.
	 *
	 * @param element
	 *            the element to get the context type for.
	 * @return the name of the context type, or {@code null} if the element does not have a context name.
	 */
	protected String getContextTypeId(final AbstractElement element) {
		if (element instanceof Keyword) {
			return GrammarUtil.getGrammar(element).getName() + ".kw_" + ((Keyword) element).getValue(); //$NON-NLS-1$
		}
		if (element instanceof Assignment) {
			return getContextTypeId(((Assignment) element).getTerminal());
		}
		if (element instanceof RuleCall) {
			AbstractRule rule = ((RuleCall) element).getRule();
			if (rule instanceof ParserRule) {
				return GrammarUtil.getGrammar(rule).getName() + "." + ((ParserRule) rule).getName(); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * Create content assist proposals for the given grammar element and pass them to the given acceptor.
	 *
	 * @param element
	 *            the grammar element to create proposals for.
	 * @param context
	 *            the content assist context.
	 * @param acceptor
	 *            the proposal acceptor.
	 */
	protected void createElementProposals(final AbstractElement element, final ContentAssistContext context,
			final IIdeContentProposalAcceptor acceptor) {
		createProposals(element, context, acceptor);
		createTemplates(element, context, acceptor);
	}

	/**
	 * Create the template content assist proposals for the given grammar element.
	 *
	 * @param element
	 *            the grammar element to create template proposals for.
	 * @param context
	 *            the content assist context.
	 * @param acceptor
	 *            the proposal acceptor.
	 */
	protected void createTemplates(final AbstractElement element, final ContentAssistContext context,
			final IIdeContentProposalAcceptor acceptor) {
		for (TemplateData template : getTemplatesForElement(element)) {
			if (!acceptor.canAcceptMoreProposals()) {
				return;
			}
			snippetProposalProvider.acceptProposal(template, context, acceptor);
		}
	}

	/**
	 * Create any additional content assist proposals not based on the grammar elements at the content assist context.
	 *
	 * @param context
	 *            the content assist context.
	 * @param acceptor
	 *            the proposal acceptor.
	 */
	protected void createAdditionalProposals(final ContentAssistContext context,
			final IIdeContentProposalAcceptor acceptor) {
		if (tokenUtil.isCommentNode(context.getCurrentNode())) {
			for (TemplateData template : getTemplatesForContextTypeId(COMMENT_CONTEXT)) {
				if (!acceptor.canAcceptMoreProposals()) {
					return;
				}
				snippetProposalProvider.acceptProposal(template, context, acceptor);
			}
		}
	}

	protected SnippetTemplateProposalProvider getSnippetProposalProvider() {
		return snippetProposalProvider;
	}
}
