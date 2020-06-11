package org.cemrc.easycorr.wizard;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.easycorr.actions.ActionExportAutodoc;

import javafx.fxml.FXML;

public class WizardExport implements IWizardPage {

	EasyCorrDocument m_document;
	
	@Override
	public void setDocument(EasyCorrDocument doc) {
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
