package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.canvas.PanAndZoomPane;
import org.cemrc.correlator.data.IMapImage;
import org.cemrc.correlator.io.ImageProvider;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.NavigatorColorEnum;
import org.cemrc.data.PixelPositionDataset;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

/**
 * Simple controller class to allow placing registration points on a map.
 * @author mrlarson2
 *
 */
public class ImageRegistrationController {
	
	// 1.0 = 100%
	double MIN_SCALE = 0.0001;
	double MAX_SCALE = 100;
	
	// Describe the bounds.
	final static int MAX_OVERVIEW_WIDTH = 300;
	final static int MAX_OVERVIEW_HEIGHT = 300;
	
	double width = 600;
	double height = 600;
	
	double globalMaxWidth = 400;
	double globalMaxHeight = 400;
	double globalFitFactor = 1f;
	
	// The stage this belongs to.
	private Stage m_stage = null;
	
	// When displaying or viewing points, what index to use?
	private int m_registrationIndex = 0;
	
	// Differentiate between dragging and clicking on canvas.
	private boolean m_dragging = false;
	
	@FXML
	ScrollPane scrollPane;
	
	@FXML
	StackPane fullPane;
	
	// For canvas operations, zoom + pan.
	PanAndZoomPane m_zoomPane;
	
	// Need to make this added in the fullPane.
	Canvas m_navigationCanvas;
	
	@FXML
	CheckBox flipx;
	
	@FXML
	CheckBox flipy;
	
	@FXML
	Button newPoints;
	
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
	
	@FXML
	Button importRegistrationPairButton;
	
	// This TableView should get updated with registration points.
	@FXML
	private TableView registrationTable;
	RegistrationTableController m_registrationTableController;
	
	// The backing data.
	private CorrelatorDocument m_document;
	private IMap m_activeMap;
	private IMapImage m_mapImage;
	
	private RegistrationPairState m_registrationState;
	
