package org.cemrc.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;

/**
 * Importing from CSV creates this container of PixelPositions that will need to be associated 
 * with a map to transform to stage positions.
 * 
 * @author larso
 *
 */
public class AutodocPositionDataset extends AbstractPositionDataset implements IPositionDataset {
	
	@XmlElement(name="autodocItemList")
	private List<AutodocPosition> m_itemList = new ArrayList<AutodocPosition>();
	// private IMap m_parent;
	@XmlElement(name="mapId")
	private int m_mapId;
	
	/**
	 * Wrapper around a GenericItem that makes working with it better.
	 * @author larso
	 *
	 */
	public class AutodocPosition {
		
		@XmlElement(name="autodocItem")
		private GenericItem m_item;
		
		public AutodocPosition() {}
		public AutodocPosition(GenericItem item) {
			m_item = item;
			
			if (! m_item.hasKey(NavigatorKey.StageXYZ)) {
				m_item.addNavigatorField(NavigatorKey.StageXYZ, new Vector3<Float>(0f,0f,0f));
			}
		}
		
		public Vector2<Float> getStagePosition() {
			Vector3<Float> fullPosition = (Vector3<Float>) m_item.getValue(NavigatorKey.StageXYZ);
			return new Vector2<Float>(fullPosition.x, fullPosition.y);
		}
		
		public Vector2<Float> getPixelPosition(IMap parent, boolean useAffineTransformation) {
			// TODO: With TEM, RawStageXY values need to be subtracted.
			// Otherwise pixel positions will be far off.
			Vector2<Float> rawStageXY = parent.getRawStageXY();
			
			Vector2<Float> stagePosition = getStagePosition();
			Vector2<Float> updateStage = new Vector2<Float>(stagePosition.x - rawStageXY.x, stagePosition.y - rawStageXY.y);
			return parent.getPixelFromStage(updateStage, useAffineTransformation);
		}
		
		public GenericItem getItem() {
			return m_item;
		}
	}
	
	/**
	 * Record stage or pixel positions from GenericItems.
	 * @param positionList
	 */
	public void addItems(List<GenericItem> positionList) {
		for (GenericItem item : positionList) {
			m_itemList.add(new AutodocPosition(item));
		}
	}
	
	/**
	 * Add an autodoc item.
	 * @param item
	 */
	public void addItem(GenericItem item) {
		m_itemList.add(new AutodocPosition(item));
	}

	@Override
	public int getMapId() {
		return m_mapId;
	}
	
	public void setMapId(int id) {
		m_mapId = id;
	}

	@Override
	public List<GenericItem> getAutodocItems() {
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		for (AutodocPosition p : m_itemList) {
			rv.add(p.getItem());
		}
		
		return rv;
	}

	@Override
	public List<Vector2<Float>> getPixelPositions() {
		List<Vector2<Float>> rv = new ArrayList<Vector2<Float>>();

		// TODO: shouldn't this be option?
		boolean useAffineTransform = false;
		
		if (getMap() != null) {
			for (AutodocPosition p : m_itemList) {
				
				rv.add(p.getPixelPosition(getMap(), useAffineTransform));
			}
		}
		
		return rv;
	}

	@Override
	public List<Vector2<Float>> getStagePositions(boolean useRegistration) {
		List<Vector2<Float>> rv = new ArrayList<Vector2<Float>>();
		
		for (AutodocPosition p : m_itemList) {
			rv.add(p.getStagePosition());
		}
		
		return rv;
	}

	@Override
	public int getNumberPositions() {
		return m_itemList.size();
	}
	
	@Override
	public String getName() {
		return "Dataset containing : " + m_itemList.size() + " positions";
	}
	
	@Override
	public void addPixelPosition(double x, double y) {
		
		// TODO
		boolean useAffineTransformation = false;
		
		if (getMap() != null) {
			Vector2<Float> stagePosition = getMap().getStageFromPixel(new Vector2<Float>((float) x, (float) y), useAffineTransformation);
			
			// TODO: this is likely insufficient.
			GenericItem item = new GenericItem();
			item.addNavigatorField(NavigatorKey.StageXYZ, new Vector3<Float>(stagePosition.x, stagePosition.y, 0.0f));
			AutodocPosition p = new AutodocPosition(item);
			m_itemList.add(p);
		}
	}
	
	@Override
	public void removePixelPositionNear(double x, double y, double near) {
		
		List<Vector2<Float>> pixelPositions = getPixelPositions();
		
		// Scan the list, find the pixel position
		// If hit, then remove the matching stage position item.
		double near2 = near * near;
		
		for (Vector2<Float> position : pixelPositions) {
			double delta = (position.x - x) * (position.x - x) + (position.y - y) * (position.y - y);
			if (delta < near2) {
				// hit, remove the matching AutodocPositionItem.
				// TODO
			}
		}
		
	}

}
