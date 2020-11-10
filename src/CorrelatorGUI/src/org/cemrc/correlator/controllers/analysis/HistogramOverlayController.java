package org.cemrc.correlator.controllers.analysis;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Controller representing three cutoffs:
 * 1. Low bound of Histogram
 * 2. Mid cutoff for binarization
 * 3. High bound of Histogram
 * 
 * We need to set the boundaries for the X movements.
 * We need to require that the low bound cannot go below lowest position
 * We need to require that the high bound cannot go above the highest position
 * 
 * 
 * @author mrlarson2
 *
 */
public class HistogramOverlayController {

	private double m_boundMinX, m_boundMaxX;
	private double m_drawY1, m_drawY2;
	
	// Use this variable to track
	public DoubleProperty m_positionMinCutoff = new SimpleDoubleProperty();
	public DoubleProperty m_positionBinaryCutoff = new SimpleDoubleProperty();
	public DoubleProperty m_positionMaxCutoff = new SimpleDoubleProperty();
	
	public HistogramOverlayController(Pane pane, Bounds b) {
		// Add interactible line nodes for min/bin/max
	    Line cutoffLine1 = new Line();
	    cutoffLine1.setStrokeWidth(3);
	    cutoffLine1.setStroke(Color.BLUE);
	    
	    Line cutoffLine2 = new Line();
	    cutoffLine2.setStrokeWidth(3);
	    cutoffLine2.setStroke(Color.BLACK);
	    
	    Line cutoffLine3 = new Line();
	    cutoffLine3.setStrokeWidth(3);
	    cutoffLine3.setStroke(Color.BLUE);
	    
	    m_boundMinX = 70;
	    m_boundMaxX = 535;
	    m_drawY1 = 0;
	    m_drawY2 = 120;
	    
	    cutoffLine1.setStartY(m_drawY1);
	    cutoffLine1.setEndY(m_drawY2);
	    cutoffLine1.setLayoutX(m_boundMinX);
	    
	    cutoffLine2.setStartY(m_drawY1);
	    cutoffLine2.setEndY(m_drawY2);
	    cutoffLine2.setLayoutX( (m_boundMaxX - m_boundMinX) / 2.0);
	    
	    cutoffLine3.setStartY(m_drawY1);
	    cutoffLine3.setEndY(m_drawY2);
	    cutoffLine3.setLayoutX(m_boundMaxX);
	    
	    pane.getChildren().add(cutoffLine1);
	    pane.getChildren().add(cutoffLine2);
	    pane.getChildren().add(cutoffLine3);
	    
	    // Setup the bounded drag movements.
	    makeDraggable(cutoffLine1, null, cutoffLine2, m_positionMinCutoff);
	    makeDraggable(cutoffLine2, cutoffLine1, cutoffLine3, m_positionBinaryCutoff);
	    makeDraggable(cutoffLine3, cutoffLine2, null, m_positionMaxCutoff);
	    
	    // Done
	}
	
	public void setBounds(double minX, double maxX) {
		m_boundMinX = minX;
		m_boundMaxX = maxX;
	}
	
	public double getLayoutMinX() {
		return m_boundMinX;
	}
	
	public double getLayoutMaxX() {
		return m_boundMaxX;
	}
	
	// TODO: manage Line(s) and updating their position in a layout.
	// Lines need to be added to a Pane, and controller enabled.
	
	/**
	 * Represents x,y position change.
	 * @author mrlarson2
	 *
	 */
    private class Delta {
        public double x;
        public double y;
    }
	
    /**
     * Setup mouse listeners for an overlay node.
     * @param node
     */
    private void makeDraggable(Node node, Node leftNeighbor, Node rightNeighbor, DoubleProperty update) {
    	
        final Delta dragDelta = new Delta();

        node.setOnMouseEntered(me -> {
            if (!me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.HAND);
            }
        });
        node.setOnMouseExited(me -> {
            if (!me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        node.setOnMousePressed(me -> {
            if (me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
            dragDelta.x = me.getX();
            dragDelta.y = me.getY();
            node.getScene().setCursor(Cursor.MOVE);
        });
        node.setOnMouseReleased(me -> {
            if (!me.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        node.setOnMouseDragged(me -> {
        	
        	// Bounded moves.
        	double x = node.getLayoutX() + me.getX() - dragDelta.x;
        	if (leftNeighbor != null) {
        		double bl = leftNeighbor.getLayoutX();
        		if (x < bl) x = bl;
        	}
        	
        	if (x <= m_boundMinX) {
        		x = m_boundMinX + 1;
        	}
        	
        	if (rightNeighbor != null) {
        		double br = rightNeighbor.getLayoutX();
        		if (x > br) x = br;
        	}
        	if (x >= m_boundMaxX) {
        		x = m_boundMaxX - 1;
        	}
        	
            node.setLayoutX(x);
            
            double percent = (x - m_boundMinX) / (m_boundMaxX - m_boundMinX);
            update.set(percent);
            
        });
    }
}
