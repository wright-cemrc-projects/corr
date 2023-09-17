package org.cemrc.correlator.wizard;

import org.cemrc.data.CorrelatorDocument;

public class WizardFinish implements IWizardPage {

	CorrelatorDocument m_document;
	
	@Override
	public void setDocument(CorrelatorDocument doc) {
		m_document = doc;
	}

	@Override
	public boolean canComplete() {
		return true;
	}

}
