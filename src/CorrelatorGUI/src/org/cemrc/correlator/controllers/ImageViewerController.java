package org.cemrc.correlator.controllers;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.canvas.PanAndZoomPane;
import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.PixelPositionDataset;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * A controller class for the image view.
 * @author larso
 *
 *
 * Goals: 
 *  1. to have a top "zoomed" canvas that focuses on a small portion of the overall image
 *  2. to have a bottom scaled canvas that shows the entire image in a max dimension (globalMaxWidth, globalMaxHeight).
 *  
 *  Problems:
 *  1. the top ScrollPane is getting shrunken
 *  2. the bottom StackPane isn't being the correct correct (globalMaxWidth, globalMaxHeight).
 *
 */
public class ImageViewerController {
	
	// 1.0 = 100%
	double MIN_SCALE = 0.0001;
	double MAX_SCALE = 100;
	
	double width = 600;
	double height = 600;
	
	double globalMaxWidth = 400;
	double globalMaxHeight = 400;
	double globalFitFactor = 1f;
	
	@FXML
	ScrollPane scrollPane;
	
	@FXML
	StackPane fullPane;
	
	// For canvas operations, zoom + pan.
	PanAndZoomPane m_zoomPane;
	
	@FXML
	ImageView imageViewFull;
	
	@FXML
	TableView<PointsTableController.PointsDatasetTableItem> pointsTableView;
	PointsTableController m_pointsTableController;
	
	@FXML
	CheckBox flipx;
	
	@FXML
	CheckBox flipy;
	
	@FXML
	Button newPoints;
	
	@FXML
	ToggleButton moveButton;
	
	@FXML
	ToggleButton addButton;
	
	@FXML
	ToggleButton removeButton;
	
	@FXML
	TextField zoomField;
	
	@FXML
	TextField rotationAngleEntry;
	
	// Controlling brightness/contrast
	@FXML
	Slider brightnessSlider1;
	@FXML
	Slider contrastSlider1;
	private ColorAdjust colorAdjust;
	
	// The backing data.
	private CorrelatorDocument m_document;
	private IMap m_activeMap;
	
