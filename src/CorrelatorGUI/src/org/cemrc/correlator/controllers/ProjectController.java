package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.correlator.actions.ActionAlignMaps;
import org.cemrc.correlator.actions.ActionInteractiveAlignment;
import org.cemrc.correlator.actions.ActionUnalignMaps;
import org.cemrc.correlator.actions.ActionViewAlignedImage;
import org.cemrc.correlator.actions.ActionViewImage;
import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.CorrelatorState;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * Controller for the main project treeview
 * @author larso
 *
 */
public class ProjectController {

	private CorrelatorState m_state;
    private CorrelatorDocument m_document;
	
	@FXML
	private TreeView<ProjectNodeItem> projectTreeView;
	
    private final Image registeredImage =  new Image(
            getClass().getResourceAsStream("/view/registered.png"));
	
    @FXML
    public void initialize() {
    	projectTreeView.setShowRoot(false);
    	TreeItem<ProjectNodeItem> root = new TreeItem<ProjectNodeItem>(new ProjectNodeItem(ProjectNodeItem.NodeType.Other, null, null, "Empty Project"));
        root.setExpanded(true);
        projectTreeView.setRoot(root);
        
        // https://stackoverflow.com/questions/43541884/how-to-make-a-context-menu-work-on-a-treeview-in-javafx
        this.projectTreeView.setCellFactory(new Callback<TreeView<ProjectNodeItem>,TreeCell<ProjectNodeItem>>(){
        	
            @Override
            public TreeCell<ProjectNodeItem> call(TreeView<ProjectNodeItem> p) {
                TreeCell<ProjectNodeItem> cell = new TreeCell<ProjectNodeItem>() {
                    @Override
                    protected void updateItem(ProjectNodeItem file, boolean empty) {
                        super.updateItem(file, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            // maybe use a more appropriate string for display here
                            // e.g. if you were using a regular java.io.File you would
                            // likely want file.getName()
                            setText(file.toString());
                        }
                    }
                };
                ContextMenu cm = createContextMenu(cell);
                cell.setContextMenu(cm);
                return cell;
            }
            
            private ContextMenu createContextMenu(TreeCell<ProjectNodeItem> cell) {
                ContextMenu cm = new ContextMenu();
                
                MenuItem openItem = new MenuItem("View Map");
                openItem.setOnAction(event -> {

                	if (cell.getItem().type == ProjectNodeItem.NodeType.Map) {
	                	IMap mapItem = cell.getItem().getMapItem();  
	                	
                    	ActionViewImage viewerAction = new ActionViewImage(m_document, mapItem);
                    	viewerAction.doAction();
                	}
                });
                
                MenuItem alignItem = new MenuItem("Paired Alignment");
                alignItem.setOnAction(event -> {
                	if (cell.getItem().type == ProjectNodeItem.NodeType.Map) {
	                	IMap mapItem = cell.getItem().getMapItem();  
	                	ActionAlignMaps alignAction = new ActionAlignMaps(m_document, mapItem);
	                	alignAction.doAction();
                	}
                });
                
        		MenuItem freeAlignItem = new MenuItem("Free Alignment");
        		freeAlignItem.setOnAction(event -> {
        				ActionInteractiveAlignment startAlignmentGUI = new ActionInteractiveAlignment(m_state.getDocument(), null, null); 
        				startAlignmentGUI.doAction();
        		});
                
                MenuItem unalignItem = new MenuItem("Un-Align Map");
                unalignItem.setOnAction(event -> {
                	if (cell.getItem().type == ProjectNodeItem.NodeType.Map) {
	                	IMap mapItem = cell.getItem().getMapItem();  
	                	ActionUnalignMaps unalignAction = new ActionUnalignMaps(m_document, mapItem);
	                	unalignAction.doAction();
	                	if (mapItem.getRegistration() == null) {
	                		cell.getTreeItem().setGraphic(null);
	                	}
                	}
                });
                
                MenuItem viewAlignedMapItem = new MenuItem("View Aligned Maps");
                viewAlignedMapItem.setOnAction(event -> {
                	if (cell.getItem().type == ProjectNodeItem.NodeType.Map) {
	                	IMap mapItem = cell.getItem().getMapItem();  
	                	
                    	ActionViewAlignedImage viewerAction = new ActionViewAlignedImage(m_document, mapItem);
                    	viewerAction.doAction();
                	}
                });
                
                cm.getItems().addAll(openItem, alignItem, freeAlignItem, unalignItem, viewAlignedMapItem);
                // other menu items...
                
                return cm ;
            }
        });

    }
    
