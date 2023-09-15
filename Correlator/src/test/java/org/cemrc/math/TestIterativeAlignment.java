package org.cemrc.math;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math4.legacy.linear.MatrixUtils;
import org.apache.commons.math4.legacy.linear.RealMatrix;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.junit.Test;

/**
 * Unit test for the IterativeAlignment procedure.
 * @author mrlarson2
 *
 */
public class TestIterativeAlignment {
	
	private List<Vector2<Float>> getMovedPoints(List<Vector2<Float>> points) {
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
		
		List<Vector2<Float>> testPoints = new ArrayList<Vector2<Float>>();
		
		for (Vector2<Float> pt : points) {
				
			Vector3<Float> position = new Vector3<Float>(pt.x, pt.y, 1.0f);		
			Vector3<Float> result = MatrixMath.multiply(m_rot_scale_translate, position);
			testPoints.add(new Vector2<Float>(result.x, result.y));
		}
		
		return testPoints;
	}
	
	@Test
	public void testFindBestPairs() {
		
		// Test #1 
		
		// Create two sets of points that should overlap with and without transformations.
		List<Vector2<Float>> t1_target = new ArrayList<Vector2<Float>>();
		List<Vector2<Float>> t1_reference = new ArrayList<Vector2<Float>>();
		
		// Create 9 expected overlaps.
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				t1_target.add(new Vector2<Float>((float) i, (float) j)); 
				t1_reference.add(new Vector2<Float>((float) i, (float) j)); 
			}
		}
		
		AffineTransformation transform = AffineTransformation.generate(t1_reference, t1_target);
		
		// Test should verify 9 registration pairs, with an identity transform
		IterativeAlignment aligner = new IterativeAlignment();
		List<RegistrationPair> pairs = aligner.findBestPairs(transform, t1_reference, t1_target);
		
		assertEquals(t1_target.size(), pairs.size());
		
		// --------------
		// Test #2 
		
		// Make a more complicated set with a mismatch or make the range a parameter.
		
		// Create 9 expected overlaps.
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				t1_target.add(new Vector2<Float>((float) i, (float) j)); 
				t1_reference.add(new Vector2<Float>((float) i, (float) j)); 
			}
		}
		
		// Initial alignment points
		// pt0: 0, 0
		// pt1: 1, 0  .. (2, 0) ..
		// pt3: 0, 1
		
		// Transformation could rotate and translate the target.
		List<Vector2<Float>> moved = getMovedPoints(t1_target);
		
		AffineTransformation transform2 = AffineTransformation.generate(moved, t1_reference);
		
		List<RegistrationPair> pairs2 = aligner.findBestPairs(transform2, t1_reference, moved);
		
		assertEquals(t1_target.size(), pairs2.size());
		
		// Add some mismatched points.
		t1_target.add(new Vector2<Float>(15f, 20f));
		moved.add(new Vector2<Float>(-30f, -50f));
		
		List<RegistrationPair> pairs3 = aligner.findBestPairs(transform2, t1_reference, moved);
		
		// Assume 1 mismatch
		assertEquals(t1_target.size() - 1, pairs3.size());
	}

}
