package org.cemrc.data;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.cemrc.autodoc.Vector2;
import org.cemrc.math.AffineTransformation;

public class Registration {

	@XmlElement(name="stageTransform3x3")
	private double [][] m_stageMatrix;
	
	@XmlElement(name="pixeltTransform3x3")
	private double [][] m_pixelMatrix;
	
	@XmlElement(name="note")
	private String m_note;
	
	@XmlElement(name="mapId")
	private int m_registerMapId;
	
	@XmlElement(name="regis")
	private int m_regis;
	
	@XmlElement(name="stageZ")
	private float m_stageZ;
	
	public Registration() {
	}
	
	public void setStageMatrix(double[][] mat) {
		m_stageMatrix = mat;
	}
	
	public double[][] getStageMatrix() {
		return m_stageMatrix;
	}
	
	public void setPixelMatrix(double[][] mat) {
		m_pixelMatrix = mat;
	}
	
	public double[][] getPixelMatrix() {
		return m_pixelMatrix;
	}
	
	public void setNote(String note) {
		m_note = note;
	}
	
	public String getNote() {
		return m_note;
	}
	
	public void setRegisterMapId(int id) {
		m_registerMapId = id;
	}
	
	public int getRegisterMapId() {
		return m_registerMapId;
	}
	
	public int getRegis() {
		return m_regis;
	}
	
	public void setRegis(int r) {
		m_regis = r;
	}
	
	public String getPrettyString() {
		if (m_stageMatrix != null) {
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < 3; i++) {
				
				for (int j = 0; j < 3; j++) {
					sb.append(String.format("%.2f",  m_stageMatrix[i][j]));
					sb.append(" ");
				}
				
				sb.append("\n");
			}
			
			return sb.toString();
		} else return "";
	}
	
	
	public static Registration generate(IPositionDataset target, IPositionDataset reference) {
		
		// When creating a new registration, need to use original MapScaleMat
		boolean useRegistration = false;
		
		// Stage Matrix
		List<Vector2<Float>> targetStagePts = target.getStagePositions(useRegistration);
		List<Vector2<Float>> referenceStagePts = reference.getStagePositions(useRegistration);
		
		// calculate the AffinineTransformation.calculateAffineTransform
		AffineTransformation mat = AffineTransformation.generate(targetStagePts, referenceStagePts);
		
		// Pixel Matrix
		List<Vector2<Float>> targetPixelPts = target.getPixelPositions();
		List<Vector2<Float>> referencePixelPts = reference.getPixelPositions();
		
		// calculate the AffinineTransformation.calculateAffineTransform
		AffineTransformation pixelMat = AffineTransformation.generate(targetPixelPts, referencePixelPts);
		
		Registration rv = new Registration();
		rv.setStageMatrix(mat.getMatrix());
		rv.setPixelMatrix(pixelMat.getMatrix());
		rv.setNote("src: " + reference.getMapId());
		rv.setRegisterMapId(reference.getMapId());
		rv.setRegis(reference.getMap().getRegis());
		
		// Record a stageZ
		if (reference.getMap() != null) {
			rv.setStageZ(reference.getMap().getStageZ());
		}
		
		return rv;
	}
	
	public float getStageZ() {
		return m_stageZ;
	}
	
	public void setStageZ(float z) {
		m_stageZ = z;
	}
}
