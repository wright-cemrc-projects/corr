package org.cemrc.easycorr.actions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.AutodocWriter;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.easycorr.EasyCorrConfig;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ActionExportAutodoc {
	private EasyCorrDocument m_document;
	
	public ActionExportAutodoc(EasyCorrDocument doc) {
		m_document = doc;
	}
	
	/**
	 * Prompt a user for a filename.
	 */
	public void doAction() {
		
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export to navigator autodoc file (.nav)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Navigator files (*.nav)", "*.nav");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
        	exportFile(file);
        }
	}
	
	private List<GenericItem> consecutiveIndex(List<GenericItem> autodoc) {
		Integer index = 0;
		
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		for (GenericItem item : autodoc) {
			GenericItem c = item.clone();
			
			try { 
				Integer currentIndex = Integer.parseInt(c.getName());
				
				if (currentIndex <= index) {
					index++;
					c.setName(index.toString());
				} else {
					index = currentIndex;
				}
			} catch (NumberFormatException e) {}
			
			rv.add(c);
		}
		
		return rv;
	}
	
	/**
	 * Export the navigate file to disk.
	 * @param filename
	 */
	private void exportFile(File file) {
		try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
			List<GenericItem> autoDocFormat = m_document.getData().getAutodoc();
			AutodocWriter.write(consecutiveIndex(autoDocFormat), stream);
			
		} catch (IOException ex) {
			System.err.println("Unable to export autodoc: " + ex.getMessage());
		}
	}
}
