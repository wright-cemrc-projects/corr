package org.cemrc.correlator.data;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

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
	                // Initialize the image array with image chunks
	            	// https://stackoverflow.com/questions/5836128/how-do-i-make-javas-imagebuffer-to-read-a-png-file-correctly
	            	int imageType = image.getType();
	            	if (imageType == 0) imageType = 5;
	                m_sourceTiles[count] = new BufferedImage(tile_X, tile_Y, imageType);
	
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
	
	private BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private int truncate(int value) {
		if (value < 0) return 0;
		if (value > 255) return 255;
		return value;
	}
	
	@Override
	public void adjustImage(float brightness, float contrast) {
		int tiles = m_rows * m_columns;
		
		for (int tile = 0; tile < tiles; tile++) {
		
			// https://ie.nitk.ac.in/blog/2020/01/19/algorithms-for-adjusting-brightness-and-contrast-of-an-image/
			BufferedImage adjustedImage = deepCopy(m_sourceTiles[tile]);
			
			// Use the brightness/contrast values and pixel-by-pixel do the algorithm for brightness & contrast.
		    m_imageWidth = m_sourceTiles[tile].getWidth();
		    m_imageHeight = m_sourceTiles[tile].getHeight();
		    
		    int brightValue = (int) (brightness * 256);
		    float c = contrast * 256;
		    
		    float contrastFactor = 259.0f*(255.0f+c)/(255.0f*(259.0f-c));
		
		    for (int i = 0; i < m_imageHeight; i++) {
		      for (int j = 0; j < m_imageWidth; j++) {
		        int pixel = m_sourceTiles[tile].getRGB(j, i);
		        
		        int alpha = (pixel >> 24) & 0xff;
		        int red = (pixel >> 16) & 0xff;
		        int green = (pixel >> 8) & 0xff;
		        int blue = (pixel) & 0xff;
		        
		        // brightness adjust
		        red = truncate(red + brightValue);
		        green = truncate(green + brightValue);
		        blue = truncate(blue + brightValue);
		        
		        // contrast adjust
		        red = truncate( (int) ( (contrastFactor * (red - 128.0f)) + 128.0f ) );
		        green = truncate( (int) ( (contrastFactor * (green - 128.0f)) + 128.0f ) );
		        blue = truncate( (int) ( (contrastFactor * (blue - 128.0f)) + 128.0f ) );
		        
		        int rgb = (alpha << 24) + (red << 16) + (green << 8) + (blue);
		        
		        adjustedImage.setRGB(j, i, rgb);
		      }
		    }
		    // System.out.println("adjustedImage");
		    m_imageTiles[tile] = SwingFXUtils.toFXImage(adjustedImage, null);
		}
	}


	@Override
	public int getImageWidth() {
		return m_imageWidth;
	}

	@Override
	public int getImageHeight() {
		return m_imageHeight;
	}


	@Override
	public int getPixelARGB(int x, int y) {
		// TODO determine which tile has x, y
		// TODO determine read the aRGB value from the tile.
		return 0;
	}


	@Override
	public void drawImage(Graphics2D destination, int width, int height) {
		// TODO Auto-generated method stub
		
	}

}
