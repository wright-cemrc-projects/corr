package org.cemrc.correlator.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;
import org.cemrc.math.MatrixMath;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

/**
 * A controller class for an interactive alignment.
 * @author larso
 *
 */
public class InteractiveAlignmentController {
	
	// 1.0 = 100%
	double MIN_SCALE = 0.0001;
	double MAX_SCALE = 100;
	
	//IPositionDataset m_referencePoints, m_targetPoints;
	IMap m_referenceMap, m_targetMap;
	//Image m_referenceImage, m_targetImage;
	
	@FXML 
	public ScrollPane targetPane;
	
	@FXML
	public ScrollPane referencePane;
	
	// Canvas contained in Panes.
	private Canvas m_targetCanvas, m_referenceCanvas;
	
	@FXML
	public RadioButton radioPt1;
	
	@FXML
	public RadioButton radioPt2;
	
	@FXML
	public RadioButton radioPt3;
	
	@FXML
	public TextField tZoom;
	
	@FXML
	public TextField rZoom;
	
	@FXML
	public TextField tRotate;
	
	@FXML
	public TextField rRotate;

	/**
	 * Structure tracking Canvas transforms.
	 */
	private CanvasState m_targetCanvasState, m_referenceCanvasState;
	
	// These state track what point we are currently selecting in the canvas.
	private int m_currentPoint = 0;
	
	/**
	 * CanvasState describes the rotation, flip state for a Canvas.
	 * @author mrlarson2
	 *
	 */
	public class CanvasState {

		private Canvas canvas = null;
		private Image image = null;
		
		private double rotation = 0;
		public boolean flipX = false;
		public boolean flipY = false;
		
		// Selected points.
		private Map<Integer, Vector3<Float>> m_selectedPoints = new HashMap<Integer, Vector3<Float>>();
		private IPositionDataset m_registrationPoints = null;
		
		/**
		 * Provide a canvas to track state.
		 * @param c
		 */
		public CanvasState(Canvas c) {
			canvas = c;
		}
		
		public Canvas getCanvas() {
			return canvas;
		}
		
		public double getCanvasWidth() {
			return canvas.getWidth();
		}
		
		public double getCanvasHeight() {
			return canvas.getHeight();
		}
		
		public Map<Integer, Vector3<Float>> getPoints() {
			return m_selectedPoints;
		}
		
		public void setRegistrationPoints(IPositionDataset points) {
			m_registrationPoints = points;
		}
		
		public IPositionDataset getRegistrationPoints() {
			return m_registrationPoints;
		}
		
		public void setImage(Image im) {
			image = im;
		}
		
		private Image getImage() {
			return image;
		}
		
		/**
		 * Set the scale
		 * @param x
		 * @param y
		 * @param z
		 */
		private void setZoom(double x, double y, double z) {
			canvas.setScaleX(x);
			canvas.setScaleY(y);
			canvas.setScaleY(z);
		}
		
		/**
		 * Get the scale
		 * @return
		 */
		private Vector3<Double> getZoom() {
			return new Vector3<Double>(canvas.getScaleX(), canvas.getScaleY(), canvas.getScaleZ());
		}
		
		/**
		 * Handle scroll changes to scale state
		 * @param event
		 */
		public void updateZoom(ScrollEvent event) {
			double delta = 1.2;
            double scale = getZoom().x;
		
            if (event.getDeltaY() < 0)
                scale /= delta;
            else
                scale *= delta;

            scale = clamp( scale, MIN_SCALE, MAX_SCALE);
            setZoom(scale, scale, scale);
		}
		
		public void setRotation(double r) {
			rotation = r;
		}
		
		public double getRotation() {
			return rotation;
		}
	}
	
	/**
	 * 
	 * @param referencePoints
	 */
	public void setReferencePoints(IPositionDataset referencePoints) {
		
		CanvasState state = m_referenceCanvasState;
		state.setRegistrationPoints(referencePoints);
		
		if (referencePoints != null) {
			m_referenceMap = referencePoints.getMap();
			if (m_referenceMap != null) {
				state.setImage(getImage(m_referenceMap));
				setupCanvas(state.getImage(), referencePoints, m_referenceCanvasState, referencePane);
			}
		}
	}
	
	/**
	 * 
	 * @param targetPoints
	 */
	public void setTargetPoints(IPositionDataset targetPoints) {
		
		CanvasState state = m_targetCanvasState;
		state.setRegistrationPoints(targetPoints);
		
		if (targetPoints != null) {
			m_targetMap = targetPoints.getMap();
			if (m_targetMap != null) {
				state.setImage( getImage(m_targetMap) );
				setupCanvas(state.getImage(), targetPoints, m_targetCanvasState, targetPane);
			}
		}
	}
	
