package org.cemrc.correlator.actions;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.ImportPointsController;
import org.cemrc.data.CorrelatorDocument;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionImportPoints {
	
	private CorrelatorDocument m_document;
	
	public ActionImportPoints(CorrelatorDocument doc) {
		m_document = doc;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 */
	public void doAction() {
		
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/ImportPointsView.fxml"));
			Parent importView = loader.load();
			ImportPointsController controller = (ImportPointsController) loader.getController();
			controller.setDocument(m_document);
			
			// create a stage
	    	Scene importScene = new Scene(importView, 600, 450);
	    	Stage dialogStage = new Stage();
	    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
	    	
	    	controller.setStage(dialogStage);
	    	
	    	dialogStage.setScene(importScene);
	    	dialogStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
