package org.cemrc.correlator.analysis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	 * Do the steps of the CHT algorithm
	 */
	private BufferedImage preprocess(BufferedImage src) {
		if (src == null) {
			return null;
		}
		
		// 1. Convert to greyscale if needed
		BufferedImage image = getGreyscale(src);
		
		// 2. Perform Sobel edge detection transform on image
		return getSobel(image);
	}
	
	private List<Vector2<Integer>> CHT(BufferedImage image) {
		// Circle Hough Transform to find circles (x,y) center points.
		
		// Create accumulator -> A[x,y,r]
		int width = image.getWidth();
		int height = image.getHeight();
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
				for (int r = 0; r < radiusMax; r++) {
					
					int value = image.getRGB(x, y);
					if (value > 128) {
						// Is this an edge?
					
						for (int theta = 0; theta < 360; theta++) {  // the possible  theta 0 to 360 
							// get polar coordinates for center of a center with edge at this pixel x,y
							int a = x - (int) (r * Math.cos(theta * Math.PI / 180)); 
							int b = y - (int) (r * Math.sin(theta * Math.PI / 180));  
							// add a vote
							accumulator[a][b][r] +=1;
						}
					}
				}
			}
		}
		
		//  -> Save the list of the (X,Y) centers.
		List<Vector2<Integer>> rv = new ArrayList<Vector2<Integer>>();
		int minVote = 16;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Pixel x,y
				for (int r = 0; r < radiusMax; r++) {
					if (accumulator[x][y][r] > minVote) {
						rv.add(new Vector2<Integer>(x,y));
						break;
					}
				}
				
			}
		}
		return rv;
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
		BufferedImageOp edge = new ConvolveOp(new Kernel(3, 3, edgeKernel));
		
		BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY); 
		
        Graphics2D g2 = image.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.drawImage(image, edge, 0, 0);
        g2.setColor(Color.BLACK);
		g2.dispose();
		
		return image;
	}
	
}
