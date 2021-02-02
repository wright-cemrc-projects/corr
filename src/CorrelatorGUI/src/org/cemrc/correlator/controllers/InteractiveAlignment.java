package org.cemrc.correlator.controllers;

import org.cemrc.autodoc.Vector2;
import org.cemrc.correlator.actions.ActionViewImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class InteractiveAlignment {

	private CorrelatorDocument m_doc;
	private IMap m_referenceMap, m_targetMap;
	private Stage m_stage = null;
	
	// This ListView should get updated with registration points.
	@FXML
	private ListView registrationList;
	
	@FXML
	private ComboBox targetMapComboBox;
	
	@FXML
	private ComboBox referenceMapComboBox;
	
	/**
	 * Represent a pair of registration points.
	 * @author larso
	 *
	 */
	public class RegistrationPair {
		
		private IMap m_referenceMap, m_targetMap;
		private Vector2<Float> m_referencePoint, m_targetPoint;
		
		
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
