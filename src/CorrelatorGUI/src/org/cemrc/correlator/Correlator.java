package org.cemrc.correlator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.cemrc.correlator.actions.ActionAlignMaps;
import org.cemrc.correlator.actions.ActionExportAutodoc;
import org.cemrc.correlator.actions.ActionImportAutodoc;
import org.cemrc.correlator.actions.ActionImportImageMap;
import org.cemrc.correlator.actions.ActionImportPoints;
import org.cemrc.correlator.actions.ActionInteractiveAlignment;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 * Main application class built with JavaFX framework.
 * @author larso
 *
 * note: https://stackoverflow.com/questions/860187/access-restriction-on-class-due-to-restriction-on-required-library-rt-jar
 */
public class Correlator extends Application {
	
	Stage m_primaryStage;

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
				updateTitle();
			}
			
		});
	}
	
	private void updateTitle() {
		
		StringBuilder titleBuilder = new StringBuilder();
		titleBuilder.append(CorrelatorConfig.AppName);
		titleBuilder.append(" ");
		if (m_state.hasSavefile()) {
			titleBuilder.append("(" + m_state.getFilename());
			
			if (m_state.getDocument().isDirty()) {
				titleBuilder.append("*");
			}
			
			titleBuilder.append(")");
		} else {
			if (m_state.getDocument().isDirty()) {
				titleBuilder.append("(Untitled*)");
			}
		}
		
		// Stage is where visual parts of JavaFX application are displayed.
        m_primaryStage.setTitle(titleBuilder.toString());
	}
	
	/**
	 * Gracefully handle close-with-save
	 * @param event
	 */
	private void closeWindowEvent(WindowEvent event) {
		if (checkExisting()) {
			// Yes (and save) or No, we can continue exiting...
		} else {
			// Cancel means abort exiting...
			event.consume();
		}
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
        mb.getMenus().add(createExportMenu());
        mb.getMenus().add(createAlignmentMenu());
        mb.getMenus().add(createAnalysisMenu());
        mb.getMenus().add(createHelpMenu());
        
        // create a VBox
        VBox vb = new VBox(mb);
        
        // load in the project view.
		FXMLLoader loader = new FXMLLoader(Correlator.class.getResource("/view/ProjectView.fxml"));
		Parent projectView = loader.load();
		m_projectController = (ProjectController) loader.getController();
		m_projectController.setDocument(m_state.getDocument());
		m_projectController.setState(m_state);
		
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
        
        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        m_primaryStage = primaryStage;
	}
	
	/**
	 * When a document is dirty, ask to save.
	 * @return
	 */
	private boolean checkExisting() {
		
		if (m_state.getDocument().isDirty()) {
		
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle("Save project?");
			alert.setContentText("The project data has changed, would you like to save?");
			ButtonType okButton = new ButtonType("Yes", ButtonData.YES);
			ButtonType noButton = new ButtonType("No", ButtonData.NO);
			ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(okButton, noButton, cancelButton);

			alert.showAndWait();
			
			ButtonType type = alert.getResult();
			if (type == okButton) {
				handleSaveProject();
				return true;
			} else if (type == noButton) {
				return true;
			} else {
				alert.close();
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Create the file menu and setup callbacks.
	 * @return
	 */
	private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");
        
        // create menuitems
        MenuItem projectWizardMenu = new MenuItem("New Project Wizard");
        MenuItem newProjectMenu = new MenuItem("New");
        MenuItem openProjectMenu = new MenuItem("Open");
        MenuItem saveProjectMenu = new MenuItem("Save");
        MenuItem saveasProjectMenu = new MenuItem("Save As...");
        
        // add to the menu
        fileMenu.getItems().add(projectWizardMenu);
        fileMenu.getItems().add(newProjectMenu);
        fileMenu.getItems().add(openProjectMenu);
        fileMenu.getItems().add(saveProjectMenu);
        fileMenu.getItems().add(saveasProjectMenu);
        
        // setup EventHandler(s)
        EventHandler<ActionEvent> newProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	if (checkExisting()) {
            		m_state.setDocument(new CorrelatorDocument());
            		m_state.setSaveFile(null);
            		updateTitle();
            	}
            } 
        };
        newProjectMenu.setOnAction(newProjectEvent);
        
        EventHandler<ActionEvent> startWizardEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	if (checkExisting()) {
            		handleProjectWizard();
            		m_state.setSaveFile(null);
            		updateTitle();
            	}
            } 
        };
        projectWizardMenu.setOnAction(startWizardEvent);
        
        EventHandler<ActionEvent> openProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	if (checkExisting() ) {
            		handleOpenProject();
            		updateTitle();
            	}
            } 
        };
        openProjectMenu.setOnAction(openProjectEvent);
        
        EventHandler<ActionEvent> saveProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	handleSaveProject();
            	updateTitle();
            } 
        };
        saveProjectMenu.setOnAction(saveProjectEvent);
        
        EventHandler<ActionEvent> saveasProjectEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	handleSaveAsProject();
            	updateTitle();
            } 
        };
        saveasProjectMenu.setOnAction(saveasProjectEvent);
        
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
        		m_state.setSaveFile(file);
        		m_state.setDocument(doc);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
	
	private void handleSaveProject() {
		
		if (m_state.hasSavefile()) {
			m_state.save();
		} else {
			handleSaveAsProject();
		}
		
	}
	
	private void handleSaveAsProject() {
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
        		m_state.setSaveFile(file);
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
	
	private Menu createExportMenu() {
		Menu menu = new Menu("Export");
	    MenuItem exportProject = new MenuItem("Export to Navigator");
	    
	        
	    // add to the menu
	    menu.getItems().add(exportProject);
	        
        EventHandler<ActionEvent> exportEvent = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
            	ActionExportAutodoc exporter = new ActionExportAutodoc(m_state.getDocument());
            	exporter.doAction();
            } 
        };
        exportProject.setOnAction(exportEvent);
        
        return menu;
	}
	
	private Menu createAlignmentMenu() {
		Menu menu = new Menu("Alignment");
        MenuItem alignItem = new MenuItem("Paired Alignment");
        alignItem.setOnAction(event -> {
            	ActionAlignMaps alignAction = new ActionAlignMaps(m_state.getDocument(), null);
            	alignAction.doAction();
        	}
        );
		
	    // add to the menu
	    menu.getItems().add(alignItem);
	    
		MenuItem freeAlignItem = new MenuItem("Free Alignment");
		freeAlignItem.setOnAction(event -> {
				ActionInteractiveAlignment startAlignmentGUI = new ActionInteractiveAlignment(m_state.getDocument(), null, null); 
				startAlignmentGUI.doAction();
		});
		
		menu.getItems().add(freeAlignItem);

		return menu;
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
			holesController.setComboSelected(m_state.getActiveMap());

	    	Scene wizardScene = new Scene(dialog, 800, 740);
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
