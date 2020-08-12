package org.cemrc.correlator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.cemrc.correlator.actions.ActionExportAutodoc;
import org.cemrc.correlator.actions.ActionImportAutodoc;
import org.cemrc.correlator.actions.ActionImportImageMap;
import org.cemrc.correlator.actions.ActionImportPoints;
import org.cemrc.correlator.controllers.FindHolesController;
import org.cemrc.correlator.controllers.ProjectController;
import org.cemrc.correlator.wizard.WizardController;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.CorrelatorState;

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
 * Main application class built with JavaFX framework.
 * @author larso
 *
 * note: https://stackoverflow.com/questions/860187/access-restriction-on-class-due-to-restriction-on-required-library-rt-jar
 */
public class Correlator extends Application {

	// The application data state.
	CorrelatorState m_state;
	
	// The ProjectController should be updated when changes occur in program.
	ProjectController m_projectController;
	
	public Correlator() {
		m_state = new CorrelatorState();

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
        primaryStage.setTitle(CorrelatorConfig.AppName);
        
        // JavaFX Stage == a window
        // JavaFX Scene is the root of a JavaFX Scene graph
        
        // Menubar for the application
        MenuBar mb = new MenuBar();
        
        // add menus to menubar
        mb.getMenus().add(createFileMenu());
        mb.getMenus().add(createImportMenu());
        mb.getMenus().add(createAnalysisMenu());
        mb.getMenus().add(createHelpMenu());
        
        // create a VBox
        VBox vb = new VBox(mb);
        
        // load in the project view.
		FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/ProjectView.fxml"));
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
        primaryStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
        
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
            	m_state.setDocument(new CorrelatorDocument());
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
		fileChooser.setTitle("Open an existing " + CorrelatorConfig.AppName + " project (.xml)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(CorrelatorConfig.AppName + " (*.xml)", "*.xml");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
        	try {
        		CorrelatorDocument doc = CorrelatorDocument.deserialize(file);
        		m_state.setDocument(doc);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
	
	private void handleSaveProject() {
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save " + CorrelatorConfig.AppName + " project (.xml)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(CorrelatorConfig.AppName + " (*.xml)", "*.xml");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
        	try {
        		CorrelatorDocument.serialize(m_state.getDocument(), file);
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
        MenuItem importImage = new MenuItem("Import Image File");
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
        MenuItem aboutMenu = new MenuItem("About " + CorrelatorConfig.AppName);
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
	 * Create the help menu and callbacks.
	 * @return
	 */
	private Menu createAnalysisMenu() {
        // create help menu
        Menu menuAnalysis = new Menu("Analysis");
        MenuItem findHoles = new MenuItem("Find Holes");
        menuAnalysis.getItems().add(findHoles);
        
        EventHandler<ActionEvent> aboutWindowEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	findHolesDialog();
            } 
        };
        
        menuAnalysis.setOnAction(aboutWindowEvent);
        return menuAnalysis;
	}
	
	private void findHolesDialog() {
    	Stage stage = new Stage();
    	stage.getIcons().add(CorrelatorConfig.getApplicationIcon());
    	
        // load in the project view.
    	try {
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/FindHolesDialog.fxml"));
			Parent dialog = loader.load();
			FindHolesController holesController = (FindHolesController) loader.getController();
			holesController.setDocument(m_state.getDocument());
			holesController.setStage(stage);
			
	    	Scene wizardScene = new Scene(dialog, 650, 600);
			stage.setScene(wizardScene);
	    	stage.show();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	/**
	 * Create a project wizard dialog.
	 */
	private void handleProjectWizard() {
    	Stage wizardStage = new Stage();
    	wizardStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
    	
        // load in the project view.
    	try {
			FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/wizard/Wizard.fxml"));
			Parent wizard = loader.load();
			WizardController wizardController = (WizardController) loader.getController();
			wizardController.setOwner(wizardStage);
			
			// Provide the backing data.
			wizardController.setState(m_state);
			wizardController.setDocument(new CorrelatorDocument());
			
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
	    	aboutStage.getIcons().add(CorrelatorConfig.getApplicationIcon());
			
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Correlator.class.getResource("/view/AboutLayout.fxml"));
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
