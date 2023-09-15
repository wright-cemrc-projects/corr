package org.cemrc.correlator.actions;

import java.io.IOException;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.InteractiveAlignment;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionInteractiveAlignment {

	private final CorrelatorDocument m_document;
	private final IMap m_target, m_reference;
	
	
	public ActionInteractiveAlignment(CorrelatorDocument doc, IMap target, IMap reference) {
		m_document = doc;
		m_target = target;
		m_reference = reference;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 * @throws IOException 
	 */
	public void doAction()  {
		
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/InteractiveAlignmentEntryGUI.fxml"));
			Parent importView = loader.load();
			
			// Note: we really don't need IPositionDatasets, instead maps...
			InteractiveAlignment controller = loader.getController();
			controller.setDocument(m_document);
			controller.setTargetMap(m_target);
			controller.setReferenceMap(m_reference);
			controller.setupMaps();
			
			// create a stage
	    	Scene importScene = new Scene(importView, 500, 400);
	    	Stage dialogStage = new Stage();
	    	controller.setStage(dialogStage);
	    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
	    	dialogStage.setScene(importScene);
	    	dialogStage.setTitle(CorrelatorConfig.getApplicationName());
	    	dialogStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
