package org.cemrc.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.cemrc.autodoc.NavigatorKey;

public class GenericItemAdaptor extends XmlAdapter<GenericMapElement[], Map<NavigatorKey, Object>> {

	@Override
	public GenericMapElement[] marshal(Map<NavigatorKey, Object> v) throws Exception {
        GenericMapElement[] mapElements = new GenericMapElement[v.size()];
        int i = 0;
        for (Entry<NavigatorKey, Object> entry : v.entrySet())
            mapElements[i++] = new GenericMapElement(entry.getKey(), entry.getValue());

        return mapElements;
	}

	@Override
	public Map<NavigatorKey, Object> unmarshal(GenericMapElement[] v) throws Exception {
        Map<NavigatorKey, Object> r = new TreeMap<NavigatorKey, Object>();
        for (GenericMapElement mapelement : v)
            r.put(mapelement.key, mapelement.value);
        return r;
	}

}
