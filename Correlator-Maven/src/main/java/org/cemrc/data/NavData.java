package org.cemrc.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.cemrc.autodoc.FloatList;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector3;
import org.cemrc.math.MatrixMath;

/**
 * NavData consists of multiple autodoc items.
 * 
 * @author larso
 *
 */
@XmlRootElement(name="navdata")
public class NavData {
	
	public static String DOCUMENT_CHANGED="DOCUMENT_CHANGED";
	
	// Cause UI updates when data model changes.
	@XmlTransient
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
	
	// Maps can be associated with items such as points an polygons.
	@XmlElements({ 
	    @XmlElement(name="autodocMap", type=AutodocMap.class),
	    @XmlElement(name="imageMap", type=RawImageMap.class)
	})
	@XmlElementWrapper(name="maps")
	private List<IMap> m_maps = new ArrayList<IMap>();
	
	// PixelData are sets of PixelPositions where each of the PixelPositions is tied to the same MapData.
	
	@XmlElements({ 
	    @XmlElement(name="pixelPositionDataset", type=PixelPositionDataset.class),
	    @XmlElement(name="autodocPositionDataset", type=AutodocPositionDataset.class)
	})
	@XmlElementWrapper(name="positions")
    private List<IPositionDataset> m_positionData = new ArrayList<IPositionDataset>();
	
	@XmlElement(name="uniqueCount")
	int m_uniqueID = 1;
	public int getUniquePointsID() {
		return m_uniqueID++;
	}
	
