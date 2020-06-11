package org.cemrc.easycorr.actions;



import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.Vector2;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;
import org.cemrc.easycorr.EasyCorr;
import org.cemrc.easycorr.EasyCorrConfig;
import org.cemrc.easycorr.controllers.ImageViewerController;

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

	private final EasyCorrDocument m_document;
	private final IMap m_activeMap;
	
	public ActionViewImage(EasyCorrDocument doc, IMap active) {
		m_document = doc;
		m_activeMap = active;
	}
	
	/**
	 * This should open a file prompt to find a points file or a dialog first.
	 */
	public void doAction() {
	
		try {
	        // load in the project view.
			FXMLLoader loader = new FXMLLoader(EasyCorr.class.getResource("/view/ViewImage.fxml"));
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
	    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
	    	dialogStage.setScene(importScene);
	    	dialogStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
