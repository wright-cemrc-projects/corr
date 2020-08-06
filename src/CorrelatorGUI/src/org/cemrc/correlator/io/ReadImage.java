package org.cemrc.correlator.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class ReadImage {

	/**
	 * Method to wrap internal MRC image reader and JAI libraries.
	 * @param file
	 * @return
	 */
	public static BufferedImage readImage(File file) {
		BufferedImage rv = null;
		
		if (file.getName().endsWith(".st") || file.getName().endsWith(".mrc")) {
			rv = ReadMRC.parseSerialEM(file);
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
					rv = bf;
					/*
					WritableImage wr = null;
					if (bf != null) {
					    wr= SwingFXUtils.toFXImage(bf, null);   //convert bufferedImage (awt) into Writable Image(fx)
					}
					rv = wr;
					*/
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
