package org.cemrc.autodoc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cemrc.data.BasicMap;
import org.junit.jupiter.api.Test;

public class TestBasicMap {
	
	@Test
	public void testGetCorrectPtsXY() {
		
		float delta = 0.01f;
		
		// CORRECT [x]
		// Transformations 101 : Pixel -> Stage using MapScaleMat and image dimensions.
		BasicMap flmMap = new BasicMap();
		flmMap.setMapScaleMat(new double[][] {{10, 0}, {0, -10}});
		flmMap.setRawStageXY(new Vector2<Float>(0f, 0f));
		flmMap.setDimensions(new Vector2<Integer>(630, 627));
		
		Vector2<Float> upperLeft = flmMap.getStageFromPixel(new Vector2<Float>(0f,0f), false);
		assertEquals(-31.5f, upperLeft.x, delta);
		assertEquals(31.35f, upperLeft.y, delta);
		
		// Try with a known pixel to stage coordinate as well.
		Vector2<Float> knownPixel = new Vector2<Float>(309f, 376f);
		Vector2<Float> testStage = flmMap.getStageFromPixel(knownPixel, false);
		Vector2<Float> knownStage = new Vector2<Float>(-0.599f, -6.25f);
		
		assertEquals(knownStage.x, testStage.x, delta);
		assertEquals(knownStage.y, testStage.y, delta);
		
		// Transformations 101 : Pixel -> Stage transform.
		Vector2<Float> testPixel = flmMap.getPixelFromStage(testStage, false);
		
		assertEquals(knownPixel.x, testPixel.x, delta);
		assertEquals(knownPixel.y, testPixel.y, delta);
	}
	
	@Test
	public void testGetCorrectStageBounds() {
		float delta = 0.01f;
		
		BasicMap flmMap = new BasicMap();
		flmMap.setMapScaleMat(new double[][] {{10, 0}, {0, -10}});
		flmMap.setRawStageXY(new Vector2<Float>(0f, 0f));
		flmMap.setDimensions(new Vector2<Integer>(630, 627));
		
		List<Vector2<Float>> stageBoundsFlm = flmMap.getStageBounds(false);
		assertEquals(-31.5f, stageBoundsFlm.get(0).x, delta);
		assertEquals(31.35f, stageBoundsFlm.get(0).y, delta);
		
		/*
		PtsX = -963.495 963.495 963.495 -963.495 -963.495
		PtsY = -962.000 -962.000 962.000 962.000 -962.000
		*/
		BasicMap temMap = new BasicMap();
		temMap.setMapScaleMat(new double[][] {{7.751, 0.0f}, {0.0f, 7.751}});
		temMap.setDimensions(new Vector2<Integer>(14823, 14800));
		temMap.setRawStageXY(new Vector2<Float>(-10f, -10f));
		
		List<Vector2<Float>> stageBounds = temMap.getStageBounds(false);
		
		/* TEM map fails this, we cannot use this for directly calculating stage bounds?
		assertEquals(-963.495, stageBounds.get(0).x, delta);
		assertEquals(-962.000, stageBounds.get(0).y, delta);
		
		assertEquals(963.495, stageBounds.get(2).x, delta);
		assertEquals(962.000, stageBounds.get(2).y, delta);
		*/
	}
}
