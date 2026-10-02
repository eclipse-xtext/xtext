package org.eclipse.xtext.ide.editor.quickfix;

/*******************************************************************************
 * Copyright (c) 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

import jakarta.inject.Singleton;

/**
 * @author Heinrich Weichert
 *
 * @since 2.24
 */
@Beta
@Singleton
public class AbstractDeclarativeIdeQuickfixProvider implements IQuickFixProvider {

	private static final Logger LOG = Logger.getLogger(AbstractDeclarativeIdeQuickfixProvider.class);

	private final Map<String, List<Method>> methods = new ConcurrentHashMap<>();	
	private final Map<String, DiagnosticResolution> resolutionIds = new ConcurrentHashMap<>();

	@Inject
	private Provider<DiagnosticResolutionAcceptor> issueResolutionAcceptorProvider;

	private boolean getFixMethodPredicate(Method input, String issueCode) {
		for (QuickFix annotation : input.getAnnotationsByType(QuickFix.class)) {
			boolean result = annotation != null && Objects.equals(issueCode, annotation.value())
					&& input.getParameterTypes().length == 1 && Void.TYPE == input.getReturnType()
					&& input.getParameterTypes()[0].isAssignableFrom(DiagnosticResolutionAcceptor.class);
			if (result) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<DiagnosticResolution> getResolutions(Options options, Diagnostic diagnostic) {
		DiagnosticResolutionAcceptor issueResolutionAcceptor = issueResolutionAcceptorProvider.get();
		for (Method fixMethod : getFixMethods(diagnostic)) {
			try {
				// will throw if this is not a public method, but it should be
				fixMethod.invoke(this, issueResolutionAcceptor);
			} catch (Exception e) {
				LOG.error("Error executing fix method", e);
			}
		}
		return issueResolutionAcceptor.getDiagnosticResolutions(options, diagnostic);
	}

	private List<Method> collectMethods(Class<? extends AbstractDeclarativeIdeQuickfixProvider> clazz,
			String issueCode) {
		return Arrays.stream(clazz.getMethods()).filter(method -> getFixMethodPredicate(method, issueCode)).toList();
	}

	@Override
	public boolean handlesDiagnostic(Diagnostic diagnostic) {
		return !getFixMethods(diagnostic).isEmpty();
	}

	public List<Method> getFixMethods(Diagnostic diagnostic) {
		if (diagnostic == null || diagnostic.getCode() == null || diagnostic.getMessage() == null
				|| diagnostic.getSeverity() == null) {
			return Collections.emptyList();
		}
		String issueCode = diagnostic.getCode().getLeft();
		if (Strings.isNullOrEmpty(issueCode)) {
			return Collections.emptyList();
		}

		return methods.computeIfAbsent(issueCode, c -> this.collectMethods(getClass(), c));
	}

	/**
	 * Creates a singleton list with only one {@link TextEdit} that replaces the region of the diagnostic with the given
	 * text
	 * 
	 * @param diagnostic
	 *            the {@link Diagnostic}
	 * @param text
	 *            the text
	 * @return a singleton list with only one {@link TextEdit}
	 * @since 2.27
	 */
	protected List<TextEdit> createTextEdit(Diagnostic diagnostic, String text) {
		return Collections.singletonList(new TextEdit(diagnostic.getRange(), text));
	}

	@Override
	public WorkspaceEdit resolveResolution(DiagnosticResolutionInfo info) {
		DiagnosticResolution edit = resolutionIds.remove(info.getResolutionId());
		return edit == null ? new WorkspaceEdit() : edit.apply();
	}

	@Override
	public String cacheResolution(DiagnosticResolution resolution) {
		String id = UUID.randomUUID().toString();
		resolutionIds.put(id, resolution);
		return id;
	}
}
