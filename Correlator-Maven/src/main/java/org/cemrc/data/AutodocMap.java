package org.cemrc.data;

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.autodoc.Vector4;

/**
 * MapData describes the magnification, stage position offset, and more.
 * @author larso
 *
 */
@XmlRootElement(name="autodocMap")
public class AutodocMap extends BasicMap implements IMap {
	
	@XmlElement(name="autodocItem")
	private GenericItem m_item = new GenericItem();
	
	public AutodocMap() {}
	
	public AutodocMap(GenericItem item) {
		m_item = item;
		
		if (m_item.hasKey(NavigatorKey.RawStageXY)) {
			Vector2<Float> rs = (Vector2<Float>) m_item.getValue(NavigatorKey.RawStageXY);
			setRawStageXY(rs);
		} else {
			setRawStageXY(new Vector2<Float>(0f, 0f));
		}
		
		if (m_item.hasKey(NavigatorKey.MapScaleMat)) {
			Vector4<Float> values = (Vector4<Float>) m_item.getValue(NavigatorKey.MapScaleMat);
			setMapScaleMat(new double[][] {{values.x, values.y}, {values.z, values.w}});
		}

		if (m_item.hasKey(NavigatorKey.MapWidthHeight)) {
			Vector2<Integer> values = (Vector2<Integer>) m_item.getValue(NavigatorKey.MapWidthHeight);
			setDimensions(values);
		}
		
		if (m_item.hasKey(NavigatorKey.MapFile)) {
			setImage(new File((String) m_item.getValue(NavigatorKey.MapFile)));
		}
		
		if (m_item.hasKey(NavigatorKey.StageXYZ)) {
			Vector3<Float> stageXYZ = (Vector3<Float>) m_item.getValue(NavigatorKey.StageXYZ);
			setStageZ(stageXYZ.z);
		}
	}
	
	public int getId() {
		if (m_item.hasKey(NavigatorKey.MapID)) {
			return (Integer) m_item.getValue(NavigatorKey.MapID);
		}
		return -1;
	}

	@Override
	public GenericItem getAutoDoc() {
		return m_item;
	}

	@Override
	public String getName() {
		
		if (m_item.hasKey(NavigatorKey.MapFile)) {
			return (String) m_item.getValue(NavigatorKey.MapFile);
		}
		
		return "[unnamed map]";
	}
		
	public Vector2<Integer> getDimensions() {
		return (Vector2<Integer>) m_item.getValue(NavigatorKey.MapWidthHeight);
	}
	
	@Override
	public Integer getRegis() {
		if (m_item.hasKey(NavigatorKey.Regis)) {
			return (Integer) m_item.getValue(NavigatorKey.Regis);
		}
		
		return null;
	}
}
