package org.cemrc.correlator.controllers.analysis;

import java.awt.event.MouseEvent;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;

/**
 * Derived from a StackPane, this will feature interactible cutoff lines for min/max.
 * @author mrlarson2
 *
 */
public class HistogramPane extends StackPane {
	
	private LineChart<Number, Number> imageHistogram;
	
	public HistogramPane(Pane parent, Image image) {
		
		parent.getChildren().add(this);
		
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		imageHistogram = new LineChart<Number, Number>(xAxis, yAxis);
		imageHistogram.setPrefSize(parent.getWidth(), parent.getHeight());
		
		buildChart(imageHistogram, image);
		
		// Add controllable marker lines.
		imageHistogram.setCache(true);
		
		// Style the image histogram
		imageHistogram.setCreateSymbols(false);
		imageHistogram.setAnimated(false);
	
		// These lines are present in the wrapping Pane class, not chart itself.
		//LineMarker lineMarker = new LineMarker(pane, (NumberAxis) chart.getXAxis(), 0.0, (NumberAxis) chart.getYAxis());
		//lineMarker.updateMarker(10);
		
		// Add interactible line nodes for min/bin/max
	    Line cutoffLine1 = new Line();
	    cutoffLine1.setStrokeWidth(3);
	    Line cutoffLine2 = new Line();
	    cutoffLine2.setStrokeWidth(3);
	    
	    makeDraggable(cutoffLine1);
	    makeDraggable(cutoffLine2);

	    Pane pane = new Pane(cutoffLine1, cutoffLine2);
		// Add the histogram
		this.getChildren().add(imageHistogram);
	    this.getChildren().add(pane);
	    this.layout();

	    Bounds b = xAxis.getBoundsInParent();
	    Node axis_p = xAxis.getParent();
	    Bounds bp = axis_p.localToParent(b);

	    cutoffLine1.setStartY(0);
	    cutoffLine1.setEndY(bp.getMinY());
	    cutoffLine1.setStartX(bp.getMinX());
	    cutoffLine1.setEndX(bp.getMinX());
	    
	    cutoffLine2.setStartY(0);
	    cutoffLine2.setEndY(bp.getMinY());
	    cutoffLine2.setStartX(bp.getMaxX() - 1);
	    cutoffLine2.setEndX(bp.getMaxX() - 1);
	    
	}
	
    private class Delta {
        public double x;
        public double y;
    }
	
    private void makeDraggable(Node node) {
    	
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
            node.setLayoutX(node.getLayoutX() + me.getX() - dragDelta.x);
            // node.setLayoutY(node.getLayoutY() + me.getY() - dragDelta.y);
        });
    }
	
	// Setup a chart with a draggable line
	public static void buildChart(LineChart<Number, Number> chart, Image image) {
		
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
	        
	        chart.getData().addAll(seriesRed, seriesGreen, seriesBlue);
        }
	}
	
}
