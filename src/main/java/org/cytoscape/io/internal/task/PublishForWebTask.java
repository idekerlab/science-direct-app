package org.cytoscape.io.internal.task;

import java.io.File;
import java.io.FileOutputStream;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class PublishForWebTask extends AbstractTask {

	private static final String FILE_EXTENSION = ".zip";

	@ProvidesTitle
	public String getTitle() {
		return "Export zip archive for web publication";
	}

	@Tunable(description = "Export Current Network View and Styles as:", params = "fileCategory=archive;input=false")
	public File file;

	private final CySessionWriterFactory publishForWebFactory;
	private CyWriter writer;

	public PublishForWebTask(final CySessionWriterFactory publishForWebFactory) {
		super();
		this.publishForWebFactory = publishForWebFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (file == null) {
			return;
		}

		if (!file.getName().endsWith(FILE_EXTENSION))
			file = new File(file.getPath() + FILE_EXTENSION);

		final FileOutputStream os = new FileOutputStream(file);
		this.writer = publishForWebFactory.createWriter(os, null);
		this.writer.run(taskMonitor);
		os.close();
		taskMonitor.setProgress(1.0);
	}

	@Override
	public void cancel() {
		super.cancel();
		if (writer != null)
			writer.cancel();
	}
}