package org.cemrc.math;

import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.Vector2;

/**
 * Idealized representation of an endless lattice of points.
 * 
 * 
 * @author mrlarson2
 */
public class GridSpacing {
	
	private Vector2<Double> m_origin;
	private Vector2<Double> m_axis_h;
	private Vector2<Double> m_axis_w;
	
	/**
	 * Constructor
	 * @param origin : origin point on grid, best if near center of image
	 * @param axish : directional vector h
	 * @param axisw : directional vector w
	 */
	public GridSpacing(Vector2<Double> origin, Vector2<Double> axish, Vector2<Double> axisw) {
		m_origin = origin;
		m_axis_h = axish;
		m_axis_w = axisw;
	}
	
	/**
	 * Returns the points that are on the grid.
	 * @param pts
	 * @param range
	 * @return
	 */
	public List<Vector2<Double>> getOnGrid(List<Vector2<Double>> pts, double range) {
		List<Vector2<Double>> rv = new ArrayList<Vector2<Double>>();
		
		// TODO
		
		// Could imagine a simple grid along X and Y axis with interval T with origin 0,0
		// In this case a point pt1 is on the grid if we can take the Modulus by int_h and int_w
		//  and if doing so have no remainder or remainder < difference.
		
		// Assume 
		//
		// For each point pt in pts
		//  Transform pt to be in the relative coordinate system
		
		
		return rv;
	}
}
