package org.cemrc.correlator.controllers;

import java.io.File;

import org.cemrc.correlator.io.ReadImage;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;

/**
 * A controller class for an interactive alignment.
 * @author larso
 *
 */
public class InteractiveAlignmentController {
	
	IPositionDataset m_referencePoints, m_targetPoints;
	IMap m_referenceMap, m_targetMap;
	Image m_referenceImage, m_targetImage;

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
				setupCanvas(m_referenceImage, m_referenceCanvas, referencePane);
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
				setupCanvas(m_targetImage, m_targetCanvas, targetPane);
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
	private void setupCanvas(Image fxImage, Canvas canvas, ScrollPane pane) {
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
		
		updateCanvas(canvas, fxImage);
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
	
	private void updateCanvas(Canvas canvas, Image image) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Create an affine transformation from a rotation.
		//Rotate r = getRotate(gc, m_currentRotation, m_zoomCanvas.getWidth() / 2.0 , m_zoomCanvas.getHeight() / 2.0);
		
		// Rotation transformation.
		//Affine t = new Affine(r.getMxx(), r.getMxy(), r.getTx(), r.getMyx(), r.getMyy(), r.getTy());
		
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
		
		// Draw each checked off points set.
		//List<IPositionDataset> drawPoints = m_pointsTableController.getVisible(m_activeMap);
		
		//for (IPositionDataset item : drawPoints) {
		//	drawPixels(gc, item, item.getColor(), t);
		//}
	}
}
