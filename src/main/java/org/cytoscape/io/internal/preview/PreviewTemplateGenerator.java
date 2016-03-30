package org.cytoscape.io.internal.preview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewTemplateGenerator {
	
	private final static Logger logger = LoggerFactory.getLogger(PreviewTemplateGenerator.class);

	private static final String VERSION = "0.1.5";
	private static final String TEMPLATE_NAME = "template.zip";
	private static final String VERSION_NAME = "version.txt";
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
		} else if(destination.exists()) {
			// Maybe there is an old version
			final File versionFile = new File(destination, VERSION_NAME);
			if(!versionFile.exists()) {
				logger.info("Version file not found.  Creating new preview template...");
				deleteAll(destination);
				unzipTemplate(source, destination);
			} else {
				// Check version number
				final String contents = Files.lines(Paths.get(versionFile.toURI()))
					.reduce((t, u) -> t+u).get();
				
				logger.info("Preview template version: " + contents);
				logger.info("Current template version: " + VERSION);
				
				if(!contents.equals(VERSION)) {
					logger.info("Updating template to version " + VERSION);
					deleteAll(destination);
					unzipTemplate(source, destination);
				} else {
					logger.info("No need to update preview template.");
				}
			}
		}
	}
	
	private final void deleteAll(final File f) {
		if(f.isDirectory()) {
			final File[] files = f.listFiles();
			Arrays.stream(files).forEach(file->deleteAll(file));
		}
		f.delete();
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