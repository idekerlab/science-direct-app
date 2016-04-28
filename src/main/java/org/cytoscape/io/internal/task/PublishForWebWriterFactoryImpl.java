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
import org.cytoscape.view.vizmap.VisualStyleFactory;


public class PublishForWebWriterFactoryImpl implements CyWriterFactory, CySessionWriterFactory {

	private final CyFileFilter filter;
	private final VisualMappingManager vmm;
	private final CyApplicationManager cyApplicationManager;
	
	// TODO: fix json-impl's core bug to use service ID
	private VizmapWriterFactory jsonStyleWriterFactory;
	private CyNetworkViewWriterFactory cytoscapejsWriterFactory;
	
	private final VisualStyleFactory styleFactory;

	public PublishForWebWriterFactoryImpl(
			final VisualMappingManager vmm, 
			final CyFileFilter filter,
			final CyApplicationManager cyApplicationManager,
			final VisualStyleFactory styleFactory) {

		this.vmm = vmm;
		this.filter = filter;
		this.cyApplicationManager = cyApplicationManager;
		this.styleFactory = styleFactory;
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		if(this.jsonStyleWriterFactory == null) {
			throw new IllegalStateException("Could not find a dependency: JSON Style writer service");
		}
		
		return new PublishForWebWriterImpl(outputStream, jsonStyleWriterFactory, vmm, cytoscapejsWriterFactory,
				cyApplicationManager, styleFactory);
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
	
	@SuppressWarnings("rawtypes")
	public void registerViewWriterFactory(final CyNetworkViewWriterFactory writerFactory, final Map props) {
		final Object idObj = props.get("id");
		if(idObj == null) {
			return;
		}
		
		if(idObj.toString().equals("cytoscapejsNetworkWriterFactory")) {
			this.cytoscapejsWriterFactory = writerFactory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void unregisterViewWriterFactory(final CyNetworkViewWriterFactory writerFactory, final Map props) {
	}
}
