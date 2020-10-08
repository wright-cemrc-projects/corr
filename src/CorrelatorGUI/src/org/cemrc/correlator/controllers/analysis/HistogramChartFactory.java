package org.cemrc.correlator.controllers.analysis;

import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.shape.Line;

/**
 * This class either describes a controller for a LineChart representing an image histogram
 * or is a factory for creating an image histogram LineChart from an Image.
 * @author mrlarson2
 *
 */
public class HistogramChartFactory {
	
	public class Histogram<X,Y> extends ScatterChart<X,Y> {
		
		private ObservableList<Data<X,Y>> verticalMarkers;
		
		public Histogram(Axis<X> xAxis, Axis<Y> yAxis) {
			super(xAxis, yAxis);
			
			
		}
		
		public void addVerticalValueMarker(Data<X,Y> marker) {
			Objects.requireNonNull(marker, "the marker must not be null");
			if (verticalMarkers.contains(marker)) return;
			
			Line line = new Line();
			marker.setNode(line);
			getPlotChildren().add(line);
			verticalMarkers.add(marker);
			
		}
		
		/*
		private void makeDraggable(Node node, Data<X,Y> marker) {

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
	            x = me.getX();
	            y = me.getY();
	            node.getScene().setCursor(Cursor.MOVE);
	        });
	        node.setOnMouseReleased(me -> {
	            if (!me.isPrimaryButtonDown()) {
	                node.getScene().setCursor(Cursor.DEFAULT);
	            }
	        });
	        node.setOnMouseDragged(me -> {
	            node.setLayoutX(node.getLayoutX() + me.getX() - x);
	            node.setLayoutY(node.getLayoutY() + me.getY() - y);
	        });
	    }
	    */
	}
	
	// Setup a chart with a draggable line
	public static void buildChart(LineChart<String, Number> chart, Image image) {
		
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
	        XYChart.Series seriesAlpha = new XYChart.Series();
	        XYChart.Series seriesRed = new XYChart.Series();
	        XYChart.Series seriesGreen = new XYChart.Series();
	        XYChart.Series seriesBlue = new XYChart.Series();
	        
	        seriesAlpha.setName("alpha");
	        seriesRed.setName("red");
	        seriesGreen.setName("green");
	        seriesBlue.setName("blue");
	
	        for (int i = 0; i < 256; i++) {
	            seriesAlpha.getData().add(new XYChart.Data(String.valueOf(i), alpha[i]));
	            seriesRed.getData().add(new XYChart.Data(String.valueOf(i), red[i]));
	            seriesGreen.getData().add(new XYChart.Data(String.valueOf(i), green[i]));
	            seriesBlue.getData().add(new XYChart.Data(String.valueOf(i), blue[i]));
	        }
	        
	        chart.getData().addAll(seriesRed, seriesGreen, seriesBlue);
        }
	}
	
}
