package org.cytoscape.io.internal.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PublishForWebWriterImpl extends AbstractTask implements CyWriter {

	private static final String JSON_EXT = ".json";
	private static final String CYJS_EXT = ".cyjs";
	private static final String STYLE_FILE_NAME = "style";
	private static final String FILE_NAME_SEPARATOR = "____";
	
	private static final String DEF_STYLE_NAME = "default";
	
	private static final String STYLE_COLUMN_NAME = "assignedStyle";

	private ZipOutputStream zos;

	private final OutputStream outputStream;
	private final VizmapWriterFactory jsonStyleWriterFactory;
	private final VisualMappingManager vmm;
	private final CyNetworkViewWriterFactory cytoscapejsWriterFactory;

	private final CyApplicationManager appManager;
	
	private final VisualStyleFactory styleFactory;
	

	public PublishForWebWriterImpl(final OutputStream outputStream, final VizmapWriterFactory jsonStyleWriterFactory,
			final VisualMappingManager vmm, final CyNetworkViewWriterFactory cytoscapejsWriterFactory,
			final CyApplicationManager appManager, final VisualStyleFactory styleFactory) {

		this.outputStream = outputStream;
		this.jsonStyleWriterFactory = jsonStyleWriterFactory;
		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.appManager = appManager;
		this.styleFactory = styleFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		try {
			tm.setProgress(0.1);
			tm.setTitle("Archiving into zip files");
			zos = new ZipOutputStream(outputStream);
			writeFiles(tm);
		} finally {
			try {
				if (zos != null) {
					zos.close();
					zos = null;
				}
			} catch (Exception e) {
				throw new IOException("Error closing zip output stream", e);
			}
		}
	}

	public void writeFiles(final TaskMonitor tm) throws Exception {
		// Files to be archived
		final Collection<File> files = new HashSet<File>();

		// Write current network view into Cytoscape.js
		tm.setStatusMessage("Saving current network view as a Cytoscape.js JSON file...");

		final CyNetworkView netView = this.appManager.getCurrentNetworkView();
		final File viewFile = createViewFile(netView, tm);
		files.add(viewFile);

		tm.setProgress(0.5);
		if (cancelled)
			return;

		// Generate Style file.
		tm.setStatusMessage("Saving Visual Styles as a JSON file...");
		final File styleFile = createStyleFile(tm);
		files.add(styleFile);
		tm.setProgress(0.9);

		zipAll(files);
		tm.setProgress(1.0);
	}

	private final void zipAll(final Collection<File> files) throws IOException {
		zos = new ZipOutputStream(outputStream);
		addDir(files.toArray(new File[0]), zos);
		zos.close();
	}

	private final void addDir(final File[] files, final ZipOutputStream out) throws IOException {
		final byte[] buffer = new byte[4096];

		for (final File file : files) {
			final FileInputStream in = new FileInputStream(file);
			String zipFilePath = null;

			// Archive name
			final String originalName = file.getName();
			
			String archiveName = originalName.split(FILE_NAME_SEPARATOR)[0];
			if(originalName.startsWith(STYLE_FILE_NAME)) {
				archiveName += JSON_EXT;
			} else {
				archiveName += CYJS_EXT;
			}
			
			final Path dataFilePath = Paths.get(archiveName);
			zipFilePath = dataFilePath.toString();

			// This is for Windows System: Replace file separator to slash.
			if (File.separatorChar != '/') {
				zipFilePath = zipFilePath.replace('\\', '/');
			}

			// Add normalized path name;
			final ZipEntry entry = new ZipEntry(zipFilePath);
			out.putNextEntry(entry);

			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			out.closeEntry();
			in.close();
		}
	}

	private final File createStyleFile(final TaskMonitor tm) throws Exception {
		// Use only the first one.
		final Set<VisualStyle> styles = new HashSet<>();
		final VisualStyle style = vmm.getCurrentVisualStyle();
		
		//TODO: THIS IS A HACK.  We should NOT depend on the name "default." 
		VisualStyle copyStyle = styleFactory.createVisualStyle(style);
		copyStyle.setTitle(DEF_STYLE_NAME); // Force to change the name to "default"
		styles.add(copyStyle);
		
		final File styleFile = File.createTempFile(STYLE_FILE_NAME + FILE_NAME_SEPARATOR, null);
		final CyWriter vizmapWriter = jsonStyleWriterFactory.createWriter(new FileOutputStream(styleFile), styles);
		vizmapWriter.run(tm);
		
		copyStyle = null;
		
		return styleFile;
	}

	private File createViewFile(final CyNetworkView view, final TaskMonitor tm) throws Exception {
		if (view == null) {
			throw new IllegalArgumentException("Network view is missing");
		}
		final CyNetwork network = view.getModel();
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		
		// Assign column name to the Style
		CyColumn col = network.getDefaultNetworkTable().getColumn(STYLE_COLUMN_NAME);
		if(col == null) {
			// Create col for the network
			network.getDefaultNetworkTable().createColumn(STYLE_COLUMN_NAME, String.class, false);
		}
		
		network.getRow(network).set(STYLE_COLUMN_NAME, DEF_STYLE_NAME);
		
		final String jsonFileName = networkName;
		final File tempFile = File.createTempFile(jsonFileName + FILE_NAME_SEPARATOR, null);
		final CyWriter writer = cytoscapejsWriterFactory.createWriter(new FileOutputStream(tempFile), view);
		writer.run(tm);
		return tempFile;
	}
}
