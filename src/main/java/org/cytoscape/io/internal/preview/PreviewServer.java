package org.cytoscape.io.internal.preview;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewServer {
	
	private static final Logger logger = LoggerFactory.getLogger(PreviewServer.class);
	
	private static final Integer PORT = 3333;
	
	private final PreviewUtil util;
	
	private Server server;
	
	
	public PreviewServer(final PreviewUtil util) {
		this.util = util;
	}

	public void startServer() throws Exception {
		this.server = new Server(PORT);
		final ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[] { "index.html" });
		resource_handler.setResourceBase(util.getTemplatePath());

		// Add the ResourceHandler to the server.
		GzipHandler gzip = new GzipHandler();
		server.setHandler(gzip);
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
		gzip.setHandler(handlers);

		server.start();
		logger.info("Preview server is listening on port " + PORT.toString());
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
}