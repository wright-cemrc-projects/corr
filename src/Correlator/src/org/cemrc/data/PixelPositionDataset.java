package org.cemrc.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.cemrc.autodoc.FloatList;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;

/**
 * Get the logic from 
 * @author larso
 *
 */
@XmlRootElement(name="pixelPositionDataset")
public class PixelPositionDataset extends AbstractPositionDataset implements IPositionDataset {
	
	@XmlElement(name="drawnID")
	private Integer m_drawnID = new Integer(0);
	@XmlElement(name="regisID")
	private Integer m_regisID = new Integer(0);
	@XmlElement(name="imported")
	private Integer m_imported = new Integer(1);
	@XmlElement(name="backlash")
	private Vector2<Float> m_backlash = new Vector2<Float>(0f, 0f);
	@XmlElement(name="positions")
	private List<Vector2<Float>> m_pixelPositions = new ArrayList<Vector2<Float>>();

	/**
	 * Helper method to build the converter.
	 * @param id
	 * @return
	 */
	public PixelPositionDataset setDrawnID(int id) {
		m_drawnID = id;
		return this;
	}
	
	/**
	 * Set the regis ID to use.
	 * @param id
	 * @return
	 */
	public PixelPositionDataset setRegisID(int id) {
		m_regisID = id;
		return this;
	}
	
	/**
	 * Set whether imported (1) or not (0)
	 * @param imported
	 * @return
	 */
	public PixelPositionDataset setImported(int imported) {
		m_imported = imported;
		return this;
	}
	
	/**
	 * Set the backlashXY values
	 * @param backlash
	 * @return
	 */
	public PixelPositionDataset setBacklash(Vector2<Float> backlash) {
		m_backlash = backlash;
		return this;
	}
	
	/**
	 * Helpr method to set the pixel positions
	 * @param positions
	 * @return
	 */
	public PixelPositionDataset setPixelPositions(List<Vector2<Float>> positions) {
		m_pixelPositions = positions;
		return this;
	}
	
	/**
	 * Get the stage positions conversion from the pixel positions;
	 */
	@Override
	public List<Vector2<Float>> getStagePositions(boolean useRegistration) {
		
		if (getMap() == null) {
			throw new IllegalArgumentException("No parent map set.");
		}
		
		List<Vector2<Float>> stagePositions = new ArrayList<Vector2<Float>>();
		
		for (Vector2<Float> position : m_pixelPositions) {
			Vector2<Float> stagePos = getMap().getStageFromPixel(position, useRegistration);
			
			// Needed for TEM maps with imported positions.
			Vector2<Float> RawStageXY = getMap().getRawStageXY();
			stagePos.x += RawStageXY.x;
			stagePos.y += RawStageXY.y;
			stagePositions.add(stagePos);
		}
		
		return stagePositions;
	}
	
	@Override
	public List<GenericItem> getAutodocItems() {
		int item = 1;

		boolean useRegistration = false;
		List<Vector2<Float>> stagePositions = getStagePositions(useRegistration);

		// Create a .nav prototype Item
		GenericItem genericItem = new GenericItem();
		// Set as a Point item type.
		genericItem.addNavigatorField(NavigatorKey.Type, 0);
		genericItem.addNavigatorField(NavigatorKey.NumPts, 1);
		// Set provided values.
		genericItem.addNavigatorField(NavigatorKey.Color, getColor().ordinal());
		
		if (m_drawnID != null) {
			genericItem.addNavigatorField(NavigatorKey.DrawnID, m_drawnID);
		}
		if (m_regisID != null) {
			genericItem.addNavigatorField(NavigatorKey.Regis, m_regisID);
		}
		if (m_backlash != null) {
			genericItem.addNavigatorField(NavigatorKey.BklshXY, m_backlash);
		}
		if (m_imported != null) {
			genericItem.addNavigatorField(NavigatorKey.Imported, m_imported);
		}
		
		// Renumber
		List<GenericItem> outputItems = buildItems(genericItem, stagePositions);
		for (GenericItem i : outputItems) {
			i.setName(Integer.toString(item++));
		}
		
		return outputItems;
	}
	
	/**
	 * Build GenericItems from a list of StagePositions.
	 * @param template
	 * @param stagePositions
	 * @return
	 */
	private List<GenericItem> buildItems(GenericItem template, List<Vector2<Float>> stagePositions) {
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		for (Vector2<Float> position : stagePositions) {
			GenericItem item = template.clone();
			FloatList ptsX = new FloatList(); ptsX.getValues().add(position.x);
			item.addNavigatorField(NavigatorKey.PtsX, ptsX);
			FloatList ptsY = new FloatList(); ptsY.getValues().add(position.y);
			item.addNavigatorField(NavigatorKey.PtsY, ptsY);
			item.addNavigatorField(NavigatorKey.StageXYZ, new Vector3<Float>(position.x, position.y, 0f));
			rv.add(item);
		}
		
		return rv;
	}
	
	/**
	 * Adjust the pixel position origin to be relative to center of the image.
	 * @param positions
	 * @param mapwidth
	 * @param mapheight
	 * @return
	 */
	private List<Vector2<Float>> movePositions(List<Vector2<Float>> positions, int mapwidth, int mapheight) {
		List<Vector2<Float>> rv = new ArrayList<Vector2<Float>>();
		
		float halfWidth = (float) mapwidth / 2.0f;
		float halfHeight = (float) mapheight / 2.0f;
		
		for (Vector2<Float> position : positions) {
			rv.add(new Vector2<Float>(position.x - halfWidth, position.y - halfHeight));
		}
		
		return rv;
	}

	@Override
	public int getMapId() {
		return m_drawnID;
	}
	
	public void setMapId(int id) {
		m_drawnID = id;
	}

	@Override
	public List<Vector2<Float>> getPixelPositions() {
		return m_pixelPositions;
	}

	@Override
	public int getNumberPositions() {
		return m_pixelPositions.size();
	}
	
	@Override
	public void addPixelPosition(double x, double y) {
		m_pixelPositions.add(new Vector2<Float>((float) x, (float) y));
	}
	
	@Override
	public void removePixelPositionNear(double x, double y, double near) {
		List<Vector2<Float>> updateList = new ArrayList<Vector2<Float>>();
		
		double near2 = near * near;
		
		for (Vector2<Float> position : m_pixelPositions) {
			double delta = (position.x - x) * (position.x - x) + (position.y - y) * (position.y - y);
			if (delta > near2) {
				updateList.add(position);
			}
		}
		
		m_pixelPositions = updateList;
	}
}
