package org.cytoscape.io.internal.task;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PublishForWebTaskFactory extends AbstractTaskFactory {

	private final CySessionWriterFactory publishForWebFactory;

	public PublishForWebTaskFactory(final CySessionWriterFactory publishForWebFactory) {
		super();
		this.publishForWebFactory = publishForWebFactory;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final PublishForWebTask exportTask = new PublishForWebTask(publishForWebFactory);

		return new TaskIterator(exportTask);
	}
}
