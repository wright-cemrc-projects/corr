package org.cemrc.correlator.controllers.analysis;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * CutoffLineChart has manipulatible lower/upper bounds that return a value
 *  for min and max cutoff positions.
 *  
 * @author mrlarson2
 */
public class CutoffLineChart extends LineChart<Number, Number> {
	
	private double m_boundMinX, m_boundMaxX;
	private double m_drawY1, m_drawY2;
	private Image m_image;
	
	Axis<Number> m_axisX;
	
	// Use this variable to track
	public DoubleProperty m_positionMinCutoff = new SimpleDoubleProperty();
	public DoubleProperty m_positionBinaryCutoff = new SimpleDoubleProperty();
	public DoubleProperty m_positionMaxCutoff = new SimpleDoubleProperty();

	public CutoffLineChart(NumberAxis xAxis, NumberAxis yAxis, Image image) {
		super(xAxis, yAxis);
		m_axisX = xAxis;
		
		m_image = image;
		buildChart(m_image);
		
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(256);
		xAxis.setAutoRanging(false);
		
		setupDraggableLines();
		// Add controllable marker lines.
		setCache(true);
		
		// Style the image histogram
		setCreateSymbols(false);
		setAnimated(false);
		setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
	}
	
	private void setupDraggableLines() {
		Bounds b = m_axisX.getBoundsInLocal();
		
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
	    
	    m_boundMinX = 1;
	    m_boundMaxX = 130;
	    m_drawY1 = 0;
	    m_drawY2 = 100;
	    
	    cutoffLine1.setStartY(m_drawY1);
	    cutoffLine1.setEndY(m_drawY2);
	    cutoffLine1.setLayoutX(m_boundMinX);
	    
	    cutoffLine2.setStartY(m_drawY1);
	    cutoffLine2.setEndY(m_drawY2);
	    cutoffLine2.setLayoutX( (m_boundMaxX - m_boundMinX) / 2.0);
	    
	    cutoffLine3.setStartY(m_drawY1);
	    cutoffLine3.setEndY(m_drawY2);
	    cutoffLine3.setLayoutX(m_boundMaxX);
	    
	    this.getPlotChildren().add(cutoffLine1);
	    this.getPlotChildren().add(cutoffLine2);
	    this.getPlotChildren().add(cutoffLine3);
	    
	    // Setup the bounded drag movements.
	    makeDraggable(cutoffLine1, null, cutoffLine2, m_positionMinCutoff);
	    makeDraggable(cutoffLine2, cutoffLine1, cutoffLine3, m_positionBinaryCutoff);
	    makeDraggable(cutoffLine3, cutoffLine2, null, m_positionMaxCutoff);
	    
	    // Done
	}
	
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

	private void buildChart(Image image) {
		
        long alpha[] = new long[256];
        long red[] = new long[256];
        long green[] = new long[256];
        long blue[] = new long[256];
		
        //init
        for (int i = 0; i < 256; i++) {
            alpha[i] = red[i] = green[i] = blue[i] = 0;
        }
        
        PixelReader pixelReader = image.getPixelReader();
        
        if (pixelReader != null) {
	        //count pixels
	        for (int y = 0; y < image.getHeight(); y++) {
	            for (int x = 0; x < image.getWidth(); x++) {
	                int argb = pixelReader.getArgb(x, y);
	                int a = (0xff & (argb >> 24));
	                int r = (0xff & (argb >> 16));
	                int g = (0xff & (argb >> 8));
	                int b = (0xff & argb);
	
	                alpha[a]++;
	                red[r]++;
	                green[g]++;
	                blue[b]++;
	
	            }
	        }
	        
	        // Use the pixel counts to fill in series for a chart.
	        XYChart.Series<Number, Number> seriesAlpha = new XYChart.Series<Number, Number>();
	        XYChart.Series<Number, Number> seriesRed = new XYChart.Series<Number, Number>();
	        XYChart.Series<Number, Number> seriesGreen = new XYChart.Series<Number, Number>();
	        XYChart.Series<Number, Number> seriesBlue = new XYChart.Series<Number, Number>();
	        
	        seriesAlpha.setName("alpha");
	        seriesRed.setName("red");
	        seriesGreen.setName("green");
	        seriesBlue.setName("blue");
	
	        for (int i = 0; i < 256; i++) {
	            seriesAlpha.getData().add(new XYChart.Data<Number, Number>(i, alpha[i]));
	            seriesRed.getData().add(new XYChart.Data<Number, Number>(i, red[i]));
	            seriesGreen.getData().add(new XYChart.Data<Number, Number>(i, green[i]));
	            seriesBlue.getData().add(new XYChart.Data<Number, Number>(i, blue[i]));
	        }
	        
	        getData().addAll(seriesRed, seriesGreen, seriesBlue);
        }
	}
}
