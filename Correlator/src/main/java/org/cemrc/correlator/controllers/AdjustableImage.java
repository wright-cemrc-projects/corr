package org.cemrc.correlator.controllers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;

/**
 * JavaFX ColorAdjust causes issues when aligning + transforming images.
 * It appears that if the overlapping images exceed 4K texture sizes the GPU memory is exhausted and
 * the effect fails with no resulting texture.
 * 
 * This is a software approach to pixel-wise adjust brightness and contrast in a BufferedImage that
 * is wrapped as a JavaFX Image, and provides an alternative solution
 * 
 * @author larso
 *
 */
public class AdjustableImage {

	private BufferedImage m_srcImage;
	private BufferedImage m_adjustedImage;
	private Image m_fxImage;
	
	/**
	 * Prepare buffered images.
	 * @param image
	 */
	public AdjustableImage(BufferedImage image) {
		m_srcImage = image;
		m_adjustedImage = deepCopy(image);
		m_fxImage = SwingFXUtils.toFXImage(m_adjustedImage, null);
	}
	
	/**
	 * Return the JavaFX wrapped Image.
	 * @return
	 */
	public Image getImage() {
		return m_fxImage;
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
	
	public void adjustImage(float brightness, float contrast) {
		if (m_srcImage == null) return;
		
		// https://ie.nitk.ac.in/blog/2020/01/19/algorithms-for-adjusting-brightness-and-contrast-of-an-image/
		
		// Use the brightness/contrast values and pixel-by-pixel do the algorithm for brightness & contrast.
	    int w = m_srcImage.getWidth();
	    int h = m_srcImage.getHeight();
	    
	    int brightValue = (int) (brightness * 256);
	    float c = contrast * 256;
	    
	    float contrastFactor = 259.0f*(255.0f+c)/(255.0f*(259.0f-c));
	
	    for (int i = 0; i < h; i++) {
	      for (int j = 0; j < w; j++) {
	        int pixel = m_srcImage.getRGB(j, i);
	        
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
	        
	        m_adjustedImage.setRGB(j, i, rgb);
	      }
	    }
	    // System.out.println("adjustedImage");
		m_fxImage = SwingFXUtils.toFXImage(m_adjustedImage, null);
	}
}
