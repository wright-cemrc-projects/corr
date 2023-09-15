package org.cemrc.correlator.actions;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.ImageRegistrationController;
import org.cemrc.correlator.controllers.RegistrationPairState;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This action should open a window for selection of registration points.
 * @author mrlarson2
 *
 */
public class ActionRegisterImage {
	private final CorrelatorDocument m_document;
	private final IMap m_activeMap;
	private final RegistrationPairState m_registrationState;
	private int m_registrationIndex;
	
	public ActionRegisterImage(CorrelatorDocument doc, int registrationIndex, IMap active, RegistrationPairState state) {
		m_document = doc;
		m_activeMap = active;
		m_registrationState = state;
		m_registrationIndex = registrationIndex;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 */
	public void doAction() {
	
		try {
			
			// View and controller that allow picking registration points.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/RegisterImage.fxml"));
			Parent importView = loader.load();
		
			ImageRegistrationController controller = loader.getController();
			controller.setDocument(m_document);
			controller.setActiveMap(m_activeMap);
			controller.setRegistrationState(m_registrationState);
			controller.setRegistrationIndex(m_registrationIndex);
			
			// create a stage
	    	Scene importScene = new Scene(importView, 750, 800);
	    	Stage dialogStage = new Stage();
	    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
	    	dialogStage.setScene(importScene);
	    	dialogStage.setTitle(controller.getTitle());
	    	dialogStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
