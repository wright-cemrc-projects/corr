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
	
	/** 
	 * Data structure represent directional change between points.
	 * @author mrlarson2
	 */
	public static class DeltaPoint {
		// Describe the direction and distance
		public double angle, dx;
		
		// Original points
		CircleHoughTransformTask.ClusterMinima pt1, pt2;
		
		public DeltaPoint(CircleHoughTransformTask.ClusterMinima pt1, CircleHoughTransformTask.ClusterMinima pt2) {
			this.pt1 = pt1;
			this.pt2 = pt2;
			
			double x = pt1.center.x - pt2.center.x;
			double y = pt1.center.y - pt2.center.y;
			
			// Keep directionality in the first two quadrants.
			if (y < 0) {
				x *= -1.0;
				y *= -1.0;
			}
			
			dx = Math.sqrt(x*x + y*y);
			angle = Math.atan(y / x);
		}
	}

	/**
	 * A cluster contains one or more DeltaPoint(s)
	 * We want to find the largest cluster of similar orientation, spacing.
	 * We will use this to establish a grid.
	 * 
	 * @author mrlarson2
	 *
	 */
	public static class GridCluster implements Comparable<GridCluster> {
		
		// Points in polar cluster.
		List<Double> angleList = new ArrayList<Double>();
		List<Double> distanceList = new ArrayList<Double>();
		
		// These are the cluster center.
		Double averageAngle;
		Double averageDistance;
		
		/**
		 * Start a grid cluster with a known angle/distance
		 * @param p
		 */
		public GridCluster(Double angle, Double distance) {
			addPair(angle, distance);
		}
		
		/**
		 * Add a pair of {angle, distance}
		 * @param angle
		 * @param distance
		 */
		public void addPair(Double angle, Double distance) {
			angleList.add(angle);
			distanceList.add(distance);
			updateAverages();
		}
		
		/**
		 * Returns the number of members in the cluster.
		 * AngleList and DistanceList should be matching dimensions.
		 * @return
		 */
		public int getSize() {
			return angleList.size();
		}
		
		/**
		 * Update the average angle and distances
		 */
		private void updateAverages() {
			averageAngle = calculateAverage(angleList);
			averageDistance = calculateAverage(distanceList);
		}
		
		private double calculateAverage(List<Double> values) {
			
			if (values.size() > 0) {
				double rv = values.stream().reduce(0.0, Double::sum);
				return rv / values.size();
			}
			return 0.0;
			
		}
		
		/**
		 * Similar returns true if 
		 * @param p
		 * @return
		 */
		public boolean similar(DeltaPoint p, double deltaAngle, double deltaDistance) {
			double dA = Math.abs(averageAngle - p.angle);
			double dX = Math.abs(averageDistance - p.dx);
			return (dA < deltaAngle && dX < deltaDistance);
		}
		
		/**
		 * Allow sorting clusters by size.
		 */
	    @Override
	    public int compareTo(GridCluster o) {
	    	if (this.getSize() < o.getSize()) return -1;
	    	if (this.getSize() > o.getSize()) return 1;
	    	return 0;
	    }
	    
	    @Override
	    public String toString() {
	    	return Integer.toString(this.getSize());
	    }
	}
	
	
	public List<CircleHoughTransformTask.ClusterMinima> findGridPoints(List<CircleHoughTransformTask.ClusterMinima> unfiltered) {
		List<CircleHoughTransformTask.ClusterMinima> rv = new ArrayList<CircleHoughTransformTask.ClusterMinima>();
		
		final double MAX_LENGTH = 20.0;
		
		// Transform into deltas between 2D points.
		int length = unfiltered.size();
		List<DeltaPoint> deltaList = new ArrayList<DeltaPoint>();
		
		// Create deltas
		for (int i = 0; i < length; i++) {
			for (int j = i+1; j < length; j++) {
				DeltaPoint p = new DeltaPoint(unfiltered.get(i), unfiltered.get(j));
				
				if (p.dx < MAX_LENGTH) {
					deltaList.add(p);
				}
			}
		}
		
		// Cluster directions and stepsizes.
		double dA = 5.0 / 180.0 * Math.PI;
		double dX = 5.0;
		
		List<GridCluster> clusters = new ArrayList<GridCluster>();
		
		for (DeltaPoint p : deltaList) {
			boolean found = false;
			
			for (GridCluster c : clusters) {
				if (c.similar(p,  dA, dX)) {
					c.addPair(p.angle, p.dx);
					found = true;
					break;
				}
			}
			
			if (found == false) {
				clusters.add(new GridCluster(p.angle, p.dx));
			}
		}
		
		// Establish a grid from two vectors, two spacings and origin point.
		// Find list of points within distance XX of the grid intersections.
		
		for (GridCluster c : clusters) {
			// Establish a grid from two vectors, two spacings and origin point.
			// Find list of points within distance XX of the grid intersections.
			
			// The list with the most accepted points is the best grid, keep that list and return.
		}
		
		return rv;
	}
	
}
