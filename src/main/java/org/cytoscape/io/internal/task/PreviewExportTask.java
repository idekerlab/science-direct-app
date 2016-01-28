package org.cytoscape.io.internal.task;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PreviewExportTask extends AbstractTask {

	private final OpenBrowser openBrowser;

	public PreviewExportTask(final OpenBrowser openBrowser) {
		this.openBrowser = openBrowser;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				openBrowser.openURL("http://localhost:3000/");
				JOptionPane optionPane = new JOptionPane("Export for web");
				JDialog dialog = optionPane.createDialog("Does this looks OK?");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				dialog.requestFocus();
				optionPane.getSelectionValues();
//				int selection = JOptionPane.showConfirmDialog(null, "Does this preview look OK?");
			}
		});

	}
}
