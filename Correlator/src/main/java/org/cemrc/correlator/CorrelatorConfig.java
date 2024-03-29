package org.cemrc.correlator;

import javafx.scene.image.Image;

/**
 * Keep text strings centralized in this class to make it easier to support other locales.
 * @author larso
 *
 */
public class CorrelatorConfig {
	
	public static String VersionName = "1.40.0 OpenJDK";
	
	public static String AppName = "CorRelator";
	
	private static Image ApplicationIcon = new Image(CorrelatorConfig.class.getResourceAsStream("/view/App.png"));
	
	public static String getApplicationName() {
		return AppName;
	}
	
	public static String getAboutText() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(AppName + " supports Correlative Light and Electron Microscopy workflows.\n");
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
