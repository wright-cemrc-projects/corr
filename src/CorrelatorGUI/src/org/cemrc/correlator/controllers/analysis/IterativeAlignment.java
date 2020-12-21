package org.cemrc.correlator.controllers.analysis;

import java.util.List;

import org.cemrc.data.IPositionDataset;
import org.cemrc.data.Registration;

/**
 * Mock class for an iterative alignment procedure
 * @author larso
 *
 */
public class IterativeAlignment {

	public static Registration doAlignment(List<RegistrationPair> registrations, IPositionDataset reference, IPositionDataset target) {
		Registration rv = new Registration();
		
		// If registrations contains minimum 3 pairs of points:
		
		// Calculate the initial affine transform matrix from reference -> target with 3 or more points.
		// Calculate the overall LSQ error of the transformation	
		
		// Loop while improving transformation:
		
			// Apply the affine transform to the target points to get a new positions in the local coordinates of the reference image.
			// Update found matching reference pairs between IPositionDatasets if within a cutoff distance and closest matching point.
			// Calculate the overall LSQ error of the transformation
			// If the error is decreased, 
			
		return rv;
	}
	
}
