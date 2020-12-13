package org.cemrc.correlator.controllers.analysis;

import javafx.scene.chart.NumberAxis;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Will be refactored for the LineChart improvements.
 * @author mrlarson2
 *
 */
public class HistogramPane2 extends StackPane {
	
	private CutoffLineChart imageHistogram;
	
	public HistogramPane2(Pane parent, Image image) {
		
		parent.getChildren().add(this);
		
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		
		imageHistogram = new CutoffLineChart(xAxis, yAxis, image);
	
		// Add the histogram chart and an overlay pane
		this.getChildren().add(imageHistogram);
	    this.layout();
	}
	
	public CutoffLineChart getChart() {
		return imageHistogram;
	}
}