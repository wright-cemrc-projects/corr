package org.cemrc.autodoc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="floatList")
public class FloatList {
	
	@XmlElement(name="values")
	private List<Float> m_values = new ArrayList<Float>();
	
	public FloatList() {}
	
	public FloatList(String value) {
		String[] parts = value.split("\\s+");
		for (String part : parts) {
			m_values.add(Float.parseFloat(part));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Float f : m_values) {
			sb.append(f.toString() + " ");
		}
		
		return sb.toString().trim();
	}
	
	public List<Float> getValues() {
		return m_values;
	}
}
