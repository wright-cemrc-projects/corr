package org.cemrc.correlator.controllers;

import org.cemrc.correlator.CorrelatorConfig;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class AboutController {

	@FXML
	private ImageView imageView;
	
    @FXML
    private Label label;
    
    @FXML
    public void initialize() {
        // Initialization code can go here. 
        // The parameters url and resources can be omitted if they are not needed
    	
    	label.setText(CorrelatorConfig.getAboutText());
    }  
	
}
