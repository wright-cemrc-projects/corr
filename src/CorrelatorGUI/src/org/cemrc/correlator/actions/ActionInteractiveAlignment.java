package org.cemrc.correlator.actions;

import java.io.IOException;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.InteractiveAlignment;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IPositionDataset;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionInteractiveAlignment {

	private final CorrelatorDocument m_document;
	private final IPositionDataset m_targetPoints, m_referencePoints;
	
	
	public ActionInteractiveAlignment(CorrelatorDocument doc, IPositionDataset target, IPositionDataset reference) {
		m_document = doc;
		m_targetPoints = target;
		m_referencePoints = reference;
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
			controller.setTargetMap(m_targetPoints.getMap());
			controller.setReferenceMap(m_referencePoints.getMap());
			
			// create a stage
	    	Scene importScene = new Scene(importView, 400, 400);
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
