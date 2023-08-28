package org.cemrc.data;

import java.util.List;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.Vector2;

/**
 * A set of positions on a map.
 * @author larso
 *
 */
public interface IPositionDataset {
	
	public int getId();
	
	/**
	 * What map is this position set associated with?
	 * @return mapId
	 */
	public int getMapId();
	
	/**
	 * Get a list of Autodoc item entries.
	 * @return
	 */
	public List<GenericItem> getAutodocItems();
	
	/**
	 * Get the points as pixel positions.
	 * @return
	 */
	public List<Vector2<Float>> getPixelPositions();
	
	/**
	 * Get the points as stage positions.
	 * @return
	 */
	public List<Vector2<Float>> getStagePositions(boolean useRegistration);
	
	/**
	 * Return a count of positions in the dataset.
	 * @return
	 */
	public int getNumberPositions();
	
	/**
	 * Get a name or pretty string
	 * @return
	 */
	public String getName();
	
	/**
	 * Set a name for the dataset.
	 * @param name  
	 */
	public void setName(String name);
	
	/**
	 * Set the parent map this position set belongs to.
	 * @param map
	 */
	public void setMap(IMap map);
	
	/**
	 * Get the parent map this position set belongs to.
	 * @return
	 */
	public IMap getMap();
	
	/**
	 * Set true to indicate this pixel set is used in registration.
	 */
	public void setIsRegistrationPoints(boolean isRegistered);
	
	/**
	 * Returns whether is an active registeration position set.
	 * @return
	 */
	public boolean isRegisterationPoints();
	
	/**
	 * Add a new pixel position to the dataset
	 * @param x
	 * @param y
	 */
	public void addPixelPosition(double x, double y);
	
	/**
	 * Remove existing pixel positions near this point.
	 * @param x
	 * @param y
	 * @param near
	 */
	public void removePixelPositionNear(double x, double y, double near);
	
	/**
	 * Get the color of the pointset
	 * @return
	 */
	public NavigatorColorEnum getColor();
	
	/**
	 * Set the color of the pointset
	 * @param color
	 */
	public void setColor(NavigatorColorEnum color);
	
	/**
	 * Get the unique GroupID for this dataset
	 * @return
	 */
	public int getGroupID();
	
	/**
	 * Set the unique GroupID for this dataset
	 */
	public void setGroupID(int value);
}
