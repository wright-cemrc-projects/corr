package org.cemrc.correlator.controllers;

import org.cemrc.autodoc.Vector2;
import org.cemrc.data.IMap;

/**
 * Represent a pair of registration points.
 * @author larso
 *
 */
public class RegistrationPair {
	
	private static String UNASSIGNED = "Unassigned";
	private IMap m_referenceMap, m_targetMap;
	private Vector2<Float> m_referencePoint, m_targetPoint;
	private Integer m_id = 0;
	
	
	public IMap getReferenceMap() {
		return m_referenceMap;
	}
	
	public void setReferenceMap(IMap referenceMap) {
		m_referenceMap = referenceMap;
	}

	public IMap getTargetMap() {
		return m_targetMap;
	}

	public void setTargetMap(IMap targetMap) {
		m_targetMap = targetMap;
	}

	public Vector2<Float> getReferencePoint() {
		return m_referencePoint;
	}

	public void setReferencePoint(Vector2<Float> referencePoint) {
		m_referencePoint = referencePoint;
	}

	public Vector2<Float> getTargetPoint() {
		return m_targetPoint;
	}

	public void setTargetPoint(Vector2<Float> targetPoint) {
		m_targetPoint = targetPoint;
	}
	
	// Provides a table label
	public Integer getId() {
		return m_id;
	}
	
	public String getName() {
		return m_id.toString();
	}
	
	public void setId(Integer id) {
		m_id = id;
	}
	
	public String getTargetMapName() {
		if (m_targetMap != null) return m_targetMap.getName();
		else return UNASSIGNED;
	}
	
	public String getTargetPointName() {
		if (m_targetPoint != null) return m_targetPoint.toString();
		else return UNASSIGNED;
	}
	
	public String getReferenceMapName() {
		if (m_referenceMap != null) return m_referenceMap.getName();
		else return UNASSIGNED;
	}
	
	public String getReferencePointName() {
		if (m_referencePoint != null) return m_referencePoint.toString();
		else return UNASSIGNED;
	}
}