    public void updateTreeView(CorrelatorDocument doc) {
    	List<IMap> maps = doc.getData().getMapData();
    	
    	List<IPositionDataset> unassociated = new ArrayList<IPositionDataset>();
    	
    	TreeItem<ProjectNodeItem> root = new TreeItem<ProjectNodeItem>(new ProjectNodeItem(ProjectNodeItem.NodeType.Other, null, null, "Project"));
        root.setExpanded(true);
    	
    	for (IPositionDataset d : doc.getData().getPositionData()) {
    		if (d.getMapId() == IMap.UNASSIGNED_MAP) {
    			unassociated.add(d);
    		}
    	}
    	
    	if (maps.size() > 0) {
	        
	        for (IMap map : maps) {
	        	// ProjectNodeItem itemName = map.getRegistration() == null ? map.getName() : map.getName() + " Registered to [" + map.getId() + "]";
	        	ProjectNodeItem mapItem = new ProjectNodeItem(ProjectNodeItem.NodeType.Map, null, map, "Map");
	        	
	        	TreeItem<ProjectNodeItem> item;
            	if (map.getRegistration() != null) {
            		item = new TreeItem<ProjectNodeItem>(mapItem, new ImageView(registeredImage));
            	} else {
            		item = new TreeItem<ProjectNodeItem>(mapItem);
            	}
	        	root.getChildren().add(item);
	        	
	        	// Add the matching positions as children.
		    	for (IPositionDataset data : doc.getData().getPositionData()) {
		    		int positions = data.getNumberPositions();
		    		
		    		// Create a child node below the Map it belongs.
		    		if (map.getId() == data.getMapId()) {	    			
		    			ProjectNodeItem pointItem = new ProjectNodeItem(ProjectNodeItem.NodeType.Position, data, null, "Positions");
			        	TreeItem<ProjectNodeItem> pointsItem = new TreeItem<ProjectNodeItem>(pointItem);
			        	item.getChildren().add(pointsItem);
		    		}
		    	}
		    	
	        }
    	}
	        
    	// Handle unassociated points somehow.
    	if (unassociated.size() > 0) {
        	TreeItem<ProjectNodeItem> item = new TreeItem<ProjectNodeItem>(new ProjectNodeItem(ProjectNodeItem.NodeType.Other, null, null, "Unassociated Points"));
        	root.getChildren().add(item);
        	
        	for (IPositionDataset data : unassociated) {
    			ProjectNodeItem pointItem = new ProjectNodeItem(ProjectNodeItem.NodeType.Position, data, null, "Positions");
	        	TreeItem<ProjectNodeItem> pointsItem = new TreeItem<ProjectNodeItem>(pointItem);
	        	item.getChildren().add(pointsItem);
        	}
    	}

    	projectTreeView.setRoot(root);  
    	
    	projectTreeView.getSelectionModel().selectedItemProperty().addListener(
    		new ChangeListener<TreeItem<ProjectNodeItem>>() {

				@Override
				public void changed(ObservableValue<? extends TreeItem<ProjectNodeItem>> observable,
						TreeItem<ProjectNodeItem> oldValue, TreeItem<ProjectNodeItem> newValue) {

					if (m_state != null) {
						if (newValue != null && newValue.getValue().type == ProjectNodeItem.NodeType.Map) {
							m_state.setActiveMap(newValue.getValue().getMapItem());
						} else {
							m_state.setActiveMap(null);
						}
					} 
				}
        });
    }
    
    public void setDocument(CorrelatorDocument doc) {
    	m_document = doc;
    	
    	doc.getData().addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				updateTreeView(doc);
			}
    		
    	});
    	
    	updateTreeView(m_document);
    }
    
    public void setState(CorrelatorState state) {
    	m_state = state;
    }
}
