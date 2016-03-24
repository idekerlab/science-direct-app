package org.cytoscape.io.internal.preview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.application.CyApplicationConfiguration;

public class PreviewTemplateGenerator {

	private static final String TEMPLATE_NAME = "template.zip";
	public static final String WEB_RESOURCE_DIR_NAME = "preview_template";

	private final CyApplicationConfiguration appConfig;

	public PreviewTemplateGenerator(final CyApplicationConfiguration appConfig) {
		this.appConfig = appConfig;
	}

	public final void extractPreviewTemplate() throws IOException {

		// Get the location of web preview template
		final URL source = this.getClass().getClassLoader().getResource(TEMPLATE_NAME);
		final File configLocation = this.appConfig.getConfigurationDirectoryLocation();
		final File destination = new File(configLocation, WEB_RESOURCE_DIR_NAME);

		// Unzip resource to this directory in CytoscapeConfig
		if (!destination.exists() || !destination.isDirectory()) {
			unzipTemplate(source, destination);
		}
	}

	public void unzipTemplate(final URL source, final File destDir) throws IOException {

		destDir.mkdir();
		final ZipInputStream zipIn = new ZipInputStream(source.openStream());

		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			final String filePath = destDir.getPath() + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				unzipEntry(zipIn, filePath);
			} else {
				final File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	private final void unzipEntry(final ZipInputStream zis, final String filePath) throws IOException {
		final byte[] buffer = new byte[4096];
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		int read = 0;
		while ((read = zis.read(buffer)) != -1) {
			bos.write(buffer, 0, read);
		}
		bos.close();
	}
}