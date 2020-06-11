package org.cemrc.easycorr.actions;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;

public class ActionUnalignMaps {

	private EasyCorrDocument m_document;
	private IMap m_map;
	
	public ActionUnalignMaps(EasyCorrDocument doc, IMap map) {
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
			m_document.getData().forceUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
