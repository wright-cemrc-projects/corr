package org.cemrc.data;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.math4.legacy.linear.MatrixUtils;
import org.apache.commons.math4.legacy.linear.QRDecomposition;
import org.apache.commons.math4.legacy.linear.RealMatrix;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.autodoc.Vector4;
import org.cemrc.math.MatrixMath;

/**
 * Core functionality for IMap implementations.
 * @author larso
 *
 */
public class BasicMap implements IMap {
	
	@XmlElement(name="rawStageXY")
	private Vector2<Float> m_rawStageXY = new Vector2<Float>();
	
	@XmlElement(name="mapScaleMat")
	private double[][] m_mapScaleMat = new double[2][2];
	
	@XmlElement(name="dimensions")
	private Vector2<Integer> m_dimensions = new Vector2<Integer>();
	
	@XmlElement(name="registration")
	private Registration m_registration = null;
	
	@XmlElement(name="image")
	private File m_image = null;
	
	@XmlElement(name="sourceFile")
	private File m_sourceFile = null;
	
	@XmlElement(name="stageZ")
	private float m_stageZ;
	
	@Override
	public float getStageZ() {
		return m_stageZ;
	}
	
	public void setStageZ(float z) {
		m_stageZ = z;
	}
	
	public File getSourceFile() {
		return m_sourceFile;
	}
	
	public void setImage(File f) {
		m_image = f;
	}
	
	public void setSourceFile(File f) {
		m_sourceFile = f;
	}
	
	@Override
	public File getImage() {
		return m_image;
	}
	
	@Override
	public File getAltImage() {
		if (m_image != null && m_sourceFile != null) {
			String name = m_image.getName();
			String parent = m_sourceFile.getParent();
			
			return Paths.get(parent).resolve(name).toFile();
		}
		
		return null;
	}

	/**
	 * Get the MapScaleMat matrix 2x2
	 * @return
	 */
	public double[][] getMapScaleMat() {
		return m_mapScaleMat;
	}
	
	/**
	 * Set the MapScaleMat matrix 2x2
	 * @param mat
	 */
	public void setMapScaleMat(double[][] mat) {
		m_mapScaleMat = mat;
	}
	
	/**
	 * Set the two value RawStageXY
	 * @param rawStage
	 */
	public void setRawStageXY(Vector2<Float> rawStage) {
		m_rawStageXY = rawStage;
	}
	
	/**
	 * Get the 2 value RawStageXY
	 * @return
	 */
	public Vector2<Float> getRawStageXY() {
		return m_rawStageXY;
	}
	
	/**
	 * Get the registration
	 * @return
	 */
	public Registration getRegistration() {
		return m_registration;
	}
	
	/**
	 * Set the registration
	 * @param r
	 */
	public void setRegistration(Registration r) {
		m_registration = r;
	}
	
	/**
	 * Set the image dimensions
	 * @param dimensions
	 */
	public void setDimensions(Vector2<Integer> dimensions) {
		m_dimensions = dimensions;
	}
	
	/**
	 * Get the image dimensions.
	 */
	@Override
	public Vector2<Integer> getDimensions() {
		return m_dimensions;
	}
	
	/**
	 * MapScaleMat is the matrix describing conversion from StagePosition -> PixelPosition
	 * It's inverse can be used to make the inverse transformation.
	 * 
	 * It is a 2x2 matrix and the separate translation coordinates RawStageXY are also needed.
	 */
	public Vector4<Float> getMapScaleMat(boolean useRegistration) {
		double[][] msmValues = getMapScaleMat();
		
		if (useRegistration && getRegistration() != null) {
			
			double [][] ResultMatrix = getAffinePixelStage3x3();
			
			Vector4<Float> updateValues = new Vector4<Float>();
			updateValues.x = (float) ResultMatrix[0][0];
			updateValues.y = (float) ResultMatrix[0][1];
			updateValues.z = (float) ResultMatrix[1][0];
			updateValues.w = (float) ResultMatrix[1][1];
			
			return updateValues;
		} else {
		
			return new Vector4<Float>((float)msmValues[0][0], (float)msmValues[0][1], (float)msmValues[1][0], (float)msmValues[1][1]);
		}
	}
	
	/**
	 * Get the 3x3 affine transformation matrix including translation component.
	 * @return
	 */
	public double[][] getAffinePixelStage3x3() {
		double[][] msmValues = getMapScaleMat();
		Registration registration = getRegistration();
		
		float halfWidth = getDimensions().x / 2.0f;
		float halfHeight = getDimensions().y / 2.0f;
		
		double[][] MapScaleMat3x3_array = 
				{ {msmValues[0][0], msmValues[0][1], halfWidth}, 
				{msmValues[1][0], msmValues[1][1], halfHeight},
				{0f, 0f, 1f} };
			
		RealMatrix MapScaleMat3x3 = MatrixUtils.createRealMatrix(MapScaleMat3x3_array);
		RealMatrix AffineMatrix = MatrixUtils.createRealMatrix(getRegistration().getStageMatrix());
		RealMatrix invAffineMatrix = MatrixUtils.inverse(AffineMatrix);
		
		RealMatrix ResultMatrix = MapScaleMat3x3.multiply(invAffineMatrix);
		return ResultMatrix.getData();
	}

