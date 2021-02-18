package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

/**
 * The current state of a list of RegistrationPair, which can be shared between
 * a main GUI dialog and separate views for each map involved.
 * 
 * Controller classes can subscribe to the as listeners to receive events for
 * when the list data has been changed, to update the GUI.
 * 
 * @author mrlarson2
 *
 */
public class RegistrationPairState {
	
	// Current list of known registration pairs.
	private List<RegistrationPair> m_registrationPairs = new ArrayList<RegistrationPair>();
	// Selected element
	private RegistrationPair m_selected;

	// Cause UI updates when data model changes.
	@XmlTransient
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
	
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
	 * This property listener can alert when some values have updated.
	 * @param listener
	 */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
	/**
	 * Get the selected row of the table
	 * @return
	 */
	public RegistrationPair getSelected() {
		return m_selected;
	}
	
	/**
	 * Set the selected RegistrationPair by reference.
	 * @param selected
	 */
	public void setSelected(RegistrationPair selected) {
		m_selected = selected;
	}
	
	/**
	 * Clear the selections.
	 */
	public void clearSelection() {
		m_selected = null;
	}
	
	/**
	 * Get the registration list.
	 * @return
	 */
	public List<RegistrationPair> getRegistrationList() {
		return m_registrationPairs;
	}
}
