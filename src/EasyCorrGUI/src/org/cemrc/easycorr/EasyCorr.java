package org.cemrc.easycorr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.EasyCorrState;
import org.cemrc.easycorr.actions.ActionExportAutodoc;
import org.cemrc.easycorr.actions.ActionImportAutodoc;
import org.cemrc.easycorr.actions.ActionImportImageMap;
import org.cemrc.easycorr.actions.ActionImportPoints;
import org.cemrc.easycorr.controllers.ProjectController;
import org.cemrc.easycorr.wizard.WizardController;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


/**
 * Main application class for EasyCorr, built with JavaFX framework.
 * @author larso
 *
 * note: https://stackoverflow.com/questions/860187/access-restriction-on-class-due-to-restriction-on-required-library-rt-jar
 */
public class EasyCorr extends Application {

	// The application data state.
	EasyCorrState m_state;
	
	// The ProjectController should be updated when changes occur in program.
	ProjectController m_projectController;
	
	public EasyCorr() {
		m_state = new EasyCorrState();

		m_state.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				m_projectController.setDocument(m_state.getDocument());
				m_projectController.updateTreeView(m_state.getDocument());
			}
			
		});
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Stage is where visual parts of JavaFX application are displayed.
        primaryStage.setTitle(EasyCorrConfig.AppName);
        
        // JavaFX Stage == a window
        // JavaFX Scene is the root of a JavaFX Scene graph
        
        // Menubar for the application
        MenuBar mb = new MenuBar();
        
        // add menus to menubar
        mb.getMenus().add(createFileMenu());
        mb.getMenus().add(createImportMenu());
        mb.getMenus().add(createHelpMenu());
        
        // create a VBox
        VBox vb = new VBox(mb);
        
        // load in the project view.
		FXMLLoader loader = new FXMLLoader(EasyCorr.class.getResource("/view/ProjectView.fxml"));
		Parent projectView = loader.load();
		m_projectController = (ProjectController) loader.getController();
		m_projectController.setDocument(m_state.getDocument());
		
		// Add to the view.
		vb.getChildren().add(projectView);
        
        // create a scene
        Scene sc = new Scene(vb, 500, 300);
        
        // set the scene
        primaryStage.setScene(sc);
        
        // Set the icon
        primaryStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        
        // Makes application visible in a window
        primaryStage.show();
	}
	
	/**
	 * Create the file menu and setup callbacks.
	 * @return
	 */
	private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");
        
        // create menuitems
        MenuItem projectWizardMenu = new MenuItem("New Project Wizard");
        MenuItem newProjectMenu = new MenuItem("New Project");
        MenuItem openProjectMenu = new MenuItem("Open Project");
        MenuItem saveProjectMenu = new MenuItem("Save Project");
        MenuItem exportProject = new MenuItem("Export to Navigator");
        
        // add to the menu
        fileMenu.getItems().add(projectWizardMenu);
        fileMenu.getItems().add(newProjectMenu);
        fileMenu.getItems().add(openProjectMenu);
        fileMenu.getItems().add(saveProjectMenu);
        fileMenu.getItems().add(exportProject);
        
        // setup EventHandler(s)
        EventHandler<ActionEvent> newProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	m_state.setDocument(new EasyCorrDocument());
				// m_projectController.updateTreeView(m_state.getDocument());
            } 
        };
        newProjectMenu.setOnAction(newProjectEvent);
        
        EventHandler<ActionEvent> startWizardEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	handleProjectWizard();
            } 
        };
        projectWizardMenu.setOnAction(startWizardEvent);
        
        EventHandler<ActionEvent> openProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	handleOpenProject();
            } 
        };
        openProjectMenu.setOnAction(openProjectEvent);
        
        EventHandler<ActionEvent> saveProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	handleSaveProject();
            } 
        };
        saveProjectMenu.setOnAction(saveProjectEvent);
        
        EventHandler<ActionEvent> exportEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	ActionExportAutodoc exporter = new ActionExportAutodoc(m_state.getDocument());
            	exporter.doAction();
            } 
        };
        exportProject.setOnAction(exportEvent);
        
        return fileMenu;
	}
	
	private void handleOpenProject() {
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open an existing EasyCorr project (.xml)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("EasyCorr (*.xml)", "*.xml");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
        	try {
        		EasyCorrDocument doc = EasyCorrDocument.deserialize(file);
        		m_state.setDocument(doc);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
	
	private void handleSaveProject() {
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save EasyCorr project (.xml)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("EasyCorr (*.xml)", "*.xml");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
        	try {
        		EasyCorrDocument.serialize(m_state.getDocument(), file);
        		m_state.getDocument().setDirt(false);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
	
	/**
	 * Create the import menu and callbacks.
	 * @return
	 */
	private Menu createImportMenu() {
        // create import menu
        Menu menuImport = new Menu("Import");
        MenuItem importImage = new MenuItem("Import Image file (.tif)");
        MenuItem importNav = new MenuItem("Import Navigator File (.nav)");
        MenuItem importPoints = new MenuItem("Import Pixel Positions (.csv)");
        menuImport.getItems().add(importImage);
        menuImport.getItems().add(importNav);
        menuImport.getItems().add(importPoints);
        
        // setup EventHandler(s)
        EventHandler<ActionEvent> importImageEvent = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) 
            { 
            	ActionImportImageMap action = new ActionImportImageMap(m_state.getDocument());
            	action.doAction();
            } 
        };
        importImage.setOnAction(importImageEvent);
        
        EventHandler<ActionEvent> importNavEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	ActionImportAutodoc action = new ActionImportAutodoc(m_state.getDocument());
            	action.doAction();
            } 
        };
        importNav.setOnAction(importNavEvent);
        
        EventHandler<ActionEvent> importPointsEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	ActionImportPoints action = new ActionImportPoints(m_state.getDocument());
            	action.doAction();
            } 
        };
        importPoints.setOnAction(importPointsEvent);
        
        return menuImport;
	}
	
	/**
	 * Create the help menu and callbacks.
	 * @return
	 */
	private Menu createHelpMenu() {
        // create help menu
        Menu menuHelp = new Menu("Help");
        MenuItem aboutMenu = new MenuItem("About EasyCorr");
        menuHelp.getItems().add(aboutMenu);
        
        EventHandler<ActionEvent> aboutWindowEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	createAboutWindow();
            } 
        };
        
        menuHelp.setOnAction(aboutWindowEvent);
        
        return menuHelp;
	}
	
	/**
	 * Create a project wizard dialog.
	 */
	private void handleProjectWizard() {
    	Stage wizardStage = new Stage();
    	wizardStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
    	//ProjectWizard wizardPanel = new ProjectWizard(wizardStage, m_state, new EasyCorrDocument());
    	//Scene wizardScene = new Scene(wizardPanel, 500, 300);
    	//wizardStage.setScene(wizardScene);
    	
        // load in the project view.
    	try {
			FXMLLoader loader = new FXMLLoader(EasyCorr.class.getResource("/view/wizard/Wizard.fxml"));
			Parent wizard = loader.load();
			WizardController wizardController = (WizardController) loader.getController();
			wizardController.setOwner(wizardStage);
			
			// Provide the backing data.
			wizardController.setState(m_state);
			wizardController.setDocument(new EasyCorrDocument());
			
	    	Scene wizardScene = new Scene(wizard, 600, 450);
			wizardStage.setScene(wizardScene);
	    	wizardStage.show();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	/**
	 * This is a test-run for working with FXML.
	 * @throws IOException 
	 */
	private void createAboutWindow() {
		try {
			Stage aboutStage = new Stage();
	    	aboutStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
			
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(EasyCorr.class.getResource("/view/AboutLayout.fxml"));
			VBox aboutLayout = (VBox) loader.load();
			
			Scene scene = new Scene(aboutLayout);
			aboutStage.setScene(scene);
			aboutStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the application run
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}
}
