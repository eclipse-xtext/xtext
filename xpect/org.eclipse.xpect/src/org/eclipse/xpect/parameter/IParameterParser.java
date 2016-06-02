/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xpect.parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.eclipse.xpect.XpectInvocation;
import org.eclipse.xpect.text.IRegion;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public interface IParameterParser {

	public interface IClaimedRegion extends IRegion {
		IParameterParser getClaminer();
	}

	public interface IMultiParameterParser extends IParameterParser {
		IRegion claimRegion(XpectInvocation invocation);

		List<IParsedParameterProvider> parseRegion(XpectInvocation invocation, List<IClaimedRegion> claims);
	}

	public interface IParsedParameterProvider extends IParameterProvider {
		IRegion getClaimedRegion();

		List<IRegion> getSemanticRegions();
	}

	public interface ISingleParameterParser extends IParameterParser {
		IRegion claimRegion(XpectInvocation invocation, int paramIndex);

		IParsedParameterProvider parseRegion(XpectInvocation invocation, int paramIndex, List<IClaimedRegion> claims);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.ANNOTATION_TYPE })
	public @interface MultiParameterParser {
		Class<? extends IMultiParameterParser> value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.ANNOTATION_TYPE })
	public @interface SingleParameterParser {
		Class<? extends ISingleParameterParser> value();
	}

}
