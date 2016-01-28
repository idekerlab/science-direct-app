package org.cytoscape.io.internal.preview;

import java.io.File;

import org.cytoscape.application.CyApplicationConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

public class PreviewServer {
	
	private final CyApplicationConfiguration config;
	
	public PreviewServer(final CyApplicationConfiguration config) {
		this.config = config;
	}
	

	public void startServer() throws Exception {
		final File configRoot = config.getConfigurationDirectoryLocation();
		System.out.println(configRoot.getAbsolutePath());
		
		final Server server = new Server(3000);
		final ResourceHandler resource_handler = new ResourceHandler();

		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[] { "index.html" });
		resource_handler.setResourceBase(configRoot.getAbsolutePath());

		// Add the ResourceHandler to the server.
		GzipHandler gzip = new GzipHandler();
		server.setHandler(gzip);
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
		gzip.setHandler(handlers);

		server.start();
		System.out.println("listening....");
		
	}
}