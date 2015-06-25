package org.eclipse.xtext.idea.example.entities.idea.facet;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import org.eclipse.xtext.idea.facet.AbstractFacetConfiguration;

@State(name = "org.eclipse.xtext.idea.example.entities.EntitiesGenerator", storages = {
		@Storage(id = "ipr", file = "$PROJECT_FILE$"),
		@Storage(id = "prjDir", file = "${PROJECT_CONFIG_DIR$/org.eclipse.xtext.idea.example.entities.EntitiesGeneratorConfig.xml", scheme = StorageScheme.DIRECTORY_BASED)})
public class EntitiesFacetConfiguration extends AbstractFacetConfiguration {
	@Override
	protected String getTabTitle() {
		return "org.eclipse.xtext.idea.example.entities.Entities facet";
	}
}