	// An image with software-based brightness/contrast
	private AdjustableImage m_image = null;
	
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
		m_pointsTableController.setDocument(doc);
	}
	
	private void unsetActions() {
		m_zoomPane.setPointState(PanAndZoomPane.PointState.None);
	}
	
	@FXML
	public void movePressed() {
		if (moveButton.isSelected()) {
			m_zoomPane.setPointState(PanAndZoomPane.PointState.Move);
		} else {
			unsetActions();
		}
	}

	@FXML
	public void addPointsPressed() {
		if (addButton.isSelected()) {
			m_zoomPane.setPointState(PanAndZoomPane.PointState.Add);
		} else {
			unsetActions();
		}
	}
	
	@FXML
	public void removePointsPressed() {
		if (removeButton.isSelected()) {
			m_zoomPane.setPointState(PanAndZoomPane.PointState.Remove);
		} else {
			unsetActions();
		}
	}
	
	@FXML
	public void createPositionDataset() {
		PixelPositionDataset dataset = new PixelPositionDataset();
		dataset.setMap(m_activeMap);
		dataset.setDrawnID(m_activeMap.getId());
		dataset.setRegisID(m_activeMap.getRegis());
		dataset.setName("Point Set " + m_document.getData().getUniquePointsID());
		
		m_document.getData().addPositionData(dataset);		
		m_document.setDirt(true);
		m_pointsTableController.updatePointsTableView();
		m_pointsTableController.select(dataset);
	}
	
	@FXML
	public void zoomChanged() {
	
		String text = zoomField.getText();
		
		try {
			// Convert from percentage back to 1.0 scale.
			float scale = Float.parseFloat(text) / 100.0f;
			
			if (scale >= 0.0f && scale <= MAX_SCALE) {		
				m_zoomPane.setScale(scale);
			}
			
		} catch (NumberFormatException ex) {
		}
	}
	
	@FXML
	public void rotateChanged() {
		String text = rotationAngleEntry.getText();
		
		try {
			float value = Float.parseFloat(text);

			if (value < -360.0f || value > 360.0f) {
				rotationAngleEntry.setText(Double.toString(m_zoomPane.getRotation()));
			} else {
				m_zoomPane.setRotation(value);
				updateZoomCanvas();
			}
			
		} catch (NumberFormatException ex) {
		}
	}
	
	public void updateZoomText() {
		double currentZoom = m_zoomPane.getScale();
		currentZoom *= 100.0;
		
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		decimalFormat.setMaximumFractionDigits(2);
		zoomField.setText(decimalFormat.format(currentZoom));
	}
	
	/**
	 * Set the active image.
	 * @param map
	 */
	public void setActiveMap(IMap map) {
		m_activeMap = map;
		
		File imageLocation = map.getImage();
		if (! imageLocation.exists()) {
			File altImage = map.getAltImage();
			if ( altImage != null && altImage.exists()) {
				imageLocation = altImage;
			}
		}
		
		loadImage(imageLocation);
		m_pointsTableController.addMap(map);
		m_pointsTableController.updatePointsTableView();
		updateZoomCanvas();
	}
	
	@FXML
	public void updatePoints() {
		updateZoomCanvas();
	}
	
	private void loadImage(File file) {
		
		m_image = new AdjustableImage(ReadImage.readImage(file));
		Image fxImage = m_image.getImage();
		
		imageViewFull.setImage(m_image.getImage());
		
		// Setup the zoom view
		m_zoomPane.getCanvas().setWidth(fxImage.getWidth());
		m_zoomPane.getCanvas().setHeight(fxImage.getHeight());
		m_zoomPane.getCanvas().setTranslateX(m_zoomPane.getWidth() / 2);
		m_zoomPane.getCanvas().setTranslateY(m_zoomPane.getHeight() / 2);
		
		updateZoomCanvas();
	}
	
	public void updateZoomCanvas() {
		m_zoomPane.clearCanvas();
		
		// Calculate the current affine transform based on rotate and flips.
		Affine mat = m_zoomPane.getMat();
		
		if (m_image != null) {
			m_zoomPane.drawImage(m_image.getImage(), mat, false);
		}
		
		// Draw each checked off points set.
		List<IPositionDataset> drawPoints = m_pointsTableController.getVisible(m_activeMap);

		for (IPositionDataset item : drawPoints) {
			m_zoomPane.drawPositions(item, mat);
			Map<Integer, Vector3<Float>> points = new HashMap<Integer, Vector3<Float>>();
			int i = 1;
			
			for (Vector2<Float> position : item.getPixelPositions()) {
				points.put(new Integer(i++), new Vector3<Float>(position.x, position.y, 0f));
			}
			
			m_zoomPane.drawLabels(points, mat);
		}
	}
	
    public static double clamp( double value, double min, double max) {

        if( Double.compare(value, min) < 0)
            return min;

        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }
	
	@FXML
	public void initialize() {
		
		// Setup the table
		m_pointsTableController = new PointsTableController(pointsTableView);
		m_pointsTableController.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateZoomCanvas();
			}
			
		});
		
		ToggleGroup group = new ToggleGroup();
		moveButton.setToggleGroup(group);
		addButton.setToggleGroup(group);
		removeButton.setToggleGroup(group);
		
		// Setup a button image
        // load the image
        Image addButtonImage = new Image("/view/addition.png");
        ImageView buttonImageView = new ImageView(addButtonImage);
        newPoints.setGraphic(buttonImageView);
        
        zoomField.textProperty().addListener((observable, oldValue, newValue) -> {
            zoomChanged();
        });
        
        m_zoomPane = new PanAndZoomPane();
			
		// Maybe better PannableCanvas example.
		// https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
        scrollPane.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
				double delta = 1.2;
				
	            double scale = m_zoomPane.getScale(); // currently we only use Y, same value is used for X
	            if (event.getDeltaY() < 0)
	                scale /= delta;
	            else
	                scale *= delta;

	            scale = clamp( scale, MIN_SCALE, MAX_SCALE);
	            m_zoomPane.setScale(scale);

	            // note: pivot value must be untransformed, i. e. without scaling
	            // m_zoomCanvas.setPivot(f*dx, f*dy);
	            updateZoomText();

	            event.consume();
			}
			
		});
	
		ChangeListener<Boolean> checkChange = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				updateZoomCanvas();
			}
		};
		
		// Add properties for changes.
		flipx.selectedProperty().bindBidirectional(m_zoomPane.flipX);
		flipy.selectedProperty().bindBidirectional(m_zoomPane.flipY);
		flipx.selectedProperty().addListener(checkChange);
		flipy.selectedProperty().addListener(checkChange);
		
		scrollPane.setContent(m_zoomPane);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		
		m_zoomPane.getCanvas().setOnMouseClicked(event -> {
			canvasClickedCallback(event.getX(), event.getY());
		});
		
		colorAdjust = new ColorAdjust();
		colorAdjust.brightnessProperty().bind(brightnessSlider1.valueProperty());
		colorAdjust.contrastProperty().bind(contrastSlider1.valueProperty());
		
		ChangeListener<Number> updateUI = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (m_image != null) {
					m_image.adjustImage((float)colorAdjust.getBrightness(), (float)colorAdjust.getContrast());
				}
				updateZoomCanvas();
			}
			
		};
		
		colorAdjust.brightnessProperty().addListener(updateUI);
		colorAdjust.contrastProperty().addListener(updateUI);
		imageViewFull.setEffect(colorAdjust);
	}
	
	public void canvasClickedCallback(double x, double y) {
		double near = 5;
		
		Vector3<Float> actualPosition = m_zoomPane.getActualPixelPosition(x, y);
		
		IPositionDataset activePoints = m_pointsTableController.getSelected();
		
		if (activePoints != null) {
			switch (m_zoomPane.getPointState()) {
			case Move:
				break;
			case Add:
				activePoints.addPixelPosition(actualPosition.x, actualPosition.y);
				m_document.setDirt(true);
				break;
			case Remove: 
				activePoints.removePixelPositionNear(actualPosition.x, actualPosition.y, near);
				m_document.setDirt(true);
				break;
			default:
				return;
			}
			
			updateZoomCanvas();
			pointsTableView.refresh();
		}
	}
	
	@FXML
	public void doSaveImage() {
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save the overlay image.");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.png)", "*.png");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
        	WritableImage saveImage = new WritableImage((int) m_zoomPane.getCanvasWidth(), (int) m_zoomPane.getCanvasHeight());
        	m_zoomPane.getCanvas().snapshot(null, saveImage);
        	
            BufferedImage bImage = SwingFXUtils.fromFXImage(saveImage, null);
            try {
              ImageIO.write(bImage, "png", file);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
        }
	}
}
