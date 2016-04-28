package org.cytoscape.io.internal.task;

import java.awt.Paint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.preview.PreviewTemplateGenerator;
import org.cytoscape.io.internal.preview.PreviewUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class GeneratePreviewFileTask extends AbstractTask {

	private static final String NETWORK_FILE_NAME = "network.json";
	private static final String STYLE_FILE_NAME = "style.json";
	private static final String BACKGROUND_CSS_FILE_NAME = "bg.css";
	

	private final VizmapWriterFactory jsonStyleWriterFactory;
	private final VisualMappingManager vmm;
	private final CyNetworkViewWriterFactory cytoscapejsWriterFactory;
	private final PreviewUtil util;
	private final CyApplicationManager appManager;
	
	private final File resourceLocation;

	
	public GeneratePreviewFileTask(final VizmapWriterFactory jsonStyleWriterFactory, final VisualMappingManager vmm,
			final CyNetworkViewWriterFactory cytoscapejsWriterFactory, final PreviewUtil util,
			final CyApplicationManager appManager, final CyApplicationConfiguration appConfig) {
		super();
		this.util = util;
		this.jsonStyleWriterFactory = jsonStyleWriterFactory;
		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.appManager = appManager;
		
		final File configLocation = appConfig.getConfigurationDirectoryLocation();
		this.resourceLocation = new File(configLocation, PreviewTemplateGenerator.WEB_RESOURCE_DIR_NAME);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final String destinationDir = util.getTemplatePath();
		
		// Write current network view for preview
		writeNetworkFile(destinationDir, appManager.getCurrentNetworkView(), tm);
		
		// Write current Visual Style in JSON
		writeStyleFile(destinationDir, tm);
		
		tm.setProgress(1.0);
	}


	private final void writeNetworkFile(final String dir, final CyNetworkView view, final TaskMonitor tm)
			throws Exception {
		if (view == null) {
			throw new IllegalArgumentException("Network view is missing");
		}

		final File networkFile = new File(dir, NETWORK_FILE_NAME);
		// Remove previous preview
		if (networkFile.exists()) {
			networkFile.delete();
		}
		
		final CyWriter writer = cytoscapejsWriterFactory.createWriter(new FileOutputStream(networkFile), view);
		writer.run(tm);
		tm.setProgress(0.8);
	}
	
	private final void writeStyleFile(final String dir, final TaskMonitor tm) throws Exception {
		final File styleFile = new File(dir, STYLE_FILE_NAME);
		
		// Remove previous preview
		if (styleFile.exists()) {
			styleFile.delete();
		}
		
		// Export current Style only.
		final Set<VisualStyle> styles = new HashSet<>();
		final VisualStyle style = vmm.getCurrentVisualStyle();
		styles.add(style);
		
		// Extract background color
		File bgFile = new File(dir + File.separator + "styles", BACKGROUND_CSS_FILE_NAME);
		writeBackground(bgFile, style);
		
		final CyWriter vizmapWriter = jsonStyleWriterFactory.createWriter(new FileOutputStream(styleFile), styles);
		vizmapWriter.run(tm);
	}
	
	private final void writeBackground(final File cssFile, final VisualStyle style) throws IOException {
		final Paint bgPaint = style.getDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		String bgString = BasicVisualLexicon.NETWORK_BACKGROUND_PAINT.toSerializableString(bgPaint);
		final BufferedWriter writer = new BufferedWriter(new FileWriter(cssFile));
		writer.write("#cy{background-color:" + bgString + "}");
		writer.close();
	}
}
