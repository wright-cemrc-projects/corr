package org.cemrc.correlator.wizard;

import org.cemrc.correlator.Correlator;
import org.cemrc.correlator.actions.ActionImportPoints;
import org.cemrc.correlator.controllers.ProjectController;
import org.cemrc.data.CorrelatorDocument;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

public class WizardImportPositionsController implements IWizardPage {

	@FXML
	private HBox projectPanel;
	
	private CorrelatorDocument m_document;
	private ProjectController m_projectController;
	
	/**
	 * On changing documents, should set this to update the view.
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
		m_projectController.setDocument(m_document);
	}
	
	@FXML
	public void initialize() {
		try {
			// load a ProjectView into the content panel.
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/ProjectView.fxml"));
			Parent projectView = loader.load();
			m_projectController = (ProjectController) loader.getController();
			
			// Add to the view.
			projectPanel.getChildren().add(projectView);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canComplete() {
		return true;
	}
	
	@FXML
	public void doImport() {
    	ActionImportPoints action = new ActionImportPoints(m_document);
    	action.doAction();
	}
}
