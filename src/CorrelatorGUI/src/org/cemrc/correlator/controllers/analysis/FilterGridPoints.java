package org.cemrc.correlator.controllers.analysis;

import java.util.ArrayList;
import java.util.List;

import org.cemrc.correlator.analysis.CircleHoughTransformTask;

/**
 * Given an input list of 2D positions determine which points on a lattice:
 * 1. this should create a distance map (only upper half)
 * 2. determine the common intervals between grid points 
 * 3. determine when a point does not obey the grid intervals
 * 4. filter out any points that don't obey grid intervals and return a list.
 * 
 * @author mrlarson2
 *
 */
public class FilterGridPoints {

	
	public static List<CircleHoughTransformTask.ClusterMinima> findGridPoints(List<CircleHoughTransformTask.ClusterMinima> unfiltered) {
		List<CircleHoughTransformTask.ClusterMinima> rv = new ArrayList<CircleHoughTransformTask.ClusterMinima>();
		
		// TODO
		// Goal is to discover the lines containing multiple points at even spacing
		//  A grid point will be part of two such lines.
		//  It may also be true that diagonal lines exhibit regular point spacing.
		
		// Step 1. Find the distances and unit normal vectors between all points
		
		// The dot product between two unit vectors a_u * b_u = cos theta
		
		// If we only care about 2D vectors, can use theta = atan2(v2.y, v2.x) - atan2(v1.y, v1.x)
		
		// To find the points on the same directional lines, will cluster within 5 degrees the unit vector points
		
		// Eliminate points that don't fall into both of the 2 largest directional clusterings?
		
		// Think more on how to use the directional information to clear disorganized points.
		
		return rv;
	}
	
}
