package org.cemrc.easycorr.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector4;
import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.IMap;
import org.cemrc.data.PixelPositionDataset;
import org.cemrc.easycorr.EasyCorrConfig;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller class for the helper dialog to import CSV pixel positions to the autodoc file.
 * @author larso
 *
 */
public class ImportPointsController {
	
	@FXML
	Button browseFile, cancelButton, importButton;
	
	@FXML
	ComboBox<String> mapCombo, xCombo, yCombo, colorCombo;
	
	@FXML
	TableView<List<StringProperty>> table;
	
	EasyCorrDocument m_document;
	Stage m_parentStage;
	List<List<StringProperty>> m_data;
	IMap selectedMap;
	int columnX = 2;
	int columnY = 3;
	int colorID = 2;
	int regid = 2;
	
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;
		setupMapSelection();
	}
	
	public void setStage(Stage parent) {
		m_parentStage = parent;
	}
	
	private void setupMapSelection() {
		List<IMap> maps = m_document.getData().getMapData();
		
		mapCombo.getItems().clear();
		for (IMap map : maps) {
			mapCombo.getItems().add(map.getName());
		}
	}

	@FXML
	public void initialize() {
		importButton.setDisable(true);
		colorCombo.getItems().addAll(new String []{"0", "1", "2", "3", "4", "5"});
	}
	
	@FXML
	public void onBrowse() {
	    FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose pixel positions file (.csv)");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Comma-separated values (*.csv)", "*.csv");
    	fileChooser.getExtensionFilters().add(extFilter);
    	
    	Stage dialogStage = new Stage();
    	dialogStage.getIcons().add(EasyCorrConfig.getApplicationIcon());
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
        	parseCSVasString(file);
    		importButton.setDisable(false);
        }
	}
	
	private void parseCSVasString(File file) {
		int columns = 0;
		
		List<List<StringProperty>> rowdata = new ArrayList<List<StringProperty>>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			
			// Skip Byte-Order-Mark
	        reader.mark(1);
	        char[] possibleBOM = new char[1];
	        reader.read(possibleBOM);

	        if (possibleBOM[0] != '\ufeff')
	        {
	            reader.reset();
	        }
			
			String line;
			while ((line = reader.readLine()) != null) {
				String [] parts = line.split(",");
				if (parts.length > columns) {
					columns = parts.length;
				}
				
				// Parse each line into a data blob 
				List<StringProperty> data = new ArrayList<StringProperty>();
				for (String p : parts) {
					data.add(new SimpleStringProperty(p));
				}
				rowdata.add(data);
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} 
		
		// Clear the menu dropdowns.
		xCombo.getItems().clear();
		yCombo.getItems().clear();
		
		// Setup the TableView:
		table.getColumns().clear();
		table.getItems().clear();
		char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		
		// Create TableColumn(s)
		for (int i = 0; i < columns; i++) {
			if (i < alphabet.length) {
				String columnName = Character.toString(alphabet[i]).toUpperCase();
				
				TableColumn<List<StringProperty>, String> column = new TableColumn<>(columnName);
				column.setMinWidth(80);
				final int columnIndex = i;
				column.setCellValueFactory(data -> data.getValue().get(columnIndex));
				table.getColumns().add(column);
				
				xCombo.getItems().add(columnName);
				yCombo.getItems().add(columnName);
			}
		}
		
		// Add the row data to match the CSV.
		for (int i = 0; i < rowdata.size(); i++) {
			table.getItems().add(rowdata.get(i));
		}
		
		// Save the parsed data for the onImport method.
		m_data = rowdata;
	}
	
	@FXML
	public void onChooseMap() {
		String selected = mapCombo.getValue();
		
		for (IMap map : m_document.getData().getMapData()) {
			if (selected.equals(map.getName())) {
				selectedMap = map;
				break;
			}
		}
	}
	
	@FXML
	public void onChooseX() {
		String selected = xCombo.getValue();
		
		List<String> names = xCombo.getItems();
		for (int i = 0; i < xCombo.getVisibleRowCount(); i++) {
			if (selected.equals(names.get(i))) {
				columnX = i;
				break;
			}
		}
	}
	
	@FXML
	public void onChooseY() {
		String selected = yCombo.getValue();
		
		List<String> names = yCombo.getItems();
		for (int i = 0; i < yCombo.getVisibleRowCount(); i++) {
			if (selected.equals(names.get(i))) {
				columnY = i;
				break;
			}
		}
	}
	
	@FXML
	public void onChooseColor() {
		String selected = colorCombo.getValue();
		
		List<String> names = colorCombo.getItems();
		for (int i = 0; i < colorCombo.getVisibleRowCount(); i++) {
			if (selected.equals(names.get(i))) {
				colorID = i;
				break;
			}
		}
	}
	
	@FXML
	public void onCancel() {
		if (m_parentStage != null) {
			m_parentStage.close();
		}
	}
	
	@FXML
	public void onImport() {
		if (m_data == null) {
			throw new IllegalArgumentException("No CSV parsed yet to import.");
		}

		// Get the X, Y values from correct columns.
		List<Vector2<Float>> parsedPositions = new ArrayList<Vector2<Float>>();
		
		// Construct a PixelPositionDataset and add to the data model.
		for (List<StringProperty> row : m_data) {
			try {
				StringProperty xfield = row.get(columnX);
				StringProperty yfield = row.get(columnY);
				
				float x = Float.parseFloat(xfield.get());
				float y = Float.parseFloat(yfield.get());
				
				Vector2<Float> pixelPosition = new Vector2<Float>(x, y);
				parsedPositions.add(pixelPosition);
				
			} catch (Exception e) {
				// Skip a bad row.
			}
		}
		
		PixelPositionDataset pixelPositions = new PixelPositionDataset();
		pixelPositions.setPixelPositions(parsedPositions);
		pixelPositions.setColorID(colorID);

		// Get useful values from the map.
		if (selectedMap != null) {
			pixelPositions.setMap(selectedMap);
			pixelPositions.setMapId(selectedMap.getId());
			
			GenericItem mapItem = selectedMap.getAutoDoc();
			if (mapItem.hasKey(NavigatorKey.MapID)) {
				pixelPositions.setDrawnID((Integer)mapItem.getValue(NavigatorKey.MapID));
			}
			
			if (mapItem.hasKey(NavigatorKey.Regis)) {
				pixelPositions.setRegisID((Integer)mapItem.getValue(NavigatorKey.Regis));
			}
			
			if (mapItem.hasKey(NavigatorKey.Imported)) {
				pixelPositions.setImported((Integer)mapItem.getValue(NavigatorKey.Imported));
			}
			
			if (mapItem.hasKey(NavigatorKey.BklshXY)) {
				pixelPositions.setBacklash((Vector2<Float>)mapItem.getValue(NavigatorKey.BklshXY));
			}
			
		} else {
			pixelPositions.setMapId(IMap.UNASSIGNED_MAP);
		}
		
		m_document.getData().addPositionData(pixelPositions);
		
		if (m_parentStage != null) {
			m_parentStage.close();
		}
	}
	
}
