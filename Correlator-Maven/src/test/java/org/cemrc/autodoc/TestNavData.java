package org.cemrc.autodoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cemrc.data.NavData;
import org.junit.Test;

public class TestNavData {

	@Test
	public void testParseNav() {
		
		InputStream inputStream = getClass()
				.getClassLoader().getResourceAsStream("clem.nav");
		
		assertNotNull(inputStream);
		
		try {
			// Deserialize the autodoc from text file.
			List<GenericItem> docItems = AutodocParser.parse(inputStream);
			
			// Check for expected records.
			assertEquals(docItems.size(), 13);			
		
			// Get a NavData from the flat autodoc list.
			NavData navData = new NavData();
			navData.mergeAutodoc(docItems, null);
			
			// Rebuild the autodoc items.
			List<GenericItem> restoredItems = navData.getAutodoc();
			
			// Check equivalence
			assertEquals(restoredItems.size(), docItems.size());
			
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	
}
