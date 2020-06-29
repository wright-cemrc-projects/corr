package org.cemrc.easycorr.controllers;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;
import org.cemrc.data.PixelPositionDataset;
import org.cemrc.easycorr.EasyCorrConfig;
import org.cemrc.easycorr.io.ReadImage;
import org.cemrc.math.MatrixMath;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
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
	
	double MIN_SCALE = 0.0001;
	double MAX_SCALE = 100;
	
	double width = 600;
	double height = 600;
	
	double globalMaxWidth = 400;
	double globalMaxHeight = 400;
	double globalFitFactor = 1f;

	public enum PointState { None, Add, Remove };
	public PointState mode = PointState.None;
	
	@FXML
	ScrollPane zoomPane;
	
	@FXML
	StackPane fullPane;
	
	@FXML
	ImageView imageViewFull;
	
	@FXML
	TableView<PointsTableController.PointsDatasetTableItem> pointsTableView;
	PointsTableController m_pointsTableController;
	
	@FXML
	Button newPoints;
	
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
	
	// Our canvas for drawing.
	private Canvas m_zoomCanvas;
	private double m_currentRotation = 0;
	
	// The backing data.
	private EasyCorrDocument m_document;
	private IMap m_activeMap;
	private Image m_image = null;
	
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;
		m_pointsTableController.setDocument(doc);
	}

	@FXML
	public void addPointsPressed() {
		if (addButton.isSelected()) {
			mode = PointState.Add;
		} else {
			mode = PointState.None;
		}
	}
	
	@FXML
	public void removePointsPressed() {
		if (removeButton.isSelected()) {
			mode = PointState.Remove;
		} else {
			mode = PointState.None;
		}
	}
	
	@FXML
	public void createPositionDataset() {
		PixelPositionDataset dataset = new PixelPositionDataset();
		dataset.setMap(m_activeMap);
		dataset.setDrawnID(m_activeMap.getId());
		dataset.setRegisID(m_activeMap.getRegis());
		
		m_document.getData().addPositionData(dataset);		
		m_pointsTableController.updatePointsTableView();
		m_pointsTableController.select(dataset);
	}
	
	@FXML
	public void zoomChanged() {
		String text = zoomField.getText();
		
		try {
			float value = Float.parseFloat(text);

			if (value >= 0.0f && value <= 100.0f) {
				double scale = value /= 100.0;
				
				m_zoomCanvas.setScaleX(scale);
				m_zoomCanvas.setScaleY(scale);
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
				rotationAngleEntry.setText(Double.toString(m_currentRotation));
			} else {
				m_currentRotation = value;
				updateZoomCanvas();
			}
			
		} catch (NumberFormatException ex) {
		}
	}
	
	public void updateZoomText() {
		double currentZoom = m_zoomCanvas.getScaleY();
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
	}
	
	@FXML
	public void updatePoints() {
		updateZoomCanvas();
	}
	
	private void loadImage(File file) {
		
		m_image = ReadImage.readImage(file);
		
		if (m_image != null) {
			imageViewFull.setImage(m_image);
		
			if (width < m_image.getWidth()) {
				width = m_image.getWidth();
			}
			
			if (height < m_image.getHeight()) {
				height = m_image.getHeight();
			}
			
			// Setup the zoom view
			m_zoomCanvas.setWidth(width);
			m_zoomCanvas.setHeight(height);
			zoomPane.setPrefViewportWidth(600);
			zoomPane.setPrefViewportHeight(300);
			
			updateZoomCanvas();
		}
	}
	
    /**
     * Sets the transform for the GraphicsContext to rotate around a pivot point.
     *
     * @param gc the graphics context the transform to applied to.
     * @param angle the angle of rotation.
     * @param px the x pivot co-ordinate for the rotation (in canvas co-ordinates).
     * @param py the y pivot co-ordinate for the rotation (in canvas co-ordinates).
     */
    private Rotate getRotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        return r;
    }
	
    /**
     * Draw crosshair pixel positions in a color on the canvas.
     * @param gc
     * @param pixelPositions
     * @param colorId
     */
    private void drawPixels(GraphicsContext gc, IPositionDataset positions, NavigatorColorEnum color, Affine t) {
    	
    	if (positions == null) return;
    	
    	Point2D offset = new Point2D(-10f, -5f);
    	int i = 1;
    	
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
    		Point2D movedPt = t.transform(pt);
    		
    		gc.setStroke(c);
    		gc.setFill(c);
            gc.moveTo(movedPt.getX() + 2, movedPt.getY());
            gc.lineTo(movedPt.getX() - 2, movedPt.getY());
            gc.moveTo(movedPt.getX(), movedPt.getY() + 2);
            gc.lineTo(movedPt.getX(), movedPt.getY() - 2);
            gc.stroke();
            
            // TODO, make this optional.
            drawLabelText(gc, movedPt, offset, Integer.toString(i));
            i++;
    	}	
    	gc.closePath();
    }
    
    private void drawLabelText(GraphicsContext gc, Point2D pixel, Point2D offset, String text) {
    	gc.fillText(text, pixel.getX() + offset.getX(), pixel.getY() + offset.getY());
    }
	
	public void updateZoomCanvas() {
		GraphicsContext gc = m_zoomCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, m_zoomCanvas.getWidth(), m_zoomCanvas.getHeight());

		// Create an affine transformation from a rotation.
		Rotate r = getRotate(gc, m_currentRotation, m_zoomCanvas.getWidth() / 2.0 , m_zoomCanvas.getHeight() / 2.0);
		//Affine t = new Affine(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
		Affine t = new Affine(r.getMxx(), r.getMxy(), r.getTx(), r.getMyx(), r.getMyy(), r.getTy());
		
		// Save the transform state
		gc.save();
        gc.setTransform(t);
		
		// Set color effects
		gc.setEffect(colorAdjust);
		gc.drawImage(m_image,  0,  0);

		// Restore transform state
		gc.restore();
		
		// Draw each checked off points set.
		List<IPositionDataset> drawPoints = m_pointsTableController.getVisible(m_activeMap);
		
		for (IPositionDataset item : drawPoints) {
			drawPixels(gc, item, item.getColor(), t);
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
		
		// Setup a Canvas and make this drawable.
		m_zoomCanvas = new Canvas(width, height);
		
		// Maybe better PannableCanvas example.
		// https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
		m_zoomCanvas.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
				double delta = 1.2;
				
	            double scale = m_zoomCanvas.getScaleY(); // currently we only use Y, same value is used for X
	            double oldScale = scale;
				
	            if (event.getDeltaY() < 0)
	                scale /= delta;
	            else
	                scale *= delta;

	            scale = clamp( scale, MIN_SCALE, MAX_SCALE);

	            double f = (scale / oldScale)-1;

	            double dx = (event.getSceneX() - (m_zoomCanvas.getBoundsInParent().getWidth()/2 + m_zoomCanvas.getBoundsInParent().getMinX()));
	            double dy = (event.getSceneY() - (m_zoomCanvas.getBoundsInParent().getHeight()/2 + m_zoomCanvas.getBoundsInParent().getMinY()));

	            m_zoomCanvas.setScaleX( scale);
	            m_zoomCanvas.setScaleY( scale);

	            // note: pivot value must be untransformed, i. e. without scaling
	            // m_zoomCanvas.setPivot(f*dx, f*dy);
	            updateZoomText();

	            event.consume();
			}
			
		});
		
		// zoomPane.setMaxSize(m_zoomCanvas.getWidth(), m_zoomCanvas.getHeight());
		zoomPane.setFitToHeight(true);
		zoomPane.setFitToWidth(true);
		zoomPane.setContent(m_zoomCanvas);
		
		
		//fullPane.getChildren().add(m_globalCanvas);
		//fullPane.setPrefSize(globalMaxWidth, globalMaxHeight);
		
		m_zoomCanvas.setOnMouseClicked(event -> {
			canvasClickedCallback(event.getX(), event.getY());
		});
		
		colorAdjust = new ColorAdjust();
		colorAdjust.brightnessProperty().bind(brightnessSlider1.valueProperty());
		colorAdjust.contrastProperty().bind(contrastSlider1.valueProperty());
		
		ChangeListener<Number> updateUI = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				updateZoomCanvas();
			}
			
		};
		
		colorAdjust.brightnessProperty().addListener(updateUI);
		colorAdjust.contrastProperty().addListener(updateUI);
		imageViewFull.setEffect(colorAdjust);
	}
	
	/**
	 * Rotation
	 * @param x
	 * @param y
	 * @return
	 */
	private Vector3<Float> getActualPixelPosition(double x, double y) {
		double center_x = m_zoomCanvas.getWidth() / 2.0;
		double center_y = m_zoomCanvas.getHeight() / 2.0;
		
		// subtract by pivot point
		double pixel_x = x - center_x;
		double pixel_y = y - center_y;
		
		// rotate by rotation matrix
		double rotationRadians = -m_currentRotation * Math.PI / 180.0;
		double [][] rotationMatrix = MatrixMath.getRotation(rotationRadians);
		Vector3<Float> rv = MatrixMath.multiply(rotationMatrix, new Vector3<Float>((float) pixel_x, (float) pixel_y, 0f));
		
		// add back the pivot point
		rv.x = rv.x + (float) center_x;
		rv.y = rv.y + (float) center_y;
		
		return rv;
	}
	
	public void canvasClickedCallback(double x, double y) {
		double near = 5;
		
		Vector3<Float> actualPosition = getActualPixelPosition(x, y);
		
		IPositionDataset activePoints = m_pointsTableController.getSelected();
		
		if (activePoints != null) {
			switch (mode) {
			case Add:
				activePoints.addPixelPosition(actualPosition.x, actualPosition.y);
				break;
			case Remove:
				activePoints.removePixelPositionNear(actualPosition.x, actualPosition.y, near);
				break;
			default:
				break;
			}
		}
		
		updateZoomCanvas();
		pointsTableView.refresh();
	}
	
	@FXML
	public void doSaveImage() {
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save the overlay image.");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.png)", "*.png");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
        	WritableImage saveImage = new WritableImage((int) m_zoomCanvas.getWidth(), (int) m_zoomCanvas.getHeight());
        	m_zoomCanvas.snapshot(null, saveImage);
        	
            BufferedImage bImage = SwingFXUtils.fromFXImage(saveImage, null);
            try {
              ImageIO.write(bImage, "png", file);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
        }
	}
}
