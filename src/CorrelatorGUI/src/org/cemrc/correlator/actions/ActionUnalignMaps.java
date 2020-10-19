package org.cemrc.correlator.actions;

import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

public class ActionUnalignMaps {

	private CorrelatorDocument m_document;
	private IMap m_map;
	
	public ActionUnalignMaps(CorrelatorDocument doc, IMap map) {
		m_document = doc;
		m_map = map;
	}
	
	/**
	 * Prompt a user for a filename.
	 */
	public void doAction() {
		
		try {

			// Need to know what map, and then can do it.
			m_map.setRegistration(null);
			m_document.setDirt(true);
			m_document.getData().forceUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
