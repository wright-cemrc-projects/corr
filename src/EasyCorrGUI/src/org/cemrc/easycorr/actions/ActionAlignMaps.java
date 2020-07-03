package org.cemrc.easycorr.actions;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;
import org.cemrc.easycorr.EasyCorr;
import org.cemrc.easycorr.EasyCorrConfig;
import org.cemrc.easycorr.controllers.AlignMapsController;
import org.cemrc.easycorr.controllers.ImageViewerController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionAlignMaps {

	private EasyCorrDocument m_document;
	private IMap m_target;
	
	public ActionAlignMaps(EasyCorrDocument doc, IMap target) {
		m_document = doc;
		m_target = target;
	}
	
	/**
	 * Prompt a user for a filename.
	 */
	public void doAction() {
		
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(EasyCorr.class.getResource("/view/AlignToMapView.fxml"));
			Parent importView = loader.load();
			
			// Get the controller, provide the required components.
			AlignMapsController controller = loader.getController();
			controller.setDocument(m_document);
			controller.setTargetMap(m_target);
			
			// create a stage
	    	Scene importScene = new Scene(importView, 400, 250);
	    	Stage dialogStage = new Stage();
	    	controller.setStage(dialogStage);
	    	
	    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
	    	dialogStage.setScene(importScene);
	    	dialogStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
