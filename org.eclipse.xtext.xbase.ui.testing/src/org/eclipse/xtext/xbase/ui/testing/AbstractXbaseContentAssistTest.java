/*******************************************************************************
 * Copyright (c) 2013, 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.testing;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.eclipse.xtext.common.types.access.jdt.JdtTypeProviderFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.testing.ContentAssistProcessorTestBuilder;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.ResourceLoadHelper;
import org.eclipse.xtext.util.JavaRuntimeVersion;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.XExpression;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

/**
 * @since 2.12
 */
public abstract class AbstractXbaseContentAssistTest extends Assert implements ResourceLoadHelper, IJavaProjectProvider {

	@Inject
	protected IWorkspace workspace;

	protected String fileExtension;

	private boolean demandFeatureComputation = false;
	
	/**
	 * @since 2.3
	 */
	@Before
	public void setUp() throws Exception {
		getInjector().injectMembers(this);
		initFeatures();
	}

	/**
	 * @since 2.3
	 */
	@After
	public void tearDown() throws Exception {
		if (doCleanWorkspace())
			IResourcesSetupUtil.cleanWorkspace();
		if (demandFeatureComputation) {
			STATIC_CLASS_FEATURES = null;
			STATIC_STRING_FEATURES = null;
			STATIC_BIGDECIMAL_FEATURES = null;
			STATIC_MAP_FEATURES = null;
			CLASS_FEATURES = null;
			STRING_FEATURES = null;
			BIGDECIMAL_FEATURES = null;
			MAP_FEATURES = null;
			demandFeatureComputation = false;
		}
	}
	
	protected boolean doCleanWorkspace() {
		return true;
	}
	
	protected abstract Injector getInjector();
	
	@Override
	public abstract IJavaProject getJavaProject(ResourceSet resourceSet);
	
	public <T> T get(Class<T> clazz) {
		return getInjector().getInstance(clazz);
	}

	protected XExpression expression(String string) throws Exception {
		XtextResource resource = getResourceFor(new StringInputStream(string));
		assertEquals(resource.getErrors().toString(), 0, resource.getErrors().size());
		XExpression result = (XExpression) resource.getContents().get(0);
		return result;
	}
	
	protected final XtextResourceSet getResourceSet() {
		XtextResourceSet resourceSet = get(XtextResourceSet.class);
		IJvmTypeProvider.Factory typeProviderFactory = new JdtTypeProviderFactory(this);
		typeProviderFactory.findOrCreateTypeProvider(resourceSet);
		resourceSet.setClasspathURIContext(getJavaProject(resourceSet));
		return resourceSet;
	}
	
