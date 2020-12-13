package org.cemrc.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;

/**
 * Idealized representation of an endless lattice of points.
 * 
 * 
 * @author mrlarson2
 */
public class GridSpacing {
	
	private Vector2<Double> m_origin;
	private Vector2<Double> m_axis_h;
	private Vector2<Double> m_axis_w;
	private double m_length_h;
	
	/**
	 * Constructor
	 * @param origin : origin point on grid, best if near center of image
	 * @param axish : directional vector h
	 * @param axisw : directional vector w
	 */
	public GridSpacing(Vector2<Double> origin, Vector2<Double> axish, Vector2<Double> axisw) {
		m_origin = origin;
		m_axis_h = axish;
		m_axis_w = axisw;
		
		setLength(Math.sqrt(m_axis_h.x*m_axis_h.x + m_axis_h.y * m_axis_h.y));
	}
	
	public double getLength() {
		return m_length_h;
	}
	
	public void setLength(double l) {
		m_length_h = l;
	}
	
	public Vector2<Double> getOrigin() {
		return m_origin;
	}
	
	public Vector2<Double> getAxisH() {
		return m_axis_h;
	}
	
	public Vector2<Double> getAxisW() {
		return m_axis_w;
	}
	
	public Vector2<Double> transform(Vector2<Double> pt) {
		// 1. Translate points from origin to 0,0,0
		// 2. Create a matrix representing the coordinate system
		double[][] matrix_rotscale = { {m_axis_w.x, m_axis_w.y, 0.0}, {m_axis_h.x, m_axis_h.y, 0.0}, {0.0, 0.0, 1.0} };
		RealMatrix m_rot_scale = MatrixUtils.createRealMatrix(matrix_rotscale);
		
		// Apply this after the rotation/scaling
		double[][] matrix_trans = { {1.0, 0.0, m_origin.x }, {0.0, 1.0, m_origin.y }, {0, 0, 1.0} };
		RealMatrix m_translate = MatrixUtils.createRealMatrix(matrix_trans);
		
		RealMatrix m_rot_scale_translate = m_translate.multiply(m_rot_scale);
		RealMatrix invAffineMatrix = MatrixUtils.inverse(m_rot_scale_translate);
		
		// 3. Each pt in pts can be transformed by the inverse matrix to get pt'
		// 4. If pt' is on the grid it should have unit values of 1, otherwise any remainder is error or distance from the grid.
		Vector3<Float> ptNot = MatrixMath.multiply(invAffineMatrix, new Vector3<Float>(pt.x.floatValue(), pt.y.floatValue(), 1.0f));
		// Check the pointNot
		return new Vector2<Double>((double) ptNot.x, (double) ptNot.y);
	}
	
	/**
	 * Returns the points that are on the grid.
	 * @param pts
	 * @param range
	 * @return
	 */
	public List<Vector2<Double>> getOnGrid(List<Vector2<Double>> pts, double range) {
		List<Vector2<Double>> rv = new ArrayList<Vector2<Double>>();
		
		// 1. Translate points from origin to 0,0,0
		// 2. Create a matrix representing the coordinate system
		
		double[][] matrix_rotscale = { {m_axis_w.x, m_axis_w.y, 0.0}, {m_axis_h.x, m_axis_h.y, 0.0}, {0.0, 0.0, 1.0} };
		RealMatrix m_rot_scale = MatrixUtils.createRealMatrix(matrix_rotscale);
		
		// Apply this after the rotation/scaling
		double[][] matrix_trans = { {1.0, 0.0, m_origin.x }, {0.0, 1.0, m_origin.y }, {0, 0, 1.0} };
		RealMatrix m_translate = MatrixUtils.createRealMatrix(matrix_trans);
		
		RealMatrix m_rot_scale_translate = m_translate.multiply(m_rot_scale);
		RealMatrix invAffineMatrix = MatrixUtils.inverse(m_rot_scale_translate);
		
		// 3. Each pt in pts can be transformed by the inverse matrix to get pt'
		// 4. If pt' is on the grid it should have unit values of 1, otherwise any remainder is error or distance from the grid.
		for (Vector2<Double> pt : pts) {
			Vector3<Float> ptNot = MatrixMath.multiply(invAffineMatrix, new Vector3<Float>(pt.x.floatValue(), pt.y.floatValue(), 1.0f));
			// Check the pointNot
			float xMod = ptNot.x % 1;
			float yMod = ptNot.y % 1;
			
			if (xMod < range && yMod < range) {
				rv.add(pt);
			}
		}
		
		return rv;
	}
	
	public boolean isOnGrid(Vector2<Double> pt, double range) {
		List<Vector2<Double>> rv = new ArrayList<Vector2<Double>>();
		
		// 1. Translate points from origin to 0,0,0
		// 2. Create a matrix representing the coordinate system
		
		double[][] matrix_rotscale = { {m_axis_w.x, m_axis_w.y, 0.0}, {m_axis_h.x, m_axis_h.y, 0.0}, {0.0, 0.0, 1.0} };
		RealMatrix m_rot_scale = MatrixUtils.createRealMatrix(matrix_rotscale);
		
		// Apply this after the rotation/scaling
		double[][] matrix_trans = { {1.0, 0.0, m_origin.x }, {0.0, 1.0, m_origin.y }, {0, 0, 1.0} };
		RealMatrix m_translate = MatrixUtils.createRealMatrix(matrix_trans);
		
		RealMatrix m_rot_scale_translate = m_translate.multiply(m_rot_scale);
		RealMatrix invAffineMatrix = MatrixUtils.inverse(m_rot_scale_translate);
		
		// 3. Each pt in pts can be transformed by the inverse matrix to get pt'
		// 4. If pt' is on the grid it should have unit values of 1, otherwise any remainder is error or distance from the grid.

		Vector3<Float> ptNot = MatrixMath.multiply(invAffineMatrix, new Vector3<Float>(pt.x.floatValue(), pt.y.floatValue(), 1.0f));
		// Check the pointNot
		float xMod = ptNot.x % 1;
		float yMod = ptNot.y % 1;
		
		return (xMod < range && yMod < range);
	}
}
