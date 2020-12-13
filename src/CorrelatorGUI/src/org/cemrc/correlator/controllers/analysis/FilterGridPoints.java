package org.cemrc.correlator.controllers.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cemrc.autodoc.Vector2;
import org.cemrc.correlator.analysis.ClusterMinima;
import org.cemrc.math.GridSpacing;

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
		public ClusterMinima pt1, pt2;
		
		public DeltaPoint(ClusterMinima pt1, ClusterMinima pt2) {
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
		Vector2<Double> origin;
		
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
	
	
	public List<ClusterMinima> findGridPoints(List<ClusterMinima> unfiltered) {
		
		final double MAX_LENGTH = 20.0;
		final double MIN_OFF_GRID = 0.1;
		
		// Transform into deltas between 2D points.
		int length = unfiltered.size();
		List<DeltaPoint> deltaList = new ArrayList<DeltaPoint>();
		
		// search for grids should begin with points nearest to the center.
		List<ClusterMinima> centricPoints = new ArrayList<ClusterMinima>(unfiltered);
		// TODO: sort by distance from center.
		double x = 1024;
		double y = 1024;
		
		centricPoints.sort(new Comparator<ClusterMinima>() {

			@Override
			public int compare(ClusterMinima o1, ClusterMinima o2) {
				double dx1 = o1.center.x - x;
				double dy1 = o1.center.y - y;
				double d1 = dx1*dx1 + dy1*dy1;
				
				double dx2 = o2.center.x - x;
				double dy2 = o2.center.y - y;
				double d2 = dx2*dx2 + dy2*dy2;
				
				if (d1 < d2) return -1;
				if (d1 > d2) return 1;
				return 0;
			}
			
		});
		
		// Create deltas
		for (int i = 0; i < length; i++) {
			for (int j = i+1; j < length; j++) {
				DeltaPoint p = new DeltaPoint(centricPoints.get(i), centricPoints.get(j));
				
				if (p.dx < MAX_LENGTH) {
					deltaList.add(p);
				}
			}
		}

		Map<GridSpacing, Set<Vector2<Double>>> localGrids = new HashMap<GridSpacing, Set<Vector2<Double>>>();
		for (DeltaPoint p : deltaList) {
			
			boolean onGrid = false;
			
			for (GridSpacing s : localGrids.keySet()) {

				// Add in additional points to local grids
				if (s.isOnGrid(p.pt1.center, MIN_OFF_GRID) && s.isOnGrid(p.pt2.center, MIN_OFF_GRID)) {
					localGrids.get(s).add(p.pt1.center);
					localGrids.get(s).add(p.pt2.center);
					onGrid = true;
				}
			}
			
			// When to start another local grid? When angle + spacing don't match -or- point cannot match grid?
			if (! onGrid) {
				double x1 = p.dx * Math.cos(p.angle);
				double y1 = p.dx * Math.sin(p.angle);
				
				double perpAngle = p.angle + Math.PI/2.0;
				
				double x2 = p.dx * Math.cos(perpAngle);
				double y2 = p.dx * Math.sin(perpAngle);
				
			
				Vector2<Double> axis_w = new Vector2<Double>(x1, y1);
				Vector2<Double> axis_h = new Vector2<Double>(x2, y2);
				
				GridSpacing grid = new GridSpacing(p.pt1.center, axis_w, axis_h);
				localGrids.put(grid, new HashSet<Vector2<Double>>());
				localGrids.get(grid).add(p.pt1.center);
				localGrids.get(grid).add(p.pt2.center);
			}
		}
		
		int largestGrid = 0;
		GridSpacing bestSpacing = null;
		for (GridSpacing s : localGrids.keySet()) {
			if (localGrids.get(s).size() > largestGrid) {
				largestGrid = localGrids.get(s).size();
				bestSpacing = s;
			}
		}
		
		double bestDistance = Double.MAX_VALUE;
		ClusterMinima origin = null;
		for (ClusterMinima m : unfiltered) {
			// find the closest to the origin.
			Vector2<Double> center = m.center;
			double dx = bestSpacing.getOrigin().x - center.x;
			double dy = bestSpacing.getOrigin().y - center.y;
			double d = Math.sqrt(dx*dx + dy*dy);
			if (d < bestDistance) {
				origin = m;
				bestDistance = d;
			}
		}
		
		// Grow a grid with the identified hough centroids
		// return growGrid(bestSpacing, unfiltered, origin);
		
		return hashGrid(bestSpacing, unfiltered, origin);
	}
	
	/**
	 * Puts points on a HashMap.
	 * @param s
	 * @param points
	 * @param origin
	 * @return
	 */
	private List<ClusterMinima> hashGrid(GridSpacing s, List<ClusterMinima> points, ClusterMinima origin) {
		Map<String, ClusterMinima> rv = new HashMap<String, ClusterMinima>();
		
		double MAX_ERROR = 0.8;
		
		// Foreach ClusterMinima transform the coordinate system to the grid
		//  then round each index to integer
		//   put in the HashMap if >= ClusterMinima radius.
		for (ClusterMinima p : points) {
			Vector2<Double> pNot = s.transform(p.center);
			
			double x_error = pNot.x % 1.0;
			double y_error = pNot.y % 1.0;
			
			if (x_error > MAX_ERROR || y_error > MAX_ERROR) continue;
			
			Integer x = (int) Math.round(pNot.x);
			Integer y = (int) Math.round(pNot.y);
			
			String index = x.toString() + ":" + y.toString();
			
			// Preference to larger radius circles in each square.
			if (rv.containsKey(index)) {
				if (rv.get(index).radius > p.radius) continue;
			} 
			
			rv.put(index, p);
		}
		
		return new ArrayList<ClusterMinima>(rv.values());
	}
	
	/**
	 * Growing grid algorithm to build a grid of points at even spacings with one point per grid spot.
	 * @param s
	 * @param points
	 * @param origin
	 * @return
	 */
	private List<ClusterMinima> growGrid(GridSpacing s, List<ClusterMinima> points, ClusterMinima origin) {
		
		// Points on the growing grid.
		Set<ClusterMinima> filled = new HashSet<ClusterMinima>();
		Set<ClusterMinima> next = new HashSet<ClusterMinima>(points);
		Map<Vector2<Double>, ClusterMinima> edges = new HashMap<Vector2<Double>, ClusterMinima>();
		
		double cutoff = s.getLength() + 5.0;
		
		// Initialize the search and expand edges list if possible
		List<Vector2<Double>> initEdges = expandEdges(s, origin.center);
		for (Vector2<Double> e : initEdges) {
			edges.put(e, null);
		}	
		next.remove(origin);
		filled.add(origin);
		
		// Iterative search to build grid.
		boolean searching = true;
		while (searching) {
			
			// Greedily pair edge -> next.
			for (ClusterMinima option : next) {

				for (Vector2<Double> edge : edges.keySet()) {
					double dx = Math.abs(edge.x - option.center.x);
					double dy = Math.abs(edge.y - option.center.y);
					double distance = Math.sqrt(dx*dx + dy*dy);
					
					// TODO: instead go for the best next not any next.
					if (distance < cutoff) {
						if (edges.get(edge) == null) {		
							edges.put(edge, option);
							break;
						} else {
							double dx2 = Math.abs(edge.x - edges.get(edge).center.x);
							double dy2 = Math.abs(edge.y - edges.get(edge).center.y);
							double distance2 = Math.sqrt(dx2*dx2 + dy2*dy2);
							
							if (distance < distance2) {
								edges.put(edge, option);
								break;
							}
						}
					}
				}
			}
			
			// Update the filled, edges, and next
			// If there are any updates then need to iterate again.
			searching = false;
			
			Map<Vector2<Double>, ClusterMinima> updateEdges = new HashMap<Vector2<Double>, ClusterMinima>();
			for (Vector2<Double> edge : edges.keySet()) {
				ClusterMinima option = edges.get(edge);
				if (option != null) {
					
					// 1. Remove option from next.
					next.remove(option);
					
					// 2. Add option to filled.
					filled.add(option);
					
					// Expand edges list if possible
					List<Vector2<Double>> addEdges = expandEdges(s, option.center);
					
					for (Vector2<Double> e : addEdges) {
						// TODO: should filter to prevent adding filled or existing edges.
						updateEdges.put(e, null);
					}
					
					searching = true;
				} else {
					updateEdges.put(edge, null);
				}
			}
			
			// Update the edges lists to only be new or unfilled edges.
			edges = updateEdges;
		}
		
		return new ArrayList<ClusterMinima>(filled);
	}
	
	private List<Vector2<Double>> expandEdges(GridSpacing s, Vector2<Double> origin) {
		List<Vector2<Double>> addEdges = new ArrayList<Vector2<Double>>();
			
		// Create new edge possibilities
		double x = origin.x;
		double y = origin.y;
		
		Vector2<Double> w = s.getAxisW();
		Vector2<Double> h = s.getAxisH();
		
		// +w +h
		addEdges.add(new Vector2<Double>(x + w.x + h.x, y + w.y + h.y));
		
		// +w -h
		addEdges.add(new Vector2<Double>(x + w.x - h.x, y + w.y - h.y));
		
		// -w +h
		addEdges.add(new Vector2<Double>(x - w.x + h.x, y - w.y + h.y));
		
		// -w -h
		addEdges.add(new Vector2<Double>(x - w.x - h.x, y - w.y - h.y));
		
		return addEdges;
	}
	
}
