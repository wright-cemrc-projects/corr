package org.cemrc.easycorr.actions;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.cemrc.autodoc.AutodocParser;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.easycorr.EasyCorrConfig;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ActionImportImageMap {
	
	private EasyCorrDocument m_document;
	
	public ActionImportImageMap(EasyCorrDocument doc) {
		m_document = doc;
	}
	
	/**
	 * Creates a file open dialog to find an autodoc.
	 * Merge in the data if user selected an autodoc.
	 */
	public void doAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open an image file (.tif, .png)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.tif, *.png)", "*.tif, *.png");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
        	openFile(file);
        }
	}
	
	/**
	 * Read image file to generate Autodoc GenericItem wrapper.
	 * @param file
	 */
	private void openFile(File file) {
		
		List<GenericItem> docItems = new ArrayList<GenericItem>();
		docItems.add(mapItemFromImage(file));
		m_document.getData().mergeAutodoc(docItems, file);
		
	}
	
	private GenericItem mapItemFromImage(File file) {
		GenericItem mapItem = new GenericItem();
		
		mapItem.addNavigatorField(NavigatorKey.Type, 2);
		
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
					WritableImage wr = null;
					if (bf != null) {
					    wr= SwingFXUtils.toFXImage(bf, null);   //convert bufferedImage (awt) into Writable Image(fx)
					}
					
					Double width = wr.getWidth();
					Double height = wr.getHeight();
					
					String widthHeight = String.format("%d %d", width, height);
					
					mapItem.addNavigatorField(NavigatorKey.MapFile, file.getPath());
					mapItem.addNavigatorField(NavigatorKey.MapWidthHeight, widthHeight);
					mapItem.addNavigatorField(NavigatorKey.MapScaleMat, "10 0 0 -10");
				}
			}
			
		} catch (FileNotFoundException ex) {
	        ex.printStackTrace();
		} catch (IOException ex) {
		        ex.printStackTrace();
		}
		
		return mapItem;
	}
}