	// An image with software-based brightness/contrast
	// private AdjustableImage m_image = null;
	
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
	}
	
	private void unsetActions() {
		m_zoomPane.setPointState(PanAndZoomPane.PointState.None);
	}
	
	@FXML
	public void createPositionDataset() {
		PixelPositionDataset dataset = new PixelPositionDataset();
		dataset.setMap(m_activeMap);
		dataset.setDrawnID(m_activeMap.getId());
		dataset.setRegisID(m_activeMap.getRegis());
		dataset.setGroupID(m_document.getData().getUniqueGroupID());
		dataset.setName("Group " + dataset.getGroupID());
		m_document.getData().addPositionData(dataset);		
		m_document.setDirt(true);
		m_document.setDirt(true);
		m_document.getData().forceUpdate();
	}
	
	@FXML
	public void zoomChanged() {
	
		String text = zoomField.getText();
		
		try {
			// Convert from percentage back to 1.0 scale.
			float scale = Float.parseFloat(text) / 100.0f;
			
			if (scale >= 0.0f && scale <= MAX_SCALE) {		
				m_zoomPane.setScale(scale);
				updateZoomCanvas();
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
		updateZoomCanvas();
	}
	
	/**
	 * Set the registration pair state
	 * (Required to be able to place and draw points)
	 * @param table
	 */
	public void setRegistrationState(RegistrationPairState state) {
		m_registrationState = state;
		m_registrationTableController.setState(state);
	}
	
	/**
	 * Get a title for the window
	 * @return
	 */
	public String getTitle() {
		
		StringBuilder titleBuilder = new StringBuilder();
		titleBuilder.append(CorrelatorConfig.AppName);
		titleBuilder.append(" ");

		if (m_activeMap != null) {
			titleBuilder.append("(" + m_activeMap.getName() + ")");		
		}
		
		// Stage is where visual parts of JavaFX application are displayed.
      return titleBuilder.toString();
	}
	
	@FXML
	public void updatePoints() {
		updateZoomCanvas();
	}
	
	private void loadImage(File file) {
		
		m_mapImage = ImageProvider.readImage(file);
		
		// Setup the zoom view
		m_zoomPane.getCanvas().setWidth(m_mapImage.getImageWidth());
		m_zoomPane.getCanvas().setHeight(m_mapImage.getImageHeight());
		m_zoomPane.getCanvas().setTranslateX(m_zoomPane.getWidth() / 2);
		m_zoomPane.getCanvas().setTranslateY(m_zoomPane.getHeight() / 2);
		
		// Setup a canvas for a miniature view at the bottom.
		// Determine a scale based on what is needed to fit vertically or horizontally into the Canvas.
		double widthRatio = (double) MAX_OVERVIEW_WIDTH / (double) m_mapImage.getImageWidth();
		double heightRatio = (double) MAX_OVERVIEW_HEIGHT / (double) m_mapImage.getImageHeight();
		
		double scaleFactor = widthRatio < heightRatio ? widthRatio : heightRatio;
		
		m_navigationCanvas.setWidth(m_mapImage.getImageWidth() * scaleFactor);
		m_navigationCanvas.setHeight(m_mapImage.getImageHeight() * scaleFactor);
		m_navigationCanvas.setTranslateX(0);
		m_navigationCanvas.setTranslateY(0);
		
		updateZoomCanvas();
	}
	
	public void updateZoomCanvas() {
		m_zoomPane.clearCanvas();
		
		// Calculate the current affine transform based on rotate and flips.
		Affine mat = m_zoomPane.getMat();
		
		if (m_mapImage != null) {
			m_mapImage.drawImage(m_zoomPane.getCanvas(), mat, false);
			// m_zoomPane.drawImage(m_image.getImage(), mat, false);
		}
		
		// Label positions that need to be drawn.
		Map<Integer, Vector2<Float>> points = new HashMap<Integer, Vector2<Float>>();
		
		if (m_registrationState != null && m_registrationState.getRegistrationList() != null) {
			// Draw each of the registration points on the map.
			for (RegistrationPair item : m_registrationState.getRegistrationList()) {
				// m_zoomPane.drawPositions(item, mat);
	
				if (m_activeMap == item.getMap(RegistrationPair.REFERENCE_ID)) {
					points.put(item.getId(), item.getPoint(RegistrationPair.REFERENCE_ID));
				} else if (m_activeMap == item.getMap(RegistrationPair.TARGET_ID)) {
					points.put(item.getId(), item.getPoint(RegistrationPair.TARGET_ID));
				}
				 
				// points.put(new Integer(i++), new Vector3<Float>(position.x, position.y, 0f));
				// m_zoomPane.drawLabels(points, mat, item.getColor());
			}
		}
		
		m_zoomPane.drawPositions(new ArrayList<Vector2<Float>>(points.values()), NavigatorColorEnum.Red, mat);
		m_zoomPane.drawLabel2D(points, mat, NavigatorColorEnum.Red);
		
		// Also update the little view.
		updateOverviewCanvas();
	}
	
	public void updateOverviewCanvas() {
		
		// Must be a valid image to draw
		if (m_mapImage.getImageHeight() == 0 || m_mapImage.getImageWidth() == 0) {
			return;
		}
		
		// What are the extents of a visible canvas in the ScrollPane/Pane?

		// The bound box changes with dragging
		Bounds b = m_zoomPane.getCanvas().getBoundsInParent();

		// The bound box doesn't change with zoom
		double scale = m_zoomPane.getScale();
		
		// System.out.println(b.getMinX() + " " + b.getMinY() + " " + b.getMaxX() + " " + b.getMaxY() + " " + scale);
		
		// At start, 0.0 and 0.0 is the canvas translation, scale is 1.0
		// Other bounds describe the overall dimensions.
		
		// I need to know the size of the viewport
		// Then offset that viewport with b.getMinX() and b.getMinY()
		// And consider the scale factor!
		Bounds sp = scrollPane.getBoundsInLocal();

		// Determine a scale based on what is needed to fit vertically or horizontally into the Canvas.
		double widthRatio = (double) MAX_OVERVIEW_WIDTH / (double) m_mapImage.getImageWidth();
		double heightRatio = (double) MAX_OVERVIEW_HEIGHT / (double) m_mapImage.getImageHeight();
		
		double scaleFactor = widthRatio < heightRatio ? widthRatio : heightRatio;
		
		// Set the transform
		Affine mat = new Affine(new Scale (scaleFactor, scaleFactor));
		
		GraphicsContext gc = m_navigationCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, m_navigationCanvas.getWidth(), m_navigationCanvas.getHeight());
		
		// Draw image
		m_mapImage.drawImage(m_navigationCanvas, mat, false);
		
		// Draw bounding box
		// TODO: ugh, this is almost correct but its behaving oddly with the left corner X/Y.
		double dx = (sp.getMaxX() - sp.getMaxX() / scale) / 2;
		double dy = (sp.getMaxY() - sp.getMaxY() / scale) / 2;
		
		// double box_xl = - b.getMinX();
		// double box_yl = - b.getMinY();
		// double box_xr = - b.getMinX() + sp.getMaxX() / scale;
		// double box_yr = - b.getMinY() + sp.getMaxY() / scale;
		
		double box_xl = - b.getMinX() + dx;
		double box_yl = - b.getMinY() + dy;
		double box_xr = - b.getMinX() + sp.getMaxX() / scale + dx;
		double box_yr = - b.getMinY() + sp.getMaxY() / scale + dy;
		
		// Clamp to limit the min/max view boxes.
		//if (box_xl < 0) box_xl = 0;
		//if (box_yl < 0) box_yl = 0;
		//if (box_xr > m_navigationCanvas.getWidth()) box_xr = m_navigationCanvas.getWidth();
		//if (box_yr > m_navigationCanvas.getHeight()) box_yr = m_navigationCanvas.getHeight();
		
		// Let's describe how the viewport would be in the canvas.
		Vector3<Float> pt1 = m_zoomPane.getActualPixelPosition(box_xl, box_yl);
		Vector3<Float> pt2 = m_zoomPane.getActualPixelPosition(box_xl, box_yr);
		Vector3<Float> pt3 = m_zoomPane.getActualPixelPosition(box_xr, box_yr);
		Vector3<Float> pt4 = m_zoomPane.getActualPixelPosition(box_xr, box_yl);
		
		// Save the transform state
		gc.save();
        gc.setTransform(mat);
        
		Color c = Color.RED;
        // Draw some lines.
		gc.beginPath();
		gc.setStroke(c);
		gc.setFill(c);
		gc.setLineWidth(1.0 / scaleFactor);
		
        gc.moveTo(pt1.x, pt1.y);
        gc.lineTo(pt2.x, pt2.y);
        gc.moveTo(pt2.x, pt2.y);
        gc.lineTo(pt3.x, pt3.y);
        
        gc.moveTo(pt3.x, pt3.y);
        gc.lineTo(pt4.x, pt4.y);
        gc.moveTo(pt4.x, pt4.y);
        gc.lineTo(pt1.x, pt1.y);
        
        gc.stroke();
        gc.closePath();
        
        gc.restore();
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
		
		// TODO: disabled until feature to import sets of unpaired points.
		importRegistrationPairButton.setVisible(false);
		
		// TODO: need a mechanism to share the same state among all the GUI windows
		m_registrationTableController = new RegistrationTableController(registrationTable);
		m_registrationTableController.getState().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO: updateGUI();
			}
		});
		
		ToggleGroup group = new ToggleGroup();        
        zoomField.textProperty().addListener((observable, oldValue, newValue) -> {
            zoomChanged();
        });
        
        m_zoomPane = new PanAndZoomPane();
        m_zoomPane.addDragListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				updateOverviewCanvas();
			}
			
		});
        
        // Setup the bottom canvas.
        m_navigationCanvas = new Canvas();
        fullPane.getChildren().add(m_navigationCanvas);
			
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
		
		m_zoomPane.getCanvas().setOnMousePressed(event -> {
			// TODO: would be nice to distinguish between click and dragging
			// https://stackoverflow.com/questions/41655507/javafx-distinguish-drag-and-click
			canvasClickedCallback(event.getX(), event.getY());
		});
		
		colorAdjust = new ColorAdjust();
		colorAdjust.brightnessProperty().bind(brightnessSlider1.valueProperty());
		colorAdjust.contrastProperty().bind(contrastSlider1.valueProperty());
		
		ChangeListener<Number> updateUI = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (m_mapImage != null) {
					m_mapImage.adjustImage((float)colorAdjust.getBrightness(), (float)colorAdjust.getContrast());
				}
				updateZoomCanvas();
			}
			
		};
		
		colorAdjust.brightnessProperty().addListener(updateUI);
		colorAdjust.contrastProperty().addListener(updateUI);
	}
	
	public void canvasClickedCallback(double x, double y) {
		Vector3<Float> actualPosition = m_zoomPane.getActualPixelPosition(x, y);
		
		if (m_registrationState.getSelected() != null) {
			if (m_registrationState.getSelected() != null) {
				m_registrationState.getSelected().setPoint(m_registrationIndex, new Vector2<Float>(actualPosition.x, actualPosition.y));
				m_registrationState.getSelected().setMap(m_registrationIndex, m_activeMap);
				m_registrationState.forceUpdate("TEXT_CHANGED");
			}
		}
		updateZoomCanvas();
	}
	
	@FXML
	public void addRegistrationPair() {
		// Create a new row entry in the list.
		m_registrationState.addEmptyPair();
	}
	
	@FXML
	public void removeRegistrationPair() {
		// If there is an active/highlighted row, it could be removed.
		RegistrationPair selected = m_registrationState.getSelected();
		m_registrationState.removePair(selected);
	}
	
	@FXML
	public void importRegistrationPair() {
		// TODO: this should require user to choose pixel positions to add to the registration list.
		// It could use a simple dialog dropdown that adds from a IPositionDataset list / group.
	}

	/**
	 * Pixel positions are set based on this index in a RegistrationPair
	 * @return
	 */
	public int getRegistrationIndex() {
		return m_registrationIndex;
	}

	/**
	 * Pixel positions are set based on this index in a RegistrationPair
	 * @param registrationIndex
	 */
	public void setRegistrationIndex(int registrationIndex) {
		m_registrationIndex = registrationIndex;
	}
	
}
