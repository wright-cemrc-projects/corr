package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.cemrc.correlator.actions.ActionRegisterImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
			if (p.getReferencePoint() != null && p.getTargetPoint() != null) {
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
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Starting alignment...");
		alert.setHeaderText("[Not implemented]");
		alert.setContentText("Should create an alignment with 3 pairs of pixel positions.");
		alert.showAndWait().ifPresent(rs -> {
		    if (rs == ButtonType.OK) {
		        System.out.println("OK pressed.");
		    }
		});
	}
	
	@FXML
	public void addRegistrationPair() {
		// Create a new row entry in the list.
		m_registrationTableController.addRow();
	}
	
	@FXML
	public void removeRegistrationPair() {
		// If there is an active/highlighted row, it could be removed.
		m_registrationTableController.removeSelectedRow();
	}
	
	@FXML
	public void openTargetMap() {
		// This and open reference map can open a reusable GUI window similar to the ImageViewer that contains a 
		// list of registration pairs and allows the user to select points on both maps. At least three
		// registration pairs would need to be provided with matches on both maps.
		// Interactive alignment could also allow selection of additional points to use for finding new registration
		// points as a dropdown or combo box.
		RegistrationPairState state = m_registrationTableController.getState();
		ActionRegisterImage action = new ActionRegisterImage(m_doc, m_targetMap, state);
		action.doAction();
	}
	
	@FXML
	public void openReferenceMap() {
		RegistrationPairState state = m_registrationTableController.getState();	
		ActionRegisterImage action = new ActionRegisterImage(m_doc, m_referenceMap, state);
		action.doAction();
	}
}
