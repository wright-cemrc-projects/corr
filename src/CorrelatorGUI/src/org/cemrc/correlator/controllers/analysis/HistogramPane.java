package org.cemrc.correlator.controllers.analysis;

import javafx.animation.AnimationTimer;
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
		
		// Add the histogram
		this.getChildren().add(imageHistogram);
		
		// Add interactible line nodes for min/bin/max
	    Line cutoffLine1 = new Line();
	    cutoffLine1.setStrokeWidth(1);
	    Line cutoffLine2 = new Line();
	    cutoffLine2.setStrokeWidth(1);

	    Pane pane = new Pane(cutoffLine1, cutoffLine2);
	    this.getChildren().add(pane);
	    
	    cutoffLine1.setStartY(0);
	    cutoffLine1.setEndY(parent.getHeight());
	    cutoffLine1.setStartX(5);
	    cutoffLine1.setEndX(5);
	    
	    cutoffLine2.setStartY(0);
	    cutoffLine2.setEndY(parent.getHeight());
	    cutoffLine2.setStartX(55);
	    cutoffLine2.setEndX(55);

	    /*
        AnimationTimer loop = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                verticleLine.setStartY(0);
                verticleLine.setEndY(pane.getHeight());
                verticleLine.setEndX(mouseX);
                verticleLine.setStartX(mouseX);

                horizontalLine.setStartX(0);
                horizontalLine.setEndX(pane.getWidth());
                horizontalLine.setEndY(mouseY);
                horizontalLine.setStartY(mouseY);
            }
        };
        */
		
		// Add this StackPane to the parent.
		parent.getChildren().add(this);
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
