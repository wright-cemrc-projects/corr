package org.cemrc.easycorr.wizard;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.easycorr.EasyCorr;
import org.cemrc.easycorr.controllers.ProjectController;
import org.cemrc.easycorr.actions.ActionImportAutodoc;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

public class WizardImportMapController implements IWizardPage {

	@FXML
	private HBox projectPanel;
	
	private EasyCorrDocument m_document;
	private ProjectController m_projectController;
	
	/**
	 * On changing documents, should set this to update the view.
	 * @param doc
	 */
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;
		m_projectController.setDocument(m_document);
	}
	
	@FXML
	public void initialize() {
		try {
			// load a ProjectView into the content panel.
			FXMLLoader loader = new FXMLLoader(EasyCorr.class.getResource("/view/ProjectView.fxml"));
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
	public void onImport() {
		ActionImportAutodoc action = new ActionImportAutodoc(m_document);
		action.doAction();
	}
}
