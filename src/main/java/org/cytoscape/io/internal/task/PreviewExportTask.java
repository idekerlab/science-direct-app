package org.cytoscape.io.internal.task;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class PreviewExportTask extends AbstractTask {

	private static final String PREVIEW_URL = "http://localhost:3333/";

	private final Icon icon;

	private static final String OPTION_EXPORT = "Export";
	private static final String OPTION_CANCEL = "Cancel";
	private static final String[] OPTIONS = { OPTION_EXPORT, OPTION_CANCEL };

	// Dependencies
	private final OpenBrowser openBrowser;
	private final CySessionWriterFactory publishForWebFactory;
	private final DialogTaskManager taskManager;


	public PreviewExportTask(final OpenBrowser openBrowser, final CySessionWriterFactory publishForWebFactory,
			DialogTaskManager taskManager) {

		this.openBrowser = openBrowser;
		this.publishForWebFactory = publishForWebFactory;
		this.taskManager = taskManager;

		this.icon = new ImageIcon(this.getClass().getResource("/images/publish-32.png"));
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				openBrowser.openURL(PREVIEW_URL);
				final JOptionPane optionPane = new JOptionPane("Do you want to export this preview?",
						JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, icon, OPTIONS, OPTION_EXPORT);
				final JDialog dialog = optionPane.createDialog("Cytoscape: Publish to Web");
				dialog.setAlwaysOnTop(true);
				dialog.setModal(true);
				dialog.setVisible(true);
				dialog.setLocationByPlatform(true);
				dialog.requestFocus();
				final Object selection = optionPane.getValue();
				if (selection.toString().equals(OPTION_EXPORT)) {
					runExport();
				}
			}
		});
	}

	/**
	 * This is a bit hackey way to sequentially execute tasks. Simply call new
	 * list of tasks from EDT.
	 */
	private final void runExport() {
		PublishTaskFactory tf = new PublishTaskFactory(publishForWebFactory);
		taskManager.execute(tf.createTaskIterator());
	}
}
