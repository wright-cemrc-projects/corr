package org.cemrc.autodoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.cemrc.math.MatrixMath;
import org.junit.Test;

public class TestMatrixMath {

	@Test
	public void testMatrixMath() {
		
		// Allowable error
		float delta = 0.0001f;
		
		// Multiplication should occur without changing values.
		try {
			// Pixel Position to Stage Position unit tests.
			Vector2<Float> pixelPosition = new Vector2<Float>(1f, 2f);
			double [][] identityMatrix = { {1, 0}, {0, 1} };
			Vector2<Float> testPosition = MatrixMath.multiply(identityMatrix, pixelPosition);
			
			assertEquals(1.0, testPosition.x, delta);
			assertEquals(2.0, testPosition.y, delta);
			
		} catch (IllegalArgumentException ex) {
			fail(ex.getMessage());
		}
		
		// Known multiplications here.
		// TODO.
		
	}
	
}
