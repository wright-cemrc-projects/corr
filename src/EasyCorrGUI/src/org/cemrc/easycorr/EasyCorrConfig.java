package org.cemrc.easycorr;

import javafx.scene.image.Image;

/**
 * Keep text strings centralized in this class to make it easier to support other locales.
 * @author larso
 *
 */
public class EasyCorrConfig {
	
	public static String VersionName = "0.11.9";
	
	public static String AppName = "Correlator";
	
	private static Image ApplicationIcon = new Image(EasyCorrConfig.class.getResourceAsStream("/view/EasyCorr_App.png"));
	
	public static String getApplicationName() {
		return AppName;
	}
	
	public static String getAboutText() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(AppName + " is a graphical workflow to support Correlative Light and Electron Microscopy workflows.\n");
		sb.append(AppName + " is maintained by the UW Madison Cryo-EM Research Center (CEMRC).\n");
		sb.append(getVersionText());
		
		return sb.toString();
	}
	
	public static String getVersionText() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Version: " + VersionName + "\n");
		
		return sb.toString();
	}
	
	public static Image getApplicationIcon() {
		return ApplicationIcon;
	}
}