	@Override
	public final XtextResource getResourceFor(InputStream stream) {
		try {
			XtextResource result = (XtextResource) getResourceSet().createResource(URI.createURI("Test." + fileExtension));
			result.load(stream, null);
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Inject
	protected void setExtensions(@Named(Constants.FILE_EXTENSIONS) String extensions) {
		this.fileExtension = extensions.split(",")[0];
	}
	
	@Test public void testEmptyInput() throws Exception {
		newBuilder().assertText(getKeywordsAndStatics());
	}
	
	protected String[] getKeywordsAndStatics() {
		return KEYWORDS_AND_STATICS;
	}
	
	protected static String[] KEYWORDS_AND_STATICS = {
		"if", 
		"while", "for", "do",
		"true", "false",
		"typeof",
		"try",
		"switch",
		"new",
		"throw",
		"return",
		"null",
		// Collection Literals
		"emptyList",
		"emptySet",
		"emptyMap",
		"newArrayOfSize()",
		"newArrayOfSize()",
		"newBooleanArrayOfSize()", 
		"newBooleanArrayOfSize()", 
		"newByteArrayOfSize()", 
		"newByteArrayOfSize()", 
		"newShortArrayOfSize()",
		"newShortArrayOfSize()",
		"newCharArrayOfSize()", 
		"newCharArrayOfSize()", 
		"newIntArrayOfSize()", 
		"newIntArrayOfSize()", 
		"newLongArrayOfSize()", 
		"newLongArrayOfSize()", 
		"newFloatArrayOfSize()", 
		"newFloatArrayOfSize()", 
		"newDoubleArrayOfSize()", 
		"newDoubleArrayOfSize()", 
		"newImmutableList()",
		"newImmutableSet()",
		"newImmutableMap()",
		"newArrayList",
		"newArrayList()",
		"newLinkedList",
		"newLinkedList()",
		"newHashSet",
		"newHashSet()",
		"newLinkedHashSet",
		"newLinkedHashSet()",
		"newTreeSet()",
		"newTreeSet()",
		"newHashMap()",
		"newHashMap",
		"newLinkedHashMap()",
		"newLinkedHashMap",
		"newTreeMap()",
		"newTreeMap()",
		// InputOutput,
		"print()",
		"println",
		"println()",
		"synchronized"
	};
	
	protected static String[] STRING_OPERATORS = {
		"===", "!==", "==", "!=",
		"+",
		"<=", "<=>", ">=", "<", ">",
		"->","=>", "?:"
	};
	
	protected static String[] CAST_INSTANCEOF = {
		"as", "instanceof"
	};
	
	protected static String[] STRING_FEATURES;
	protected static String[] STATIC_STRING_FEATURES;
	protected static String[] BIGDECIMAL_FEATURES;
	protected static String[] STATIC_BIGDECIMAL_FEATURES;
	protected static String[] CLASS_FEATURES;
	protected static String[] STATIC_CLASS_FEATURES;
	protected static String[] MAP_FEATURES;
	protected static String[] STATIC_MAP_FEATURES;
	
	protected void initFeatures() {
		if (STRING_FEATURES != null) {
			return;
		}
		demandFeatureComputation = true;
		doInitFeatures(getJavaProject(getResourceSet()));
	}
	
	protected static void doInitFeatures(IJavaProject javaProject) {
		try {
			doInitStringFeatures(javaProject);
			doInitMapFeatures(javaProject);
			doInitClassFeatures(javaProject);
			doInitBigDecimalFeatures(javaProject);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static void doInitStringFeatures(IJavaProject javaProject) throws JavaModelException {
		IType stringType = javaProject.findType(String.class.getName());
		Set<String> featuresOrTypes = Sets.newHashSet();
		List<String> features = Lists.newArrayList();
		List<String> staticFeatures = Lists.newArrayList();
		addMethods(stringType, features, staticFeatures, featuresOrTypes);
		// compareTo(T) is actually overridden by compareTo(String) but contained twice in String.class#getMethods
		features.remove("compareTo()");
		if (JavaRuntimeVersion.isJava13OrLater()) {
			features.remove("resolveConstantDesc()");
		}
		Set<String> featuresAsSet = Sets.newHashSet(features);
		Set<String> staticFeaturesAsSet = Sets.newHashSet(staticFeatures);
		Set<String> types = Sets.newHashSet();
		addFields(stringType, features, staticFeatures, featuresAsSet, staticFeaturesAsSet, types);
		// StringExtensions
		features.add("toFirstLower");
		features.add("toFirstUpper");
		features.add("nullOrEmpty");
		// Object extensions
		features.add("identityEquals()");
		STRING_FEATURES = features.toArray(new String[features.size()]);
		STATIC_STRING_FEATURES = staticFeatures.toArray(new String[staticFeatures.size()]);
	}
	
	protected static void doInitBigDecimalFeatures(IJavaProject javaProject) throws JavaModelException {
		IType bigDecimalType = javaProject.findType(BigDecimal.class.getName());
		Set<String> featuresOrTypes = Sets.newHashSet();
		List<String> features = Lists.newArrayList();
		List<String> staticFeatures = Lists.newArrayList();
		addMethods(bigDecimalType, features, staticFeatures, featuresOrTypes);
		// compareTo(T) is actually overridden by compareTo(String) but contained twice in String.class#getMethods
		features.remove("compareTo()");
		Set<String> featuresAsSet = Sets.newHashSet(features);
		Set<String> staticFeaturesAsSet = Sets.newHashSet(staticFeatures);
		Set<String> types = Sets.newHashSet();
		addFields(bigDecimalType, features, staticFeatures, featuresAsSet, staticFeaturesAsSet, types);
		// Object extensions
		features.add("identityEquals()");
		BIGDECIMAL_FEATURES = features.toArray(new String[features.size()]);
		STATIC_BIGDECIMAL_FEATURES = staticFeatures.toArray(new String[staticFeatures.size()]);
	}
	
	protected static void doInitMapFeatures(IJavaProject javaProject) throws JavaModelException {
		IType bigDecimalType = javaProject.findType(Map.class.getName());
		Set<String> featuresOrTypes = Sets.newHashSet();
		List<String> features = Lists.newArrayList();
		List<String> staticFeatures = Lists.newArrayList();
		addMethods(bigDecimalType, features, staticFeatures, featuresOrTypes);
		// compareTo(T) is actually overridden by compareTo(String) but contained twice in String.class#getMethods
		features.remove("compareTo()");
		Set<String> featuresAsSet = Sets.newHashSet(features);
		Set<String> staticFeaturesAsSet = Sets.newHashSet(staticFeatures);
		Set<String> types = Sets.newHashSet();
		addFields(bigDecimalType, features, staticFeatures, featuresAsSet, staticFeaturesAsSet, types);
		// Object extensions
		features.add("identityEquals()");
		MAP_FEATURES = features.toArray(new String[features.size()]);
		STATIC_MAP_FEATURES = staticFeatures.toArray(new String[staticFeatures.size()]);
	}
	
	protected static void doInitClassFeatures(IJavaProject javaProject) throws JavaModelException {
		IType classType = javaProject.findType(Class.class.getName());
		Set<String> featuresOrTypes = Sets.newHashSet();
		List<String> features = Lists.newArrayList();
		List<String> staticFeatures = Lists.newArrayList();
		addMethods(classType, features, staticFeatures, featuresOrTypes);
		if (JavaRuntimeVersion.isJava13OrLater()) {
			features.remove("componentType");
			features.remove("arrayType");
			features.add("getComponentType");
		}
		Set<String> featuresAsSet = Sets.newHashSet(features);
		Set<String> staticFeaturesAsSet = Sets.newHashSet(staticFeatures);
		Set<String> types = Sets.newHashSet();
		addFields(classType, features, staticFeatures, featuresAsSet, staticFeaturesAsSet, types);
		// Object extensions
		features.add("identityEquals()");
		CLASS_FEATURES = features.toArray(new String[features.size()]);
		STATIC_CLASS_FEATURES = staticFeatures.toArray(new String[staticFeatures.size()]);
	}

	protected static void addFields(IType type, List<String> features, List<String> staticFeatures, Set<String> featuresAsSet,
			Set<String> staticFeaturesAsSet, Set<String> types) throws JavaModelException {
		if (types.add(type.getFullyQualifiedName())) {
			for(IField field: type.getFields()) {
				Set<String> asSet = featuresAsSet;
				List<String> list = features;
				if (Modifier.isStatic(field.getFlags())) {
					list = staticFeatures;
					asSet = staticFeaturesAsSet;
				}
				if (Flags.isPublic(field.getFlags()) && asSet.add(field.getElementName()))
					list.add(field.getElementName());
			}
			String superclassName = type.getSuperclassName();
			if (superclassName != null) {
				int idx = superclassName.indexOf('<');
				if (idx != -1) {
					superclassName = superclassName.substring(0, idx);
				}
				IType superType = type.getJavaProject().findType(superclassName);
				addFields(superType, features, staticFeatures, featuresAsSet, staticFeaturesAsSet, types);
			}
			String[] interfaceNames = type.getSuperInterfaceNames();
			for(String interfaceName: interfaceNames) {
				int idx = interfaceName.indexOf('<');
				if (idx != -1) {
					interfaceName = interfaceName.substring(0, idx);
				}
				IType superInterface = type.getJavaProject().findType(interfaceName);
				addFields(superInterface, features, staticFeatures, featuresAsSet, staticFeaturesAsSet, types);
			}
		}
	}

	protected static void addMethods(IType type, List<String> features, List<String> staticFeatures, Set<String> featuresOrTypes) throws JavaModelException {
		if (featuresOrTypes.add(type.getFullyQualifiedName())) {
			for(IMethod method: type.getMethods()) {
				List<String> list = features;
				if (Flags.isStatic(method.getFlags()))
					list = staticFeatures;
				String methodName = method.getElementName();
				if (Flags.isPublic(method.getFlags()) &&  !method.isConstructor() && !"<clinit>".equals(methodName) && !"<init>".equals(methodName) && !Flags.isSynthetic(method.getFlags())) {
					if (method.getParameterTypes().length == 0) {
						if (methodName.startsWith("get") && methodName.length() > 3) {
							String propertyName = Strings.toFirstLower(methodName.substring(3));
							if (featuresOrTypes.add(propertyName + method.getSignature()))
								list.add(propertyName);
						} else if (methodName.startsWith("is") && methodName.length() > 2) {
							String propertyName = Strings.toFirstLower(methodName.substring(2));
							if (featuresOrTypes.add(propertyName + method.getSignature()))
								list.add(propertyName);
						}  else {
							if (featuresOrTypes.add(methodName + method.getSignature()))
								list.add(methodName);
						}
					} else if (method.getParameterTypes().length == 1) {
						if (methodName.startsWith("set") && methodName.length() > 3) {
							String propertyName = Strings.toFirstLower(methodName.substring(3)) + " = value";
							if (featuresOrTypes.add(propertyName + method.getSignature()))
								list.add(propertyName);
						} else {
							methodName = methodName + "()";
							if (featuresOrTypes.add(methodName + method.getSignature()))
								list.add(methodName);	
						}
					} else {
						methodName = methodName + "()";
						if (featuresOrTypes.add(methodName + method.getSignature()))
							list.add(methodName);
					}
				}
			}
			String superclassName = type.getSuperclassName();
			if (superclassName != null) {
				int idx = superclassName.indexOf('<');
				if (idx != -1) {
					superclassName = superclassName.substring(0, idx);
				}
				IType superType = type.getJavaProject().findType(superclassName);
				addMethods(superType, features, staticFeatures, featuresOrTypes);
			}
			String[] interfaceNames = type.getSuperInterfaceNames();
			for(String interfaceName: interfaceNames) {
				int idx = interfaceName.indexOf('<');
				if (idx != -1) {
					interfaceName = interfaceName.substring(0, idx);
				}
				IType superInterface = type.getJavaProject().findType(interfaceName);
				addMethods(superInterface, features, staticFeatures, featuresOrTypes);
			}
		}
	}
	
	public String[] getStringFeatures() {
		return STRING_FEATURES;
	}
	
	public String[] getStaticStringFeatures() {
		return STATIC_STRING_FEATURES;
	}
	
	public String[] getMapFeatures() {
		return MAP_FEATURES;
	}
	
	public String[] getStaticMapFeatures() {
		return STATIC_MAP_FEATURES;
	}
	
	public String[] getBigDecimalFeatures() {
		return BIGDECIMAL_FEATURES;
	}
	
	public String[] getStaticBigDecimalFeatures() {
		return STATIC_BIGDECIMAL_FEATURES;
	}
	
	public String[] getClassFeatures() {
		return CLASS_FEATURES;
	}
	
	public String[] getStaticClassFeatures() {
		return STATIC_CLASS_FEATURES;
	}

	protected String[] expect(String[]... arrays) {
		List<String> expectation = Lists.newArrayList();
		for(String[] array: arrays) {
			expectation.addAll(Arrays.asList(array));
		}
		return expectation.toArray(new String[expectation.size()]);
	}
	
	@Test public void testOnStringLiteral_01() throws Exception {
		newBuilder().append("''").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_02() throws Exception {
		newBuilder().append("''.").assertText(getStringFeatures());
	}
	
	@Test public void testOnStringLiteral_03() throws Exception {
		newBuilder().append("''.").assertTextAtCursorPosition(".", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_04() throws Exception {
		newBuilder().append("''+''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_05() throws Exception {
		newBuilder().append("''+''").assertTextAtCursorPosition("+''", 1, expect(new String[]{"+"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_06() throws Exception {
		newBuilder().append("''==''").assertTextAtCursorPosition("==", 1, "==", "=>", "===");
	}
	
	@Test public void testOnStringLiteral_07() throws Exception {
		newBuilder().append("''==''").assertTextAtCursorPosition("==", 2, expect(new String[]{"==", "==="}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_08() throws Exception {
		newBuilder().append("''<=''").assertTextAtCursorPosition("<=", 1, expect(new String[]{"<", "<=", "<=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_09() throws Exception {
		newBuilder().append("''<=''").assertTextAtCursorPosition("<=", 2, expect(new String[]{"<=", "<=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_10() throws Exception {
		newBuilder().append("'' ").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_11() throws Exception {
		newBuilder().append("''. ").assertText(getStringFeatures());
	}
	
	@Test public void testOnStringLiteral_12() throws Exception {
		newBuilder().append("'' .").assertTextAtCursorPosition(".", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_13() throws Exception {
		newBuilder().append("'' + ''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_14() throws Exception {
		newBuilder().append("'' + ''").assertTextAtCursorPosition("+ ''", 2, getKeywordsAndStatics());
		newBuilder().append("'' + ''").assertTextAtCursorPosition("+ ''", 1, expect(new String[]{"+"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_15() throws Exception {
		newBuilder().append("'' == ''").assertTextAtCursorPosition("==", 1, "==", "=>", "===");
	}
	
	@Test public void testOnStringLiteral_16() throws Exception {
		newBuilder().append("'' == ''").assertTextAtCursorPosition("==", 2, expect(new String[]{"==", "==="}, getKeywordsAndStatics()));
		newBuilder().append("'' == ''").assertTextAtCursorPosition("==", 3, getKeywordsAndStatics());
	}
	
	@Test public void testOnStringLiteral_17() throws Exception {
		newBuilder().append("'' <= ''").assertTextAtCursorPosition("<=", 1, expect(new String[]{"<", "<=", "<=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_18() throws Exception {
		newBuilder().append("'' <= ''").assertTextAtCursorPosition("<=", 2, expect(new String[]{"<=", "<=>"}, getKeywordsAndStatics()));
		newBuilder().append("'' <= ''").assertTextAtCursorPosition("<=", 3, getKeywordsAndStatics());
	}
	
	@Test public void testOnStringLiteral_19() throws Exception {
		newBuilder().append("''.toString").assertText(expect(STRING_OPERATORS, new String[]{"toString"}));
	}
	
	@Test public void testOnStringLiteral_20() throws Exception {
		newBuilder().append("''.toString.").assertText(getStringFeatures());
	}
	
	@Test public void testOnStringLiteral_21() throws Exception {
		newBuilder().append("''.toString.").assertTextAtCursorPosition("g.", 1, expect(STRING_OPERATORS, new String[]{"toString"}));
	}
	
	@Test public void testOnStringLiteral_22() throws Exception {
		newBuilder().append("''.toString+''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, new String[]{"toString"}));
	}
	
	@Test public void testOnStringLiteral_23() throws Exception {
		newBuilder().append("''.toString+''").assertTextAtCursorPosition("+''", 1, expect(new String[]{"+"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_24() throws Exception {
		newBuilder().append("''.toString==''").assertTextAtCursorPosition("==", 1, expect(new String[] {"===", "==", "=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_25() throws Exception {
		newBuilder().append("''.toString==''").assertTextAtCursorPosition("==", 2, expect(new String[]{"==", "==="}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_26() throws Exception {
		newBuilder().append("''.toString<=''").assertTextAtCursorPosition("<=", 1, expect(new String[]{"<", "<=", "<=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_27() throws Exception {
		newBuilder().append("''.toString<=''").assertTextAtCursorPosition("<=", 2, expect(new String[]{"<=", "<=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_28() throws Exception {
		newBuilder().append("''.toString.toString").assertTextAtCursorPosition(".", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_29() throws Exception {
		newBuilder().append("''.toString.toString.toString").assertTextAtCursorPosition("g.", 1, expect(STRING_OPERATORS, new String[]{"toString"}));
	}
	
	@Test public void testOnStringLiteral_30() throws Exception {
		newBuilder().append("('')").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_31() throws Exception {
		newBuilder().append("('').").assertText(getStringFeatures());
	}
	
	@Test public void testOnStringLiteral_32() throws Exception {
		newBuilder().append("(''.toString)").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_33() throws Exception {
		newBuilder().append("(''.toString )").assertTextAtCursorPosition(")", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_34() throws Exception {
		newBuilder().append("''.toString ").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}

	@Test public void testOnStringLiteral_35() throws Exception {
		newBuilder().append("''.toString .").assertTextAtCursorPosition("g .", 2, expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_36() throws Exception {
		newBuilder().append("''.toString +''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testOnStringLiteral_37() throws Exception {
		newBuilder().append("''.toString ==''").assertTextAtCursorPosition("==", 1, expect(new String[] {"==", "===", "=>"}, getKeywordsAndStatics()));
	}
	
	@Test public void testOnStringLiteral_38() throws Exception {
		newBuilder().append("''.toString.toString").assertTextAtCursorPosition("g.", 1, expect(STRING_OPERATORS, new String[]{"toString"}));
	}
	
	@Test public void testOnStringLiteral_39() throws Exception {
		newBuilder().append("''.invalid = ''").assertTextAtCursorPosition(".", 1, getStringFeatures());
	}
	
	@Test public void testOnStringLiteral_40() throws Exception {
		newBuilder().append("''.length = 'invalid'").assertTextAtCursorPosition("le", 2, new String[]{"length"});
	}

	@Test public void testOnVoidMethod_01() throws Exception {
		newBuilder().append("(null as java.util.List).clear ").assertText();
	}
	
	@Test public void testAfterBinaryOperation_01() throws Exception {
		newBuilder().append("''+''").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testAfterBinaryOperation_02() throws Exception {
		newBuilder().append("'' + ''+''").assertTextAtCursorPosition("''+", 2, expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testAfterBinaryOperation_03() throws Exception {
		newBuilder().append("(''+'')").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}

	@Test public void testAfterBinaryOperation_04() throws Exception {
		newBuilder().append("(''+'').").assertText(getStringFeatures());
	}
	
	@Test public void testAfterBinaryOperation_05() throws Exception {
		newBuilder().append("((''+''))").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testAfterBinaryOperation_06() throws Exception {
		newBuilder().append("((''+''))").assertTextAtCursorPosition("))", 1, expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}
	
	@Test public void testAfterBinaryOperation_07() throws Exception {
		newBuilder().append("((''+''))").assertTextAtCursorPosition("))", expect(STRING_OPERATORS, CAST_INSTANCEOF));
	}

	@Test public void testAfterBinaryOperation_08() throws Exception {
		newBuilder().append("((''+(''.bytes)))").assertTextAtCursorPosition(")", "==", "===", "!=", "!==", "+=", "-=", "+", "->", "=>", "?:", "bytes");
	}
	
	@Test public void testAfterBinaryOperation_09() throws Exception {
		newBuilder().append("((''+''.bytes))").assertTextAtCursorPosition(")", "==", "===", "!=", "!==", "+=", "-=", "+", "->", "=>", "?:", "bytes");
	}
	
	@Test public void testAfterBinaryOperation_10() throws Exception {
		newBuilder().append("((''+null))").assertTextAtCursorPosition(")", 
				"null", "!=", "!==", "==", "===", 
				"->", "=>", 
				"+", 
				"?:",
				"<", "<=", "<=>", ">=", ">");
	}
	
	@Ignore("TODO binary operator precedence is not implemented in CA yet")
	@Test public void testAfterBinaryOperation_11() throws Exception {
		newBuilder().append("''+1").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF /* number operators */));
	}
	
	@Test public void testStaticFeatures_01() throws Exception {
		newBuilder().append("String::").assertText(getStaticStringFeatures());
	}
	
	@Test public void testStaticFeatures_02() throws Exception {
		newBuilder().append("String.").assertText(expect(getStaticStringFeatures(), getClassFeatures()));
	}

	@Test public void testNestedTypes_01() throws Exception {
		newBuilder().append("java.util.Map.").assertText(expect(new String[] {"Entry"}, getClassFeatures(), getStaticMapFeatures()));
	}
	
	@Test public void testNull() throws Exception {
		newBuilder().append("null").assertText("null", "!=", "!==", "+", "==", "===", "->", "?:", "=>",
				"%", "*", "**", "-", "+=", "-=", "/", "<", "<=", "<=>", ">=", ">", "++", "--");
	}
	
	@Test public void testForLoop_01() throws Exception {
		newBuilder().append("for (String s: null) ").assertText(expect(new String[]{"s"}, getKeywordsAndStatics()));
	}
	
	@Test public void testForLoop_02() throws Exception {
		newBuilder().append("for (String string: null) string").assertTextAtCursorPosition(") string", 6, "string");
	}
	
	@Test public void testForLoop_03() throws Exception {
		newBuilder().append("for (String string: null) ''+").assertText(expect(new String[] {"string", "+"}, getKeywordsAndStatics()));
	}
	
	@Test public void testForLoop_04() throws Exception {
		newBuilder().append("for (String string: ").assertText(getKeywordsAndStatics());
	}
	
	@Test public void testForLoop_05() throws Exception {
		newBuilder().append("for (String string: )").assertTextAtCursorPosition(")", getKeywordsAndStatics());
	}
	
	@Test public void testClosure_01() throws Exception {
		newBuilder().append("[String a, String b|").assertText(expect(new String[]{"a", "b"}, getKeywordsAndStatics(), new String[] {"val", "var"}));
	}
	
	@Test public void testClosure_02() throws Exception {
		newBuilder().append("#['a', 'b'].filter[it==it]").assertTextAtCursorPosition("it==", expect(new String[]{"it", "var", "val", "self"}, getKeywordsAndStatics(), getStringFeatures()));
	}

	@Test public void testClosure_03() throws Exception {
		newBuilder().append("{val slow = #['a', 'b'].filter[it==it] }").assertTextAtCursorPosition("it==", expect(new String[]{"it", "var", "val", "self"}, getKeywordsAndStatics(), getStringFeatures()));
	}
	
	@Test public void testClosure_04() throws Exception {
		newBuilder().append("{val slow = #[0bd, 1bd].filter[i > 0]}").assertTextAtCursorPosition("i ", expect(new String[]{"it", "var", "val", "self"}, getKeywordsAndStatics(), getBigDecimalFeatures()));
	}
	
	@Test public void testCatchParameter_01() throws Exception {
		newBuilder().append("try {} catch(NullPointerException e) e").assertTextAtCursorPosition(") e", 2, expect(new String[]{"e"}, getKeywordsAndStatics()));
	}
	
	@Test public void testCatchParameter_02() throws Exception {
		newBuilder().append("try {} catch(NullPointerException e) ").assertText(expect(new String[]{"e"}, getKeywordsAndStatics()));
	}
	
	@Test public void testCamelCase_01() throws Exception {
		newBuilder().append("newLLis").assertText("newLinkedList","newLinkedList()");
	}
	
	@Test public void testCamelCase_02() throws Exception {
		newBuilder().append("new ArrBloQu").assertText("java.util.concurrent.ArrayBlockingQueue");
	}
	
	@Test public void testCamelCase_03() throws Exception {
		newBuilder().append("new ArrBloQu").assertText("java.util.concurrent.ArrayBlockingQueue");
	}
	
	@Test public void testSwitchOnEnum_01() throws Exception {
		newBuilder().append("switch java.lang.annotation.RetentionPolicy.SOURCE { case ").assertText(expect(
				new String[]{"SOURCE", "CLASS", "RUNTIME"}, getKeywordsAndStatics()));
	}
	
	@Test public void testSwitchOnEnum_02() throws Exception {
		newBuilder().append("switch java.lang.annotation.RetentionPolicy.SOURCE { case SOUR").assertProposal("SOURCE");
	}
	
	@Test public void testSwitchOnEnum_03() throws Exception {
		newBuilder().append("switch java.lang.annotation.RetentionPolicy.SOURCE { case SOURCE: ").assertText(getKeywordsAndStatics());
	}
	
	/**
	 * @since 2.3
	 */
	protected final ContentAssistProcessorTestBuilder newBuilder() throws Exception {
		ContentAssistProcessorTestBuilder builder = new ContentAssistProcessorTestBuilder(getInjector(), this) {
			@Override
			public ContentAssistProcessorTestBuilder assertTextAtCursorPosition(
					String cursorPosition, String... expectedText) throws Exception {
				return assertTextAtCursorPosition(getModel().indexOf(cursorPosition, getPrefix().length()), expectedText);
			}
			
			@Override
			public ContentAssistProcessorTestBuilder assertTextAtCursorPosition(
					String cursorPosition, int offset, String... expectedText) throws Exception {
				return assertTextAtCursorPosition(getModel().indexOf(cursorPosition, getPrefix().length()) + offset, expectedText);
			}
		};
		String prefix = getPrefix();
		if (prefix.length() > 0) {
			builder = builder.appendNl(prefix);
		}
		String suffix = getSuffix();
		if (suffix.length() > 0) {
			builder = builder.appendSuffix(suffix);
		}
		return builder;
	}
	
	/**
	 * @since 2.6
	 */
	protected String getPrefix() {
		return "";
	}
	
	/**
	 * @since 2.6
	 */
	protected String getSuffix() {
		return "";
	}
	
}
