package org.cemrc.correlator.controllers.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;
import org.cemrc.math.MatrixMath;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

/**
 * This is meant as a drop-in solution for Canvas-in-Pane(s) that need to be Zoomable and Pannable.
 * It may depend still on using a SceneGestures helper class for various mouse drags, scrolls, etc.
 * Using this with the InteractiveAlignmentController could replace the use of the CanvasState?
 * 
 * @author mrlarson2
 *
 * Notes: https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
 */
public class PanAndZoomPane extends Pane {
	public static final double DEFAULT_DELTA = 1.3d;
    public DoubleProperty deltaY = new SimpleDoubleProperty(0.0);
     
    // Maintain a single scale value.
    public static String DRAG_CHANGED="DRAG_CHANGED";
    
    // This value needs to be accessible
	private DoubleProperty m_scale = new SimpleDoubleProperty(1.0);
	private Canvas m_canvas = null;
	
	private double m_rotation = 0;
	public BooleanProperty flipX = new SimpleBooleanProperty(false);
	public BooleanProperty flipY = new SimpleBooleanProperty(false);
	
	public static final Affine IDENTITY = new Affine(1, 0, 0, 0, 1, 0);
	
	// Affects hover cursor on the view.
	public enum PointState { None, Move, Add, Remove };
	private PointState mode = PointState.Move;
	
	// Cause UI updates when data model changes.
    private final List<PropertyChangeListener> dragListeners = new ArrayList<>();
    
	/**
	 * This property listener can alert when some values have updated.
	 * @param listener
	 */
    public void addDragListener(PropertyChangeListener listener) {
        dragListeners.add(listener);
    }
	
	/**
	 * Set the current zoom pane interaction
	 * @param state
	 */
	public void setPointState(PointState state) {
		mode = state;
	}
	
	/**
	 * Get the current zoom pane state
	 * @return
	 */
	public PointState getPointState() {
		return mode;
	}
	
	/**
	 * Mouse drag context used for scene and nodes.
	 */
	class DragContext {

	    double mouseAnchorX;
	    double mouseAnchorY;

	    double translateAnchorX;
	    double translateAnchorY;

	}
	private DragContext nodeDragContext = new DragContext();
	
	/**
	 * Get the scale factor for drawing text and labels on the canvas
	 * @return
	 */
	private double getScaleFactor() {
		double relativeFactor = 6;
		
		// Ranged scale prevents going beyond bounds.
		double rangedScale = m_scale.get();
		
		if (rangedScale > 4.5) {
			rangedScale = 4.5;
		} 
		
		double minScale = 5000 / getCanvasDiagonal();
		
		if (rangedScale < minScale) {
			rangedScale = minScale;
		}
		
		double scaleFactor = relativeFactor / rangedScale;
		
		/*
		if (m_canvas != null) {
			scaleFactor *= Math.log(getCanvasDiagonal());
		}
		*/
		
		return scaleFactor;
	}
	
	
    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( !event.isPrimaryButtonDown())
                return;

