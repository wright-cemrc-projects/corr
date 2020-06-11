package org.cemrc.easycorr.wizard;

import org.cemrc.data.EasyCorrDocument;

public class WizardFinish implements IWizardPage {

	EasyCorrDocument m_document;
	
	@Override
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;
	}

	@Override
	public boolean canComplete() {
		return true;
	}

}
