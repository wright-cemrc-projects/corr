package org.cemrc.correlator.controllers.analysis;

import javafx.geometry.Bounds;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Derived from a StackPane, this will feature interactible cutoff lines for min/max.
 * @author mrlarson2
 *
 */
public class HistogramPane extends StackPane {
	
	private LineChart<Number, Number> imageHistogram;
	private HistogramOverlayController m_cutoffController;
	
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
	
		// Add the histogram chart and an overlay pane
		this.getChildren().add(imageHistogram);
	    Pane pane = new Pane();
	    this.getChildren().add(pane);
	    this.layout();
	    
	    // Configure interactible lines on the overlay pane
	    Bounds b = xAxis.getBoundsInParent();
	    m_cutoffController = new HistogramOverlayController(pane, b);
	}
	
	/**
	 * Get the HistogramOverlayController, to get current positions
	 * @return
	 */
	public HistogramOverlayController getHistogramController() {
		return m_cutoffController;
	}
	
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
