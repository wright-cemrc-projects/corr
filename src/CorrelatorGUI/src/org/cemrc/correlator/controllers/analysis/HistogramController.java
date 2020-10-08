package org.cemrc.correlator.controllers.analysis;

import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.chart.ValueAxis;
import javafx.scene.shape.Line;

/**
 * This class either describes a controller for a LineChart representing an image histogram
 * or is a factory for creating an image histogram LineChart from an Image.
 * @author mrlarson2
 *
 */
public class HistogramController {
	
	
	
	public HistogramController(Pane pane, LineChart<Number, Number> chart, Image image) {
		buildChart(chart, image);
		
		// Add controllable marker lines.
		chart.setCache(true);
		// These lines are present in the wrapping Pane class, not chart itself.
		LineMarker lineMarker = new LineMarker(pane, (NumberAxis) chart.getXAxis(), 0.0, (NumberAxis) chart.getYAxis());
		
		pane.getChildren().addAll(chart);
		lineMarker.updateMarker(10);
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
