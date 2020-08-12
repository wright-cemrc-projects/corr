package org.cemrc.correlator.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.correlator.analysis.CircleHoughTransformTask;
import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.PixelPositionDataset;
import org.cemrc.data.Registration;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FindHolesController {
	// The active document.
	private CorrelatorDocument m_document;
	
	private CircleHoughTransformTask m_task;
	
	// The target and reference map.
	private IMap m_targetMap = null;
	
	// The stage this belongs to.
	private Stage m_stage = null;
	
	@FXML
	private ComboBox<IMap> targetMapCombo;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button holesButton;
	
	@FXML
	private ImageView imageView;
	
	@FXML
	private Slider binarizationSlider;
	
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
		
		ChangeListener<Number> updateUI = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (m_task != null) {
					m_task.setBinarizationCutoff(newValue.intValue());
					BufferedImage proc = m_task.getProcessed();
					Image show =  SwingFXUtils.toFXImage(proc, null);
					imageView.setImage(show);
				}
			}
			
		};
		binarizationSlider.setValue(240);
		binarizationSlider.valueProperty().addListener(updateUI);
	}
	
	/**
	 * Check if all the required data fields are set.
	 * @return
	 */
	private boolean canAlign() {
		return (m_targetMap != null);
	}
	
	private void updateButton() {
		holesButton.setDisable(!canAlign());
	}
	
	
	@FXML
	private void updateTargetMap() {
		m_targetMap = targetMapCombo.getValue();
		setTask();
		
		updateButton();
	}
	
	@FXML
	public void doCancel() {
		if (m_stage != null) {
			m_stage.close();
		}
	}
	
	private void setTask() {
		if (m_targetMap == null) return;
		
		File imageLocation = m_targetMap.getImage();
		if (! imageLocation.exists()) {
			File altImage = m_targetMap.getAltImage();
			if ( altImage != null && altImage.exists()) {
				imageLocation = altImage;
			}
		}
		
		BufferedImage buffer = ReadImage.readImage(imageLocation);
		
		m_task = new CircleHoughTransformTask(buffer);
		BufferedImage proc = m_task.getProcessed();
		Image show =  SwingFXUtils.toFXImage(proc, null);
		
		imageView.setImage(show);
	}
	
	@FXML
	public void doCircleHoughTransform() {
		
		List<Vector2<Integer>> points = m_task.findCircles();
		
		if (points.size() > 0) {
			addPoints(points, m_targetMap);
		}
		
		m_stage.close();
	}
	
	private void addPoints(List<Vector2<Integer>> points, IMap map) {
		List<Vector2<Float>> parsedPositions= new ArrayList<Vector2<Float>>(); 
		for (Vector2<Integer> pt : points) {
			parsedPositions.add(new Vector2<Float>(new Float(pt.x), new Float( pt.y) )); 
		}
		
		PixelPositionDataset pixelPositions = new PixelPositionDataset();
		pixelPositions.setPixelPositions(parsedPositions);
		// pixelPositions.setColor(Color.RE);
		pixelPositions.setName("Point set " + m_document.getData().getUniquePointsID() + " - Found Holes [ " + points.size() + "]");

		// Get useful values from the map.
		if (map != null) {
			pixelPositions.setMap(map);
			pixelPositions.setMapId(map.getId());
			
			GenericItem mapItem = map.getAutoDoc();
			if (mapItem.hasKey(NavigatorKey.MapID)) {
				pixelPositions.setDrawnID((Integer)mapItem.getValue(NavigatorKey.MapID));
			}
			
			if (mapItem.hasKey(NavigatorKey.Regis)) {
				pixelPositions.setRegisID((Integer)mapItem.getValue(NavigatorKey.Regis));
			}
			
			if (mapItem.hasKey(NavigatorKey.Imported)) {
				pixelPositions.setImported((Integer)mapItem.getValue(NavigatorKey.Imported));
			}
			
			if (mapItem.hasKey(NavigatorKey.BklshXY)) {
				pixelPositions.setBacklash((Vector2<Float>)mapItem.getValue(NavigatorKey.BklshXY));
			}
			
		} else {
			pixelPositions.setMapId(IMap.UNASSIGNED_MAP);
		}
		
		m_document.getData().addPositionData(pixelPositions);
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
		
		for (IMap map : maps) {
			targetMapCombo.getItems().add(map);
		}
	}
	
	/**
	 * Set the target map.
	 * @param selected
	 */
	public void setTargetMap(IMap selected) {
		m_targetMap = selected;
	}
}
