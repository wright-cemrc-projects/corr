package org.cemrc.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
	
	// Start with an empty document.
	CorrelatorDocument m_document = new CorrelatorDocument();
	
	// Cause UI updates when data model changes.
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
	
	/**
	 * get the current CorrelatorDocument.
	 * @return
	 */
	public CorrelatorDocument getDocument() {
		return m_document;
	}
	
	/**
	 * Set the document for the application.
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc) {
		CorrelatorDocument oldDoc = m_document;
		m_document = doc;
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
}