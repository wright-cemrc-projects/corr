package org.cemrc.easycorr.actions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector4;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.easycorr.EasyCorrConfig;
import org.cemrc.easycorr.io.ReadMRC;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
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
		fileChooser.setTitle("Open an image file (.mrc, .st, .tif, .png)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.mrc, *.st, *.tif, *.png)", "*.mrc", "*.st", "*.tif", "*.png");
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
		
		BufferedImage mapImage = null;
		
		if (file.getName().endsWith(".st") || file.getName().endsWith(".mrc")) {
			mapImage = ReadMRC.parseSerialEM(file);
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
					mapImage = bf;
					/*
					if (bf != null) {
					    mapImage = SwingFXUtils.toFXImage(bf, null);   //convert bufferedImage (awt) into Writable Image(fx)
					}
					*/
				}
			}
			
		} catch (FileNotFoundException ex) {
	        ex.printStackTrace();
		} catch (IOException ex) {
		        ex.printStackTrace();
		}
		}
		
		if (mapImage != null) {
		
			double width = mapImage.getWidth();
			double height = mapImage.getHeight();
	
			mapItem.addNavigatorField(NavigatorKey.MapFile, file.getPath());
			mapItem.addNavigatorField(NavigatorKey.MapWidthHeight, new Vector2<Integer>((int) width, (int) height));
			mapItem.addNavigatorField(NavigatorKey.MapScaleMat, new Vector4<Float>(10f, 0f, 0f, -10f));
			mapItem.addNavigatorField(NavigatorKey.Regis, 0);
			int uniqueId = m_document.getData().getUniqueMapId();
			mapItem.addNavigatorField(NavigatorKey.MapID, uniqueId);
		} else {
			// TODO : error message
		}
		
		return mapItem;
	}
}