	/**
	 * Set the active image.
	 * @param map
	 */
	private Image getImage(IMap map) {
		File imageLocation = map.getImage();
		if (! imageLocation.exists()) {
			File altImage = map.getAltImage();
			if ( altImage != null && altImage.exists()) {
				imageLocation = altImage;
			}
		}
		
		AdjustableImage image = new AdjustableImage(ReadImage.readImage(imageLocation));
		Image fxImage = image.getImage();
		return fxImage;
	}
	
	/**
	 * Set the image on a JavaFX Canvas
	 * @param fxImage
	 * @param state
	 * @param pane
	 */
	private void setupCanvas(Image fxImage, IPositionDataset points, CanvasState state, ScrollPane pane) {
		double height = pane.getPrefViewportHeight();
		double width = pane.getPrefViewportWidth();
		
		if (width < fxImage.getWidth()) {
			width = fxImage.getWidth();
		}
		
		if (height < fxImage.getHeight()) {
			height = fxImage.getHeight();
		}
		
		// Setup the zoom view
		state.getCanvas().setWidth(width);
		state.getCanvas().setHeight(height);
		pane.setPrefViewportWidth(600);
		pane.setPrefViewportHeight(300);
		updateCanvas(state);
	}

	@FXML
	public void initialize() {
		
		int width = 640;
		int height = 480;
		
		// Setup a Canvas and make this drawable.
		m_targetCanvas = new Canvas(width, height);
		targetPane.setFitToHeight(true);
		targetPane.setFitToWidth(true);
		targetPane.setContent(m_targetCanvas);
		m_targetCanvasState = new CanvasState(m_targetCanvas);
		
		// Setup a Canvas and make this drawable.
		m_referenceCanvas = new Canvas(width, height);
		referencePane.setFitToHeight(true);
		referencePane.setFitToWidth(true);
		referencePane.setContent(m_referenceCanvas);
		m_referenceCanvasState = new CanvasState(m_referenceCanvas);
		
		m_targetCanvas.setOnMouseClicked(event -> {
			targetClickedCallback(event.getX(), event.getY());
		});
		
		m_referenceCanvas.setOnMouseClicked(event -> {
			referenceClickedCallback(event.getX(), event.getY());
		});
		
		// Setup button toggle grouping
		ToggleGroup group = new ToggleGroup();
		radioPt1.setToggleGroup(group);
		radioPt2.setToggleGroup(group);
		radioPt3.setToggleGroup(group);
		
		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				// Has selection.
				if (group.getSelectedToggle() != null) {
					RadioButton button = (RadioButton) group.getSelectedToggle();
					if (button == radioPt1) {
						m_currentPoint = 1;
					} else if (button == radioPt2) {
						m_currentPoint = 2;
					} else if (button == radioPt3) {
						m_currentPoint = 3;
					}
				}
			}
		});
		
		// Rotate and Zoom controls
		tZoom.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = tZoom.getText();
			
			try {
				// Convert from percentage back to 1.0 scale.
				float scale = Float.parseFloat(text) / 100.0f;
				
				if (scale >= 0.0f && scale <= MAX_SCALE) {	
					m_targetCanvasState.setZoom(scale, scale, scale);
				}
				
			} catch (NumberFormatException ex) {
			}
        });
		
		tRotate.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = tRotate.getText();
			try {
				double value = Double.parseDouble(text);
				if (value < -360.0f || value > 360.0f) {
					tRotate.setText(Double.toString(m_targetCanvasState.getRotation()));
				} else {
					m_targetCanvasState.setRotation(value);
					updateCanvas(m_targetCanvasState);
				}
			} catch (NumberFormatException ex) {}
		});
		
		rZoom.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = rZoom.getText();
			
			try {
				// Convert from percentage back to 1.0 scale.
				float scale = Float.parseFloat(text) / 100.0f;
				
				if (scale >= 0.0f && scale <= MAX_SCALE) {	
					m_referenceCanvasState.setZoom(scale, scale, scale);
				}
				
			} catch (NumberFormatException ex) {
			}
        });
		
		rRotate.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = rRotate.getText();
			try {
				double value = Double.parseDouble(text);
				if (value < -360.0f || value > 360.0f) {
					rRotate.setText(Double.toString(m_referenceCanvasState.getRotation()));
				} else {
					m_referenceCanvasState.setRotation(value);
					updateCanvas(m_referenceCanvasState);
				}
			} catch (NumberFormatException ex) {}
		});
		
		m_targetCanvas.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			final CanvasState state = m_targetCanvasState;
			
			@Override
			public void handle(ScrollEvent event) {
				state.updateZoom(event);
	            event.consume();
			}
		});
		
		m_referenceCanvas.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			final CanvasState state = m_referenceCanvasState;
			
			@Override
			public void handle(ScrollEvent event) {
				state.updateZoom(event);
	            event.consume();
			}
		});
		
		
	}
	
    private double clamp( double value, double min, double max) {
        if( Double.compare(value, min) < 0)
            return min;

        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }
	
	private void updateCanvas(CanvasState state) {
		GraphicsContext gc = state.getCanvas().getGraphicsContext2D();
		gc.clearRect(0, 0, state.getCanvasWidth(), state.getCanvasHeight());
		
		// Create an affine transformation from a rotation.
		Rotate r = getRotate(gc, state.rotation, state.getCanvasWidth() / 2.0 , state.getCanvasHeight() / 2.0);
		
		// Rotation transformation.
		Affine t = new Affine(r.getMxx(), r.getMxy(), r.getTx(), r.getMyx(), r.getMyy(), r.getTy());		
		
		// Flip transformation
		float xFlipTrans = state.flipX ? -1.0f : 1.0f;
		float yFlipTrans = state.flipY ? -1.0f : 1.0f;
		Affine t2 = new Affine(xFlipTrans, 0f, state.flipX ? state.getCanvasWidth() : 0f, 0f, yFlipTrans, state.flipY ? state.getCanvasHeight() : 0f);
		t.append(t2);
		
		// Save the transform state
		gc.save();
        gc.setTransform(t);
		
		// Set color effects
		if (state.getImage() != null) {
			gc.drawImage(state.getImage(),  0,  0);
		}

		// Restore transform state
		gc.restore();
		
		drawPixels(gc, state.getRegistrationPoints(), state.getRegistrationPoints().getColor(), t);
		
		Point2D offset = new Point2D(-10f, -5f);
		
		for (Integer i : state.getPoints().keySet()) {
			// For each of these registration points draw a label
    		Point2D pt = new Point2D(state.getPoints().get(i).x, state.getPoints().get(i).y);
    		Point2D movedPt = t.transform(pt);
    		drawLabelText(gc, movedPt, offset, i.toString());
		}
	}
	
    private void drawLabelText(GraphicsContext gc, Point2D pixel, Point2D offset, String text) {
    	gc.fillText(text, pixel.getX() + offset.getX(), pixel.getY() + offset.getY());
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
    	}	
    	gc.closePath();
    }
    
	/**
	 * Rotation
	 * @param x
	 * @param y
	 * @return
	 */
	private Vector3<Float> getActualPixelPosition(double x, double y, CanvasState state) {

		double center_x = state.getCanvasWidth() / 2.0;
		double center_y = state.getCanvasHeight() / 2.0;
		
		// subtract by pivot point
		double pixel_x = x - center_x;
		double pixel_y = y - center_y;
		
		// rotate by rotation matrix
		double rotationRadians = -state.rotation * Math.PI / 180.0;
		double [][] rotationMatrix = MatrixMath.getRotation(rotationRadians);
		Vector3<Float> rv = MatrixMath.multiply(rotationMatrix, new Vector3<Float>((float) pixel_x, (float) pixel_y, 0f));
		
		// add back the pivot point
		rv.x = rv.x + (float) center_x;
		rv.y = rv.y + (float) center_y;
		
		// check flips
		if (state.flipX) {
			rv.x = (float) state.getCanvasWidth() - rv.x;
		}
		
		if (state.flipY) {
			rv.y = (float) state.getCanvasHeight() - rv.y;
		}

		return rv;
	}
	
	public void targetClickedCallback(double x, double y) {
		canvasClickedCallback(x, y, m_targetCanvasState);
	}
	
	public void referenceClickedCallback(double x, double y) {
		canvasClickedCallback(x, y, m_referenceCanvasState);
	}
    
	public void canvasClickedCallback(double x, double y, CanvasState state) {
		double near = 5;
		Vector3<Float> actualPosition = getActualPixelPosition(x, y, state);
		
		if (m_currentPoint > 0) {
			// TODO: should snap to an existing registration point.
			state.getPoints().put(m_currentPoint, actualPosition);
		}

		updateCanvas(state);
	}
}
