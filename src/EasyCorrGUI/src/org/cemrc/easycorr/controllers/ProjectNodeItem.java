package org.cemrc.easycorr.controllers;

import org.cemrc.data.IMap;
import org.cemrc.data.IPositionDataset;

/**
 * An item in a Project TreeView.
 * @author larso
 *
 */
public class ProjectNodeItem {
	
	public enum NodeType { Map, Position, Other };
	public NodeType type;
	
	private IPositionDataset m_dataset;
	private IMap m_map;
	private String m_customName;
	
	public ProjectNodeItem(NodeType type, IPositionDataset dataset, IMap map, String custom) {
		this.type = type;
		this.m_dataset = dataset;
		this.m_map = map;
	}
	
	public String getVisibleName() {
		
		switch (type) {
			case Map:
				return m_map.getName();
			case Position:
				return m_dataset.getName();
			default: 
				return m_customName;
		}
	}
	
	public IMap getMapItem() {
		return m_map;
	}
	
	public IPositionDataset getPositionDataset() {
		return m_dataset;
	}
	
	@Override
	public String toString() {
		return getVisibleName();
	}
}
