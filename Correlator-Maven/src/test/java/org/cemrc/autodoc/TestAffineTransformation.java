package org.cemrc.autodoc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.cemrc.data.BasicMap;
import org.cemrc.data.NavData;
import org.cemrc.data.PixelPositionDataset;
import org.cemrc.data.Registration;
import org.cemrc.math.AffineTransformation;
import org.cemrc.math.MatrixMath;
import org.junit.Assert;
import org.junit.Test;

public class TestAffineTransformation {

	@Test
	public void testAffineTransformation() {
		
		double delta = 0.001f;
		
		// Create a list of 10 points
		List<Vector2<Float>> testPoints = new ArrayList<Vector2<Float>>();
		testPoints.add(new Vector2<Float>(1f,0f));
		testPoints.add(new Vector2<Float>(11100f, 14124f));
		testPoints.add(new Vector2<Float>(2525f, 12414f));
		testPoints.add(new Vector2<Float>(23525f, 15124f));
		
		// Use AffineTransformation to calculate the matrix needed to acheive this.
		double [][] rv = AffineTransformation.calculateAffineTransform(testPoints, testPoints);
		
		// Verify this matrix is within error bounds of the expected values.
		Assert.assertEquals(1.0, rv[0][0], delta);
		Assert.assertEquals(1.0, rv[1][1], delta);
		Assert.assertEquals(1.0, rv[2][2], delta);
		
		// Calculate a rotation and see if we can figure it out.
		double [][] rotateMat = MatrixMath.getRotation(90 * Math.PI / 180);
		List<Vector2<Float>> rotatedPoints = new ArrayList<Vector2<Float>>();
		
		for (Vector2<Float> pt : testPoints) {
			Vector3<Float> pt3 = new Vector3<Float>(pt.x, pt.y, 1.0f);
			Vector3<Float> rotated = MatrixMath.multiply(rotateMat, pt3);
			rotatedPoints.add(new Vector2<Float>(rotated.x, rotated.y));
		}
		
		// Correct, the above rotation needed the correct matrix multiplication.
		double [][] affineTest2 = AffineTransformation.calculateAffineTransform(testPoints, rotatedPoints);
		
		// Compare matrices.
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				Assert.assertEquals(rotateMat[i][j], affineTest2[i][j], delta);
			}
		}
	}
	
	@Test
	public void testNavDataIntegration() {
		NavData data = new NavData();
		
		// NavData integration testing.
		
		// 1. Create 2 known maps
		BasicMap temMap = new BasicMap();
		temMap.setMapScaleMat(new double[][] {{-26.5269f, 1.68092f}, {1.64926f, 25.6455f}});
		temMap.setRawStageXY(new Vector2<Float>(472.989f, 349.672f));
		temMap.setDimensions(new Vector2<Integer>(2880, 2046));
		
		BasicMap flmMap = new BasicMap();
		flmMap.setMapScaleMat(new double[][] {{10, 0}, {0, -10}});
		flmMap.setRawStageXY(new Vector2<Float>(0f, 0f));
		flmMap.setDimensions(new Vector2<Integer>(630, 627));
		
		// 2. Create known registration pixel positions on each
		List<Vector2<Float>> temRegistrationPositions = new ArrayList<Vector2<Float>>();
		temRegistrationPositions.add(new Vector2<Float>(1373.664f, 1262.531f));
		temRegistrationPositions.add(new Vector2<Float>(1232.33f, 1153.898f));
		temRegistrationPositions.add(new Vector2<Float>(1796.103f, 1176.501f));
		temRegistrationPositions.add(new Vector2<Float>(1387.511f, 771.1838f));
		temRegistrationPositions.add(new Vector2<Float>(1091.024f, 1045.289f));
		temRegistrationPositions.add(new Vector2<Float>(1295.306f, 1247.936f));
		temRegistrationPositions.add(new Vector2<Float>(1060.289f, 1204.198f));
		temRegistrationPositions.add(new Vector2<Float>(1372.157f, 850.6502f));
		
		PixelPositionDataset temRegistrationDataset = new PixelPositionDataset();
		temRegistrationDataset.setPixelPositions(temRegistrationPositions);
		temRegistrationDataset.setMap(temMap);
		
		List<Vector2<Float>> flmRegistrationPositions = new ArrayList<Vector2<Float>>();
		flmRegistrationPositions.add(new Vector2<Float>(309f, 376f));
		flmRegistrationPositions.add(new Vector2<Float>(287f, 330f));
		flmRegistrationPositions.add(new Vector2<Float>(426f, 409f));
		flmRegistrationPositions.add(new Vector2<Float>(374f, 254f));
		flmRegistrationPositions.add(new Vector2<Float>(265f, 285f));
		flmRegistrationPositions.add(new Vector2<Float>(291f, 362f));
		flmRegistrationPositions.add(new Vector2<Float>(237f, 321f));
		flmRegistrationPositions.add(new Vector2<Float>(360f, 272f));
		
		PixelPositionDataset flmRegistrationDataset = new PixelPositionDataset();
		flmRegistrationDataset.setPixelPositions(flmRegistrationPositions);
		flmRegistrationDataset.setMap(flmMap);
		
		// 3. Unit test check that StagePosition conversion is correct.
		List<Vector2<Float>> checkTemPositions = temRegistrationDataset.getStagePositions(false);
		List<Vector2<Float>> checkFlmPositions = flmRegistrationDataset.getStagePositions(false);
		
		float delta = 0.1f;
		// Check pt.1 of TEM (Some precision difference compared to Jae's..)
		assertEquals(476.05792f, checkTemPositions.get(0).x, delta);
		assertEquals(358.8109f, checkTemPositions.get(0).y, delta);
		
		// Check pt.1 of FLM
		assertEquals(-0.6f, checkFlmPositions.get(0).x, delta);
		assertEquals(-6.25f, checkFlmPositions.get(0).y, delta);
		
		// 3. Add image positions on the target (FLM) map
		List<Vector2<Float>> flmImagePositions = new ArrayList<Vector2<Float>>();
		flmImagePositions.add(new Vector2<Float>(393f, 379.5f));
		flmImagePositions.add(new Vector2<Float>(285.5f, 348f));
		flmImagePositions.add(new Vector2<Float>(234.5f, 280f));
		flmImagePositions.add(new Vector2<Float>(310.5f, 227f));
		flmImagePositions.add(new Vector2<Float>(327.5f, 382.5f));
		flmImagePositions.add(new Vector2<Float>(321f, 310.5f));
		flmImagePositions.add(new Vector2<Float>(346f, 290f));
		flmImagePositions.add(new Vector2<Float>(332.5f, 308.5f));
		
		PixelPositionDataset flmImageDataset = new PixelPositionDataset();
		flmImageDataset.setPixelPositions(flmImagePositions);
		flmImageDataset.setMap(flmMap);
		
		// 4. Create do a registration operation between the correct pairs
		Registration register = Registration.generate(flmRegistrationDataset, temRegistrationDataset);
		flmMap.setRegistration(register);
		
		// Check that the registration Mstage matrix is correct
		assertEquals(-1.227f, register.getStageMatrix()[0][0], delta);
		assertEquals(-1.2667f, register.getStageMatrix()[1][1], delta);
		assertEquals(1f, register.getStageMatrix()[2][2], delta);
		
		// 5. Request the MapScaleMat on registered map /w and without registration (*)
		Vector4<Float> msmOriginal = flmMap.getMapScaleMat(false);
		assertEquals(10, msmOriginal.x, delta);
		assertEquals(0, msmOriginal.y, delta);
		assertEquals(0, msmOriginal.z, delta);
		assertEquals(-10, msmOriginal.w, delta);
		
		// 6. Request a Pixel -> Stage using the affine transformation.
		Vector2<Float> knownPixel = new Vector2<Float>(630f, 627f);
		Vector2<Float> stageNoAffine = flmMap.getStageFromPixel(knownPixel, false);
		Vector2<Float> stageAffine = flmMap.getStageFromPixel(knownPixel,  true);
		
		assertEquals(31.5f, stageNoAffine.x, delta);
		assertEquals(-31.35f, stageNoAffine.y, delta);
		
		// If these tests pass, then I can correct get stage positions now, including bounds.
		assertEquals(423.725f, stageAffine.x, delta);
		assertEquals(372.809f, stageAffine.y, delta);
		
		Vector2<Float> testPixel = flmMap.getPixelFromStage(stageAffine, true);
		assertEquals(knownPixel.x, testPixel.x, delta);
		assertEquals(knownPixel.y, testPixel.y, delta);
	}

}
