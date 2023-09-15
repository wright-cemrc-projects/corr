package org.cemrc.math;

import org.cemrc.autodoc.Vector2;

/**
 * Registration Pairs are used for alignments.
 * @author larso
 *
 */
public class RegistrationPair {
	
	private Vector2<Float> m_referencePoint;
	private Vector2<Float> m_targetPoint;
	private boolean m_userProvided;
	
	public RegistrationPair(Vector2<Float> ref, Vector2<Float> target) {
		m_referencePoint = ref;
		m_targetPoint = target;
	}
	
	public RegistrationPair() {}
	
	public Vector2<Float> getReferencePoint() {
		return m_referencePoint;
	}

	public void setReferencePoint(Vector2<Float> reference_pt) {
		m_referencePoint = reference_pt;
	}

	public Vector2<Float> getTargetPoint() {
		return m_targetPoint;
	}

	public void setTargetPoint(Vector2<Float> target_pt) {
		m_targetPoint = target_pt;
	}
}