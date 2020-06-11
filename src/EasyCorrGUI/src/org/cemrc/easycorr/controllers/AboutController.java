package org.cemrc.easycorr.controllers;

import org.cemrc.easycorr.EasyCorrConfig;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
    	
    	label.setText(EasyCorrConfig.getAboutText());
    	
        // load the image
        Image image = new Image("/view/EasyCorr.png");
    	imageView.setImage(image);
    }  
	
}
