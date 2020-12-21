package org.cemrc.correlator.controllers.analysis;

import org.cemrc.autodoc.Vector2;

/**
 * Registration Pairs are used for alignments.
 * @author larso
 *
 */
public class RegistrationPair {
	
	private Vector2<Double> m_referencePoint;
	private Vector2<Double> m_targetPoint;
	private boolean m_userProvided;
	
	public Vector2<Double> getReferencePoint() {
		return m_referencePoint;
	}

	public void setReferencePoint(Vector2<Double> reference_pt) {
		m_referencePoint = reference_pt;
	}

	public Vector2<Double> getTargetPoint() {
		return m_targetPoint;
	}

	public void setTargetPoint(Vector2<Double> target_pt) {
		m_targetPoint = target_pt;
	}
}