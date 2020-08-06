package org.cemrc.correlator.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cemrc.autodoc.AutodocParser;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.data.CorrelatorDocument;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ActionImportAutodoc {

	private CorrelatorDocument m_document;
	
	public ActionImportAutodoc(CorrelatorDocument doc) {
		m_document = doc;
	}
	
	/**
	 * Creates a file open dialog to find an autodoc.
	 * Merge in the data if user selected an autodoc.
	 */
	public void doAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open an navigator autodoc file (.nav)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Navigator files (*.nav)", "*.nav");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
        	openFile(file);
        }
	}
	
	/**
	 * Open a .nav file to get initial Map and Points data.
	 * @param file
	 */
	private void openFile(File file) {
		try (InputStream io = new BufferedInputStream(new FileInputStream(file)) ){
			
			// Deserialize the autodoc from text file.
			List<GenericItem> docItems = AutodocParser.parse(io);
			
			// Get a NavData from the flat autodoc list.
			m_document.getData().mergeAutodoc(docItems, file);

		} catch (IOException e) {
			System.err.println("Unable to open " + file.getName() + " " + e.getMessage());
		}
	}
}
