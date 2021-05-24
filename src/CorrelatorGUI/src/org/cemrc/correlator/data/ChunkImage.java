package org.cemrc.correlator.data;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * ChunkImage describes in memory image consisting of multiple tiles and is intented as a solution to drawing
 * extremely large dimension images onto a Canvas.
 * 
 * When asked to draw to a canvas this will iterate through each of the chunks and draw separately at correct
 * transformational offsets.
 * 
 * @author mrlarson2
 *
 */
public class ChunkImage {

	// Currently handling brightness/contrast with CPU operations
	// This could be very slow with these larger images.
	
	// Individual image chunks.
	private BufferedImage[] m_chunks;
	
	// Describe the arrangment of tiles
	private int m_rows = 0;
	private int m_columns = 0;
	
	// Describe the dimensions of the images
	private int m_tileX = 0;
	private int m_tileY = 0;
	
	// TODO: also, can we describe 
	
	/**
	 * Create a ChunkImage from a larger BufferedImage. 
	 *  In this example, each of the tiles is translationally offset by tile_X, tile_Y steps.
	 * 
	 * @param image
	 * @param tile_X
	 * @param tile_Y
	 */
	public ChunkImage(BufferedImage image, int tile_X, int tile_Y) {
		
		// Start values for the height and width for the source image.
		int srcHeight = image.getHeight();
		int srcWidth = image.getWidth();
		
		
		// TODO: determine the number of rows and columns of chunks required.
		
		/*
		
		// The goal here is to divide
        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                //Initialize the image array with image chunks
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                // draws the image chunk
                Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }
        
        */
	}
	
	/**
	 * 
	 * 
	 * @param stack : a stack containing layers of images.
	 */
	public ChunkImage(File stack) {
		// TODO: create a chunk image from a stack with layers for each tile.
		// SerialEM can provide .st images with a mosaic of image tiles
		
		// TODO: think about how 
	}
	
}
