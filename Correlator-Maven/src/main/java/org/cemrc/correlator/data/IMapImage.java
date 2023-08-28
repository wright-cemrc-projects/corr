package org.cemrc.correlator.data;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javafx.scene.canvas.Canvas;
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
	 * Draws an IMapImage to the canvas with transformations being done.
	 * 
	 * @param destination
	 * @param transformMat
	 * @param transparent
	 */
	public void drawImage(Canvas destination, Affine transformMat, boolean transparent);
	
	/**
	 * Draws an IMapImage to the Graphics2D as width/height into a Graphics2D
	 * 
	 * @param destination Graphics2D
	 */
	public void drawImage(Graphics2D destination, int width, int height);

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
	// public Image getImage();
	
	/**
	 * Get the width of the image
	 * @return
	 */
	public int getImageWidth();
	
	/**
	 * Get the height of the image
	 * @return
	 */
	public int getImageHeight();
	
	
	/**
	 * [DEPRECATED]
	 * 
	 * Get the ARGB value of a pixel (x, y) on the tiled image.
	 * 
	 * @param x
	 * @param y
	 * @return
	 
	public int getPixelARGB(int x, int y);
	 */

	/**
	 * Return a BufferedImage of the contents, rescaled to width and height.
	 * @param width
	 * @param height
	 * @return
	 */
	public BufferedImage getBufferedImage(int width, int height);
}
