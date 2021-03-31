package org.cemrc.correlator.actions;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.AlignMapsController;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionFreeAlignMaps {
	
	private CorrelatorDocument m_document;
	private IMap m_target;
	
	public ActionFreeAlignMaps(CorrelatorDocument doc, IMap target) {
		m_document = doc;
		m_target = target;
	}

	/**
	 * Prompt a user for a filename.
	 */
	public void doAction() {
		
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/AlignToMapView.fxml"));
			Parent importView = loader.load();
			
			// Get the controller, provide the required components.
			AlignMapsController controller = loader.getController();
			controller.setDocument(m_document);
			controller.setTargetMap(m_target);
			
			// create a stage
	    	Scene importScene = new Scene(importView, 500, 250);
	    	Stage dialogStage = new Stage();
	    	controller.setStage(dialogStage);
	    	
	    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
	    	dialogStage.setScene(importScene);
	    	dialogStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
