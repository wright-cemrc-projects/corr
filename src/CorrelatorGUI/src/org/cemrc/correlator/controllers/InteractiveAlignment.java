package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.cemrc.correlator.actions.ActionViewImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class InteractiveAlignment {

	private CorrelatorDocument m_doc;
	private IMap m_referenceMap, m_targetMap;
	private Stage m_stage = null;
	
	// This TableView should get updated with registration points.
	@FXML
	private TableView registrationTable;
	RegistrationTableController m_registrationTableController;
	
	@FXML
	private ComboBox targetMapComboBox;
	
	@FXML
	private ComboBox referenceMapComboBox;
	
	
	@FXML
	public void initialize() {
		m_registrationTableController = new RegistrationTableController(registrationTable);
		m_registrationTableController.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO: change button active state?
			}
		});
	}
	
	/**
	 * This is the stage of the window.
	 * @param s
	 */
	public void setStage(Stage s) {
		m_stage = s;
	}
	
	/**
	 * Set the target map
	 * @param target
	 */
	public void setTargetMap(IMap target) {
		m_targetMap = target;
	}
	
	/**
	 * Set the reference map
	 * @param reference
	 */
	public void setReferenceMap(IMap reference) {
		m_referenceMap = reference;
	}
	
	/**
	 * Set a document
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc) {
		m_doc = doc;
		
		// TODO: could fill out the GUI for the drop-down map selections.
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
		// Create a new row entry in the list.
	}
	
	@FXML
	public void removeRegistrationPair() {
		// TODO
		// If there is an active/highlighted row, it could be removed.
	}
	
	@FXML
	public void openTargetMap() {
		// This and open reference map can open a reusable GUI window similar to the ImageViewer that contains a 
		// list of registration pairs and allows the user to select points on both maps. At least three
		// registration pairs would need to be provided with matches on both maps.
		// Interactive alignment could also allow selection of additional points to use for finding new registration
		// points as a dropdown or combo box.
		
		// TODO: Stub for the existing image viewer which could be generalized to describe registration pairs, or...
		ActionViewImage action = new ActionViewImage(m_doc, m_targetMap);
		action.doAction();
	}
	
	@FXML
	public void openReferenceMap() {
		// TODO: Stub for the existing image viewer which could be generalized to describe registration pairs, or...
		ActionViewImage action = new ActionViewImage(m_doc, m_referenceMap);
		action.doAction();
	}
}
