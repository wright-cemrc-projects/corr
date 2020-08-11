package org.cemrc.correlator.analysis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.Vector2;

/**
 * This class will represent a hole-finding task
 * Input: a BufferedImage and parameters (radius range, cutoff)
 * Output: a list of (x,y) pixel positions of circles.
 * 
 * @author larso
 *
 */
public class CircleHoughTransformTask {

	private BufferedImage m_src;
	private BufferedImage m_greyScale;
	private float m_scale = 1.0f;
	
	public CircleHoughTransformTask(BufferedImage src) {
		m_src = src;
	}
	
	/**
	 * Run the CHT to find the circles in the image.
	 * @return
	 */
	public List<Vector2<Integer>> findCircles() {
		if (m_greyScale == null) {
			m_greyScale = preprocess(m_src);
		}
		
		return CHT(m_greyScale);
	}
	
	/**
	 * Get the preprocessed image for display.
	 * @return
	 */
	public BufferedImage getProcessed() {
		if (m_greyScale == null) {
			m_greyScale = preprocess(m_src);
		}
		
		return m_greyScale;
	}
	
	/**
	 * Do the steps of the CHT algorithm
	 */
	private BufferedImage preprocess(BufferedImage src) {
		if (src == null) {
			return null;
		}
		
		m_scale = 512.0f / src.getWidth();
		
		// 1. Convert to scale and greyscale
		// BufferedImage image = getGreyscale(src);
		BufferedImage image = getScaled(src, Math.round(m_scale * src.getWidth()), Math.round(m_scale * src.getHeight()));
		
		// 2. Blur the image
		image = getBlurred(image);
		
		// 3. Binarize image;
		binarize(image, 200);
		
		return image;
		
		// 4. Perform Sobel edge detection transform on image
		// return getSobel(image);
	}
	
	private List<Vector2<Integer>> CHT(BufferedImage image) {
		// Circle Hough Transform to find circles (x,y) center points.
		
		// Create accumulator -> A[x,y,r]
		int width = image.getWidth();
		int height = image.getHeight();
		
		int radiusMin = 6;
		int radiusMax = 50;
		
		int accumulator [][][] = new int[width][height][radiusMax]; 
		
		// Incoming image should already be blurred, grayscaled, and edge detected.
		// Voting algorithm will produce peaks in the transformed space representing circles.
		// Filter through the accumulator to find the best circle positions.
		
		// CHT Algorithm:
		/**
		 * For each A[a,b,r] = 0;
		 *  Process the filtering algorithm on image Gaussian Blurring, convert the image to grayscale ( grayScaling), make Canny operator, The Canny operator gives the edges on image.
		 *  Vote the all possible circles in accumulator.
		 *  The local maximum voted circles of Accumulator A gives the circle Hough space.
		 * The maximum voted circle of Accumulator gives the circle.
		 */
		
		// Clear the array.
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				for (int k = 0; k < radiusMax; k++)
				{
					accumulator[i][j][k] = 0;
				}
			}
		}
		
		// Accumulate the votes.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Pixel x,y
				for (int r = radiusMin; r < radiusMax; r++) {
					
					int value = new Color( image.getRGB(x, y)).getRed();
					if (value > 128) {
						// Is this an edge?
						System.out.println("[" + x + ", " + y + "]");
					
						for (int theta = 0; theta < 360; theta += 10) {  // the possible  theta 0 to 360 
							// get polar coordinates for center of a center with edge at this pixel x,y
							int a = x - (int) (r * Math.cos(theta * Math.PI / 180)); 
							int b = y - (int) (r * Math.sin(theta * Math.PI / 180));  
							
							if (a >= 0 && a < width && b >= 0 && b < height ) {
								// add a vote
								accumulator[a][b][r] +=1;
							}
						}
					}
				}
			}
		}
		
		float scale_x = m_src.getWidth() / width;
		float scale_y = m_src.getHeight() / height;
		
		//  -> Save the list of the (X,Y) centers.
		List<Vector2<Integer>> rv = new ArrayList<Vector2<Integer>>();
		int minVote = 16;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Pixel x,y
				for (int r = radiusMin; r < radiusMax; r++) {
					int value = accumulator[x][y][r];
					if (value > minVote) {
						rv.add(new Vector2<Integer>(Math.round(x * scale_x), Math.round(y * scale_y)));
						break;
					}
				}
				
			}
		}
		return rv;
	}
	
	private BufferedImage getScaled(BufferedImage src, int newWidth, int newHeight) {
		BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, newWidth, newHeight, 0, 0, src.getWidth(),
		    src.getHeight(), null);
		g.dispose();
		
		return resized;
	}
	
	private BufferedImage getBlurred(BufferedImage src) {
		float frac = 1.0f / 9.0f;
		float[] blurKernel = {
				frac, frac, frac,
				frac, frac, frac,
				frac, frac, frac
		};
		BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
		
		BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY); 
		
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(src, 0, 0, src.getWidth(), src.getHeight(), null);
		g2.dispose();
	
		image = blur.filter(image, null);
		
		return image;
	}
	
	/**
	 * Get a greyscale variant of the image.
	 * @param src
	 * @return a grey image
	 */
	private BufferedImage getGreyscale(BufferedImage src) {
		BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY);  
			Graphics g = image.getGraphics();  
			g.drawImage(src, 0, 0, null);  
			g.dispose();
		return image;
	}
	
	/**
	 * Simple binarization function for image.
	 * @param image
	 * @param cutoff
	 */
	private void binarize(BufferedImage image, int cutoff) {
		for (int x = 0; x < image.getWidth(); x++ ) {
			for (int y = 0; y < image.getHeight(); y++ ) {
				int grayLevel = image.getRGB(x, y) & 0xFF;;
				
				if (grayLevel > cutoff) {
					grayLevel = 0xFF;
				} else {
					grayLevel = 0;
				}
			
		        int gray = (0xFF << 24) + (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
				
				image.setRGB(x, y, gray);
			}
		}
	}
	
	/**
	 * Basic Edge Detection algorithm.
	 * @param image
	 * @return edge detection image
	 */
	private BufferedImage getSobel(BufferedImage src) {
		float[] edgeKernel = {
				0.0f, -1.0f, 0.0f,
				-1.0f, 4.0f, -1.0f,
				0.0f, -1.0f, 0.0f
				};
		
		float[] sobelKernel = {
			     -1.0f, -1.0f, -1.0f,
			     -1.0f, 8.0f, -1.0f,
			     -1.0f, -1.0f, -1.0f
			 };
		
		BufferedImageOp edge = new ConvolveOp(new Kernel(3, 3, sobelKernel));
		
		BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY); 
		
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(src, 0, 0, src.getWidth(), src.getHeight(), null);
		g2.dispose();
	
		image = edge.filter(image, null);
		
		return image;
	}
	
}
