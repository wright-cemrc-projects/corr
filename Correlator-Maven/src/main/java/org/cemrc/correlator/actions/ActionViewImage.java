package org.cemrc.correlator.actions;



import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.CorrelatorConfig;
import org.cemrc.correlator.controllers.ImageViewerController;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.IMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This action should open an image and display it in a JavaFX view.
 * @author larso
 *
 */
public class ActionViewImage {

	private final CorrelatorDocument m_document;
	private final IMap m_activeMap;
	
	public ActionViewImage(CorrelatorDocument doc, IMap active) {
		m_document = doc;
		m_activeMap = active;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 */
	public void doAction() {
	
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/ViewImage.fxml"));
			Parent importView = loader.load();
			
			ImageViewerController controller = loader.getController();
			controller.setDocument(m_document);
			controller.setActiveMap(m_activeMap);
			
			// Proof-of-concept, now needs better color contrast and to work with the backing data.
			//List<Vector2<Float>> testPoints = new ArrayList<Vector2<Float>>();
			//testPoints.add(new Vector2<Float>(415f, 315f));
			//controller.addPixelPositions(1, testPoints);
			
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
