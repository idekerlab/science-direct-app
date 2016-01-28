package org.cytoscape.io.internal.task;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PublishForWebTaskFactory extends AbstractTaskFactory {

	private final CySessionWriterFactory publishForWebFactory;
	private final OpenBrowser openBrowser;

	public PublishForWebTaskFactory(final CySessionWriterFactory publishForWebFactory, final OpenBrowser openBrowser) {
		super();
		this.publishForWebFactory = publishForWebFactory;
		this.openBrowser = openBrowser;
	}

	@Override
	public TaskIterator createTaskIterator() {
//		final PublishForWebTask exportTask = new PublishForWebTask(publishForWebFactory);
		PreviewExportTask previewExportTask = new PreviewExportTask(openBrowser);
//		return new TaskIterator(exportTask);
		return new TaskIterator(previewExportTask);
	}
}
