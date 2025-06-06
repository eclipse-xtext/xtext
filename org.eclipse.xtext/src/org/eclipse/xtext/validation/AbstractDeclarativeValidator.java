/*******************************************************************************
 * Copyright (c) 2008, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Exceptions;
import org.eclipse.xtext.util.SimpleCache;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Allows subclasses to specify invariants in a declarative manner using {@link Check} annotation.
 * 
 * Example:
 * 
 * <pre>
 * &#064;Check
 * void checkName(ParserRule rule) {
 * 	if (!toFirstUpper(rule.getName()).equals(rule.getName())) {
 * 		warning(&quot;Name should start with a capital.&quot;, XtextPackage.Literals.ABSTRACT_RULE__NAME);
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * By default {@link NullPointerException NullPointerExceptions} occurring in the invocation of validation code are swallowed. 
 * This behavior can be switched by overriding {@link #handleExceptionDuringValidation(Throwable)}.
 * </p>
 * 
 * @author Sven Efftinge - Initial contribution and API
 * @author Michael Clay
 * @author Karsten Thoms
 */
public abstract class AbstractDeclarativeValidator extends AbstractInjectableValidator implements
		ValidationMessageAcceptor {

	@Inject
	private IssueSeveritiesProvider issueSeveritiesProvider;

	private static final Logger log = Logger.getLogger(AbstractDeclarativeValidator.class);

	private static final GuardException guardException = new GuardException();

	public static class StateAccess {

		private AbstractDeclarativeValidator validator;

		private StateAccess(AbstractDeclarativeValidator validator) {
			this.validator = validator;
		}

		public State getState() {
			State result = validator.state.get();
			if (result == null) {
				result = new State();
				validator.state.set(result);
			}
			return result;
		}

	}

	/**
	 * @since 2.6
	 */
	protected static class MethodWrapper {
		private final Method method;
		private final String s;
		private final AbstractDeclarativeValidator instance;
		private final CheckType checkType;

		protected MethodWrapper(AbstractDeclarativeValidator instance, Method m) {
			this.instance = instance;
			this.method = m;
			this.s = m.getName() + ":" + m.getParameterTypes()[0].getName();
			Check annotation = m.getAnnotation(Check.class);
			checkType = annotation.value();
		}

		@Override
		public int hashCode() {
			return s.hashCode() ^ instance.hashCode();
		}

		public boolean isMatching(Class<?> param) {
			return method.getParameterTypes()[0].isAssignableFrom(param);
		}

		public void invoke(State state) {
			State instanceState = instance.state.get();
			if (instanceState != null && instanceState != state)
				throw new IllegalStateException("State is already assigned.");
			boolean wasNull = instanceState == null;
			if (wasNull)
				instance.state.set(state);
			try {
				if (!state.checkMode.shouldCheck(checkType))
					return;
				try {
					state.currentMethod = method;
					state.currentCheckType = checkType;
					method.setAccessible(true);
					method.invoke(instance, state.currentObject);
				} catch (IllegalArgumentException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					Throwable targetException = e.getTargetException();
					handleInvocationTargetException(targetException, state);
				}
			} finally {
				if (wasNull)
					instance.state.remove();
			}
		}
		
		protected void handleInvocationTargetException(Throwable targetException, State state) {
			instance.handleExceptionDuringValidation(targetException);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodWrapper))
				return false;
			MethodWrapper mw = (MethodWrapper) obj;
			return s.equals(mw.s) && instance == mw.instance;
		}
		
		public AbstractDeclarativeValidator getInstance() {
			return instance;
		}
		
		public Method getMethod() {
			return method;
		}
	}

	private volatile Set<MethodWrapper> checkMethods = null;

	private ValidationMessageAcceptor messageAcceptor;

	@Inject
	private Injector injector;

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	public AbstractDeclarativeValidator() {
		this.state = new ThreadLocal<State>();
		this.messageAcceptor = this;
	}

	private List<MethodWrapper> collectMethods(Class<? extends AbstractDeclarativeValidator> clazz) {
		List<MethodWrapper> checkMethods = new ArrayList<MethodWrapper>();
		Set<Class<?>> visitedClasses = new HashSet<Class<?>>(4);
		collectMethods(this, clazz, visitedClasses, checkMethods);
		return checkMethods;
	}

	private void collectMethods(AbstractDeclarativeValidator instance,
			Class<? extends AbstractDeclarativeValidator> clazz, Collection<Class<?>> visitedClasses,
			Collection<MethodWrapper> result) {
		if (visitedClasses.contains(clazz))
			return;
		collectMethodsImpl(instance, clazz, visitedClasses, result);
		Class<? extends AbstractDeclarativeValidator> k = clazz;
		while (k != null) {
			ComposedChecks checks = k.getAnnotation(ComposedChecks.class);
			if (checks != null) {
				for (Class<? extends AbstractDeclarativeValidator> external : checks.validators())
					collectMethods(null, external, visitedClasses, result);
			}
			k = getSuperClass(k);
		}
	}

	private Class<? extends AbstractDeclarativeValidator> getSuperClass(
			Class<? extends AbstractDeclarativeValidator> clazz) {
		try {
			Class<? extends AbstractDeclarativeValidator> superClass = clazz.getSuperclass().asSubclass(
					AbstractDeclarativeValidator.class);
			if (AbstractDeclarativeValidator.class.equals(superClass))
				return null;
			return superClass;
		} catch (ClassCastException e) {
			return null;
		}
	}

	private void collectMethodsImpl(AbstractDeclarativeValidator instance,
			Class<? extends AbstractDeclarativeValidator> clazz, Collection<Class<?>> visitedClasses,
			Collection<MethodWrapper> result) {
		if (!visitedClasses.add(clazz))
			return;
		AbstractDeclarativeValidator instanceToUse;
		instanceToUse = instance;
		if (instanceToUse == null) {
			instanceToUse = newInstance(clazz);
		}
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getAnnotation(Check.class) != null && method.getParameterTypes().length == 1) {
				result.add(createMethodWrapper(instanceToUse, method));
			}
		}
		Class<? extends AbstractDeclarativeValidator> superClass = getSuperClass(clazz);
		if (superClass != null)
			collectMethodsImpl(instanceToUse, superClass, visitedClasses, result);
	}

	/**
	 * @since 2.6
	 */
	protected MethodWrapper createMethodWrapper(AbstractDeclarativeValidator instanceToUse, Method method) {
		return new MethodWrapper(instanceToUse, method);
	}

	protected AbstractDeclarativeValidator newInstance(Class<? extends AbstractDeclarativeValidator> clazz) {
		AbstractDeclarativeValidator instanceToUse;
		if (injector == null)
			throw new IllegalStateException("the class is not configured with an injector.");
		instanceToUse = injector.getInstance(clazz);
		return instanceToUse;
	}

	private final SimpleCache<Class<?>, List<MethodWrapper>> methodsForType = new SimpleCache<Class<?>, List<MethodWrapper>>(
			new Function<Class<?>, List<MethodWrapper>>() {
				@Override
				public List<MethodWrapper> apply(Class<?> param) {
					List<MethodWrapper> result = new ArrayList<MethodWrapper>();
					for (MethodWrapper mw : checkMethods) {
						if (mw.isMatching(param))
							result.add(mw);
					}
					return result;
				}
			});

	public static class State {
		public DiagnosticChain chain = null;
		public EObject currentObject = null;
		public Method currentMethod = null;
		public CheckMode checkMode = null;
		public CheckType currentCheckType = null;
		public boolean hasErrors = false;
		public Map<Object, Object> context;
	}

	private final ThreadLocal<State> state;

	protected EObject getCurrentObject() {
		return state.get().currentObject;
	}

	protected Method getCurrentMethod() {
		return state.get().currentMethod;
	}

	protected DiagnosticChain getChain() {
		return state.get().chain;
	}

	protected CheckMode getCheckMode() {
		return state.get().checkMode;
	}

	protected Map<Object, Object> getContext() {
		return state.get().context;
	}
	
	/**
	 * Obtain a cancel indicator that is valid for the current validation run. 
	 * 
	 * @since 2.22
	 */
	protected CancelIndicator getCancelIndicator() {
		Map<Object, Object> context = getContext();
		if (context == null) {
			return CancelIndicator.NullImpl;
		}
		CancelIndicator result = (CancelIndicator) context.get(CancelableDiagnostician.CANCEL_INDICATOR);
		if (result == null) {
			return CancelIndicator.NullImpl;
		}
		return result;
	}

	@Override
	protected final boolean internalValidate(EClass class1, EObject object, DiagnosticChain diagnostics,
			Map<Object, Object> context) {
		initializeCheckMethods();
		
		CheckMode checkMode = CheckMode.getCheckMode(context);

		State state = new State();
		state.chain = diagnostics;
		state.currentObject = object;
		state.checkMode = checkMode;
		state.context = context;

		for (MethodWrapper method : getMethodsForType(object.getClass())) {
			method.invoke(state);
		}

		return !state.hasErrors;
	}

	/**
	 * This method can be called to initialize the cache. The initialization of the cache will 
	 * improve the validation performance, for the cases where the validation is executed in many threads.
	 * The caching of non initialized types are blocking all other reading threads. After initialization
	 * the readWriteLock won't block any accessing threads.
	 * 
	 * To initialize the cache, this method has to be called with every Class (and its subtypes) which is handled
	 * by this validator.
	 * 
	 * If its called internally it doesn't have to be overriden/exposed. 
	 * If its wanted to be called outside of the Validator it can be overriden and exposed.
	 * 
	 * @since 2.37
	 */
	protected List<MethodWrapper> getMethodsForType(Class<?> object) {
		return methodsForType.get(object);
	}

	/**
	 * This has to be called if the methodsForType is called to initialize the cache. 
	 * For the normal use case it will be called internally as before. 
	 *  
	 * @since 2.37
	 */
	protected void initializeCheckMethods() {
		if (checkMethods == null) {
			synchronized (this) {
				if (checkMethods == null) {
					Set<MethodWrapper> checkMethods = Sets.newLinkedHashSet();
					checkMethods.addAll(collectMethods(getClass()));
					this.checkMethods = checkMethods;
				}
			}
		}
	}

	////////////////////////////
	// Convenience methods below
	////////////////////////////

	protected void info(String message, EStructuralFeature feature) {
		info(message, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
	}

	protected void info(String message, EStructuralFeature feature, int index) {
		info(message, feature, index, null);
	}

	protected void info(String message, EStructuralFeature feature, int index, String code, String... issueData) {
		info(message, state.get().currentObject, feature, index, code, issueData);
	}

	/**
	 * @since 2.0
	 */
	protected void info(String message, EStructuralFeature feature, String code, String... issueData) {
		info(message, state.get().currentObject, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code,
				issueData);
	}
	
	/**
	 * @since 2.4
	 */
	protected void info(String message, EObject source, EStructuralFeature feature) {
		info(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
	}

	protected void info(String message, EObject source, EStructuralFeature feature, int index) {
		info(message, source, feature, index, null);
	}
	
	/**
	 * @since 2.4
	 */
	protected void info(String message, EObject source, EStructuralFeature feature, int index, String code,
			String... issueData) {
		getMessageAcceptor().acceptInfo(message, source, feature, index, code, issueData);
	}

	/**
	 * @since 2.4
	 */
	protected void info(String message, EObject source, EStructuralFeature feature, String code,
			String... issueData) {
		getMessageAcceptor().acceptInfo(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
	}

	protected void warning(String message, EStructuralFeature feature) {
		warning(message, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
	}

	protected void warning(String message, EStructuralFeature feature, int index) {
		warning(message, feature, index, null);
	}

	protected void warning(String message, EStructuralFeature feature, String code, String... issueData) {
		warning(message, state.get().currentObject, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code,
				issueData);
	}

	protected void warning(String message, EStructuralFeature feature, int index, String code, String... issueData) {
		warning(message, state.get().currentObject, feature, index, code, issueData);
	}

	/**
	 * @since 2.4
	 */
	protected void warning(String message, EObject source, EStructuralFeature feature) {
		warning(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}

	protected void warning(String message, EObject source, EStructuralFeature feature, int index) {
		warning(message, source, feature, index, null);
	}

	protected void warning(String message, EObject source, EStructuralFeature feature, int index, String code,
			String... issueData) {
		getMessageAcceptor().acceptWarning(message, source, feature, index, code, issueData);
	}

	protected void warning(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
		getMessageAcceptor().acceptWarning(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
				code, issueData);
	}

	protected void error(String message, EStructuralFeature feature) {
		error(message, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
	}

	protected void error(String message, EStructuralFeature feature, int index) {
		error(message, feature, index, null);
	}

	protected void error(String message, EStructuralFeature feature, String code, String... issueData) {
		error(message, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
	}

	protected void error(String message, EStructuralFeature feature, int index, String code, String... issueData) {
		error(message, state.get().currentObject, feature, index, code, issueData);
	}

	/**
	 * @since 2.4
	 */
	protected void error(String message, EObject source, EStructuralFeature feature) {
		error(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}

	protected void error(String message, EObject source, EStructuralFeature feature, int index) {
		error(message, source, feature, index, null);
	}

	protected void error(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
		getMessageAcceptor().acceptError(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code,
				issueData);
	}

	protected void error(String message, EObject source, EStructuralFeature feature, int index, String code,
			String... issueData) {
		getMessageAcceptor().acceptError(message, source, feature, index, code, issueData);
	}

	/**
	 * @since 2.4
	 */
	protected void addIssueToState(String issueCode, String message, EStructuralFeature feature) {
		addIssue(message, state.get().currentObject, feature, issueCode, (String[]) null);
	}
	
	/**
	 * @since 2.4
	 */
	protected void addIssue(String message, EObject source, String issueCode) {
		addIssue(message, source, null, issueCode, (String[]) null);
	}

	/**
	 * @since 2.4
	 */
	protected void addIssue(String message, EObject source, EStructuralFeature feature, String issueCode,
			String... issueData) {
		addIssue(message, source, feature, INSIGNIFICANT_INDEX, issueCode, issueData);
	}

	/**
	 * @since 2.4
	 */
	protected void addIssue(String message, EObject source, EStructuralFeature feature, int index, String issueCode,
			String... issueData) {
		Severity severity = getIssueSeverities(getContext(), getCurrentObject()).getSeverity(issueCode);
		if (severity != null) {
			switch (severity) {
				case WARNING:
					getMessageAcceptor().acceptWarning(message, source, feature, index, issueCode, issueData);
					break;
				case INFO:
					getMessageAcceptor().acceptInfo(message, source, feature, index, issueCode, issueData);
					break;
				case ERROR:
					getMessageAcceptor().acceptError(message, source, feature, index, issueCode, issueData);
					break;
				default:
					break;
			}
		}
	}
	/**
	 * @since 2.4
	 */
	protected void addIssue(String message, EObject source, int offset,  int length, String issueCode){
		addIssue(message, source, offset, length, issueCode, (String[])null);
	}
	
	/**
	 * @since 2.4
	 */
	protected void addIssue(String message, EObject source, int offset,  int length, String issueCode, String... issueData) {
		Severity severity = getIssueSeverities(getContext(), getCurrentObject()).getSeverity(issueCode);
		if (severity != null) {
			switch (severity) {
				case WARNING:
					getMessageAcceptor().acceptWarning(message, source, offset, length, issueCode, issueData);
					break;
				case INFO:
					getMessageAcceptor().acceptInfo(message, source,  offset, length, issueCode, issueData);
					break;
				case ERROR:
					getMessageAcceptor().acceptError(message, source, offset, length, issueCode, issueData);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * @since 2.4
	 */
	protected boolean isIgnored(String issueCode) {
		IssueSeverities severities = getIssueSeverities(getContext(), getCurrentObject());
		return severities.isIgnored(issueCode);
	}

	/**
	 * @since 2.4
	 */
	protected IssueSeverities getIssueSeverities(Map<Object, Object> context, EObject eObject) {
		if (context.containsKey(ISSUE_SEVERITIES)) {
			return (IssueSeverities) context.get(ISSUE_SEVERITIES);
		}
		IssueSeverities issueSeverities = issueSeveritiesProvider.getIssueSeverities(eObject.eResource());
		context.put(ISSUE_SEVERITIES, issueSeverities);
		return issueSeverities;
	}

	protected void guard(boolean guardExpression) {
		if (!guardExpression) {
			throw guardException;
		}
	}

	protected void checkDone() {
		throw guardException;
	}
	
	//////////////////////////////////////////////////////////
	// Implementation of the Validation message acceptor below
	//////////////////////////////////////////////////////////

	@Override
	public void acceptError(String message, EObject object, EStructuralFeature feature, int index, String code,
			String... issueData) {
		checkIsFromCurrentlyCheckedResource(object);
		State currentState = this.state.get();
		currentState.hasErrors = true;
		currentState.chain.add(createDiagnostic(Severity.ERROR, message, object, feature, index, code, issueData));
	}

	/**
	 * @since 2.4
	 */
	protected void checkIsFromCurrentlyCheckedResource(EObject object) {
		State currentState = this.state.get();
		if (object != null && currentState != null && currentState.currentObject != null
				&& object.eResource() != currentState.currentObject.eResource()) {
			URI uriGiven = null;
			if (object.eResource() != null)
				uriGiven = object.eResource().getURI();
			URI uri = null;
			if (currentState.currentObject.eResource() != null)
				uri = currentState.currentObject.eResource().getURI();
			throw new IllegalArgumentException(
					"You can only add issues for EObjects contained in the currently validated resource '" + uri
							+ "'. But the given EObject was contained in '" + uriGiven + "'");
		}
	}

	@Override
	public void acceptWarning(String message, EObject object, EStructuralFeature feature, int index, String code,
			String... issueData) {
		checkIsFromCurrentlyCheckedResource(object);
		state.get().chain.add(createDiagnostic(Severity.WARNING, message, object, feature, index, code, issueData));
	}

	@Override
	public void acceptInfo(String message, EObject object, EStructuralFeature feature, int index, String code,
			String... issueData) {
		checkIsFromCurrentlyCheckedResource(object);
		state.get().chain.add(createDiagnostic(Severity.INFO, message, object, feature, index, code, issueData));
	}

	@Override
	public void acceptError(String message, EObject object, int offset, int length, String code, String... issueData) {
		checkIsFromCurrentlyCheckedResource(object);
		State currentState = this.state.get();
		currentState.hasErrors = true;
		currentState.chain.add(createDiagnostic(Severity.ERROR, message, object, offset, length, code, issueData));
	}

	@Override
	public void acceptWarning(String message, EObject object, int offset, int length, String code, String... issueData) {
		checkIsFromCurrentlyCheckedResource(object);
		state.get().chain.add(createDiagnostic(Severity.WARNING, message, object, offset, length, code, issueData));
	}

	@Override
	public void acceptInfo(String message, EObject object, int offset, int length, String code, String... issueData) {
		checkIsFromCurrentlyCheckedResource(object);
		state.get().chain.add(createDiagnostic(Severity.INFO, message, object, offset, length, code, issueData));
	}

	protected Diagnostic createDiagnostic(Severity severity, String message, EObject object,
			EStructuralFeature feature, int index, String code, String... issueData) {
		int diagnosticSeverity = toDiagnosticSeverity(severity);
		Diagnostic result = new FeatureBasedDiagnostic(diagnosticSeverity, message, object, feature, index,
				state.get().currentCheckType, code, issueData);
		return result;
	}

	protected Diagnostic createDiagnostic(Severity severity, String message, EObject object, int offset, int length,
			String code, String... issueData) {
		int diagnosticSeverity = toDiagnosticSeverity(severity);
		Diagnostic result = new RangeBasedDiagnostic(diagnosticSeverity, message, object, offset, length,
				state.get().currentCheckType, code, issueData);
		return result;
	}

	protected int toDiagnosticSeverity(Severity severity) {
		int diagnosticSeverity = -1;
		switch (severity) {
			case ERROR:
				diagnosticSeverity = Diagnostic.ERROR;
				break;
			case WARNING:
				diagnosticSeverity = Diagnostic.WARNING;
				break;
			case INFO:
				diagnosticSeverity = Diagnostic.INFO;
				break;
			default:
				throw new IllegalArgumentException("Unknow severity " + severity);
		}
		return diagnosticSeverity;
	}

	public StateAccess setMessageAcceptor(ValidationMessageAcceptor messageAcceptor) {
		this.messageAcceptor = messageAcceptor;
		return new StateAccess(this);
	}

	public ValidationMessageAcceptor getMessageAcceptor() {
		return messageAcceptor;
	}

	/**
	 * Handles exceptions occuring during execution of validation code. 
	 * By default this method will swallow {@link NullPointerException NullPointerExceptions} and {@link GuardException}s.
	 * Clients may override this method to propagate {@link NullPointerException NullPointerExceptions} or more smarter
	 * handling.
	 * 
	 * @since 2.17
	 */
	protected void handleExceptionDuringValidation(Throwable targetException) throws RuntimeException {
		// ignore NullPointerException, as not having to check for NPEs all the time is a convenience feature
		// ignore GuardException, check is just not evaluated if guard is false
		if (!(targetException instanceof GuardException) && !(targetException instanceof NullPointerException)) {
			Exceptions.throwUncheckedException(targetException);
		}
	}
}