	@Override
	public GenericItem getAutoDoc() {
		throw new IllegalArgumentException("Not implemented for BasicMap.");
	}

	@Override
	public String getName() {
		throw new IllegalArgumentException("Not implemented for BasicMap.");
	}

	@Override
	public int getId() {
		throw new IllegalArgumentException("Not implemented for BasicMap.");
	}

	@Override
	public Integer getRegis() {
		return 1;
	}

	@Override
	public Vector2<Float> getStageFromPixel(Vector2<Float> pixelPosition, boolean useAffineTransformation) {
		
		// Translation for origin of image.
		float halfWidth = ((float) getDimensions().x) / 2.0f;
		float halfHeight = ((float) getDimensions().y) / 2.0f;
		
		// Get the original MapScaleMat (goes from Stage Positions -> Pixel Positions).
		Vector4<Float> scaleMatrix2x2 = getMapScaleMat(false);
		
		double [][] rawMsm = { {scaleMatrix2x2.x, scaleMatrix2x2.y, halfWidth }, {scaleMatrix2x2.z, scaleMatrix2x2.w, halfHeight}, {0, 0, 1} };
		RealMatrix mapScaleMat = MatrixUtils.createRealMatrix(rawMsm);
		
		// affineMat is a 3x3 transformation in Stage coordinate space, between P target -> P' registration positions.
		if (useAffineTransformation && getRegistration() != null) {
			RealMatrix affineMat = MatrixUtils.createRealMatrix(getRegistration().getStageMatrix());
			RealMatrix invAffineMat = MatrixUtils.inverse(affineMat);
			mapScaleMat = mapScaleMat.multiply(invAffineMat);
		}
		
		// invMapScaleMat (goes from Pixel Positions -> Stage Positions).
		RealMatrix invMapScaleMat = new QRDecomposition(mapScaleMat).getSolver().getInverse();
		
		// Add z = 1.0f, so translation math is applied.
		Vector3<Float> pointPixel = new Vector3<Float>(pixelPosition.x, pixelPosition.y, 1.0f);
		Vector3<Float> pointStage = MatrixMath.multiply(invMapScaleMat, pointPixel);
		Vector2<Float> rv = new Vector2<Float>(pointStage.x, pointStage.y);
		
		return rv;
	}

	@Override
	public Vector2<Float> getPixelFromStage(Vector2<Float> stagePosition, boolean useAffineTransformation) {
		
		// Translation for origin of image.
		float halfWidth = ((float) getDimensions().x) / 2.0f;
		float halfHeight = ((float) getDimensions().y) / 2.0f;
		
		// Get the original MapScaleMat (goes from Stage Positions -> Pixel Positions).
		Vector4<Float> scaleMatrix2x2 = getMapScaleMat(false);
		
		double [][] rawMsm = { {scaleMatrix2x2.x, scaleMatrix2x2.y, halfWidth }, {scaleMatrix2x2.z, scaleMatrix2x2.w, halfHeight}, {0, 0, 1} };
		RealMatrix mapScaleMat = MatrixUtils.createRealMatrix(rawMsm);
		
		// affineMat is a 3x3 transformation in Stage coordinate space, between P target -> P' registration positions.
		if (useAffineTransformation && getRegistration() != null) {
			RealMatrix affineMat = MatrixUtils.createRealMatrix(getRegistration().getStageMatrix());
			RealMatrix invAffineMat = MatrixUtils.inverse(affineMat);
			mapScaleMat = mapScaleMat.multiply(invAffineMat);
		}
		
		// Add z = 1.0f, so translation math is applied.
		Vector3<Float> pointPixel = new Vector3<Float>(stagePosition.x, stagePosition.y, 1.0f);
		Vector3<Float> pointStage = MatrixMath.multiply(mapScaleMat, pointPixel);
		Vector2<Float> rv = new Vector2<Float>(pointStage.x, pointStage.y);
		
		return rv;
	}

	@Override
	public List<Vector2<Float>> getStageBounds(boolean useAffineTransformation) {
		List<Vector2<Float>> pixelBounds = new ArrayList<Vector2<Float>>();
		
		// 0,0 = -,-
		pixelBounds.add(new Vector2<Float>(0f,0f));
		pixelBounds.add(new Vector2<Float>((float) getDimensions().x, 0f));
		pixelBounds.add(new Vector2<Float>((float) getDimensions().x, (float) getDimensions().y));
		pixelBounds.add(new Vector2<Float>(0f, (float) getDimensions().y));
		pixelBounds.add(new Vector2<Float>(0f,0f));
		
		List<Vector2<Float>> stageBounds = new ArrayList<Vector2<Float>>();
		for (Vector2<Float> pt : pixelBounds) {
			stageBounds.add(getStageFromPixel(pt, useAffineTransformation));
		}

		return stageBounds;
	}
}
