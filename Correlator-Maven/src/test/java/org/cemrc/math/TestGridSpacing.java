package org.cemrc.math;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.junit.Test;

public class TestGridSpacing {

	@Test
	public void testGridSpacing() {
		
		// Test #1
		// 1. Create a 3x3 grid of units of 2D points with 1-unit differences (1,1) to (2,1), to (3,1), etc.
		// 2. Transform each of the points to a different coordinate system
		// 3. Given the vectors u, v determining this repeating grid determine show that each point is on the grid.
		
		Double dx = 0.1;
		
		// Define the grid
		Vector2<Double> origin = new Vector2<Double>(2.0, 3.0);
		Vector2<Double> gridw = new Vector2<Double>(2.0, 2.0);
		Vector2<Double> gridh = new Vector2<Double>(-2.0, 2.0);
		
		// This should be the transformation to the rotated, scaled, translated grid.
		// Consider building this via separate operations, may be easier.
		
		double[][] matrix_rotscale = { {gridw.x, gridw.y, 0.0}, {gridh.x, gridh.y, 0.0}, {0.0, 0.0, 1.0} };
		RealMatrix m_rot_scale = MatrixUtils.createRealMatrix(matrix_rotscale);
		
		// Apply this after the rotation/scaling
		double[][] matrix_trans = { {1.0, 0.0, origin.x }, {0.0, 1.0, origin.y }, {0, 0, 1.0} };
		RealMatrix m_translate = MatrixUtils.createRealMatrix(matrix_trans);
		 
		RealMatrix m_rot_scale_translate = m_translate.multiply(m_rot_scale);
		
		List<Vector2<Double>> testPoints = new ArrayList<Vector2<Double>>();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				
				Vector3<Float> position = new Vector3<Float>(i*1.0f, j*1.0f, 1.0f);
				
				// Do the transform operation here to move the points.
				//Vector3<Float> rotated = MatrixMath.multiply(matrix_rotscale, position);
				// Vector3<Float> result = rotated;
				//Vector3<Float> result = MatrixMath.multiply(matrix_trans, rotated);
				
				Vector3<Float> result = MatrixMath.multiply(m_rot_scale_translate, position);

				testPoints.add(new Vector2<Double>((double)result.x, (double)result.y));
			}
		}
		
 		GridSpacing spacing = new GridSpacing(origin, gridw, gridh);
		List<Vector2<Double>> onGrid = spacing.getOnGrid(testPoints, dx);
		
		assertEquals(9, onGrid.size());
		
		// 4. Test with off-grid points to see they are removed.
		Vector2<Double> badPoint = new Vector2<Double>(2.5, 3.5);
		testPoints.set(8, badPoint);
		
		List<Vector2<Double>> onGrid2 = spacing.getOnGrid(testPoints, dx);
		assertEquals(8, onGrid2.size());
	}
	
}
