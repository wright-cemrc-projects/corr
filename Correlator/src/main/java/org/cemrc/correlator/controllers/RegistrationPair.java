package org.cemrc.correlator.controllers;

import java.util.HashMap;
import java.util.Map;

import org.cemrc.autodoc.Vector2;
import org.cemrc.data.IMap;

/**
 * Representation of registration between points.
 * @author larso
 *
 */
public class RegistrationPair {
	
	// ID values
	public static int REFERENCE_ID = 0;
	public static int TARGET_ID = 1;
	private static String UNASSIGNED = "Unassigned";
	
	private Integer m_id = 0;
	private Map<Integer, IMap> m_maps = new HashMap<Integer, IMap>();
	private Map<Integer, Vector2<Float>> m_points = new HashMap<Integer, Vector2<Float>>();
	
	/**
	 * Get a map based on an index.
	 * @param index
	 * @return
	 */
	public IMap getMap(Integer index) {
		if (m_maps.containsKey(index)) {
			return m_maps.get(index);
		}
		return null;
	}
	
	/**
	 * Get a point based on an index.
	 * @param index
	 * @return
	 */
	public Vector2<Float> getPoint(Integer index) {
		if (m_points.containsKey(index)) {
			return m_points.get(index);
		} 
		return null;
	}
	
	/**
	 * Set a map for an index.
	 * @param index
	 * @param map
	 */
	public void setMap(Integer index, IMap map) {
		m_maps.put(index,  map);
	}
	
	/**
	 * Set a point for an index.
	 * @param index
	 * @param point
	 */
	public void setPoint(Integer index, Vector2<Float> point) {
		m_points.put(index, point);
	}
	
	/**
	 * Get an integer id for this registration set.
	 * @return
	 */
	public Integer getId() {
		return m_id;
	}
	
	/**
	 * Set an integer id.
	 * @param id
	 */
	public void setId(Integer id) {
		m_id = id;
	}
	
	/**
	 * Get a string name for the registration
	 * @return
	 */
	public String getName() {
		return getId().toString();
	}
	
	/**
	 * Get a String for a map name.
	 * @param index
	 * @return
	 */
	public String getMapName(Integer index) {
		if (getMap(index) != null) return getMap(index).getName();
		else return UNASSIGNED;
	}
	
	/**
	 * Get a string for a point name.
	 * @param index
	 * @return
	 */
	public String getPointName(Integer index) {
		if (getPoint(index) != null) return getPoint(index).toString();
		else return UNASSIGNED;
	}
}
