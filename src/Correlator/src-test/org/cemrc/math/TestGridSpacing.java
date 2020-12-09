package org.cemrc.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.junit.jupiter.api.Test;

public class TestGridSpacing {

	@Test
	void testGridSpacing() {
		
		// Test #1
		// 1. Create a 3x3 grid of units of 2D points with 1-unit differences (1,1) to (2,1), to (3,1), etc.
		// 2. Transform each of the points to a different coordinate system
		// 3. Given the vectors u, v determining this repeating grid determine show that each point is on the grid.
		
		Double dx = 0.1;
		
		// Define the grid
		Vector2<Double> origin = new Vector2<Double>(2.0, 3.0);
		Vector2<Double> gridw = new Vector2<Double>(2.0, 2.0);
		Vector2<Double> gridh = new Vector2<Double>(-2.0, 2.0);
		
		// TODO:
		// This should be the transformation to the rotated, scaled, translated grid.
		// Consider building this via separate operations, may be easier.
		double[][] matrix = { {gridw.x, gridh.x, 0 }, {gridw.y, gridh.y, 0 }, {origin.x, origin.y, 1.0} };
		
		List<Vector2<Double>> testPoints = new ArrayList<Vector2<Double>>();
		for (int i = 1; i < 4; i++) {
			for (int j = 1; j < 4; j++) {
				
				Vector3<Float> position = new Vector3<Float>(i*1.0f, j*1.0f, 0.0f);
				
				// Do the transform operation here to move the points.
				Vector3<Float> result = MatrixMath.multiply(matrix, position);

				testPoints.add(new Vector2<Double>((double)result.x, (double)result.y));
			}
		}
		
		GridSpacing spacing = new GridSpacing(origin, gridw, gridh);
		List<Vector2<Double>> onGrid = spacing.getOnGrid(testPoints, dx);
		
		// TODO: not working yet.
		assertEquals(0, onGrid.size());
		
		// 4. TODO: test with off-grid points to see they are removed.
		
	}
	
}
