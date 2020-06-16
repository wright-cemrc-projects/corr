package org.cemrc.easycorr.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;
import org.cemrc.data.NavigatorColorEnum;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

public class PointsTableController {
	
	// Cause UI updates when data model changes.
	@XmlTransient
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
	
	private TableView<PointsDatasetTableItem> m_pointsTableView;
	private PointsDatasetTableItem m_selected;
	
	private EasyCorrDocument m_document = null;
	private Set<IMap> m_maps = new HashSet<IMap>();
	
    /**
     * Can be called when a some value has changed.
     * @param property
     * @param oldValue
     * @param newValue
     */
    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
    
	/**
	 * This property listener can alert when some values have updated.
	 * @param listener
	 */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
	
	/**
	 * Set the document
	 * @param doc
	 */
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;
	}
	
	/**
	 * Set the active map
	 * @param map
	 */
	public void addMap(IMap map) {
		m_maps.add(map);
	}
	
	/**
	 * Select a row.
	 * @param dataset
	 */
	public void select(IPositionDataset dataset) {
		int index = 0;
		for (PointsDatasetTableItem item : m_pointsTableView.getItems()) {
			if (item.getDataset() == dataset) {
				m_pointsTableView.getSelectionModel().clearAndSelect(index);
				break;
			} else {
				index++;
			}
		}
	}

	/**
	 * Represents a table item.
	 * @author larso
	 *
	 */
	public class PointsDatasetTableItem {
		private final BooleanProperty m_visible = new SimpleBooleanProperty();
		public BooleanProperty visibleProperty() { 
			return m_visible; 
		}
		
		private IPositionDataset m_dataset;
		
		public String getName() {
			return m_dataset.getName();
		}
		
		public void setName(String name) {
			// TODO
			// m_dataset.setName(name);
		}
		
		public int getPoints() {
			return m_dataset.getPixelPositions().size();
		}
		
		public NavigatorColorEnum getColor() {
			return m_dataset.getColor();
		}
		
		public void setColor(NavigatorColorEnum color) {
			m_dataset.setColor(color);
		}

		public IPositionDataset getDataset() {
			return m_dataset;
		}
		
		public void setDataset(IPositionDataset dataset) {
			m_dataset = dataset;
		}
	}

	
	public PointsTableController(TableView<PointsDatasetTableItem> pointsTableView) {
		m_pointsTableView = pointsTableView;
		setupTableView();
	}
	
	/**
	 * Setup the points dataset tableview columns and properties.
	 */
	private void setupTableView() {
		
		// http://tutorials.jenkov.com/javafx/tableview.html#tableview-selection-model
		
	    TableColumn<PointsDatasetTableItem, String> column1 = new TableColumn<>("Name");
	    column1.setCellValueFactory(new PropertyValueFactory<>("name"));
	    column1.setOnEditCommit(evt -> evt.getRowValue().setName(evt.getNewValue()));
	    column1.setMaxWidth(100);

	    TableColumn<PointsDatasetTableItem, String> column2 = new TableColumn<>("Points");
	    column2.setCellValueFactory(new PropertyValueFactory<>("points"));
	    
	    TableColumn<PointsDatasetTableItem, NavigatorColorEnum> column3 = new TableColumn<>("Color");
	    column3.setCellValueFactory(new PropertyValueFactory<>("color"));
	    column3.setOnEditCommit(evt -> {
	    	evt.getRowValue().setColor(evt.getNewValue());
	    	firePropertyChange("COLOR_CHANGED", evt, evt);
	    	});
	    column3.setMinWidth(80);
	    
	    ObservableList<NavigatorColorEnum> cbValues = FXCollections.observableArrayList(NavigatorColorEnum.values());
	    column3.setCellFactory(ComboBoxTableCell.forTableColumn(cbValues));
	    
	    TableColumn<PointsDatasetTableItem, Boolean> column4 = new TableColumn<>("Visible");
	    column4.setCellValueFactory(new PropertyValueFactory<>("visible"));
	    column4.setCellFactory( tc -> new CheckBoxTableCell<PointsDatasetTableItem, Boolean>());
	    
	    m_pointsTableView.getColumns().add(column1);
	    m_pointsTableView.getColumns().add(column2);
	    m_pointsTableView.getColumns().add(column3);
	    m_pointsTableView.getColumns().add(column4);
	    
	    // Add a listener for selections.
	    m_pointsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PointsDatasetTableItem>() {

			@Override
			public void changed(ObservableValue<? extends PointsDatasetTableItem> arg0, PointsDatasetTableItem old,
					PointsDatasetTableItem newvalue) {
				if (newvalue != null) {
					m_selected = newvalue;
				}
			}
	    	
	    });
	    
	    m_pointsTableView.setEditable(true);
	}
	
	/**
	 * Recreate the row items in the table view.
	 */
	public void updatePointsTableView() {
		
		Set<IPositionDataset> existing = new HashSet<IPositionDataset>();
		
		// TODO: if we implement deleting rows, this will require a change.
		for (PointsDatasetTableItem item : m_pointsTableView.getItems()) {
			existing.add(item.getDataset());
		}
		
		for (IPositionDataset p : m_document.getData().getPositionData()) {				
			if (m_maps.contains(p.getMap()) && !existing.contains(p)) {
				PointsDatasetTableItem item = new PointsDatasetTableItem();
				item.setDataset(p);
				item.setName(p.getName());
				item.visibleProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						firePropertyChange("VISIBILITY_CHANGE", item, item);
					}
					
				});
				item.visibleProperty().setValue(true);
				m_pointsTableView.getItems().add(item);
			}
		}
	}
	
	/**
	 * Get the list of visible IPositionDatsets for a map
	 * @param parent
	 * @return
	 */
	public List<IPositionDataset> getVisible(IMap parent) {
		List<IPositionDataset> rv = new ArrayList<IPositionDataset>();
		
		for (PointsDatasetTableItem item : m_pointsTableView.getItems()) {
			if (item.visibleProperty().getValue()) {
				if (item.getDataset().getMap() == parent) {
					rv.add(item.getDataset());
				}
			}
		}
		
		return rv;
	}
	
	/**
	 * Returns the currently selected row, if any.
	 * @return
	 */
	public IPositionDataset getSelected() {
		if (m_selected != null) {
			return m_selected.getDataset();
		}
		return null;
	}
}