            nodeDragContext.mouseAnchorX = event.getSceneX();
            nodeDragContext.mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            nodeDragContext.translateAnchorX = node.getTranslateX();
            nodeDragContext.translateAnchorY = node.getTranslateY();

        }

    };
    
	private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( !event.isPrimaryButtonDown() || mode != PointState.Move)
                return;

            Node node = (Node) event.getSource();
            
            double x = nodeDragContext.translateAnchorX + (( event.getSceneX() - nodeDragContext.mouseAnchorX) / m_scale.get());
            double y = nodeDragContext.translateAnchorY + (( event.getSceneY() - nodeDragContext.mouseAnchorY) / m_scale.get());
            
            double border = 10.0;
            
            if (x > m_canvas.getWidth() - border) {
            	x = m_canvas.getWidth() - border;
            }
            
            if (x < -m_canvas.getWidth() + border) {
            	x = -m_canvas.getWidth() + border;
            }

            if (y > m_canvas.getHeight() - border) {
            	y = m_canvas.getHeight() - border;
            }
            
            if (y < -m_canvas.getHeight() + border) {
            	y = -m_canvas.getHeight() + border;
            }

            
            node.setTranslateX(x);
            node.setTranslateY(y);

            event.consume();

            for (PropertyChangeListener l : dragListeners) {
                l.propertyChange(new PropertyChangeEvent(this, DRAG_CHANGED, m_canvas, m_canvas));
            }
        }
    };
    
    public PanAndZoomPane() {
    	scaleXProperty().bind(m_scale);
    	scaleYProperty().bind(m_scale);
    	
    	m_canvas = new Canvas();
    	this.getChildren().add(m_canvas);
    	
    	m_canvas.addEventFilter( MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
    	m_canvas.addEventFilter( MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
    	
    	
    	this.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				
				switch (mode) {
				case Move:
					getScene().setCursor(Cursor.HAND); //Change cursor to crosshair
					break;
				case Add:
				case Remove:
    	        	getScene().setCursor(Cursor.CROSSHAIR); //Change cursor to crosshair
    	        	break;
				default:
    	        	getScene().setCursor(Cursor.DEFAULT); //Change cursor to crosshair
				}
			}
    	});
    	
    	this.setOnMouseExited(new EventHandler<MouseEvent>( ) {
			@Override
			public void handle(MouseEvent event) {
    	        getScene().setCursor(Cursor.DEFAULT); //Change cursor to crosshair
			}
    	});
    }
     
    public double getScale() {
    	return m_scale.get();
    }

    public void setScale( double scale) {
    	m_scale.set(scale);
    }
    
    public double getRotation() {
		return m_rotation;
	}

	public void setRotation(double m_rotation) {
		this.m_rotation = m_rotation;
	}

	public void setPivot( double x, double y) {
    	setTranslateX(getTranslateX()-x);
    	setTranslateY(getTranslateY()-y);
    }
    
    /**
     * Get the width of the canvas
     * @return
     */
	public double getCanvasWidth() {
		return m_canvas.getWidth();
	}
	
	/**
	 * Get the height of the canvas
	 * @return
	 */
	public double getCanvasHeight() {
		return m_canvas.getHeight();
	}
	
	public double getCanvasDiagonal() {
		
		double h = m_canvas.getHeight();
		double w = m_canvas.getWidth();
		
		return Math.sqrt(h*h + w*w);
	}
	
	/**
	 * Get the canvas itself
	 * @return
	 */
	public Canvas getCanvas() {
		return m_canvas;
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
		
		double scaleFactor = getScaleFactor();
    	
    	for (Vector2<Float> pixel : positions.getPixelPositions()) {
    		
    		Point2D pt = new Point2D(pixel.x, pixel.y);
    		Point2D movedPt = t.transform(pt);
    		
    		gc.beginPath();
    		gc.setStroke(c);
    		gc.setFill(c);
            gc.moveTo(movedPt.getX() + 2 * scaleFactor, movedPt.getY());
            gc.lineTo(movedPt.getX() - 2 * scaleFactor, movedPt.getY());
            gc.moveTo(movedPt.getX(), movedPt.getY() + 2 * scaleFactor);
            gc.lineTo(movedPt.getX(), movedPt.getY() - 2 * scaleFactor);
            gc.stroke();
            gc.closePath();

    	}	
    }
    
    private void drawPixels(GraphicsContext gc, List<Vector2<Float>> positions, NavigatorColorEnum color, Affine t) {
    	
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
    	
    	for (Vector2<Float> pixel : positions) {
    		
    		Point2D pt = new Point2D(pixel.x, pixel.y);
    		Point2D movedPt = t.transform(pt);
    		
    		gc.beginPath();
    		gc.setStroke(c);
    		gc.setFill(c);
            gc.moveTo(movedPt.getX() + 2, movedPt.getY());
            gc.lineTo(movedPt.getX() - 2, movedPt.getY());
            gc.moveTo(movedPt.getX(), movedPt.getY() + 2);
            gc.lineTo(movedPt.getX(), movedPt.getY() - 2);
            gc.stroke();
            gc.closePath();

    	}	
    }
    
	/**
	 * Get the real pixel position for a hit on canvas (affected by rotations and flips)
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector3<Float> getActualPixelPosition(double x, double y) {

		double center_x = getCanvasWidth() / 2.0;
		double center_y = getCanvasHeight() / 2.0;
		
		// subtract by pivot point
		double pixel_x = x - center_x;
		double pixel_y = y - center_y;
		
		// rotate by rotation matrix
		double rotationRadians = -m_rotation * Math.PI / 180.0;
		double [][] rotationMatrix = MatrixMath.getRotation(rotationRadians);
		Vector3<Float> rv = MatrixMath.multiply(rotationMatrix, new Vector3<Float>((float) pixel_x, (float) pixel_y, 0f));
		
		// add back the pivot point
		rv.x = rv.x + (float) center_x;
		rv.y = rv.y + (float) center_y;
		
		// check flips
		if (flipX.get()) {
			rv.x = (float) getCanvasWidth() - rv.x;
		}
		
		if (flipY.get()) {
			rv.y = (float) getCanvasHeight() - rv.y;
		}

		return rv;
	}
	
	/**
	 * Clear the canvas
	 */
	public void clearCanvas() {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		gc.setGlobalBlendMode(null);
		gc.clearRect(0, 0, m_canvas.getWidth(), m_canvas.getHeight());
	}
	
	/**
	 * Calculate affine transform matrix based on rotation and flips.
	 * @return
	 */
	public Affine getMat() {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		
		// Create an affine transformation from a rotation.
		Rotate r = getRotate(gc, m_rotation, getCanvasWidth() / 2.0 , getCanvasHeight() / 2.0);
		
		// Rotation transformation.
		Affine t = new Affine(r.getMxx(), r.getMxy(), r.getTx(), r.getMyx(), r.getMyy(), r.getTy());		
		
		// Flip transformation
		float xFlipTrans = flipX.get() ? -1.0f : 1.0f;
		float yFlipTrans = flipY.get() ? -1.0f : 1.0f;
		Affine t2 = new Affine(xFlipTrans, 0f, flipX.get() ? getCanvasWidth() : 0f, 0f, yFlipTrans, flipY.get() ? getCanvasHeight() : 0f);
		t.append(t2);
		return t;
	}
	
	/**
	 * Image to be drawn on the canvas.
	 * @param image
	 */
	public void drawImage(Image image, Affine mat, boolean transparent) {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		
		// Save the transform state
		gc.save();
		
		if (transparent) {
			gc.setGlobalBlendMode(BlendMode.SCREEN);
		} 
	
        gc.setTransform(mat);
		
		// Set color effects
		if (image != null) {
			gc.drawImage(image,  0,  0);
		}

		// Restore transform state
		gc.restore();
	}
	
	/**
	 * Crosshair points to be drawn on the canvas
	 * @param points
	 */
	public void drawPositions(IPositionDataset points, Affine mat) {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		
		gc.save();
		gc.setGlobalBlendMode(null);		
		drawPixels(gc, points, points.getColor(), mat);
		gc.restore(); 
	}
	
	/**
	 * Crosshair points to be drawn on the canvas
	 * @param points
	 */
	public void drawPositions(List<Vector2<Float>> points, NavigatorColorEnum color, Affine mat) {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		
		gc.save();
		gc.setGlobalBlendMode(null);		
		drawPixels(gc, points, color, mat);
		gc.restore(); 
	}
	
	/**
	 * Text labels to be drawn on the canvas
	 * @param points
	 */
	public void drawLabels(Map<Integer, Vector3<Float>> points, Affine mat, NavigatorColorEnum color) {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		
		gc.save();
		gc.setGlobalBlendMode(null);
		Point2D offset = new Point2D(-5f * getScaleFactor(), -4f * getScaleFactor());
		
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
		
		for (Integer i : points.keySet()) {
			// For each of these registration points draw a label
    		Point2D pt = new Point2D(points.get(i).x, points.get(i).y);
    		Point2D movedPt = mat.transform(pt);
    		drawLabelText(gc, movedPt, offset, i.toString(), c);
		}
		gc.restore();
	}
	
	/**
	 * Text labels to be drawn on the canvas
	 * @param points
	 */
	public void drawLabel2D(Map<Integer, Vector2<Float>> points, Affine mat, NavigatorColorEnum color) {
		GraphicsContext gc = m_canvas.getGraphicsContext2D();
		
		gc.save();
		gc.setGlobalBlendMode(null);
		Point2D offset = new Point2D(-10f, -5f);
		
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
		
		for (Integer i : points.keySet()) {
			// For each of these registration points draw a label
    		Point2D pt = new Point2D(points.get(i).x, points.get(i).y);
    		Point2D movedPt = mat.transform(pt);
    		drawLabelText(gc, movedPt, offset, i.toString(), c);
		}
		gc.restore();
	}
	
    private void drawLabelText(GraphicsContext gc, Point2D pixel, Point2D offset, String text, Color c) {
    	gc.setFont(new Font(getScaleFactor() * 10.0));
    	gc.setFill(c);
    	gc.setStroke(c);
    	gc.fillText(text, pixel.getX() + offset.getX(), pixel.getY() + offset.getY());
    }
}
