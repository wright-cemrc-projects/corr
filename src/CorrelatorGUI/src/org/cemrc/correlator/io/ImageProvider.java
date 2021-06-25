package org.cemrc.correlator.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.cemrc.correlator.data.IMapImage;
import org.cemrc.correlator.data.TiledImage;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Routines for providing IMapImages.
 * 
 * @author mrlarson2
 */
public class ImageProvider {

	/**
	 * Method to wrap internal MRC image reader and JAI libraries.
	 * 
	 * @param file : filename
	 * @return IMapImage that could be a tiled image.
	 */
	public static IMapImage readImage(File file) {
		IMapImage rv = null;
		
		if (file.getName().endsWith(".st") || file.getName().endsWith(".mrc")) {
			
			// TODO, this should return an IMapImage, so we can open montages as tiled data structure.
			BufferedImage image = ReadMRC.parseSerialEM(file);
			// rv = new JavafxMapImage(image);
			rv = new TiledImage(image, 2000, 2000);
			
		} else {
				
			// Method with ImagoIO (JAI core extension for TIFF)
			ImageInputStream is;
			try {
				is = ImageIO.createImageInputStream(file);  //read tiff using imageIO (JAI component)
			if (is == null || is.length() == 0) {
				Alert errorAlert = new Alert(AlertType.ERROR);
				errorAlert.setHeaderText("Input not valid");
				errorAlert.setContentText("Cannot find image at this location: " + file.getAbsolutePath());
				errorAlert.showAndWait();
			} else {
			
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
				if (iterator == null || !iterator.hasNext()) {
				    throw new IOException("Image file format not supported by ImageIO: " + file.getAbsolutePath());
				}
				ImageReader reader = (ImageReader) iterator.next();
				reader.setInput(is);
				
				int nbPages = reader.getNumImages(true);
				
				if (nbPages > 0) {
					BufferedImage bf = reader.read(0);   //1st page of tiff file
					
					// rv = new JavafxMapImage(bf);
					rv = new TiledImage(bf, 2000, 2000);
				}
			}
			
			} catch (FileNotFoundException ex) {
			        ex.printStackTrace();
			} catch (IOException ex) {
			        ex.printStackTrace();
			}
		}
		
		return rv;
	}
}
