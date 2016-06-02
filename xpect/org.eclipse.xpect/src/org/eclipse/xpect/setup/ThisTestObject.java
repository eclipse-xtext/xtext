package org.eclipse.xpect.setup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.xpect.XpectImport;
import org.eclipse.xpect.setup.ThisTestObject.TestObjectSetup;
import org.eclipse.xpect.state.Creates;
import org.eclipse.xpect.state.XpectStateAnnotation;

@XpectStateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@XpectImport(TestObjectSetup.class)
public @interface ThisTestObject {

	@XpectSetupFactory
	public class TestObjectSetup {
		private final Class<?> testClass;

		public TestObjectSetup(@ThisTestClass Class<?> testClass) {
			super();
			this.testClass = testClass;
		}

		@Creates(ThisTestObject.class)
		public Object createTestInstance() throws InstantiationException, IllegalAccessException {
			return this.testClass.newInstance();
		}

		protected Class<?> getTestClass() {
			return testClass;
		}
	}

}
