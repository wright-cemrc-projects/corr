package org.cemrc.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This represents an active document or project in the data model.
 * 
 * @author larso
 *
 */
@XmlRootElement(name="document")
public class CorrelatorDocument {

	// Dirt describes if a save is needed on exit.
	@XmlTransient
	boolean m_dirt = false;
	
    // Data
	@XmlElement(name="navdata")
    private NavData m_data = new NavData();
    
	/**
	 * Reset the dirt state
	 */
	public void clearDirt() {
		m_dirt = false;
	}
	
	/**
	 * Is the document dirty?
	 * @return
	 */
	public boolean isDirty() {
		return m_dirt;
	}
	
	/**
	 * Set's that document has changed and cause event.
	 * @param dirty
	 */
	public void setDirt(boolean dirty) {
		m_dirt = dirty;
	}
	
    /**
     * Get the data.
     * @return
     */
    public NavData getData() {
    	return m_data;
    }
    
    /**
     * Set the data.
     * @param data
     * @return
     */
    public void setData(NavData data) {
    	m_data = data;
    }
    
    /**
     * Serialize a document to disk.
     * @param doc
     * @param file
     * @throws JAXBException
     * @throws FileNotFoundException
     */
	public static void serialize(CorrelatorDocument doc, File file) throws JAXBException, FileNotFoundException {
		JAXBContext contextObj = JAXBContext.newInstance(CorrelatorDocument.class);  
	
	    Marshaller marshallerObj = contextObj.createMarshaller();  
	    
	    /* JAXB 2.0 solution to excessive namespace schema declarations.
	    marshallerObj.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String[] getPreDeclaredNamespaceUris() {
                return new String[] { 
                    XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
                };
            }

            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI))
                    return "xsi";
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
                    return "xs";
                if (namespaceUri.equals(WellKnownNamespace.XML_MIME_URI))
                    return "xmime";
                return suggestion;

            }
        });
        */
	    
	    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);  
	    marshallerObj.marshal(doc, new FileOutputStream(file));  
	}
	
	/**
	 * Deserialize a document from disk.
	 * @param file
	 * @return
	 * @throws JAXBException
	 */
	public static CorrelatorDocument deserialize(File file) throws JAXBException {
		JAXBContext contextObj = JAXBContext.newInstance(CorrelatorDocument.class);  
      
        Unmarshaller jaxbUnmarshaller = contextObj.createUnmarshaller();    
        CorrelatorDocument rv = (CorrelatorDocument) jaxbUnmarshaller.unmarshal(file);
        rv.getData().relinkMapData();
        
        return rv;
	}
}
