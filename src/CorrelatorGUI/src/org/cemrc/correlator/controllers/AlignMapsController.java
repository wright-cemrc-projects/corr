package org.cemrc.correlator.controllers;

import java.util.List;

import org.cemrc.correlator.actions.ActionInteractiveAlignment;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.Registration;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AlignMapsController {
	
	// The active document.
	private CorrelatorDocument m_document;
	
	// The target and reference map.
	private IMap m_targetMap = null, m_referenceMap = null;
	
	// The target and reference point sets.
	private IPositionDataset m_targetPoints = null, m_referencePoints = null;
	
	// The stage this belongs to.
	private Stage m_stage = null;
	
	private static String ALIGN_MODE_INTERACTIVE = "Interactive";
	private static String ALIGN_MODE_RIGID = "Rigid";
	
	@FXML
	private ComboBox<IMap> targetMapCombo;

	@FXML
	private ComboBox<IPositionDataset> targetMapPointsCombo;
	
	@FXML
	private ComboBox<IMap> referenceMapCombo;

	@FXML
	private ComboBox<IPositionDataset> referenceMapPointsCombo;
	
	@FXML
	private ComboBox<String> comboBoxMode;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button alignButton;
	
	@FXML
	public void initialize() {
		updateButton();
		
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
		targetMapCombo.setButtonCell(cellFactory.call(null));
		targetMapCombo.setCellFactory(cellFactory);
		referenceMapCombo.setButtonCell(cellFactory.call(null));
		referenceMapCombo.setCellFactory(cellFactory);
		
		// Setup callbacks to update the views.
		Callback<ListView<IPositionDataset>, ListCell<IPositionDataset>> cellFactoryPositions = new Callback<ListView<IPositionDataset>, ListCell<IPositionDataset>>() {

		    @Override
		    public ListCell<IPositionDataset> call(ListView<IPositionDataset> l) {
		        return new ListCell<IPositionDataset>() {

		            @Override
		            protected void updateItem(IPositionDataset item, boolean empty) {
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
		
		targetMapPointsCombo.setButtonCell(cellFactoryPositions.call(null));
		targetMapPointsCombo.setCellFactory(cellFactoryPositions);
		referenceMapPointsCombo.setButtonCell(cellFactoryPositions.call(null));
		referenceMapPointsCombo.setCellFactory(cellFactoryPositions);
		
		// Setup the comboBoxMode
		comboBoxMode.getItems().addAll(ALIGN_MODE_RIGID, ALIGN_MODE_INTERACTIVE);
		comboBoxMode.setValue(ALIGN_MODE_RIGID);
	}
	
	/**
	 * Check if all the required data fields are set.
	 * @return
	 */
	private boolean canAlign() {
		return (m_targetMap != null && m_referenceMap != null && m_targetPoints != null && m_referencePoints != null);
	}
	
	private void updateButton() {
		alignButton.setDisable(!canAlign());
	}
	
	private void setupTargetPoints() {
		if (m_targetMap != null) {
			targetMapPointsCombo.getItems().clear();
			for (IPositionDataset p : m_document.getData().getPositionData()) {
				if (p.getMapId() == m_targetMap.getId()) {
					targetMapPointsCombo.getItems().add(p);
				}
			}
		}
	}
	
	private void setupReferencePoints() {
		if (m_referenceMap != null) {
			referenceMapPointsCombo.getItems().clear();
			for (IPositionDataset p : m_document.getData().getPositionData()) {
				if (p.getMapId() == m_referenceMap.getId()) {
					referenceMapPointsCombo.getItems().add(p);
				}
			}
		}
	}
	
	@FXML
	private void updateTargetMap() {
		m_targetMap = targetMapCombo.getValue();
		setupTargetPoints();
		updateButton();
	}
	
	@FXML
	private void updateTargetPoints() {
		m_targetPoints = targetMapPointsCombo.getValue();
		updateButton();
	}
	
	@FXML
	private void updateReferenceMap() {
		m_referenceMap = referenceMapCombo.getValue();
		setupReferencePoints();
		updateButton();
	}
	
	@FXML
	private void updateReferencePoints() {
		m_referencePoints = referenceMapPointsCombo.getValue();
		updateButton();
	}
	
	@FXML
	public void doCancel() {
		if (m_stage != null) {
			m_stage.close();
		}
	}
	
	@FXML
	public void doAlign() {
		// System.out.println("Will align: " + m_targetMap.getName() + m_targetPoints.getName() + " vs " + m_referenceMap.getName() + m_referencePoints.getName());
		
		if (ALIGN_MODE_RIGID.equals(comboBoxMode.getValue())) {
		
			// Check the inputs
			if (m_targetPoints.getNumberPositions() != m_referencePoints.getNumberPositions()) {
				Alert errorAlert = new Alert(AlertType.ERROR);
				errorAlert.setHeaderText("Input not valid");
				errorAlert.setContentText("An equal number of reference and target positions must be provided.");
				errorAlert.showAndWait();
			} else {
				
				// Calculate affine transformation as reigstration between maps.
				Registration register = Registration.generate(m_targetPoints, m_referencePoints);
				
				// Clear registration of any other point sets under this map.
				for (IPositionDataset d : m_document.getData().getPositionData()) {
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
				m_document.getData().forceUpdate();
				
				if (m_stage != null) {
					m_stage.close();
				}
			}
		} else {
			
			ActionInteractiveAlignment startAlignmentGUI = new ActionInteractiveAlignment(m_document, m_targetPoints, m_referencePoints);
			startAlignmentGUI.doAction();
			
			
			if (m_stage != null) {
				m_stage.close();
			}
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
	 * Set the document.
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
		
		setupMaps();
	}
	
	public void setupMaps() {
		List<IMap> maps = m_document.getData().getMapData();
		
		targetMapCombo.getItems().clear();
		referenceMapCombo.getItems().clear();
		
		for (IMap map : maps) {
			targetMapCombo.getItems().add(map);
			referenceMapCombo.getItems().add(map);
		}
	}
	
	/**
	 * Set the target map.
	 * @param selected
	 */
	public void setTargetMap(IMap selected) {
		m_targetMap = selected;
	}
	
	/**
	 * Set the target points.
	 * @param selected
	 */
	public void setTargetPoints(IPositionDataset selected) {
		m_targetPoints = selected;
	}
	
}
