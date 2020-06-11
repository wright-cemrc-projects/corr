package org.cemrc.autodoc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.cemrc.data.GenericItemAdaptor;

/**
 * A generic autodoc item in .nav file.
 * @author larso
 *
 * Consider how to make this generalized. The parser should build a strongly typed item which
 * has flexibility in what fields it contains.
 *
 */
@XmlSeeAlso({FloatList.class,Vector2.class,Vector3.class,Vector4.class})
public class GenericItem implements Cloneable {

	//@XmlTransient
	@XmlJavaTypeAdapter(GenericItemAdaptor.class)
	private Map<NavigatorKey, Object> m_autodocDictionary = new HashMap<NavigatorKey, Object>();
	
	@XmlElement(name="name")
	private String m_name;
	
	/**
	 * Set the name of the Item
	 * @param name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * Return the name of the Item
	 * @return
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * Return true if this key value pair is populated.
	 * @param key
	 * @return
	 */
	public boolean hasKey(NavigatorKey key) {
		return m_autodocDictionary.containsKey(key);
	}
	
	/**
	 * Remove a field from the GenericItem
	 * @param key
	 */
	public void removeKey(NavigatorKey key) {
		m_autodocDictionary.remove(key);
	}
	
	/**
	 * Get the value for a key value pair.
	 * @param key
	 * @return
	 */
	public Object getValue(NavigatorKey key) {
		return m_autodocDictionary.get(key);
	}
	
	/**
	 * Populate a key/value pair.
	 * @param key
	 * @param value
	 */
	public void addNavigatorField(NavigatorKey key, Object value) {
		m_autodocDictionary.put(key, value);
	}
	
	/**
	 * Writes the values as an Item record in autodoc format.
	 * @param out
	 * @throws IOException 
	 */
	public void write(OutputStream out) throws IOException {
		String itemHeader = "[Item = " + m_name + "]\n";
		
		out.write(itemHeader.getBytes(Charset.forName("UTF-8")));
		
		for (NavigatorKey key : NavigatorKey.values()) {
			if (hasKey(key)) {
				String line = key.toString() + " = " + getValue(key).toString() + "\n";
				out.write(line.getBytes(Charset.forName("UTF-8")));
			}
		}
	}
	
	@Override
	public GenericItem clone() {
		GenericItem rv = new GenericItem();
		
		rv.m_name = this.m_name;
		
		for (NavigatorKey key : m_autodocDictionary.keySet()) {
			// TODO: need a good way to clone this.
			Object value = m_autodocDictionary.get(key);
			rv.m_autodocDictionary.put(key, value);
		}
		
		return rv;
	}
}
