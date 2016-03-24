package org.cytoscape.io.internal;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_URL;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.io.IOException;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.preview.PreviewServer;
import org.cytoscape.io.internal.preview.PreviewTemplateGenerator;
import org.cytoscape.io.internal.preview.PreviewUtil;
import org.cytoscape.io.internal.task.PublishForWebTaskFactory;
import org.cytoscape.io.internal.task.PublishForWebWriterFactoryImpl;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyActivator extends AbstractCyActivator {

	private static final Logger logger = LoggerFactory.getLogger(CyActivator.class);

	private PreviewServer server;
	

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		final OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		final CyApplicationConfiguration config = getService(bc, CyApplicationConfiguration.class);
		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		final DialogTaskManager tManager = getService(bc, DialogTaskManager.class);
		
		final PreviewUtil util = new PreviewUtil(config);
		
		final BasicCyFileFilter webArchiveFilter = new BasicCyFileFilter(
				new String[] { "zip" },
				new String[] { "application/zip" }, "Zip archive file (.zip)", DataCategory.ARCHIVE, streamUtil);
		
		final CySessionWriterFactory publishForWebWriterFactory = 
				new PublishForWebWriterFactoryImpl(vmm, webArchiveFilter, applicationManager);
		
		final Properties publishForWebWriterFactoryProps = new Properties();
		publishForWebWriterFactoryProps.put(ID, "publishForWebWriterFactory");
		registerAllServices(bc, publishForWebWriterFactory, publishForWebWriterFactoryProps);
		
		
		// Export task
		final PublishForWebTaskFactory publishForWebTaskFactory = new PublishForWebTaskFactory(
				publishForWebWriterFactory, openBrowser, tManager, vmm, util, applicationManager, config);
		final Properties publishForWebTaskFactoryProps = new Properties();
		publishForWebTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		publishForWebTaskFactoryProps.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
		publishForWebTaskFactoryProps.setProperty(MENU_GRAVITY,"2.25");
		publishForWebTaskFactoryProps.setProperty(TITLE,"Publish current network for web...");
		publishForWebTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.9");
		publishForWebTaskFactoryProps.setProperty(LARGE_ICON_URL, getClass().getResource("/images/publish-32.png").toString());
		publishForWebTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		publishForWebTaskFactoryProps.setProperty(TOOLTIP,"Publish for web");
		
		registerAllServices(bc, publishForWebTaskFactory, publishForWebTaskFactoryProps);

		// TODO: fix this by assigning OSGI ID for JSON Style writer
		registerServiceListener(bc, publishForWebWriterFactory, "registerFactory", "unregisterFactory", VizmapWriterFactory.class);
		registerServiceListener(bc, publishForWebTaskFactory, "registerFactory", "unregisterFactory", VizmapWriterFactory.class);
		
		registerServiceListener(bc, publishForWebWriterFactory, 
				"registerViewWriterFactory", "unregisterViewWriterFactory", CyNetworkViewWriterFactory.class);
		registerServiceListener(bc, publishForWebTaskFactory, 
				"registerViewWriterFactory", "unregisterViewWriterFactory", CyNetworkViewWriterFactory.class);
		
		// Extract resource file if necessary
		final PreviewTemplateGenerator generator = new PreviewTemplateGenerator(config);
		try {
			generator.extractPreviewTemplate();
		} catch (IOException e1) {
			throw new RuntimeException("Could not prepare preview template.", e1);
		}
		
		this.server = new PreviewServer(util);
		try {
			server.startServer();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Faild to start preview server.", e);
		}
	}

	@Override
	public void shutDown() {
		logger.info("Shutting down Publish service...");
		try {
			server.stopServer();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Faild to stop preview server.", e);
		}
	}
}
