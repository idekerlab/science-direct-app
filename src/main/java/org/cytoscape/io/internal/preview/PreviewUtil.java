package org.cytoscape.io.internal.preview;

import java.io.File;

import org.cytoscape.application.CyApplicationConfiguration;

public class PreviewUtil {

	private final CyApplicationConfiguration appConfig;
	
	
	public PreviewUtil(final CyApplicationConfiguration appConfig) {
		this.appConfig = appConfig;
	}
	
	
	public final String getTemplatePath() {
		final File configRoot = appConfig.getConfigurationDirectoryLocation();
		return configRoot.getAbsolutePath() + File.separator + "preview_template" + File.separator + "preview";
	}
}
