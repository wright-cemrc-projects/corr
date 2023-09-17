package org.cemrc.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.cemrc.autodoc.NavigatorKey;

public class GenericMapElement {
    @XmlAttribute
    public NavigatorKey key;
    @XmlElement
    public Object value;

    private GenericMapElement() {
    } //Required by JAXB

    public GenericMapElement(NavigatorKey key, Object value) {
        this.key = key;
        this.value = value;
    }
}
