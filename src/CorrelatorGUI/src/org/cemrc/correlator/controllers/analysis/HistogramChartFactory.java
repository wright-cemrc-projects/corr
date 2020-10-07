package org.cemrc.correlator.controllers.analysis;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

/**
 * This class either describes a controller for a LineChart representing an image histogram
 * or is a factory for creating an image histogram LineChart from an Image.
 * @author mrlarson2
 *
 */
public class HistogramChartFactory {
	
	// Setup a chart.
	
	public static void buildChart(LineChart<String, Number> chart, Image m_image) {
		
        long alpha[] = new long[256];
        long red[] = new long[256];
        long green[] = new long[256];
        long blue[] = new long[256];
		
        //init
        for (int i = 0; i < 256; i++) {
            alpha[i] = red[i] = green[i] = blue[i] = 0;
        }

        PixelReader pixelReader = m_image.getPixelReader();
        if (pixelReader != null) {

	        //count pixels
	        for (int y = 0; y < m_image.getHeight(); y++) {
	            for (int x = 0; x < m_image.getWidth(); x++) {
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
