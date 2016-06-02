package org.eclipse.xpect.model;

import java.lang.reflect.Method;

import org.eclipse.xpect.util.IJavaReflectAccess;
import org.eclipse.xtext.common.types.JvmOperation;

public class XjmMethodImplCustom extends XjmMethodImpl {
	@Override
	public String getName() {
		return getJvmMethod().getSimpleName();
	}

	@Override
	public void setJvmMethod(JvmOperation newJvmMethod) {
		super.javaMethod = null;
		super.setJvmMethod(newJvmMethod);
	}

	@Override
	public Method getJavaMethod() {
		Method result = super.getJavaMethod();
		if (result != null)
			return result;
		JvmOperation jvmOperation = super.getJvmMethod();
		if (jvmOperation == null || jvmOperation.eIsProxy())
			return null;
		return super.javaMethod = IJavaReflectAccess.INSTANCE.getMethod(jvmOperation);
	}

}
