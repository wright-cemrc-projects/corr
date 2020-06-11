package org.cemrc.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;

/**
 * A class for converting from Pixel to Stage positions.
 * @author larso
 *
 */
public class MatrixMath {
	
	/**
	 * Helper method for 3x3 matrix * Vector3 position
	 * @param matrix
	 * @param position
	 * @return
	 */
	public static Vector2<Float> multiply(double [][] matrix, Vector2<Float> position) {
		return multiply(MatrixUtils.createRealMatrix(matrix), position);
	}
	
	/**
	 * Multiply a 2x2 matrix by a 2D vector.
	 * @param matrix
	 * @param position
	 * @return
	 */
	public static Vector2<Float> multiply(RealMatrix matrix, Vector2<Float> position) {
		Vector2<Float> rv = new Vector2<Float>(0f,0f);
		
		if (matrix.getRowDimension() != 2 && matrix.getColumnDimension() != 2) throw new IllegalArgumentException("Invalid matrix.");
		
		double[][] matrixData = { {position.x }, {position.y } };
		RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		
		RealMatrix product = matrix.multiply(m);
		rv.x = (float) product.getData()[0][0];
		rv.y = (float) product.getData()[1][0];
		
		return rv;
	}
	
	/**
	 * Helper method for 3x3 matrix * Vector3 position
	 * @param matrix
	 * @param position
	 * @return
	 */
	public static Vector3<Float> multiply(double [][] matrix, Vector3<Float> position) {
		return multiply(MatrixUtils.createRealMatrix(matrix), position);
	}
	
	/**
	 * result = 3x3 matrix * Vector3 position
	 * @param position
	 * @param matrix
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Vector3<Float> multiply(RealMatrix matrix, Vector3<Float> position) throws IllegalArgumentException {
		Vector3<Float> rv = new Vector3<Float>(0f,0f,0f);
		
		if (matrix.getRowDimension() != 3 && matrix.getColumnDimension() != 3) throw new IllegalArgumentException("Invalid matrix.");
		
		double[][] matrixData = { {position.x }, {position.y }, {position.z} };
		RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		
		RealMatrix product = matrix.multiply(m);
		rv.x = (float) product.getData()[0][0];
		rv.y = (float) product.getData()[1][0];
		rv.z = (float) product.getData()[2][0];
		
		return rv;
	}
	
	/**
	 * Get a rotation 3x3 matrix by an angle in radians.
	 * @param angle
	 * @return
	 */
	public static double [][] getRotation(double angle) {
		double [][] rv = new double[3][3];
		
		rv[0][0] = Math.cos(angle);
		rv[0][1] = -Math.sin(angle);
		rv[0][2] = 0;
		rv[1][0] = Math.sin(angle);
		rv[1][1] = Math.cos(angle);
		rv[1][2] = 0;
		rv[2][0] = 0;
		rv[2][1] = 0;
		rv[2][2] = 1f;
		
		return rv;
	}
}
