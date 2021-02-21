package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * Controller for the registration table of InteractiveAlignment
 * @author mrlarson2
 *
 */
public class RegistrationTableController {
	
	private TableView<RegistrationPair> m_registrationTableView;
	private RegistrationPairState m_registrationState;
	
	/**
	 * Constructor with new state.
	 * @param registrationTable
	 */
	public RegistrationTableController(TableView<RegistrationPair> registrationTable) {
		m_registrationTableView = registrationTable;
		setState(new RegistrationPairState());
		setupTableView();
	}
	
	/**
	 * Constructor taking an existing state.
	 * @param registrationTable
	 * @param state
	 */
	public RegistrationTableController(TableView<RegistrationPair> registrationTable, RegistrationPairState state) {
		m_registrationTableView = registrationTable;
		setState(state);
		setupTableView();
	}
	
	private void setupTableView() {
		// http://tutorials.jenkov.com/javafx/tableview.html#tableview-selection-model
		
	    TableColumn<RegistrationPair, String> column1 = new TableColumn<>("Name");
	    column1.setCellFactory(TextFieldTableCell.forTableColumn());
	    column1.setCellValueFactory(new PropertyValueFactory<>("name"));
	    column1.setMinWidth(40);

	    TableColumn<RegistrationPair, String> column2 = new TableColumn<>("Target Map");
	    column2.setCellValueFactory(new PropertyValueFactory<>("targetMapName"));
	    column2.setMinWidth(60);
	    
	    TableColumn<RegistrationPair, String> column3 = new TableColumn<>("Target Point");
	    column3.setCellValueFactory(new PropertyValueFactory<>("targetPointName"));
	    column3.setMinWidth(60);
	    
	    TableColumn<RegistrationPair, String> column4 = new TableColumn<>("Reference Map");
	    column4.setCellValueFactory(new PropertyValueFactory<>("referenceMapName"));
	    column4.setMinWidth(65);
	    
	    TableColumn<RegistrationPair, String> column5 = new TableColumn<>("Reference Point");
	    column5.setCellValueFactory(new PropertyValueFactory<>("referencePointName"));
	    column5.setMinWidth(75);
	    
	    m_registrationTableView.getColumns().add(column1);
	    m_registrationTableView.getColumns().add(column2);
	    m_registrationTableView.getColumns().add(column3);
	    m_registrationTableView.getColumns().add(column4);
	    m_registrationTableView.getColumns().add(column5);
	    
	    // Add a listener for selections.
	    m_registrationTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RegistrationPair>() {

			@Override
			public void changed(ObservableValue<? extends RegistrationPair> arg0, RegistrationPair old,
					RegistrationPair newvalue) {
				if (newvalue != null) {
					m_registrationState.setSelected(newvalue);
				}
			}
	    	
	    });
	    
	    // Prevents appearance of extra, emtpy column, with tradeoff of requiring same-size columns.
	    m_registrationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    m_registrationTableView.setEditable(true);
	}
	
	/**
	 * Remove the selected row from the table
	public void removeSelectedRow() {
		RegistrationPair selection = m_registrationState.getSelected();
		if (m_registrationState.getSelected() != null) {
			m_registrationTableView.getItems().remove(selection);
			m_registrationState.setSelected(null);
		}
		m_registrationTableView.refresh();
	}
	*/
	
	/**
	 * Get the registration pairs.
	 * @return
	 */
	public List<RegistrationPair> getItems() {
		List<RegistrationPair> rv = new ArrayList<RegistrationPair>();
		
		rv.addAll(m_registrationTableView.getItems());
		
		return rv;
	}
	
	/**
	 * Get the state of the backing registration data.
	 * @return
	 */
	public RegistrationPairState getState() {
		return m_registrationState;
	}
	
	/**
	 * Rebuild the GUI from the backing state.
	 */
	private void rebuildList() {
		m_registrationTableView.getItems().clear();
		for (RegistrationPair pair : m_registrationState.getRegistrationList()) {
			m_registrationTableView.getItems().add(pair);
		}
		m_registrationTableView.refresh();
		
	}
	
	/**
	 * Replace the backing state data and update the GUI.
	 * @param state
	 */
	public void setState(RegistrationPairState state) {
		
		// remove existing listeners
		if (m_registrationState != null) {
			// TODO
		}
		
		m_registrationState = state;
		
		if (m_registrationState != null) {
			rebuildList();
			
			// setup a listener
			m_registrationState.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					rebuildList();
				}
				
			});
		}
	}
}
