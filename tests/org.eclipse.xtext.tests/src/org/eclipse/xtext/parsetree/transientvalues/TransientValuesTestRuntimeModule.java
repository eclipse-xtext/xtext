/*
Generated with Xtext
*/
package org.eclipse.xtext.parsetree.transientvalues;

import org.eclipse.xtext.parsetree.reconstr.ITransientValueService;

/**
 * used to register components to be used within the IDE.
 */
public class TransientValuesTestRuntimeModule extends AbstractTransientValuesTestRuntimeModule {

	@Override
	public Class<? extends ITransientValueService> bindITransientValueService() {
		return TransientHandlerPTC.class;
	}

	public Class<? extends org.eclipse.xtext.serializer.sequencer.ITransientValueService> bindTransientHandlerSerializer() {
		return TransientHandlerSerializer.class;
	}

}
