package org.cemrc.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.Registration;

/**
 * Iterative alignment procedure
 * @author larso
 *
 */
public class IterativeAlignment {
	
	public AffineTransformation getTransformation(List<RegistrationPair> registrations) {
		List<Vector2<Float>> targetPoints = new ArrayList<Vector2<Float>>();
		List<Vector2<Float>> refPoints = new ArrayList<Vector2<Float>>();
		
		for (RegistrationPair p : registrations) {
			targetPoints.add(new Vector2<Float>( p.getTargetPoint().x.floatValue(), p.getTargetPoint().y.floatValue()));
			refPoints.add(new Vector2<Float>(p.getReferencePoint().x.floatValue(), p.getReferencePoint().y.floatValue()));
		}
		
		return AffineTransformation.generate(targetPoints, refPoints);
	}
	
	/**
	 * Given an affine matrix mapping target -> reference, find the closest registration pairs.
	 * 
	 * @param mat
	 * @param reference
	 * @param target
	 * @return
	 */
	public List<RegistrationPair> findBestPairs(AffineTransformation transform, List<Vector2<Float>> reference, List<Vector2<Float>> target) {
		List<RegistrationPair> rv = new ArrayList<RegistrationPair>();
		
		// Apply the affine transform to the target points to get a new positions in the local coordinates of the reference image.
		List<Vector2<Float>> transformedPoints = new ArrayList<Vector2<Float>>();
		
		// Transform points by the matrix
		for (Vector2<Float> pt : target) {
			Vector3<Float> tmpPt = new Vector3<Float>(pt.x, pt.y, 1.0f);
			
			Vector3<Float> pt2 = MatrixMath.multiply(transform.getMatrix(), tmpPt);
			transformedPoints.add(new Vector2<Float>(pt2.x, pt2.y));
		}
		
		// Look for nearest points between reference and transformedPoints (aligned target)
		double delta = 5; // 5 pixel different between reference and target points.
		
		// transformed -> reference
		Map<Integer, Integer> pairs = new HashMap<Integer, Integer>();
		
		// Unoptimized, comparing every point of target vs reference.
		for (int i = 0; i < transformedPoints.size(); i++) {
			
			Vector2<Float> pt = transformedPoints.get(i);
			
			// Keep the closest pair point below delta.
			double bestDistance = Double.MAX_VALUE;
			
			for (int j = 0; j < reference.size(); j++) {
				
				Vector2<Float> pt2 = reference.get(j);
				
				double dx = pt.x - pt2.x;
				double dy = pt.y - pt2.y;
				double distance = Math.sqrt(dx*dx + dy*dy);
				
				if (distance < delta && distance < bestDistance) {
					pairs.put(i, j);
				}
			}
		}
		
		// From the best pairings, create RegistrationPair with untransformed points.
		for (Integer target_index : pairs.keySet()) {
			Integer reference_index = pairs.get(target_index);
			
			RegistrationPair pair = new RegistrationPair();
			pair.setTargetPoint(target.get(target_index));
			pair.setReferencePoint(reference.get(reference_index));
			rv.add(pair);
		}
		
		return rv;
	}

	/**
	 * Perform an iterative alignment 
	 * 
	 * @param registrations
	 * @param reference
	 * @param target
	 * @return
	 */
	public Registration doAlignment(List<RegistrationPair> registrations, IPositionDataset reference, IPositionDataset target) {
		
		// Registrations must contain a minimum 3 pairs of points:
		if (registrations == null || registrations.size() < 3) {
			return null;
		}
		
		// Calculate the initial affine transform matrix from reference -> target with 3 or more points.
		// calculate the AffinineTransformation.calculateAffineTransform
		AffineTransformation pixelMat = getTransformation(registrations);
		
		// TODO: Calculate the overall LSQ error of the transformation	
		boolean improved = true;
		
		// Loop while improving transformation:
		while (improved) {
		
			// Apply the affine transform to the target points to get a new positions in the local coordinates of the reference image.
			List<Vector2<Float>> transformedPoints = new ArrayList<Vector2<Float>>();
			
			// Update found matching reference pairs between IPositionDatasets if within a cutoff distance and closest matching point.
			// Calculate the overall LSQ error of the transformation
			// If the error is decreased 
		
		}
			
		Registration rv = new Registration();
		// TODO: what stage? rv.setStageMatrix(mat.getMatrix());
		rv.setPixelMatrix(pixelMat.getMatrix());
		rv.setNote("src: " + reference.getMapId());
		rv.setRegisterMapId(reference.getMapId());
		rv.setRegis(reference.getMap().getRegis());
		
		return rv;
	}
	
}
