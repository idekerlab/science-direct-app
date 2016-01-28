package org.cytoscape.io.internal.task;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PublishTaskFactory extends AbstractTaskFactory {

	private final CySessionWriterFactory publishForWebFactory;

	public PublishTaskFactory(final CySessionWriterFactory publishForWebFactory) {
		super();
		this.publishForWebFactory = publishForWebFactory;
	}

	@Override
	public TaskIterator createTaskIterator() {
		PublishForWebTask task = new PublishForWebTask(publishForWebFactory);
		return new TaskIterator(task);
	}
}
