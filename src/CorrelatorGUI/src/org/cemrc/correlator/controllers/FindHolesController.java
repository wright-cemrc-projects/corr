package org.cemrc.correlator.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.correlator.analysis.CircleHoughTransformTask;
import org.cemrc.correlator.analysis.ClusterMinima;
import org.cemrc.correlator.controllers.analysis.CutoffLineChart;
import org.cemrc.correlator.controllers.analysis.FilterGridPoints;
import org.cemrc.correlator.data.IMapImage;
import org.cemrc.correlator.io.ImageProvider;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;
import org.cemrc.data.PixelPositionDataset;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class FindHolesController {
	// The active document.
	private CorrelatorDocument m_document;
	
	private CircleHoughTransformTask m_task;
	private IMapImage m_src;
	
	// The target and reference map.
	private IMap m_targetMap = null;
	
	// The stage this belongs to.
	private Stage m_stage = null;
	
	@FXML
	private ComboBox<IMap> targetMapCombo;
	
	//@FXML
	//private ComboBox<String> edgeAlgorithmCombo;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button holesButton;
	
	// Our canvas for drawing.
	@FXML
	private Canvas previewCanvas;
	
	@FXML
	private Canvas resultsCanvas;
	
	private Image m_greyScaleImage;
	private Image m_edgeDetectImage;
	
	@FXML
	private ProgressBar findProgressBar;
	
	@FXML
	private HBox chartBox;
	
	private PixelPositionDataset m_pixelPositions;
	
	@FXML
	private ToggleButton bwImageToggle;
	
	@FXML
	private ToggleButton edgeImageToggle;
	
	@FXML
	private ToggleButton holesToggle;
	
	// Describe hole information which could be converted into IPositionDatasets.
	private List<ClusterMinima> m_foundHoles;
	
	BooleanProperty showBWImage = new SimpleBooleanProperty(true);
	BooleanProperty showEdgeImage = new SimpleBooleanProperty(true);
	BooleanProperty showHoles = new SimpleBooleanProperty(true);
	
	@FXML
	private TextField holeMinEntry;
	
	@FXML
	private TextField holeMaxEntry;
	
	@FXML
	public void initialize() {
		updateButton();
		
		// Setup the min/max circle sizes
		
		
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
		        };
		    }
		};
		targetMapCombo.setButtonCell(cellFactory.call(null));
		targetMapCombo.setCellFactory(cellFactory);
		
		// edgeAlgorithmCombo.setItems(FXCollections.observableArrayList("Laplacian_1", "Laplacian_2", "Laplacian_3"));
		
		bwImageToggle.selectedProperty().bindBidirectional(showBWImage);
		edgeImageToggle.selectedProperty().bindBidirectional(showEdgeImage);
		holesToggle.selectedProperty().bindBidirectional(showHoles);
		
		ChangeListener<Boolean> buttonCallback = new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				updateCanvas();
			}
		};
		bwImageToggle.selectedProperty().addListener(buttonCallback);
		edgeImageToggle.selectedProperty().addListener(buttonCallback);
		holesToggle.selectedProperty().addListener(buttonCallback);
		
		resetCirclePositions();
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
		resetCirclePositions();
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
			m_document.setDirt(true);
		}
		
		if (m_stage != null) {
			m_stage.close();
		}
	}
	
	public class IntegerFilter implements UnaryOperator<TextFormatter.Change> {
	    private final Pattern DIGIT_PATTERN = Pattern.compile("\\d*");

	    @Override
	    public Change apply(TextFormatter.Change aT) {
	        return DIGIT_PATTERN.matcher(aT.getText()).matches() ? aT : null;
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
		
		m_src = ImageProvider.readImage(imageLocation);
		m_task = new CircleHoughTransformTask(m_src);
		m_task.getProcessed(false);
		m_greyScaleImage =  SwingFXUtils.toFXImage(m_task.getGreyscale(), null);
		m_edgeDetectImage =  SwingFXUtils.toFXImage(m_task.getSobel(), null);
		
		StringConverter<Number> converter = new StringConverter<Number>() {

		    @Override
		    public String toString(Number object) {
		        return object == null ? "" : object.toString();
		    }

		    @Override
		    public Number fromString(String string) {
		        if (string == null) {
		            return 0;
		        } else {
		            try {
		                return Integer.parseInt(string);
		            } catch (NumberFormatException ex) {
		                return 0;
		            }
		        }
		    }

		};

		Bindings.bindBidirectional(holeMaxEntry.textProperty(), m_task.maxHole, converter);
		Bindings.bindBidirectional(holeMinEntry.textProperty(), m_task.minHole, converter);
		
		holeMinEntry.textProperty().set("8");
		holeMaxEntry.textProperty().set("20");
		
		// Update canvas aspect ratio.
		double canvasHeight = previewCanvas.getHeight();
		double aspectRatio = (double) m_greyScaleImage.getWidth() / (double) m_greyScaleImage.getHeight();
		double canvasWidth = canvasHeight * aspectRatio;
		previewCanvas.setWidth(canvasWidth);
		resultsCanvas.setWidth(canvasWidth);
		
		updateCanvas();
		
		// Fill the histogram
		chartBox.getChildren().clear();		
		
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		
		// Histogram is generated from scan of pixel values.
		CutoffLineChart imageHistogram = new CutoffLineChart(xAxis, yAxis, m_src);
		
		imageHistogram.setPrefSize(chartBox.getWidth(), chartBox.getHeight());
		imageHistogram.setMinWidth(300);
		
		chartBox.getChildren().add(imageHistogram);
		
		// Setup a controller class to manage lines
		//HistogramPane2 histogram = new HistogramPane2(chartBox, SwingFXUtils.toFXImage(m_src, null));
		imageHistogram.m_positionBinaryCutoff.addListener((observable, oldValue, newValue) -> {
		    m_task.setBinarizationCutoff(newValue.doubleValue());
			m_task.getProcessed(false);
			m_greyScaleImage = SwingFXUtils.toFXImage(m_task.getGreyscale(), null);
			m_edgeDetectImage =  SwingFXUtils.toFXImage(m_task.getSobel(), null);
		    updateCanvas();
		});
		
		imageHistogram.m_positionMinCutoff.addListener((observable, oldValue, newValue) -> {
		    m_task.setLowCutoff(newValue.doubleValue());
			m_task.getProcessed(false);
			m_greyScaleImage = SwingFXUtils.toFXImage(m_task.getGreyscale(), null);
			m_edgeDetectImage =  SwingFXUtils.toFXImage(m_task.getSobel(), null);
		    updateCanvas();
		});
		
		imageHistogram.m_positionMaxCutoff.addListener((observable, oldValue, newValue) -> {
		    m_task.setHighCutoff(newValue.doubleValue());
			m_task.getProcessed(false);
			m_greyScaleImage = SwingFXUtils.toFXImage(m_task.getGreyscale(), null);
			m_edgeDetectImage =  SwingFXUtils.toFXImage(m_task.getSobel(), null);
		    updateCanvas();
		});
		
		// Set default values:
		
		m_task.setBinarizationCutoff(imageHistogram.m_positionBinaryCutoff.doubleValue());
		m_task.setLowCutoff(imageHistogram.m_positionMinCutoff.doubleValue());
		m_task.setHighCutoff(imageHistogram.m_positionMaxCutoff.doubleValue());
	}
	
	@FXML
	public void doCircleHoughTransform() {
		
		m_task.setProgressBar(findProgressBar);
		
		Task<Void> task = new Task<Void>() {
			@Override public Void call()
			{
				FilterGridPoints filter = new FilterGridPoints();
				List<ClusterMinima> circles = m_task.findCircles();
				
				// m_foundHoles = circles;
				// TODO: improve filtering.
				m_foundHoles = filter.findGridPoints(circles);
				
				if (m_foundHoles.size() > 0) {
					addPoints(m_foundHoles, m_targetMap);
				}
				
				return null;
			}
		};
		
	    Thread thread = new Thread(task);
	    thread.setDaemon(true);
	    thread.start();
	}
	
	@FXML
	public void doResetHoles() {
		resetCirclePositions();
		updateCanvas();
	}
	
	private void addPoints(List<ClusterMinima> points, IMap map) {
		List<Vector2<Float>> parsedPositions= new ArrayList<Vector2<Float>>(); 
		for (ClusterMinima pt : points) {
			parsedPositions.add(new Vector2<Float>(new Float(pt.center.x), new Float( pt.center.y) )); 
		}
		
		m_pixelPositions = new PixelPositionDataset();
		m_pixelPositions.setGroupID(m_document.getData().getUniqueGroupID());
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
		
		if (targetMapCombo.getItems().size() > 0) {
			targetMapCombo.getSelectionModel().select(0);
			// updateTargetMap();
		}
	}
	
	/**
	 * Set the target map.
	 * @param selected
	 */
	public void setComboSelected(IMap selected) {
		boolean update = false;
		for (int i = 0; i < targetMapCombo.getItems().size(); i++) {
			if (targetMapCombo.getItems().get(i) == selected) {
				targetMapCombo.getSelectionModel().select(i);
				update = true;
			}
		}
		//if (update) {
			updateTargetMap();
		//}
	}
	
	/**
	 * Should switch the edge detection filter.
	 */
	public void updateEdgeDetection() {
		// m_task.setEdgeFilter(edgeAlgorithmCombo.getValue());
		BufferedImage proc = m_task.getProcessed(true);
		m_edgeDetectImage =  SwingFXUtils.toFXImage(proc, null);
		redrawPreviewCanvas();
	}
	
	public void updateCanvas() {
		redrawResultsCanvas();
		redrawPreviewCanvas();
	}
	
	public void redrawResultsCanvas() {
		double width = resultsCanvas.getWidth();
		double height = resultsCanvas.getHeight();
		
		GraphicsContext gc = resultsCanvas.getGraphicsContext2D();
		gc.save();
		gc.clearRect(0, 0, width, height);
		gc.restore();
		// gc.setFill(Color.BLACK);
		// gc.fillRect(0, 0, width, height);
		
		if (m_src != null) {
			double mxx = width / m_src.getImageWidth();
			double myy = height / m_src.getImageHeight();
			
			m_src.drawImage(resultsCanvas, Transform.affine(mxx, 0, 0, myy, 0f, 0f), false);
		}
		
		if (showHoles.getValue() && m_src != null) {
			
			double mxx = width / m_src.getImageWidth();
			double myy = height / m_src.getImageHeight();
			
			gc.save();
			gc.setTransform(Transform.affine(mxx, 0, 0, myy, 0f, 0f));
			
			// Draw each checked off points set.
			gc.setGlobalBlendMode(null);
			drawPixels(gc, m_pixelPositions, NavigatorColorEnum.Red);
			drawCircles(gc, m_foundHoles, Color.RED);
			
			gc.restore();
		}
	}
	
	public void redrawPreviewCanvas() {
		double width = previewCanvas.getWidth();
		double height = previewCanvas.getHeight();
		
		GraphicsContext gc = previewCanvas.getGraphicsContext2D();
		gc.save();
		gc.clearRect(0, 0, width, height);
		gc.restore();
		// gc.setFill(Color.BLACK);
		// gc.fillRect(0, 0, width, height);
		
		if (m_greyScaleImage != null && showBWImage.getValue()) {
			double mxx = width / m_greyScaleImage.getWidth();
			double myy = height / m_greyScaleImage.getHeight();
			
			gc.save();
			gc.setTransform(Transform.affine(mxx, 0, 0, myy, 0f, 0f));
			gc.drawImage(m_greyScaleImage,  0,  0);
			gc.restore();
		}

		// Set color effects
		if (m_edgeDetectImage != null && showEdgeImage.getValue()) {
			double mxx = width / m_edgeDetectImage.getWidth();
			double myy = height / m_edgeDetectImage.getHeight();
			
			gc.save();
			gc.setTransform(Transform.affine(mxx, 0, 0, myy, 0f, 0f));
			gc.setGlobalBlendMode(BlendMode.SCREEN);
			gc.drawImage(m_edgeDetectImage,  0,  0);
			gc.restore();
		}
		
		if (showHoles.getValue() && m_src != null) {
			
			double mxx = width / m_src.getImageWidth();
			double myy = height / m_src.getImageHeight();
			
			gc.save();
			gc.setTransform(Transform.affine(mxx, 0, 0, myy, 0f, 0f));
			
			// Draw each checked off points set.
			gc.setGlobalBlendMode(null);
			drawPixels(gc, m_pixelPositions, NavigatorColorEnum.Red);
			drawCircles(gc, m_foundHoles, Color.RED);
			
			gc.restore();
		}
	}
	
	private void resetCirclePositions() {
		m_pixelPositions = new PixelPositionDataset();
		m_foundHoles = new ArrayList<ClusterMinima>();
		findProgressBar.setProgress(0.0f);
	}
	
	/**
	 * Helper function to draw circles
	 * @param positions
	 */
	private void drawCircles(GraphicsContext gc, List<ClusterMinima> positions, Color color) {
		if (positions == null) return;
		// GraphicsContext gc = previewCanvas.getGraphicsContext2D();
		
		for (ClusterMinima p : positions) {
			gc.setStroke(color);
			gc.setLineWidth(3.0);
			gc.strokeOval(p.center.x-p.radius, p.center.y-p.radius, 2* p.radius, 2* p.radius);
		}
		
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
    	
    	for (Vector2<Float> pixel : positions.getPixelPositions()) {
    		gc.beginPath();
    		Point2D pt = new Point2D(pixel.x, pixel.y);
    		
    		gc.setStroke(c);
    		gc.setFill(c);
            gc.moveTo(pt.getX() + 2, pt.getY());
            gc.lineTo(pt.getX() - 2, pt.getY());
            gc.moveTo(pt.getX(), pt.getY() + 2);
            gc.lineTo(pt.getX(), pt.getY() - 2);
            gc.stroke();
            gc.closePath();
    	}	
    }
}