	/**
	 * This property listener can alert when some values have updated.
	 * @param listener
	 */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unsubscribe a listener
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    	listeners.remove(listener);
    }

    /**
     * Can be called when a some value has changed.
     * @param property
     * @param oldValue
     * @param newValue
     */
    private void firePropertyChange(String property, Object oldValue, Object newValue) {
    	List<PropertyChangeListener> modlisteners = new ArrayList<>();
    	modlisteners.addAll(listeners);
    	
        for (PropertyChangeListener l : modlisteners) {
            l.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
	
	/**
	 * Add a MapData to the NavData
	 * @param data
	 */
	public void addMapData(IMap data) {
		m_maps.add(data);
		firePropertyChange(DOCUMENT_CHANGED, this, this);
	}
	
	/**
	 * Add PositionData to the NavData
	 * @param data
	 */
	public void addPositionData(IPositionDataset data) {
		m_positionData.add(data);
		firePropertyChange(DOCUMENT_CHANGED, this, this);
	}
	
	/**
	 * Remove PositionData from the NavData
	 * @param data
	 */
	public void removePositionData(IPositionDataset data) {
		m_positionData.remove(data);
		firePropertyChange(DOCUMENT_CHANGED, this, this);
	}
	
	/**
	 * Force update of dependent views.
	 */
	public void forceUpdate() {
		firePropertyChange(DOCUMENT_CHANGED, this, this);
	}
	
	/**
	 * Get the list of MapData in this NavData
	 * @return
	 */
	public List<IMap> getMapData() {
		return m_maps;
	}
	
	/**
	 * Get the list of Position in this NavData
	 * @return
	 */
	public List<IPositionDataset> getPositionData() {
		return m_positionData;
	}
	
	/**
	 * Merge in the autodoc 
	 * @param items
	 * @param sourceFile
	 */
	public void mergeAutodoc(List<GenericItem> items, File sourceFile) {
		
		// Bundle stage positions being drawn on the same map together. 		
		List<GenericItem> points = new ArrayList<GenericItem>();
		
		// Parse through each GenericItem
		for (GenericItem item : items) {
			if ((item.hasKey(NavigatorKey.Type))) {
				
				int itemType = (Integer) item.getValue(NavigatorKey.Type);
				
				switch (itemType) {
					case 0: 
					case 1:
						// Point type
						points.add(item);
						break;
						
					case 2:
			
						// Map type
						AutodocMap map = new AutodocMap(item);
						map.setSourceFile(sourceFile);
						m_maps.add(map);
						
						break;
					default:
						break;
				}
			}
		}
		
		// convert points into group segments associated with maps.
		processPointItems(points);
		firePropertyChange(DOCUMENT_CHANGED, this, this);
	}
	
	// convert points into group segments associated with maps.
	private void processPointItems(List<GenericItem> items) {
		
		// Need to index by MapID and GroupID
		
		Map<Integer, Map<Integer, AutodocPositionDataset>> mapping = new HashMap<Integer, Map<Integer, AutodocPositionDataset>>();
		// Map<Integer, AutodocPositionDataset> positions = new HashMap<Integer, AutodocPositionDataset>();
		Integer key = null;
		
		for (GenericItem item : items) {
			// Point type
			
			// Associate with the Map
			key = null;
			if (item.hasKey(NavigatorKey.DrawnID)) {
				key = (Integer) item.getValue(NavigatorKey.DrawnID);
			} 
			
			if (! mapping.containsKey(key)) {
				mapping.put(key, new HashMap<Integer, AutodocPositionDataset>());
			}
			
			// Segment by the GroupID
			Integer groupID = 0;
			if (item.hasKey(NavigatorKey.GroupID)) {
				groupID = (Integer) item.getValue(NavigatorKey.GroupID);
			}
			
			Map<Integer, AutodocPositionDataset> mapPositions = mapping.get(key);
			if (! mapPositions.containsKey(groupID) ) {
				AutodocPositionDataset points = new AutodocPositionDataset();
				points.setGroupID(groupID);
				points.setName("Group " + points.getGroupID());
				
				// Associate with it's parent map.
				for (IMap map : m_maps) {
					GenericItem mapItem = map.getAutoDoc();
					if (mapItem.hasKey(NavigatorKey.MapID) && 
							(mapItem.getValue(NavigatorKey.MapID).equals(key))) {
						points.setMapId(map.getId());
						points.setMap(map);
					}	
				}
				mapPositions.put(groupID, points);
			}
			
			// Add to this dataset.
			mapPositions.get(groupID).addItem(item);
			
		}
		
		for (Map<Integer, AutodocPositionDataset> mapPositions : mapping.values()) {
			for (AutodocPositionDataset p : mapPositions.values()) {
				
				if (p.getGroupID() == 0) {
					int uniqueId = getUniqueGroupID();
					p.setGroupID(uniqueId);
					p.setName("Group " + p.getGroupID());
				}
				
				m_positionData.add(p);
			}
		}
	}
	
	/**
	 * Create a dictionary of MapID -> IMap
	 * @return
	 */
	private Map<Integer, IMap> getMapDictionary() {
		Map<Integer, IMap> rv = new HashMap<Integer, IMap>();
		
		for (IMap i : m_maps) {
			rv.put(i.getId(), i);
		}
		
		return rv;
	}
	
	/**
	 * Find a unique identifer.
	 * @return
	 */
	public Integer getUniqueMapId() {
		
		// Starting block of IDs
		int baseInteger = 10000;
		
		Set<Integer> existing = new HashSet<Integer>();
		
		for (IMap i : m_maps) {
			existing.add(i.getId());
		}
		
		while (existing.contains(baseInteger)) {
			baseInteger++;
		}
		return baseInteger;
	}
	
	/**
	 * Get back data in autodoc formatted data items.
	 * @return
	 */
	public List<GenericItem> getAutodoc() {
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		// Note: instead of just writing the autodoc, an affine transformation can update the
		// positions here. Not sure if this is correct.
		Map<Integer, IMap> mapLookup = getMapDictionary();
		
		for (IMap i : m_maps) {
			GenericItem mapItem = i.getAutoDoc();
			
			if (i.getRegistration() != null) {
				rv.add(getRegisteredMapItem(mapItem, i));	
			} else {
				rv.add(mapItem);
			}
		}
		
		for (IPositionDataset s : m_positionData) {
			try {
				List<GenericItem> items = s.getAutodocItems();
				
				int mapID = s.getMapId();
				IMap parent = mapLookup.get(mapID);
							
				// Do the position records need to be updated for the registration?
				if (parent != null && parent.getRegistration() != null && !s.isRegisterationPoints()) {
					
					// StagePositions in item needs to be updated
					for (GenericItem i : items) {
						rv.add(getRegisteredPointItem(i, parent));
					}
					
				} else {
					rv.addAll(items);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rv;
	}
	
	// TODO: may be possible to hide this within the IPositionDataset itself.
	private GenericItem getRegisteredPointItem(GenericItem item, IMap registeredMap) {
		GenericItem rv = item.clone();
		
		// update the StageXYZ to a new StageXYZ using inv(AffineMatrix).
		double [][] affineMatrix = registeredMap.getRegistration().getStageMatrix();
		
		// StageXYZ -> similar to above
		if (rv.hasKey(NavigatorKey.StageXYZ)) {
			// TODO: Z coordinate = 1, workaround for matrix multiplication.
			Vector3<Float> pos = (Vector3<Float>) rv.getValue(NavigatorKey.StageXYZ);
			float pos_z = pos.z;
			pos.z = 1f;
			pos = MatrixMath.multiply(affineMatrix, pos);
			pos.z = registeredMap.getRegistration().getStageZ();
			rv.addNavigatorField(NavigatorKey.StageXYZ, pos);
			
			// PtsX, PtsY
			processPtsXY(rv, affineMatrix);
		}
		
		// Imported -> no longer imported
		if (rv.hasKey(NavigatorKey.Imported)) {
			rv.removeKey(NavigatorKey.Imported);
		}
		
		// Regis -> use the registration of the map aligned to
		Integer regis = registeredMap.getRegistration().getRegis();
		rv.addNavigatorField(NavigatorKey.Regis, regis);
		
		// DrawnID -> use the MapID of the map aligned to
		Integer drawnId = registeredMap.getRegistration().getRegisterMapId();
		rv.addNavigatorField(NavigatorKey.DrawnID, drawnId);
		
		return rv;
	}
	
	// TODO: may be a way to hide this implementation inside the IMap itself.
	private GenericItem getRegisteredMapItem(GenericItem item, IMap registeredMap) {
		GenericItem rv = item.clone();
		
		// StageXYZ -> similar to above
		if (rv.hasKey(NavigatorKey.StageXYZ)) {
			Vector3<Float> pos = (Vector3<Float>) rv.getValue(NavigatorKey.StageXYZ);
			
			// TODO: workaround for multiplication.
			float pos_z = pos.z;
			pos.z = 1.0f;
			// update the StageXYZ to a new StageXYZ using inv(AffineMatrix).
			double [][] affineMatrix = registeredMap.getRegistration().getStageMatrix();
			pos = MatrixMath.multiply(affineMatrix, pos);
			pos.z = registeredMap.getRegistration().getStageZ();
			
			rv.addNavigatorField(NavigatorKey.StageXYZ, pos);
			
			// PtsX, PtsY
			processPtsXY(rv, affineMatrix);
		}
		
		// Updated MapScaleMat
		rv.addNavigatorField(NavigatorKey.MapScaleMat, registeredMap.getMapScaleMat(true));

		// OrigReg -> this is the regis before applying
		if (rv.hasKey(NavigatorKey.Regis)) {
			rv.addNavigatorField(NavigatorKey.OrigReg, rv.getValue(NavigatorKey.Regis));
		}
		
		// Regis -> update the registration.
		Integer regis = (Integer) registeredMap.getRegistration().getRegis();
		rv.addNavigatorField(NavigatorKey.Regis, regis);
		
		// Imported -> no longer imported
		rv.addNavigatorField(NavigatorKey.Imported, -1);
		
		// RegisteredToID -> this is the map it's registered to
		rv.addNavigatorField(NavigatorKey.RegisteredToID, registeredMap.getRegistration().getRegisterMapId());
		
		return rv;
	}
	
	/**
	 * This will update-in-place the PtsX and PtsY values of the GenericItem based on the matrix.
	 * @param item
	 * @param registeredMap
	 * @param mStage
	 */
	private void processPtsXY(GenericItem item, double [][]mStage) {
		if (item.hasKey(NavigatorKey.PtsX) && item.hasKey(NavigatorKey.PtsY)) {
			FloatList PtsX = (FloatList) item.getValue(NavigatorKey.PtsX);
			FloatList PtsY = (FloatList) item.getValue(NavigatorKey.PtsY);
			
			if (PtsX == null || PtsY == null || PtsX.getValues().size() != PtsY.getValues().size()) {
				throw new IllegalArgumentException("Invalid lengths of PtsX and PtsY values.");
			}
			
			// Convert PtsX and PtsY values into 3D points
			List<Vector3<Float>> PtsVector = new ArrayList<Vector3<Float>>();
			// Create and multiple each point by the matrix
			for (int i = 0; i < PtsX.getValues().size(); i++) {
				Vector3<Float> pt = new Vector3<Float>();
				pt.x = PtsX.getValues().get(i);
				pt.y = PtsY.getValues().get(i);
				pt.z = 1f;
				Vector3<Float> transformedPt = MatrixMath.multiply(mStage, pt);
				PtsVector.add(transformedPt);
			}
			
			FloatList PtsX2 = new FloatList();
			FloatList PtsY2 = new FloatList();
			// Rebuild back the PtsX and PtsY NavigatorKey pairs in the item.
			for (Vector3<Float> pt : PtsVector) {
				PtsX2.getValues().add(pt.x);
				PtsY2.getValues().add(pt.y);
			}
			item.addNavigatorField(NavigatorKey.PtsX, PtsX2);
			item.addNavigatorField(NavigatorKey.PtsY, PtsY2);
		}
	}
	
	/**
	 * After deserialization, this should be called to restore IMap references.
	 */
	public void relinkMapData() {
		for (IPositionDataset pos : m_positionData) {
			for (IMap map : m_maps) {
				if (pos.getMapId() == map.getId()) {
					pos.setMap(map);
					break;
				}
			}
		}
	}
	
	/**
	 * Obtain a unique GroupID.
	 * @return
	 */
	public int getUniqueGroupID() {
		Set<Integer> takenIDs = new HashSet<Integer>();
		
		int rv = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
		
		for (IPositionDataset pos : m_positionData) {
			takenIDs.add(pos.getGroupID());
		}
		
		// Repeats until gets an untaken unique ID.
		while (takenIDs.contains(rv)) {
			rv = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
		}
		
		return rv;
	}
}
