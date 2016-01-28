package org.cytoscape.io.internal.task;

import java.io.OutputStream;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.session.CySession;
import org.cytoscape.view.vizmap.VisualMappingManager;


public class PublishForWebWriterFactoryImpl implements CyWriterFactory, CySessionWriterFactory {

	private final CyFileFilter filter;
	private final VisualMappingManager vmm;
	private final CyNetworkViewWriterFactory cytoscapejsWriterFactory;
	private final CyApplicationManager cyApplicationManager;
	
	// TODO: fix json-impl's core bug to use service ID
	private VizmapWriterFactory jsonStyleWriterFactory;

	public PublishForWebWriterFactoryImpl(
			final CyNetworkViewWriterFactory cytoscapejsWriterFactory,
			final VisualMappingManager vmm, 
			final CyFileFilter filter,
			final CyApplicationManager cyApplicationManager) {

		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.filter = filter;
		this.cyApplicationManager = cyApplicationManager;
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		if(this.jsonStyleWriterFactory == null) {
			throw new IllegalStateException("Could not find a dependency: JSON Style writer service");
		}
		
		return new PublishForWebWriterImpl(outputStream, jsonStyleWriterFactory, vmm, cytoscapejsWriterFactory,
				cyApplicationManager);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
	
	
	@SuppressWarnings("rawtypes")
	public void registerFactory(final VizmapWriterFactory writerFactory, final Map props) {
		if (writerFactory.getClass().getName().equals("org.cytoscape.io.internal.write.json.CytoscapeJsVisualStyleWriterFactory")) {
			this.jsonStyleWriterFactory = writerFactory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void unregisterFactory(final VizmapWriterFactory writerFactory, final Map props) {
	}
}
