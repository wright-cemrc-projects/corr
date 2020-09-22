package org.cemrc.correlator.controllers;

import java.io.File;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

/**
 * A controller class for an interactive alignment.
 * @author larso
 *
 */
public class InteractiveAlignmentController {
	
	IPositionDataset m_referencePoints, m_targetPoints;
	IMap m_referenceMap, m_targetMap;
	Image m_referenceImage, m_targetImage;
	
	// We can have user select 3 points for each.
	private Vector3<Float>[] m_pt1 = new Vector3[3];
	private Vector3<Float>[] m_pt2 = new Vector3[3];

	@FXML 
	public ScrollPane targetPane;
	
	@FXML
	public ScrollPane referencePane;
	
	// Canvas contained in Panes.
	private Canvas m_targetCanvas, m_referenceCanvas;
	
	@FXML
	public void zoomChanged() {
		// TODO: may need one for each canvas.
	}
	
	@FXML
	public RadioButton radioPt1;
	
	@FXML
	public RadioButton radioPt2;
	
	@FXML
	public RadioButton radioPt3;
	
	/**
	 * 
	 * @param referencePoints
	 */
	public void setReferencePoints(IPositionDataset referencePoints) {
		m_referencePoints = referencePoints;
		
		if (m_referencePoints != null) {
			m_referenceMap = m_referencePoints.getMap();
			if (m_referenceMap != null) {
				m_referenceImage = getImage(m_referenceMap);
				setupCanvas(m_referenceImage, referencePoints, m_referenceCanvas, referencePane);
			}
		}
	}
	
	/**
	 * 
	 * @param targetPoints
	 */
	public void setTargetPoints(IPositionDataset targetPoints) {
		m_targetPoints = targetPoints;
		
		if (m_targetPoints != null) {
			m_targetMap = m_targetPoints.getMap();
			if (m_targetMap != null) {
				m_targetImage = getImage(m_targetMap);
				setupCanvas(m_targetImage, targetPoints, m_targetCanvas, targetPane);
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
	 * @param canvas
	 * @param pane
	 */
	private void setupCanvas(Image fxImage, IPositionDataset points, Canvas canvas, ScrollPane pane) {
		double height = pane.getPrefViewportHeight();
		double width = pane.getPrefViewportWidth();
		
		if (width < fxImage.getWidth()) {
			width = fxImage.getWidth();
		}
		
		if (height < fxImage.getHeight()) {
			height = fxImage.getHeight();
		}
		
		// Setup the zoom view
		canvas.setWidth(width);
		canvas.setHeight(height);
		pane.setPrefViewportWidth(600);
		pane.setPrefViewportHeight(300);
		
		updateCanvas(canvas, fxImage, points, 0);
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
		
		// Setup a Canvas and make this drawable.
		m_referenceCanvas = new Canvas(width, height);
		referencePane.setFitToHeight(true);
		referencePane.setFitToWidth(true);
		referencePane.setContent(m_referenceCanvas);
	}
	
	private void updateCanvas(Canvas canvas, Image image, IPositionDataset item, double rotation) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Create an affine transformation from a rotation.
		Rotate r = getRotate(gc, rotation, canvas.getWidth() / 2.0 , canvas.getHeight() / 2.0);
		
		// Rotation transformation.
		Affine t = new Affine(r.getMxx(), r.getMxy(), r.getTx(), r.getMyx(), r.getMyy(), r.getTy());		
		
		// Flip transformation
		//float xFlipTrans = flipx.isSelected() ? -1.0f : 1.0f;
		//float yFlipTrans = flipy.isSelected() ? -1.0f : 1.0f;
		//Affine t2 = new Affine(xFlipTrans, 0f, flipx.isSelected() ? m_zoomCanvas.getWidth() : 0f, 0f, yFlipTrans, flipy.isSelected() ? m_zoomCanvas.getHeight() : 0f);
		//t.append(t2);
		
		// Save the transform state
		gc.save();
        //gc.setTransform(t);
		
		// Set color effects
		if (image != null) {
			gc.drawImage(image,  0,  0);
		}

		// Restore transform state
		gc.restore();
		

		drawPixels(gc, item, item.getColor(), t);
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
    	}	
    	gc.closePath();
    }
}
