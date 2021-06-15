package org.cemrc.correlator.data;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.transform.Affine;

/**
 * An implementation of the IMapImage
 * 
 * @author mrlarson2
 *
 */
public class JavafxMapImage implements IMapImage {
	
	// On a scale from -1 to 1.
	float default_brightness = 0.0f;
	float default_contrast = 0.0f;
	
	private BufferedImage m_srcImage;
	private Image m_image;
	
	private int m_imageWidth;
	private int m_imageHeight;
	
	public JavafxMapImage(BufferedImage image) {
		m_srcImage = image;
		adjustImage(default_brightness, default_contrast);
	}

	@Override
	public void drawImage(Canvas canvas, Affine mat, boolean transparent) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		// Save the transform state
		gc.save();
		
		if (transparent) {
			gc.setGlobalBlendMode(BlendMode.SCREEN);
		} 
	
        gc.setTransform(mat);
		
		// Set color effects
		if (m_image != null) {
			gc.drawImage(m_image,  0,  0);
		}

		// Restore transform state
		gc.restore();
	}
	
	@Override
	public void drawImage(Graphics2D destination, int newWidth, int newHeight) {
		
		destination.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		destination.drawImage(m_srcImage, 0, 0, newWidth, newHeight, 0, 0, getImageWidth(),
		    getImageHeight(), null);
	}
	
	public void adjustImage(float brightness, float contrast) {
		if (m_srcImage == null) return;
		
		// https://ie.nitk.ac.in/blog/2020/01/19/algorithms-for-adjusting-brightness-and-contrast-of-an-image/
		BufferedImage adjustedImage = deepCopy(m_srcImage);
		
		// Use the brightness/contrast values and pixel-by-pixel do the algorithm for brightness & contrast.
	    m_imageWidth = m_srcImage.getWidth();
	    m_imageHeight = m_srcImage.getHeight();
	    
	    int brightValue = (int) (brightness * 256);
	    float c = contrast * 256;
	    
	    float contrastFactor = 259.0f*(255.0f+c)/(255.0f*(259.0f-c));
	
	    for (int i = 0; i < m_imageHeight; i++) {
	      for (int j = 0; j < m_imageWidth; j++) {
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
	        
	        adjustedImage.setRGB(j, i, rgb);
	      }
	    }
	    // System.out.println("adjustedImage");
		m_image = SwingFXUtils.toFXImage(adjustedImage, null);
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
	public int getImageWidth() {
		return m_imageWidth;
	}

	@Override
	public int getImageHeight() {
		return m_imageHeight;
	}

	@Override
	public int getPixelARGB(int x, int y) {
		PixelReader pr = m_image.getPixelReader();
		return pr.getArgb(x, y);
	}
	
}
