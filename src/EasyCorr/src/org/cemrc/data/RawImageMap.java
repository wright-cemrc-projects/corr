package org.cemrc.data;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector4;

/**
 * Navigator map from a raw image.
 * @author larso
 *
 */
@XmlRootElement(name="imageMap")
public class RawImageMap implements IMap {

	// This represents the image on the disk
	@XmlElement(name="imageLocation")
	private File m_imageLocation;
	@XmlElement(name="drawnId")
	private int m_id;
	
	@XmlElement(name="registration")
	private Registration m_registration = null;
	
	// Image name.
	private String m_name;
	
	// Required for XML reading.
	public RawImageMap() {
	}
	
	public RawImageMap(File f) {
		m_imageLocation = f;
	}
	
	@Override
	public GenericItem getAutoDoc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getImage() {
		return m_imageLocation;
	}
	
	@Override
	public File getAltImage() {
		return null;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public int getId() {
		return m_id;
	}

	@Override
	public void setRegistration(Registration t) {
		m_registration = t;
	}
	
	@Override
	public Registration getRegistration() {
		return m_registration;
	}
	
	public Vector4<Float> getMapScaleMat(boolean useRegistration) {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}
	
	public Vector2<Integer> getDimensions() {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}

	@Override
	public Vector2<Float> getRawStageXY() {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}

	@Override
	public double[][] getAffinePixelStage3x3() {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}

	@Override
	public Integer getRegis() {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}

	@Override
	public Vector2<Float> getStageFromPixel(Vector2<Float> position, boolean useAffineTransformation) {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}

	@Override
	public Vector2<Float> getPixelFromStage(Vector2<Float> position, boolean useAffineTransformation) {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}

	@Override
	public List<Vector2<Float>> getStageBounds(boolean useAffineTransformation) {
		throw new IllegalArgumentException("Not implemented for RawImageMap.");
	}
	
	@Override
	public float getStageZ() {
		return 0;
	}
}
