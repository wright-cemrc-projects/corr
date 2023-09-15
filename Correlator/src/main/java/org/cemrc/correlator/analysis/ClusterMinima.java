package org.cemrc.correlator.analysis;

import org.cemrc.autodoc.Vector2;

public class ClusterMinima {
	public Vector2<Double> center;
	public int radius;
	public int score;

	public ClusterMinima(Vector2<Double> center, int radius, int score) {
		this.radius = radius;
		this.score = score;
		this.center = center;
	}
	
	/**
	 * Compare two circle centers and their radius. If they are overlapping, report true.
	 * @param x
	 * @param y
	 * @param radius
	 * @return
	 */
	public boolean isOverlap(int x, int y, int radius, int overlap) {
		double dx = this.center.x - x;
		double dy = this.center.y - y;
		int dr = this.radius + radius + overlap;
		
		return dx * dx + dy * dy <= dr*dr;
	}
}