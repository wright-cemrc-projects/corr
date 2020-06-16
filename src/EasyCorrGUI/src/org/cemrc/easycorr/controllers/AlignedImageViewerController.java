package org.cemrc.easycorr.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.cemrc.autodoc.Vector2;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.Registration;
import org.cemrc.easycorr.EasyCorrConfig;
import org.cemrc.math.AffineTransformation;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AlignedImageViewerController {

	double MIN_SCALE = 0.0001;
	double MAX_SCALE = 100;
	
	double width = 600;
	double height = 600;
	
	@FXML
	TextField zoomField;
	
	@FXML
	TextField rotationAngleEntry;
	
	@FXML
	ToggleButton showAligned;
	
	@FXML
	ToggleButton showReference;
	
	// Controlling brightness/contrast
	@FXML
	Slider brightnessSlider1;
	@FXML
	Slider contrastSlider1;
	private ColorAdjust bottomColor;
	
	@FXML
	Slider brightnessSlider2;
	@FXML
	Slider contrastSlider2;
	private ColorAdjust topColor;

	@FXML
	ScrollPane zoomPane;
	
	// Image
	private Image m_referenceImage;
	private Image m_alignedImage;
	
	// Our canvas for drawing.
	private Canvas m_zoomCanvas;
	private double m_currentRotation = 0;
	
	// The backing data.
	private EasyCorrDocument m_document; // may not be required
	
	// These are helpful for doing the alignment.
	private IMap m_referenceMap;
	private IMap m_activeMap;
	
	// These are the actual alignment values
	private double [][] m_alignmentMatrix = null;
	
	private boolean m_showReference = true;
	private boolean m_showActive = true;
	
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;
	}
	
	/**
	 * Set the active image (above layer)
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
		
		m_alignedImage = loadImage(imageLocation);
	}
	
	/**
	 * Set the reference image (lower layer)
	 * @param map
	 */
	public void setReferenceMap(IMap map) {
		m_referenceMap = map;
		
		File imageLocation = map.getImage();
		if (! imageLocation.exists()) {
			File altImage = map.getAltImage();
			if ( altImage != null && altImage.exists()) {
				imageLocation = altImage;
			}
		}
		
		m_referenceImage = loadImage(imageLocation);
		
		double height = m_referenceImage.getHeight();
		double width = m_referenceImage.getWidth();
		
		if (height > m_zoomCanvas.getHeight()) {
			m_zoomCanvas.setHeight(height);
		}
		
		if (width > m_zoomCanvas.getWidth()) {
			m_zoomCanvas.setWidth(width);
		}
	}
	
	/**
	 * If there are both active and reference points, can create an alignment.
	 */
	public void setAligned(boolean isAligned) {
		if (isAligned) {
			m_alignmentMatrix = m_activeMap.getRegistration().getPixelMatrix();
		} else {
			m_alignmentMatrix = null;
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
		
		setupSliders();
	}
	
	private void setupSliders() {
		ChangeListener<Number> updateUI = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				updateZoomCanvas();
			}
			
		};
		
		// Default brightness/contrast options
		bottomColor = new ColorAdjust();
		bottomColor.brightnessProperty().bind(brightnessSlider1.valueProperty());
		bottomColor.contrastProperty().bind(contrastSlider1.valueProperty());
		bottomColor.brightnessProperty().addListener(updateUI);
		bottomColor.contrastProperty().addListener(updateUI);
		
		// Default brightness/contrast options
		topColor = new ColorAdjust();
		topColor.brightnessProperty().bind(brightnessSlider2.valueProperty());
		topColor.contrastProperty().bind(contrastSlider2.valueProperty());
		topColor.brightnessProperty().addListener(updateUI);
		topColor.contrastProperty().addListener(updateUI);
	}
	
	public void updateZoomText() {
		double currentZoom = m_zoomCanvas.getScaleY();
		currentZoom *= 100.0;
		
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		decimalFormat.setMaximumFractionDigits(2);
		zoomField.setText(decimalFormat.format(currentZoom));
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
	
    /**
     * Sets the transform for the GraphicsContext to rotate around a pivot point.
     *
     * @param gc the graphics context the transform to applied to.
     * @param angle the angle of rotation.
     * @param px the x pivot co-ordinate for the rotation (in canvas co-ordinates).
     * @param py the y pivot co-ordinate for the rotation (in canvas co-ordinates).
     */
    private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }
	
	
	public void updateZoomCanvas() {
		GraphicsContext gc = m_zoomCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, m_zoomCanvas.getWidth(), m_zoomCanvas.getHeight());

		// TODO: apply a rotation transform for each image?
		// Rotate r = new Rotate(m_currentRotation, m_currentRotation, m_zoomCanvas.getWidth() / 2.0, m_zoomCanvas.getHeight() / 2.0);
		
		// Set the rotation
		rotate(gc, m_currentRotation, m_zoomCanvas.getWidth() / 2.0 , m_zoomCanvas.getHeight() / 2.0);
	
		
		if (m_referenceImage != null && m_showReference) {
			gc.save();

			// Set color effects
			gc.setEffect(bottomColor);
			
			gc.drawImage(m_referenceImage, 0, 0);
			
			gc.restore();
		}
		
		// The aligned image should be drawn transformed and with transparency.
		// How do I apply the 3x3 matrix or as 2x2 matrix /w translation component?
		
		if (m_alignedImage != null && m_showActive) {
			gc.save();
			
			gc.setGlobalBlendMode(BlendMode.OVERLAY);
	
			// Set color effects
			gc.setEffect(topColor);
			
			// This needs to also take into account rotations.
			// Is there a transform to apply?
			if (m_alignmentMatrix != null) {
				double mxx = m_alignmentMatrix[0][0];
				double mxy = m_alignmentMatrix[1][0];
				double myx = m_alignmentMatrix[0][1];
				double myy = m_alignmentMatrix[1][1];
				double tx = m_alignmentMatrix[0][2];
				double ty = m_alignmentMatrix[1][2];
				
				gc.setTransform(mxx, mxy, myx, myy, tx, ty);
			}
			
	        gc.drawImage(m_alignedImage, 0, 0);
	        
	        gc.setEffect(null);
	        
	        gc.restore();
		}
	}
	
	private Image loadImage(File file) {
		
		// Method with ImagoIO (JAI core extension for TIFF)
		ImageInputStream is;
		try {
			is = ImageIO.createImageInputStream(file);  //read tiff using imageIO (JAI component)
			
			if (is == null || is.length() == 0) {
				Alert errorAlert = new Alert(AlertType.ERROR);
				errorAlert.setHeaderText("Input not valid");
				errorAlert.setContentText("Cannot find image at this location: " + file.getAbsolutePath());
				errorAlert.showAndWait();
			} else {
			
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
				if (iterator == null || !iterator.hasNext()) {
				    throw new IOException("Image file format not supported by ImageIO: " + file.getAbsolutePath());
				}
				ImageReader reader = (ImageReader) iterator.next();
				reader.setInput(is);
				
				int nbPages = reader.getNumImages(true);
				
				if (nbPages > 0) {
					BufferedImage bf = reader.read(0);   //1st page of tiff file
					WritableImage wr = null;
					if (bf != null) {
					    wr= SwingFXUtils.toFXImage(bf, null);   //convert bufferedImage (awt) into Writable Image(fx)
					}
					return wr;
				}
			}  
		} catch (FileNotFoundException ex) {
	        ex.printStackTrace();
		} catch (IOException ex) {
		        ex.printStackTrace();
		}
		
		return null;
	}
	
	@FXML
	public void onAlignedPressed() {
		m_showActive = !m_showActive;
		updateZoomCanvas();
	}
	
	@FXML
	public void onReferencePressed() {
		m_showReference = !m_showReference;
		updateZoomCanvas();
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
