package org.cemrc.easycorr.wizard;

import org.cemrc.data.EasyCorrDocument;

public interface IWizardPage {

	/**
	 * Set the backing model data.
	 * @param doc
	 */
	public void setDocument(EasyCorrDocument doc);
	
	/**
	 * Check if the page has been completed.
	 * @return
	 */
	public boolean canComplete();
}
