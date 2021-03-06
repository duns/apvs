package ch.cern.atlas.apvs.client.ui;

import ch.cern.atlas.apvs.client.ClientFactory;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.SerializationException;

public interface Module {

	/**
	 * return true to add the element to the id'ed rootpanel
	 * 
	 * @param element
	 * @param clientFactory
	 * @param args
	 * @return
	 * @throws SerializationException 
	 */
	public boolean configure(Element element, ClientFactory clientFactory, Arguments args);
	
	/**
	 * Updates the display with the latest information. Returns true when it needs to be called again. 
	 */
	public boolean update();
}
