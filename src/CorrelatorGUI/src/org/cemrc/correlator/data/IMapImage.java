package org.cemrc.correlator.data;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

/**
 * An interface that abstracts a class that can be drawn on a canvas.
 * To be implemented by ChunkImage, a regular MapImage (BufferedImage), etc.
 * 
 * @author mrlarson2
 *
 */
public interface IMapImage {
	
	/**
	 * Draws an IMapImage to the canvas, with transformations being done.
	 * 
	 * @param destination
	 * @param transformMat
	 * @param transparent
	 */
	public void drawImage(Canvas destination, Affine transformMat, boolean transparent);

	/**
	 * Adjust the image brightness and contrast.
	 * 
	 * @param brightness
	 * @param contrast
	 */
	public void adjustImage(float brightness, float contrast);
	
	/**
	 * Get the JavaFX image
	 * @return
	 */
	public Image getImage();
}
