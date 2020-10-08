package org.cemrc.correlator.controllers.analysis;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class LineMarker extends Line{
	
	private final DoubleProperty value = new SimpleDoubleProperty();
	private Pane pane;
	private ValueAxis xAxis;
	private ValueAxis yAxis;
	
	public LineMarker(Pane pane, NumberAxis xAxis, double value, NumberAxis yAxis){
	    this.pane = pane;
	    this.xAxis = xAxis;
	    this.yAxis = yAxis;
	    Number lowerY = yAxis.toRealValue(yAxis.getLowerBound());
	    double minY = yAxis.getDisplayPosition(lowerY);
	    setStartY(getYPositionParent(minY, pane));
	    Number upperY = yAxis.toRealValue(yAxis.getUpperBound());
	    double maxY = yAxis.getDisplayPosition(upperY);
	    setEndY(getYPositionParent(maxY, pane));
	    double xPosInAxis = xAxis.getDisplayPosition(value);
	    setStartX(getXPositionParent(xPosInAxis, pane));
	    setEndX(getStartX());
	    pane.getChildren().add(this);
	}
	
	private double getXPositionParent(double xPosInAxis, Node parent){
	    double xPosInParent = xPosInAxis - xAxis.getBoundsInLocal().getMinX();
	    Node node = xAxis;
	    while(!node.equals(parent)){
	        xPosInParent = xPosInParent + node.getBoundsInParent().getMinX();
	        node = node.getParent();
	        if (node == null) break;
	    }
	    return xPosInParent;
	}
	
	private double getYPositionParent(double yPosInAxis, Node parent){
	    double yPosInParent = yPosInAxis - yAxis.getBoundsInLocal().getMinY();
	    Node node = yAxis;
	    while(!node.equals(parent)){
	        yPosInParent = yPosInParent + node.getBoundsInParent().getMinY();
	        node = node.getParent();
	        if (node == null) break;
	    }
	    return yPosInParent;
	}
	
	public void updateMarker(double value){
	    pane.getChildren().remove(this);
	    double xPosInAxis = xAxis.getDisplayPosition(value);
	    setStartX(getXPositionParent(xPosInAxis, pane));
	    setEndX(getStartX());
	    pane.getChildren().add(this);
	    pane.requestLayout();
	}
	
}
