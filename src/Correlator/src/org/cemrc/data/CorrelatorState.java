package org.cemrc.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CorrelatorState describes the application data state.
 * 
 * @author larso
 *
 */
public class CorrelatorState {

	
	public static String DOCUMENT_CHANGED="DOCUMENT_CHANGED";
	
	private File m_saveFile;
	
	// Start with an empty document.
	CorrelatorDocument m_document = new CorrelatorDocument();
	
	// Set an active map.
	IMap m_activeMap;
	
	// Cause UI updates when data model changes.
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
	
    // Listener to subscribe to changes in the underlying document data.
    private final PropertyChangeListener docDataListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			forceUpdate();
		}
	};
	
	public CorrelatorState() {
		m_document.getData().addPropertyChangeListener(docDataListener);
	}
    
	/**
	 * get the current CorrelatorDocument.
	 * @return
	 */
	public CorrelatorDocument getDocument() {
		return m_document;
	}
	
	/**
	 * Set the state of an active map.
	 * @param a_mapItem
	 */
	public void setActiveMap(IMap a_mapItem) {
		m_activeMap = a_mapItem;
	}
	
	/**
	 * Return the active map instance.
	 * @return
	 */
	public IMap getActiveMap() {
		return m_activeMap;
	}
	
	/**
	 * Set the document for the application.
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc) {
		CorrelatorDocument oldDoc = m_document;
		m_document = doc;
		
		oldDoc.getData().removePropertyChangeListener(docDataListener);
		m_document.getData().addPropertyChangeListener(docDataListener);
		
		firePropertyChange(DOCUMENT_CHANGED, oldDoc, doc);
	}
	
	/**
	 * Cause a manual update of the views.
	 */
	public void forceUpdate() {
		firePropertyChange(DOCUMENT_CHANGED, m_document, m_document);
	}
	
	
	
	/**
	 * This property listener can alert when some values have updated.
	 * @param listener
	 */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Can be called when a some value has changed.
     * @param property
     * @param oldValue
     * @param newValue
     */
    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
    
    /**
     * Get a save filename
     * @param save
     */
    public void setSaveFile(File save) {
    	m_saveFile = save;
    }
    
    /**
     * Save to a known filename
     */
    public void save() {
        if (m_saveFile != null) {
        	try {
        		CorrelatorDocument.serialize(getDocument(), m_saveFile);
        		getDocument().setDirt(false);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    }
    
    /**
     * Check whether there is a save file.
     * @return
     */
    public boolean hasSavefile() {
    	return m_saveFile != null;
    }
    
    /**
     * Return the save filename;
     * @return
     */
    public String getFilename() {
    	if (hasSavefile()) {
    		return m_saveFile.getName();
    	}
    	return "";
    }
}
