package org.cemrc.correlator.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cemrc.autodoc.Vector3;
import org.cemrc.correlator.controllers.canvas.PanAndZoomPane;
import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Affine;

/**
 * A controller class for an interactive alignment.
 * @author larso
 *
 */
public class InteractiveAlignmentController {
	
	// 1.0 = 100%
	double MIN_SCALE = 0.0001;
	double MAX_SCALE = 100;
	
	IMap m_referenceMap, m_targetMap;
	
	@FXML 
	public ScrollPane targetPane;
	
	@FXML
	public ScrollPane referencePane;
	
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
		
		private PanAndZoomPane m_zoomPane;
		private AdjustableImage m_image = null;
		
		// Selected points.
		private Map<Integer, Vector3<Float>> m_selectedPoints = new HashMap<Integer, Vector3<Float>>();
		private IPositionDataset m_registrationPoints = null;
		
		/**
		 * Provide a canvas to track state.
		 * @param c
		 */
		public CanvasState(PanAndZoomPane pane) {
			m_zoomPane = pane;
		}
		
		public PanAndZoomPane getPane() {
			return m_zoomPane;
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
		
		public void setImage(AdjustableImage im) {
			m_image = im;
			m_zoomPane.getCanvas().setWidth(m_image.getImage().getWidth());
			m_zoomPane.getCanvas().setHeight(m_image.getImage().getHeight());
			draw();
		}
		
		/**
		 * Handle scroll changes to scale state
		 * @param event
		 */
		public void updateZoom(ScrollEvent event) {
			double delta = 1.2;
			
            double scale = m_zoomPane.getScale(); // currently we only use Y, same value is used for X
            if (event.getDeltaY() < 0)
                scale /= delta;
            else
                scale *= delta;

            scale = clamp( scale, MIN_SCALE, MAX_SCALE);
            m_zoomPane.setScale(scale);

            event.consume();
		}
		
		public void draw() {
			m_zoomPane.clearCanvas();
			
			Affine mat = m_zoomPane.getMat();
			if (m_image != null) {
				m_zoomPane.drawImage(m_image.getImage(), mat, false);
			}
			
			m_zoomPane.drawPositions(getRegistrationPoints(), mat);
			m_zoomPane.drawLabels(getPoints(), mat, NavigatorColorEnum.Red);			
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
				AdjustableImage im = getImage(m_referenceMap);
				state.setImage(im);
				state.draw();
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
				state.draw();
			}
		}
	}
	
	/**
	 * Set the active image.
	 * @param map
	 */
	private AdjustableImage getImage(IMap map) {
		File imageLocation = map.getImage();
		if (! imageLocation.exists()) {
			File altImage = map.getAltImage();
			if ( altImage != null && altImage.exists()) {
				imageLocation = altImage;
			}
		}
		
		return new AdjustableImage(ReadImage.readImage(imageLocation));
	}

	@FXML
	public void initialize() {
		
		// Setup a Canvas and make this drawable.
		PanAndZoomPane m_targetZoomPane = new PanAndZoomPane();
		targetPane.setFitToHeight(true);
		targetPane.setFitToWidth(true);
		targetPane.setContent(m_targetZoomPane);
		m_targetCanvasState = new CanvasState(m_targetZoomPane);
		
		// Setup a Canvas and make this drawable.
		PanAndZoomPane m_referenceZoomPane = new PanAndZoomPane();
		referencePane.setFitToHeight(true);
		referencePane.setFitToWidth(true);
		referencePane.setContent(m_referenceZoomPane);
		m_referenceCanvasState = new CanvasState(m_referenceZoomPane);
		
		m_targetZoomPane.getCanvas().setOnMouseClicked(event -> {
			targetClickedCallback(event.getX(), event.getY());
		});
		
		m_referenceZoomPane.getCanvas().setOnMouseClicked(event -> {
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
					m_targetCanvasState.getPane().setScale(scale);
					m_targetCanvasState.draw();
				}
				
			} catch (NumberFormatException ex) {
			}
        });
		
		tRotate.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = tRotate.getText();
			try {
				double value = Double.parseDouble(text);
				if (value < -360.0f || value > 360.0f) {
					tRotate.setText(Double.toString(m_targetCanvasState.getPane().getRotation()));
				} else {
					m_targetCanvasState.getPane().setRotation(value);
					m_targetCanvasState.draw();
				}
			} catch (NumberFormatException ex) {}
		});
		
		rZoom.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = rZoom.getText();
			
			try {
				// Convert from percentage back to 1.0 scale.
				float scale = Float.parseFloat(text) / 100.0f;
				
				if (scale >= 0.0f && scale <= MAX_SCALE) {	
					m_referenceCanvasState.getPane().setScale(scale);
					m_referenceCanvasState.draw();
				}
				
			} catch (NumberFormatException ex) {
			}
        });
		
		rRotate.textProperty().addListener((observable, oldValue, newValue) -> {
			String text = rRotate.getText();
			try {
				double value = Double.parseDouble(text);
				if (value < -360.0f || value > 360.0f) {
					rRotate.setText(Double.toString(m_referenceCanvasState.getPane().getRotation()));
				} else {
					m_referenceCanvasState.getPane().setRotation(value);
					m_referenceCanvasState.draw();
				}
			} catch (NumberFormatException ex) {}
		});
		
		targetPane.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			final CanvasState state = m_targetCanvasState;
			
			@Override
			public void handle(ScrollEvent event) {
				state.updateZoom(event);
	            event.consume();
			}
		});
		
		referencePane.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
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

	public void targetClickedCallback(double x, double y) {
		canvasClickedCallback(x, y, m_targetCanvasState);
	}
	
	public void referenceClickedCallback(double x, double y) {
		canvasClickedCallback(x, y, m_referenceCanvasState);
	}
    
	public void canvasClickedCallback(double x, double y, CanvasState state) {
		
		Vector3<Float> actualPosition = state.getPane().getActualPixelPosition(x, y);
		
		if (m_currentPoint > 0) {
			// TODO: should snap to an existing registration point.
			state.getPoints().put(m_currentPoint, actualPosition);
		}

		state.draw();
	}
}
