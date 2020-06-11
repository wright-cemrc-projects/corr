package org.cemrc.easycorr.actions;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.easycorr.EasyCorr;
import org.cemrc.easycorr.EasyCorrConfig;
import org.cemrc.easycorr.controllers.ImportPointsController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionImportPoints {
	
	private EasyCorrDocument m_document;
	
	public ActionImportPoints(EasyCorrDocument doc) {
		m_document = doc;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 */
	public void doAction() {
		
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(EasyCorr.class.getResource("/view/ImportPointsView.fxml"));
			Parent importView = loader.load();
			ImportPointsController controller = (ImportPointsController) loader.getController();
			controller.setDocument(m_document);
			
			// create a stage
	    	Scene importScene = new Scene(importView, 600, 450);
	    	Stage dialogStage = new Stage();
	    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
	    	
	    	controller.setStage(dialogStage);
	    	
	    	dialogStage.setScene(importScene);
	    	dialogStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
