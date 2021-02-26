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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Affine;
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
	TableView<PointsTableController.PointsDatasetTableItem> pointsTableView;
	PointsTableController m_pointsTableController;
	
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
	private AdjustableImage m_referenceImage;
	private AdjustableImage m_alignedImage;
	
	// The backing data.
	private CorrelatorDocument m_document; // may not be required
	
	// These are helpful for doing the alignment.
	private IMap m_referenceMap;
	private IMap m_activeMap;
	
	// These are the actual alignment values
	private double [][] m_alignmentMatrix = null;
	
	private boolean m_showReference = true;
	private boolean m_showActive = true;
	
	// For canvas operations, zoom + pan.
	PanAndZoomPane m_zoomPane;
	
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
		m_pointsTableController.setDocument(doc);
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
		
		BufferedImage buffer = ReadImage.readImage(imageLocation);
		m_alignedImage = new AdjustableImage(buffer);
		// m_alignedImage = ReadImage.readImage(imageLocation);
		m_pointsTableController.addMap(map);
		m_pointsTableController.updatePointsTableView();
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
		
		BufferedImage buffer = ReadImage.readImage(imageLocation);
		m_referenceImage = new AdjustableImage(buffer);
		// m_referenceImage = ReadImage.readImage(imageLocation);
		double height = m_referenceImage.getImage().getHeight();
		double width = m_referenceImage.getImage().getWidth();
		
		Canvas c = m_zoomPane.getCanvas();
		c.setHeight(height);
		c.setWidth(width);
		
		m_pointsTableController.addMap(map);
		m_pointsTableController.updatePointsTableView();
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
		// Setup the table
		m_pointsTableController = new PointsTableController(pointsTableView);
		m_pointsTableController.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateZoomCanvas();
			}
			
		});
		
        zoomField.textProperty().addListener((observable, oldValue, newValue) -> {
            zoomChanged();
        });
		
        m_zoomPane = new PanAndZoomPane();
		
		// Maybe better PannableCanvas example.
		// https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
		zoomPane.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

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
		
		// zoomPane.setMaxSize(m_zoomCanvas.getWidth(), m_zoomCanvas.getHeight());
		zoomPane.setFitToHeight(true);
		zoomPane.setFitToWidth(true);
		zoomPane.setContent(m_zoomPane);
		
		setupSliders();
	}
	
	private void setupSliders() {
		
		bottomColor = new ColorAdjust();
		topColor = new ColorAdjust();
		
		ChangeListener<Number> updateUI = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (m_referenceImage != null) {
					m_referenceImage.adjustImage((float)bottomColor.getBrightness(), (float)bottomColor.getContrast());
				}
				
				if (m_alignedImage != null) {
					m_alignedImage.adjustImage((float)topColor.getBrightness(), (float)topColor.getContrast());
				}
				
				updateZoomCanvas();
			}
			
		};
		
		// Default brightness/contrast options
		bottomColor.brightnessProperty().bind(brightnessSlider1.valueProperty());
		bottomColor.contrastProperty().bind(contrastSlider1.valueProperty());
		bottomColor.brightnessProperty().addListener(updateUI);
		bottomColor.contrastProperty().addListener(updateUI);
		
		// Default brightness/contrast options
		topColor.brightnessProperty().bind(brightnessSlider2.valueProperty());
		topColor.contrastProperty().bind(contrastSlider2.valueProperty());
		topColor.brightnessProperty().addListener(updateUI);
		topColor.contrastProperty().addListener(updateUI);
	}
	
	public void updateZoomText() {
		double currentZoom = m_zoomPane.getScale();
		currentZoom *= 100.0;
		
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		decimalFormat.setMaximumFractionDigits(2);
		zoomField.setText(decimalFormat.format(currentZoom));	}
	
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
	
	public void updateZoomCanvas() {
		m_zoomPane.clearCanvas();
		
		// Calculate the current affine transform based on rotate and flips.
		Affine mat = m_zoomPane.getMat();
		
		if (m_referenceImage != null && m_showReference) {
			m_zoomPane.drawImage(m_referenceImage.getImage(), mat, false);
			
			// Draw each checked off points set.
			List<IPositionDataset> drawPoints = m_pointsTableController.getVisible(m_referenceMap);

			for (IPositionDataset item : drawPoints) {
				m_zoomPane.drawPositions(item, mat);
				Map<Integer, Vector3<Float>> points = new HashMap<Integer, Vector3<Float>>();
				int i = 1;
				
				for (Vector2<Float> position : item.getPixelPositions()) {
					points.put(new Integer(i++), new Vector3<Float>(position.x, position.y, 0f));
				}
				
				m_zoomPane.drawLabels(points, mat, item.getColor());
			}
		}
		
		Affine t2 = new Affine(1, 0, 0, 0, 1, 0);
		
		// The aligned image should be drawn transformed and with transparency.
		// How do I apply the 3x3 matrix or as 2x2 matrix /w translation component?
		if (m_alignedImage != null && m_showActive) {
			
			// This needs to also take into account rotations.
			// Is there a transform to apply?
			if (m_alignmentMatrix != null) {
				double mxx = m_alignmentMatrix[0][0];
				double myx = m_alignmentMatrix[1][0];
				double mxy = m_alignmentMatrix[0][1];
				double myy = m_alignmentMatrix[1][1];
				double tx = m_alignmentMatrix[0][2];
				double ty = m_alignmentMatrix[1][2];
				
				t2 = new Affine(mxx, mxy, tx, myx, myy, ty);
			}
			
			mat.append(t2);
			
			m_zoomPane.drawImage(m_alignedImage.getImage(), mat, true);
			
			// Draw each checked off points set.
			List<IPositionDataset> drawPoints = m_pointsTableController.getVisible(m_activeMap);
			
			for (IPositionDataset item : drawPoints) {
				m_zoomPane.drawPositions(item, mat);
				Map<Integer, Vector3<Float>> points = new HashMap<Integer, Vector3<Float>>();
				int i = 1;
				
				for (Vector2<Float> position : item.getPixelPositions()) {
					points.put(new Integer(i++), new Vector3<Float>(position.x, position.y, 0f));
				}
				
				m_zoomPane.drawLabels(points, mat, item.getColor());
			}
		}
		
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
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.tif, *.png)", "*.tif", "*.png");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
        	WritableImage saveImage = new WritableImage((int) m_zoomPane.getCanvasWidth(), (int) m_zoomPane.getCanvasHeight());
        	m_zoomPane.getCanvas().snapshot(null, saveImage);
        	
            BufferedImage bImage = SwingFXUtils.fromFXImage(saveImage, null);
            try {
            	String filename = file.getName();
            	String ext = filename.toString().substring(filename.lastIndexOf("."),filename.length());
            	if (".png".equals(ext)) {
            		ImageIO.write(bImage, "png", file);
            	} else if (".tif".equals(ext)) {
            		ImageIO.write(bImage, "tif", file);
            	}
            } catch (IOException e) {
            	throw new RuntimeException(e);
            }
        }
	}
}
