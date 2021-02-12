package org.cemrc.correlator.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.cemrc.autodoc.Vector2;
import org.cemrc.data.IMap;
import org.cemrc.data.NavigatorColorEnum;

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
	
	private static String UNASSIGNED = "Unassigned";
	
	private TableView<RegistrationPair> m_registrationTableView;
	private RegistrationPair m_selected;

	// Cause UI updates when data model changes.
	@XmlTransient
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
	
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
	 * Represent a pair of registration points.
	 * @author larso
	 *
	 */
	public class RegistrationPair {
		
		private IMap m_referenceMap, m_targetMap;
		private Vector2<Float> m_referencePoint, m_targetPoint;
		private Integer id = 0;
		
		
		public IMap getReferenceMap() {
			return m_referenceMap;
		}
		
		public void setReferenceMap(IMap referenceMap) {
			m_referenceMap = referenceMap;
		}

		public IMap getTargetMap() {
			return m_targetMap;
		}

		public void setTargetMap(IMap targetMap) {
			m_targetMap = targetMap;
		}

		public Vector2<Float> getReferencePoint() {
			return m_referencePoint;
		}

		public void setReferencePoint(Vector2<Float> referencePoint) {
			m_referencePoint = referencePoint;
		}

		public Vector2<Float> getTargetPoint() {
			return m_targetPoint;
		}

		public void setTargetPoint(Vector2<Float> targetPoint) {
			m_targetPoint = targetPoint;
		}
		
		// Provides a table label
		public Integer getId() {
			return id;
		}
		
		public String getTargetMapName() {
			if (m_targetMap != null) return m_targetMap.getName();
			else return UNASSIGNED;
		}
		
		public String getTargetPointName() {
			if (m_targetPoint != null) return m_targetPoint.toString();
			else return UNASSIGNED;
		}
		
		public String getReferenceMapName() {
			if (m_referenceMap != null) return m_referenceMap.getName();
			else return UNASSIGNED;
		}
	}
	
	
	public RegistrationTableController(TableView<RegistrationPair> registrationTable) {
		m_registrationTableView = registrationTable;
		setupTableView();
	}
	
	private void setupTableView() {
		// http://tutorials.jenkov.com/javafx/tableview.html#tableview-selection-model
		
	    TableColumn<RegistrationPair, String> column1 = new TableColumn<>("Name");
	    column1.setCellFactory(TextFieldTableCell.forTableColumn());
	    column1.setCellValueFactory(new PropertyValueFactory<>("id"));
	    column1.setMinWidth(40);

	    TableColumn<RegistrationPair, String> column2 = new TableColumn<>("Target Map");
	    column2.setCellValueFactory(new PropertyValueFactory<>("targetMapName"));
	    column2.setMinWidth(60);
	    
	    TableColumn<RegistrationPair, NavigatorColorEnum> column3 = new TableColumn<>("Target Point");
	    column3.setCellValueFactory(new PropertyValueFactory<>("targetPointName"));
	    column3.setMinWidth(60);
	    
	    TableColumn<RegistrationPair, Boolean> column4 = new TableColumn<>("Reference Map");
	    column4.setCellValueFactory(new PropertyValueFactory<>("referenceMapName"));
	    column4.setMinWidth(65);
	    
	    TableColumn<RegistrationPair, Boolean> column5 = new TableColumn<>("Reference Point");
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
					m_selected = newvalue;
				}
			}
	    	
	    });
	    
	    // Prevents appearance of extra, emtpy column, with tradeoff of requiring same-size columns.
	    m_registrationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    m_registrationTableView.setEditable(true);
	}
}
