package org.cemrc.correlator.controllers;

import org.cemrc.data.IMap;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class InteractiveAlignment {

	private IMap m_referenceMap, m_targetMap;
	private Stage m_stage = null;
	
	/**
	 * Represent a list of paired registration points.
	 * @author larso
	 *
	 */
	public class RegistrationPairState {
		// TODO: each pair should contain a reference to an IMap, and a pixel position or null (unassigned).
	}
	
	@FXML
	public void initialize() {
	}
	
	/**
	 * This is the stage of the window.
	 * @param s
	 */
	public void setStage(Stage s) {
		m_stage = s;
	}
	
	@FXML
	public void doAlign() {
		// TODO: Kick off an interactive alignment that displays an overlay in the right-hand side?
	
	}
	
	@FXML
	public void doCancel() {
		if (m_stage != null) {
			m_stage.close();
		}
	}
	
	@FXML
	public void doAccept() {
		// TODO: Save alignment and show a dialog?
		
		if (m_stage != null) {
			m_stage.close();
		}
	}
	
	@FXML
	public void addRegistrationPair() {
		// TODO
	}
	
	@FXML
	public void removeRegistrationPair() {
		// TODO
	}
	
	@FXML
	public void openTargetMap() {
		// TODO
	}
	
	@FXML
	public void openReferenceMap() {
		// TODO
	}
}
