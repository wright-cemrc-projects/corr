package org.cemrc.correlator.wizard;

import org.cemrc.correlator.actions.ActionExportAutodoc;
import org.cemrc.data.CorrelatorDocument;

import javafx.fxml.FXML;

public class WizardExport implements IWizardPage {

	CorrelatorDocument m_document;
	
	@Override
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
	}

	@Override
	public boolean canComplete() {
		return true;
	}

	@FXML
	public void onExport() {
		ActionExportAutodoc autodoc = new ActionExportAutodoc(m_document);
		autodoc.doAction();
	}
}
