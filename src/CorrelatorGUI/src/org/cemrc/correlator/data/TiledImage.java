package org.cemrc.correlator.data;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * TilingImage describes in memory image consisting of multiple tiles and is intented as a solution to drawing
 * extremely large dimension images onto a Canvas.
 * 
 * When asked to draw to a canvas this will iterate through each of the chunks and draw separately at correct
 * transformational offsets.
 * 
 * @author mrlarson2
 *
 */
public class TiledImage implements IMapImage {

	// Currently handling brightness/contrast with CPU operations
	// This could be very slow with these larger images.
	
	// Individual image tiles and transforms that describe their location.
	private BufferedImage[] m_sourceTiles; // Source images (no brightness/contrast applied)
	private Image[] m_imageTiles;          // For CPU-based brightness and contrast
	private Transform[] m_transforms;      // Transforms describing how tiles to be drawn in 2D.
	
	// Describe the arrangement of tiles
	private int m_rows = 0;
	private int m_columns = 0;
	
	// Describe the dimensions of the images
	private int m_tileX = 0;
	private int m_tileY = 0;
	
	// Describe the overall image dimensions (may be smaller than the extent of tiles)
	private int m_imageWidth;
	private int m_imageHeight;
	
	/**
	 * Create a ChunkImage from a larger BufferedImage. 
	 *  In this example, each of the tiles is translationally offset by tile_X, tile_Y steps.
	 * 
	 * @param image
	 * @param tile_X
	 * @param tile_Y
	 */
	public TiledImage(BufferedImage image, int tile_X, int tile_Y) {
			
		m_imageWidth = image.getWidth();
		m_imageHeight = image.getHeight();
		
		// Determine number of rows and columns required
		m_rows = (int) Math.ceil(image.getWidth() / (double) tile_X);
		m_columns = (int) Math.ceil(image.getHeight() / (double) tile_Y);
		
		if (m_rows > 0 && m_columns > 0) {
			
			int chunks = m_rows * m_columns;
			int count = 0;
			
			// The goal here is to divide
	        m_sourceTiles = new BufferedImage[chunks]; //Image array to hold image chunks
	        m_transforms = new Translate[chunks];
	        m_imageTiles = new Image[chunks];
	        for (int x = 0; x < m_rows; x++) {
	            for (int y = 0; y < m_columns; y++) {
	                //Initialize the image array with image chunks
	                m_sourceTiles[count] = new BufferedImage(tile_X, tile_Y, image.getType());
	
	                // draws the image chunk
	                Graphics2D gr = m_sourceTiles[count].createGraphics();
	                gr.drawImage(image, 0, 0, tile_X, tile_Y, tile_X * y, tile_Y * x, tile_X * y + tile_X, tile_Y * x + tile_Y, null);
	                gr.dispose();
	                
	                // Create the JavaFX Image tile
	        		m_imageTiles[count] = SwingFXUtils.toFXImage(m_sourceTiles[count], null);
	         
	                // assign a translate offset Transform for the tile.
	                int shiftX = tile_X * y;
	                int shiftY = tile_Y * x;
	                
	                Translate translate = new Translate();
	                translate.setX(shiftX);
	                translate.setY(shiftY);
	                m_transforms[count] = translate;
	                
	                count++;
	            }
	        }
		}
	}
	

	@Override
	public void drawImage(Canvas destination, Affine transformMat, boolean transparent) {
		GraphicsContext gc = destination.getGraphicsContext2D();
		
		if (m_rows > 0 && m_columns > 0) {
			
			int count = 0;
			
			for (int x = 0; x < m_rows; x++) {
				
				for (int y = 0; y < m_columns; y++) {
			
					// Save the transform state
					gc.save();
					
					if (transparent) {
						gc.setGlobalBlendMode(BlendMode.SCREEN);
					} 
					
					// This may be in the reverse order?
					Transform localTransform = m_transforms[count];
					Affine combinedMat = transformMat.clone();
					combinedMat.append(localTransform);
					
			        gc.setTransform(combinedMat);
					
			        Image tile = m_imageTiles[count];
			        
					// Set color effects
					if (tile != null) {
						gc.drawImage(tile,  0,  0);
					}
			
					// Restore transform state
					gc.restore();
					
					count++;
				}
			}
		}
	}

	@Override
	public void adjustImage(float brightness, float contrast) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int getImageWidth() {
		return m_imageWidth;
	}

	@Override
	public int getImageHeight() {
		return m_imageHeight;
	}

	/*
	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}
	*/
}
