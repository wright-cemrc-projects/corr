package org.cemrc.correlator.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.correlator.analysis.CircleHoughTransformTask;
import org.cemrc.correlator.controllers.analysis.HistogramChartFactory;
import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;
import org.cemrc.data.PixelPositionDataset;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FindHolesController {
	// The active document.
	private CorrelatorDocument m_document;
	
	private CircleHoughTransformTask m_task;
	private BufferedImage m_src;
	
	// The target and reference map.
	private IMap m_targetMap = null;
	
	// The stage this belongs to.
	private Stage m_stage = null;
	
	@FXML
	private ComboBox<IMap> targetMapCombo;
	
	@FXML
	private ComboBox<String> edgeAlgorithmCombo;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button holesButton;
	
	// Our canvas for drawing.
	@FXML
	private Canvas previewCanvas;
	private Image m_image;
	
	@FXML
	private Slider binarizationSlider;
	
	@FXML
	private ProgressBar findProgressBar;
	
	@FXML
	private LineChart imageHistogram;
	
	private PixelPositionDataset m_pixelPositions;
	
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
					BufferedImage proc = m_task.getProcessed(false);
					m_image =  SwingFXUtils.toFXImage(proc, null);
					updateCanvas();
				}
			}
			
		};
		binarizationSlider.setValue(240);
		binarizationSlider.valueProperty().addListener(updateUI);
		
		edgeAlgorithmCombo.setItems(FXCollections.observableArrayList("Laplacian_1", "Laplacian_2", "Laplacian_3"));
	
		// Style the image histogram
		imageHistogram.setCreateSymbols(false);
		imageHistogram.setAnimated(false);
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
	
	@FXML
	public void doAccept() {
		if (m_pixelPositions != null) {
			m_document.getData().addPositionData(m_pixelPositions);
		}
		
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
		
		m_src = ReadImage.readImage(imageLocation);
		m_task = new CircleHoughTransformTask(m_src);
		BufferedImage proc = m_task.getProcessed(false);
		m_image =  SwingFXUtils.toFXImage(proc, null);
		
		// Update canvas aspect ratio.
		double canvasHeight = previewCanvas.getHeight();
		double aspectRatio = (double) proc.getWidth() / (double) proc.getHeight();
		double canvasWidth = canvasHeight * aspectRatio;
		previewCanvas.setWidth(canvasWidth);
		
		updateCanvas();
		
		// Fill the histogram
		imageHistogram.getData().clear();
		HistogramChartFactory.buildChart(imageHistogram, SwingFXUtils.toFXImage(m_src, null));
	}
	
	@FXML
	public void doCircleHoughTransform() {
		
		m_task.setProgressBar(findProgressBar);
		
		Task<Void> task = new Task<Void>() {
			@Override public Void call()
			{
				List<Vector2<Integer>> points = m_task.findCircles();
				
				if (points.size() > 0) {
					addPoints(points, m_targetMap);
				}
				
				return null;
			}
		};
		
	    Thread thread = new Thread(task);
	    thread.setDaemon(true);
	    thread.start();
	}
	
	private void addPoints(List<Vector2<Integer>> points, IMap map) {
		List<Vector2<Float>> parsedPositions= new ArrayList<Vector2<Float>>(); 
		for (Vector2<Integer> pt : points) {
			parsedPositions.add(new Vector2<Float>(new Float(pt.x), new Float( pt.y) )); 
		}
		
		m_pixelPositions = new PixelPositionDataset();
		m_pixelPositions.setPixelPositions(parsedPositions);
		// pixelPositions.setColor(Color.RE);
		m_pixelPositions.setName("Point set " + m_document.getData().getUniquePointsID() + " - Found Holes [ " + points.size() + "]");

		// Get useful values from the map.
		if (map != null) {
			m_pixelPositions.setMap(map);
			m_pixelPositions.setMapId(map.getId());
			
			GenericItem mapItem = map.getAutoDoc();
			if (mapItem.hasKey(NavigatorKey.MapID)) {
				m_pixelPositions.setDrawnID((Integer)mapItem.getValue(NavigatorKey.MapID));
			}
			
			if (mapItem.hasKey(NavigatorKey.Regis)) {
				m_pixelPositions.setRegisID((Integer)mapItem.getValue(NavigatorKey.Regis));
			}
			
			if (mapItem.hasKey(NavigatorKey.Imported)) {
				m_pixelPositions.setImported((Integer)mapItem.getValue(NavigatorKey.Imported));
			}
			
			if (mapItem.hasKey(NavigatorKey.BklshXY)) {
				m_pixelPositions.setBacklash((Vector2<Float>)mapItem.getValue(NavigatorKey.BklshXY));
			}
			
		} else {
			m_pixelPositions.setMapId(IMap.UNASSIGNED_MAP);
		}
		
		updateCanvas();
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
	
	/**
	 * Should switch the edge detection filter.
	 */
	public void updateEdgeDetection() {
		m_task.setEdgeFilter(edgeAlgorithmCombo.getValue());
		BufferedImage proc = m_task.getProcessed(true);
		m_image =  SwingFXUtils.toFXImage(proc, null);
		updateCanvas();
	}
	
	public void updateCanvas() {
		double width = previewCanvas.getWidth();
		double height = previewCanvas.getHeight();
		
		GraphicsContext gc = previewCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
		
		double mxx = width / m_image.getWidth();
		double myy = height / m_image.getHeight();
		
		gc.setTransform(Transform.affine(mxx, 0, 0, myy, 0f, 0f));

		// Set color effects
		if (m_image != null) {
			gc.drawImage(m_image,  0,  0);
		}
		
		mxx = width / m_src.getWidth();
		myy = height / m_src.getHeight();
		
		gc.setTransform(Transform.affine(mxx, 0, 0, myy, 0f, 0f));
		
		// Draw each checked off points set.
		drawPixels(gc, m_pixelPositions, NavigatorColorEnum.Red);
	}
	
    /**
     * Draw crosshair pixel positions in a color on the canvas.
     * @param gc
     * @param pixelPositions
     * @param colorId
     */
    private void drawPixels(GraphicsContext gc, IPositionDataset positions, NavigatorColorEnum color) {
    	
    	if (positions == null) return;
    	
		Color c;
		switch (color) {
		case Black:
			c = Color.BLACK;
			break;
		case Red:
			c = Color.RED;
			break;
		case Blue:
			c = Color.BLUE;
			break;
		case Green:
			c = Color.GREEN;
			break;
		case Yellow:
			c = Color.YELLOW;
			break;
		case Magenta:
			c = Color.MAGENTA;
			break;
		default:
			c = Color.RED;
			break;
		}
    	
    	gc.beginPath();
    	for (Vector2<Float> pixel : positions.getPixelPositions()) {
    		
    		Point2D pt = new Point2D(pixel.x, pixel.y);
    		
    		gc.setStroke(c);
    		gc.setFill(c);
            gc.moveTo(pt.getX() + 2, pt.getY());
            gc.lineTo(pt.getX() - 2, pt.getY());
            gc.moveTo(pt.getX(), pt.getY() + 2);
            gc.lineTo(pt.getX(), pt.getY() - 2);
            gc.stroke();
    	}	
    	gc.closePath();
    }
}
