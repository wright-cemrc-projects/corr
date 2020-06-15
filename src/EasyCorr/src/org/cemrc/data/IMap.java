package org.cemrc.data;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector4;
import org.cemrc.math.AffineTransformation;

/**
 * Interface for 
 * @author larso
 *
 */
public interface IMap {
	
	public static int UNASSIGNED_MAP = -1;

	/**
	 * Convert back to autodoc format for serialization
	 * @return
	 */
	public GenericItem getAutoDoc();
	
	/**
	 * Get filename of the image
	 * @return image file
	 */
	public File getImage();
	
	/**
	 * Alternative or fallback image location to workaround .nav limitations
	 * @return
	 */
	public File getAltImage();
	
	/**
	 * Get a map's visible name
	 * @return
	 */
	public String getName();
	
	/**
	 * Get a unique identifier.
	 * @return
	 */
	public int getId();
	
	/**
	 * Set a transform that can be applied for registered maps.
	 * @param t
	 */
	public void setRegistration(Registration t);
	
	/**
	 * @return the Registration information if any.
	 */
	public Registration getRegistration();
	
	/**
	 * MapScaleMat is the matrix describing conversion from StagePosition -> PixelPosition
	 * It's inverse can be used to make the inverse transformation.
	 * 
	 * It is a 2x2 matrix and the separate translation coordinates RawStageXY are also needed.
	 */
	public Vector4<Float> getMapScaleMat(boolean useRegistration);
	
	/**
	 * Get the pixel to stage, including affine matrix math.
	 * @return
	 */
	public double[][] getAffinePixelStage3x3();
	
	/**
	 * Get the RawstageXY positioning on the map.
	 * @return
	 */
	public Vector2<Float> getRawStageXY();
	
	/**
	 * Return the width, height in pixels.
	 * @return
	 */
	public Vector2<Integer> getDimensions();
	
	/**
	 * Return the regis value, if there is one.
	 * @return
	 */
	public Integer getRegis();
	
	/**
	 * Convert a pixel position to a stage position.
	 * @param position
	 * @param useAffineTransformation
	 * @return
	 */
	public Vector2<Float> getStageFromPixel(Vector2<Float> position, boolean useAffineTransformation);
	
	/**
	 * Convert a stage position to a pixel position.
	 * @param position
	 * @param useAffineTransformation
	 * @return
	 */
	public Vector2<Float> getPixelFromStage(Vector2<Float> position, boolean useAffineTransformation);
	
	/**
	 * Gets a 5 point polygon describing StageXY bounds.
	 * @param useAffineTransformation
	 * @return
	 */
	public List<Vector2<Float>> getStageBounds(boolean useAffineTransformation);
}
