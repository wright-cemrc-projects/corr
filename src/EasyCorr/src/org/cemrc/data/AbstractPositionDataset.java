package org.cemrc.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Core functions for an IPositionDataset
 * @author larso
 *
 */
public class AbstractPositionDataset {
	
	@XmlTransient
	IMap m_parentMap = null;
	
	@XmlElement(name="registered")
	boolean m_isRegistered = false;
	
	@XmlElement(name="Id")
	private int m_Id;
	
	@XmlElement(name="color")
	private NavigatorColorEnum m_color = NavigatorColorEnum.Black;
	
	/**
	 * Set the parent map this position set belongs to.
	 * @param map
	 */
	public void setMap(IMap map) {
		m_parentMap = map;
	}
	
	/**
	 * Get the parent map this position set belongs to.
	 * @return
	 */
	@XmlTransient
	public IMap getMap() {
		return m_parentMap;
	}
	
	public boolean isRegisterationPoints() {
		return m_isRegistered;
	}
	
	public void setIsRegistrationPoints(boolean val) {
		m_isRegistered = val;
	}
	
	public int getId() {
		return m_Id;
	}
	
	public void setId(int id) {
		m_Id = id;
	}
	
	public void setColor(NavigatorColorEnum color) {
		m_color = color;
	}
	
	public NavigatorColorEnum getColor() {
		return m_color;
	}
}
