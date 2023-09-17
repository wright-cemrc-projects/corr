package org.cemrc.correlator.wizard;

import org.cemrc.data.CorrelatorDocument;

public interface IWizardPage {

	/**
	 * Set the backing model data.
	 * @param doc
	 */
	public void setDocument(CorrelatorDocument doc);
	
	/**
	 * Check if the page has been completed.
	 * @return
	 */
	public boolean canComplete();
}
