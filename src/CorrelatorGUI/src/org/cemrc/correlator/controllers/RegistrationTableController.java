package org.cemrc.correlator.controllers;

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
	
	private Integer m_counter = 1;
	
	private TableView<RegistrationPair> m_registrationTableView;
	private RegistrationPairState m_registrationState;
	
	
	public RegistrationTableController(TableView<RegistrationPair> registrationTable) {
		m_registrationState = new RegistrationPairState();
		m_registrationTableView = registrationTable;
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
	
	public void addRow() {
		RegistrationPair pair = new RegistrationPair();
		pair.setId(m_counter++);
		m_registrationTableView.getItems().add(pair);
		m_registrationTableView.refresh();
	}
	
	/**
	 * Remove the selected row from the table
	 */
	public void removeSelectedRow() {
		RegistrationPair selection = m_registrationState.getSelected();
		if (m_registrationState.getSelected() != null) {
			m_registrationTableView.getItems().remove(selection);
			m_registrationState.setSelected(null);
		}
		m_registrationTableView.refresh();
	}
	
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
}
