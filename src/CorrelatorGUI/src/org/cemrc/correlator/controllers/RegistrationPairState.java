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
	
	public static String EVENT_ROW_ADDED = "EVENT_ROW_ADDED";
	public static String EVENT_ROW_REMOVED = "EVENT_ROW_REMOVED";
	
	private Integer m_counter = 1;
	
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
     * Force updates of table GUI views.
     * @param property
     */
    public void forceUpdate(String property) {
    	firePropertyChange(property, null, null);
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
	
	/**
	 * Add an unassigned registration pair.
	 */
	public void addEmptyPair() {
		RegistrationPair pair = new RegistrationPair();
		pair.setId(m_counter++);
		m_registrationPairs.add(pair);
		firePropertyChange(EVENT_ROW_ADDED, m_registrationPairs, m_registrationPairs);
	}
	
	/**
	 * Remove a specific registration pair.
	 * @param pair
	 */
	public void removePair(RegistrationPair pair) {
		m_registrationPairs.remove(pair);
		firePropertyChange(EVENT_ROW_REMOVED, m_registrationPairs, m_registrationPairs);
	}
}
