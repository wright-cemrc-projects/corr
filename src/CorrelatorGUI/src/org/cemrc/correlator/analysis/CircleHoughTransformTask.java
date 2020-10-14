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

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

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
	private BufferedImage m_sobel;
	
	private final float FIXED_WIDTH = 2048.0f;
	private float m_scale = 1.0f;
	private int m_binarization = 240;
	
	private int m_cutoffLow = 0;
	private int m_cutoffHigh = 256;
	
	private String m_edgeFilter = "Laplacian_1";
	
	private ProgressBar m_progress;
	
	/**
	 * Describes a detected circle cluster
	 * @author mrlarson2
	 *
	 */
	public static class ClusterMinima {
		public Vector2<Float> center;
		public int radius;
		public int score;
		
		public ClusterMinima(Vector2<Float> center, int radius, int score) {
			this.radius = radius;
			this.score = score;
			this.center = center;
		}
		
		/**
		 * Compare two circle centers and their radius. If they are overlapping, report true.
		 * @param x
		 * @param y
		 * @param radius
		 * @return
		 */
		public boolean isOverlap(int x, int y, int radius) {
			float dx = this.center.x - x;
			float dy = this.center.y - y;
			int dr = this.radius + radius;
			
			return dx * dx + dy * dy <= dr*dr;
		}
	}

	public void setProgressBar(ProgressBar bar) {
		m_progress = bar;
	}
	
	private void updateProgress(float value) {
		if (m_progress != null) {
			Platform.runLater(() -> m_progress.setProgress(value) );
		}
	}
	
	public CircleHoughTransformTask(BufferedImage src) {
		m_src = src;
	}
	
	public void setEdgeFilter(String value) {
		m_edgeFilter = value;
	}
	
	/**
	 * Run the CHT to find the circles in the image.
	 * @return
	 */
	public List<ClusterMinima> findCircles() {
		if (m_sobel == null) {
			updateImages();
		}
		
		return CHT(m_sobel);
	}
	
	/**
	 * Get the preprocessed image for display.
	 * @return
	 */
	public BufferedImage getProcessed(boolean force) {
		if (m_greyScale == null || force) {
			updateImages();
		}	
		return m_sobel;
	}
	
	public BufferedImage getGreyscale() {
		return m_greyScale;
	}
	
	public BufferedImage getSobel() {
		return m_sobel;
	}
	
	public void setBinarizationCutoff(int level) {
		m_binarization = level;
		updateImages();
	}
	
	public void setLowCutoff(int level) {
		m_cutoffLow = level;
		updateImages();
	}
	
	public void setHighCutoff(int level) {
		m_cutoffHigh = level;
		updateImages();
	}
	
	private void updateImages() {
		m_greyScale = preprocess(m_src);
		m_sobel = preBinarize(m_greyScale);
	}
	
	/**
	 * Do the steps of the CHT algorithm
	 */
	private BufferedImage preprocess(BufferedImage src) {
		if (src == null) {
			return null;
		}
		
		m_scale = FIXED_WIDTH / src.getWidth();
		
		// 1. Convert to scale and greyscale
		// BufferedImage image = getGreyscale(src);
		BufferedImage image = getScaled(src, Math.round(m_scale * src.getWidth()), Math.round(m_scale * src.getHeight()));
		
		// 2. Blur the image
		BufferedImage prebinarization = getBlurred(image);
		
		// 3. Rescale the image histogram (This could be drawn on the canvas also)
		//  Should be affected by histogram controls.
		return getRescaled(prebinarization);	
	}
	
	private BufferedImage preBinarize(BufferedImage rescaled) {
		
		// 3. Binarize image;
		BufferedImage binarized = binarize(rescaled, m_binarization);
		
		// 4. Perform Sobel edge detection transform on image
		return getSobel(binarized);
	}
	
	private List<ClusterMinima> CHT(BufferedImage image) {
		// Circle Hough Transform to find circles (x,y) center points.
		
		// Create accumulator -> A[x,y,r]
		int width = image.getWidth();
		int height = image.getHeight();
		
		int radiusMin = 8;
		int radiusMax = 20;
		
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
		
		float progress = 0f;
		updateProgress(progress);
		
		// Accumulate the votes.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Pixel x,y
				for (int r = radiusMin; r < radiusMax; r++) {
					
					int value = new Color( image.getRGB(x, y)).getRed();
					if (value > 128) {
					
						for (int theta = 0; theta < 360; theta += 10) {  // the possible  theta 0 to 360 
							// get polar coordinates for center of a center with edge at this pixel x,y
							int a = x - (int) Math.round(r * Math.cos(theta * Math.PI / 180)); 
							int b = y - (int) Math.round(r * Math.sin(theta * Math.PI / 180));  
							
							if (a >= 0 && a < width && b >= 0 && b < height ) {
								// add a vote
								accumulator[a][b][r] +=1;
							}
						}
					}
				}
			}
			progress += (1.0f / width);
			updateProgress(progress);
		}
		
		float scale = (float) m_src.getWidth() / FIXED_WIDTH;
		
		//  -> Save the list of the (X,Y) centers.
		List<ClusterMinima> maximum = findLocalMaximum(accumulator, width, height, radiusMax);
		
		// Rescale to match original image dimensions.
		List<ClusterMinima> rv = new ArrayList<ClusterMinima>();
		for (ClusterMinima m : maximum) {
			
			// Imprecision in hole center due to use of Int values.
			Vector2<Float> center = new Vector2<Float>();
			center.x = m.center.x * scale;
			center.y = m.center.y * scale;
			int radius = Math.round(m.radius * scale);
			
			int score = m.score;
			ClusterMinima scaled = new ClusterMinima(center, radius, score);
			rv.add(scaled);
		}
		return rv;
	}
	
	private List<ClusterMinima> findLocalMaximum(int [][][] values, int width, int height, int radiusMax) {
		
		// First we fill these in.
		int [][] highestScores = new int[width][height];
		int [][] selectedRadius = new int[width][height];
		
		// Find the biggest scoring circle in each x, y.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int max = 0;
				for (int r = 0; r < radiusMax; r++) {
					if (values[x][y][r] >= max) {
						highestScores[x][y] = values[x][y][r];
						selectedRadius[x][y] = r;
					}
				}
			}
		}
		
		List<ClusterMinima> rv = new ArrayList<ClusterMinima>();
		
		// Add any clusters that are above the cutoff.
		int cutoff = 10;
		
		// We will find the local minima via clustering
		List<ClusterMinima> existingClusters = new ArrayList<ClusterMinima>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int score = highestScores[x][y];
				int radius = selectedRadius[x][y];
				
				if (score < cutoff) continue;
				
				// Search for a cluster overlapping this spot.
				ClusterMinima currentMinima = null;
				
				for (ClusterMinima m : existingClusters) {
					if (m.isOverlap(x, y, radius)) {
						currentMinima = m;
						break;
					}
				}
				
				if (currentMinima != null) {
					if (currentMinima.score < score) {
						// Update the circle.
						currentMinima.score = score;
						currentMinima.radius = radius;
						currentMinima.center.x = (float) x;
						currentMinima.center.y = (float) y;
					}
				} else {
					existingClusters.add(new ClusterMinima(new Vector2<Float>((float) x,  (float) y), radius, score));
				}
			}
		}
		
		// Add any clusters that are above the cutoff.
		for (ClusterMinima m : existingClusters) {
			rv.add(m);
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
		float frac = 1.0f / 15.0f;
		float[] blurKernel = {
				frac, frac, frac, frac, frac,
				frac, frac, frac, frac, frac,
				frac, frac, frac, frac, frac,
				frac, frac, frac, frac, frac,
				frac, frac, frac, frac, frac
		};
		BufferedImageOp blur = new ConvolveOp(new Kernel(5, 5, blurKernel));
		
		BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY); 
		
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(src, 0, 0, src.getWidth(), src.getHeight(), null);
		g2.dispose();
	
		image = blur.filter(image, null);
		
		return image;
	}
	
	private BufferedImage getRescaled(BufferedImage image) {
		BufferedImage rv = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = rv.getGraphics();  
		g.drawImage(image, 0, 0, null);  
		g.dispose();
		
		for (int x = 0; x < rv.getWidth(); x++ ) {
			for (int y = 0; y < rv.getHeight(); y++ ) {
				int grayLevel = rv.getRGB(x, y) & 0xFF;;
				
				if (grayLevel > m_cutoffHigh) {
					grayLevel = m_cutoffHigh;
				} else if (grayLevel < m_cutoffLow) {
					grayLevel = m_cutoffLow;
				}
				
				// Scale the image
				grayLevel = (int) Math.round( 256.0 / (double) (m_cutoffHigh - m_cutoffLow) * (double) (grayLevel + m_cutoffLow) );
			
		        int gray = (0xFF << 24) + (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
				
				rv.setRGB(x, y, gray);
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
	 * Simple binarization function for image.
	 * @param image
	 * @param cutoff
	 */
	private BufferedImage binarize(BufferedImage image, int cutoff) {
		BufferedImage rv = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = rv.getGraphics();  
		g.drawImage(image, 0, 0, null);  
		g.dispose();
		
		for (int x = 0; x < rv.getWidth(); x++ ) {
			for (int y = 0; y < rv.getHeight(); y++ ) {
				int grayLevel = rv.getRGB(x, y) & 0xFF;;
				
				if (grayLevel > cutoff) {
					grayLevel = 0xFF;
				} else {
					grayLevel = 0;
				}
			
		        int gray = (0xFF << 24) + (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
				
				rv.setRGB(x, y, gray);
			}
		}
		return rv;
	}
	
	/**
	 * Basic Edge Detection algorithm.
	 * @param image
	 * @return edge detection image
	 */
	private BufferedImage getSobel(BufferedImage src) {
		float[] Laplacian_1 = {
				0.0f, -1.0f, 0.0f,
				-1.0f, 4.0f, -1.0f,
				0.0f, -1.0f, 0.0f
				};
		
		float[] Laplacian_2 = {
			     -1.0f, -1.0f, -1.0f,
			     -1.0f, 8.0f, -1.0f,
			     -1.0f, -1.0f, -1.0f
			 };
		
		float[] Laplacian_3 = {
				-1.0f, 2.0f, -1.0f,
				2.0f, -4.0f, 2.0f,
				-1.0f, 2.0f, -1.0f
				};
		
		float[] filter;
		
		switch (m_edgeFilter) {
			case "Laplacian_1" : filter = Laplacian_1; break;
			case "Laplacian_2" : filter = Laplacian_2; break;
			case "Laplacian_3" : filter = Laplacian_3; break;
			default:
				filter = Laplacian_2;
				break;
		}
		
		BufferedImageOp edge = new ConvolveOp(new Kernel(3, 3, filter));
		
		BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY); 
		
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(src, 0, 0, src.getWidth(), src.getHeight(), null);
		g2.dispose();
	
		image = edge.filter(image, null);
		
		return image;
	}
	
}
