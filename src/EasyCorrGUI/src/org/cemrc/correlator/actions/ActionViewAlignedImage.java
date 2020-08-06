package org.cemrc.correlator.actions;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.AlignedImageViewerController;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.Registration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActionViewAlignedImage {
	
	private final CorrelatorDocument m_document;
	private final IMap m_activeMap;
	
	public ActionViewAlignedImage(CorrelatorDocument doc, IMap active) {
		m_document = doc;
		m_activeMap = active;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 */
	public void doAction() {
	
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/AlignedViewImage.fxml"));
			Parent importView = loader.load();
			
			AlignedImageViewerController controller = loader.getController();
			controller.setDocument(m_document);
			
			controller.setActiveMap(m_activeMap);
			
			Registration registration = m_activeMap.getRegistration();
			if (registration != null) {
				// Set the reference map.
				
				for (IMap map : m_document.getData().getMapData()) {
					if (map.getId() == registration.getRegisterMapId()) {
						// Found the reference
						controller.setReferenceMap(map);
						controller.setAligned(true);
						break;
					}
				}
			}
			
			controller.updateZoomCanvas();
			
			// create a stage
	    	Scene importScene = new Scene(importView, 750, 800);
	    	Stage dialogStage = new Stage();
	    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
	    	dialogStage.setScene(importScene);
	    	dialogStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
