package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.cemrc.autodoc.Vector2;
import org.cemrc.correlator.actions.ActionRegisterImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.PixelPositionDataset;
import org.cemrc.data.Registration;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class InteractiveAlignment {

	private CorrelatorDocument m_doc;
	private IMap m_referenceMap, m_targetMap;
	private Stage m_stage = null;
	
	// This TableView should get updated with registration points.
	@FXML
	private TableView registrationTable;
	RegistrationTableController m_registrationTableController;
	
	@FXML
	private ComboBox<IMap> targetMapComboBox;
	
	@FXML
	private ComboBox<IMap> referenceMapComboBox;
	
	@FXML
	private Button doAlign;
	
	@FXML
	public void initialize() {
		m_registrationTableController = new RegistrationTableController(registrationTable);
		m_registrationTableController.getState().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateGUI();
			}
		});
		
		// Setup callbacks to update the views.
		Callback<ListView<IMap>, ListCell<IMap>> cellFactory = new Callback<ListView<IMap>, ListCell<IMap>>() {

		    @Override
		    public ListCell<IMap> call(ListView<IMap> l) {
		        return new ListCell<IMap>() {

		            @Override
		            protected void updateItem(IMap item, boolean empty) {
		                super.updateItem(item, empty);
		                if (item == null || empty) {
		                    setGraphic(null);
		                } else {
		                    setText(item.getName());
		                }
		            }
		        } ;
		    }
		};
		targetMapComboBox.setButtonCell(cellFactory.call(null));
		targetMapComboBox.setCellFactory(cellFactory);
		
		referenceMapComboBox.setButtonCell(cellFactory.call(null));
		referenceMapComboBox.setCellFactory(cellFactory);
		
		// Update the align button
		updateGUI();
	}
	
	public void setupMaps() {
		List<IMap> maps = m_doc.getData().getMapData();
		
		targetMapComboBox.getItems().clear();
		referenceMapComboBox.getItems().clear();
		
		for (IMap map : maps) {
			targetMapComboBox.getItems().add(map);
			referenceMapComboBox.getItems().add(map);
		}
	}
	
	/**
	 * This is the stage of the window.
	 * @param s
	 */
	public void setStage(Stage s) {
		m_stage = s;
	}
	
	/**
	 * Update the Alignment button state, based on number of configured registration points.
	 */
	private void updateGUI() {
		int validReferencePairs = 0;
		
		for (RegistrationPair p : m_registrationTableController.getState().getRegistrationList()) {
			if (p.getPoint(RegistrationPair.REFERENCE_ID) != null && p.getPoint(RegistrationPair.TARGET_ID) != null) {
				validReferencePairs += 1;
			}
		}
		
		if (validReferencePairs > 2) {
			doAlign.setDisable(false);
		} else {
			doAlign.setDisable(true);
		}
	}
	
	/**
	 * Set the target map
	 * @param target
	 */
	public void setTargetMap(IMap target) {
		m_targetMap = target;
		targetMapComboBox.setValue(target);
	}
	
	/**
	 * Set the reference map
	 * @param reference
	 */
	public void setReferenceMap(IMap reference) {
		m_referenceMap = reference;
		referenceMapComboBox.setValue(reference);
	}
	
	public void onTargetCombo() {
		m_targetMap = targetMapComboBox.getSelectionModel().getSelectedItem();
	}
	
	public void onReferenceCombo() {
		m_referenceMap = referenceMapComboBox.getSelectionModel().getSelectedItem();
	}
	
	/**
	 * Set a document
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc) {
		m_doc = doc;
	}
	
	
	@FXML
	public void doAlign() {

		PixelPositionDataset m_targetPoints = new PixelPositionDataset();
		PixelPositionDataset m_referencePoints = new PixelPositionDataset();
		
		m_targetPoints.setMap(m_targetMap);
		m_referencePoints.setMap(m_referenceMap);
		
		for (RegistrationPair p : m_registrationTableController.getItems()) {
			if (p.getPoint(RegistrationPair.REFERENCE_ID) != null && p.getPoint(RegistrationPair.TARGET_ID) != null) {
				
				Vector2<Float> p1 = p.getPoint(RegistrationPair.TARGET_ID);
				m_targetPoints.addPixelPosition(p1.x, p1.y);
				
				Vector2<Float> p2 = p.getPoint(RegistrationPair.REFERENCE_ID);
				m_referencePoints.addPixelPosition(p2.x, p2.y);
			}
		}

		// Calculate affine transformation as reigstration between maps.
		Registration register = Registration.generate(m_targetPoints, m_referencePoints);
		register.setRegisterMapId(m_referenceMap.getId());
		
		// Clear registration of any other point sets under this map.
		for (IPositionDataset d : m_doc.getData().getPositionData()) {
			if (d.getMap() == m_targetMap) {
				d.setIsRegistrationPoints(false);
			}
		}
		
		// Set the targetPoints as registered.
		m_targetPoints.setIsRegistrationPoints(true);
		
		Alert showMatrixDialog = new Alert(AlertType.CONFIRMATION);
		showMatrixDialog.setHeaderText("Affine Matrix");
		showMatrixDialog.setContentText(register.getPrettyString());
		showMatrixDialog.showAndWait();
		
		m_targetMap.setRegistration(register);
		
		m_doc.setDirt(true);
		m_doc.getData().forceUpdate();
		
		if (m_stage != null) {
			m_stage.close();
		}
		
	}
	
	@FXML
	public void addRegistrationPair() {
		// Create a new row entry in the list.
		m_registrationTableController.getState().addEmptyPair();
	}
	
	@FXML
	public void removeRegistrationPair() {
		// If there is an active/highlighted row, it could be removed.
		RegistrationPairState s = m_registrationTableController.getState();
		s.removePair(s.getSelected());
	}
	
	@FXML
	public void openTargetMap() {
		// This and open reference map can open a reusable GUI window similar to the ImageViewer that contains a 
		// list of registration pairs and allows the user to select points on both maps. At least three
		// registration pairs would need to be provided with matches on both maps.
		// Interactive alignment could also allow selection of additional points to use for finding new registration
		// points as a dropdown or combo box.
		RegistrationPairState state = m_registrationTableController.getState();

		if (m_targetMap != null) {	
			ActionRegisterImage action = new ActionRegisterImage(m_doc, RegistrationPair.TARGET_ID, m_targetMap, state);
			action.doAction();
		}
	}
	
	@FXML
	public void openReferenceMap() {
		RegistrationPairState state = m_registrationTableController.getState();	

		if (m_referenceMap != null) {
			ActionRegisterImage action = new ActionRegisterImage(m_doc, RegistrationPair.REFERENCE_ID, m_referenceMap, state);
			action.doAction();
		}
	}
}
