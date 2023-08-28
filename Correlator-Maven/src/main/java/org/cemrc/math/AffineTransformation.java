package org.cemrc.math;

import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.cemrc.autodoc.Vector2;

/**
 * This class describes an affine transformation that can be applied to data.
 * @author larso
 *
 */
public class AffineTransformation {
	
	// Transformation matrix
	private double[][] matrix;
	
	/**
	 * Get a transformation class that can be used to update records.
	 * @param pts1
	 * @param pts2
	 * @return
	 */
	public static AffineTransformation generate(List<Vector2<Float>> pts1, List<Vector2<Float>> pts2) {
		AffineTransformation rv = new AffineTransformation();
		
		rv.setMatrix(calculateAffineTransform(pts1, pts2));
		
		return rv;
	}
	
	public void setMatrix(double[][] mat) {
		matrix = mat;
	}
	
	public double[][] getMatrix() {
		return matrix;
	}
	
	/**
	 * This will calculate the affine transformation of points. (pts1 * M = pts2)
	 * @param target (FLM) <- would be moved by this matrix to reference.
	 * @param reference (TEM)
	 * @return
	 */
	public static double[][] calculateAffineTransform(List<Vector2<Float>> target, List<Vector2<Float>> reference) {
		
		if (reference.size() < 3 && target.size() < 3) {
			throw new IllegalArgumentException("Too few points to calculate transform.");
		}
		
		double [][] Pa = new double [3][target.size()];
		double [][] Qa = new double [3][reference.size()];
		
		// Given 2D points, we pad out a 3rd dimension with value 1.0
		for (int i = 0; i < target.size(); i++) {
			Pa[0][i] = target.get(i).x;
			Pa[1][i] = target.get(i).y;
			Pa[2][i] = 1.0f;
		}
		
		for (int i = 0; i < reference.size(); i++) {
			Qa[0][i] = reference.get(i).x;
			Qa[1][i] = reference.get(i).y;
			Qa[2][i] = 1.0f;
		}
		
		// Q*TransP * inverse(P*TransP)
		RealMatrix P = MatrixUtils.createRealMatrix(Pa);
		RealMatrix Q = MatrixUtils.createRealMatrix(Qa);
		
		RealMatrix Pt = P.transpose(); // Correct till here.
		RealMatrix Q_Pt = Q.multiply(Pt); // Correct
		
		RealMatrix P_Pt = P.multiply(Pt);
		//RealMatrix Pt_P = Pt.multiply(P); // Not used.
		
		RealMatrix inv_P_Pt = new QRDecomposition(P_Pt).getSolver().getInverse(); // Correct, we think.
		//RealMatrix inv_Pt_P = new QRDecomposition(Pt_P).getSolver().getInverse(); // Wrong dim
		
		RealMatrix M = Q_Pt.multiply(inv_P_Pt);
		
		// copy out into the 3x3 return matrix.
		return M.getData();
	}
}
