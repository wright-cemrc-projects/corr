package org.cemrc.autodoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.cemrc.data.CorrelatorDocument;
import org.cemrc.data.NavData;
import org.junit.Test;

/**
 * Unit test for serializing/deserializing data.
 * @author larso
 *
 */
public class TestCorrelatorDocument {

	private CorrelatorDocument getTestData() {
		CorrelatorDocument rv = new CorrelatorDocument();
		
		InputStream inputStream = getClass()
				.getClassLoader().getResourceAsStream("clem.nav");
		
		assertNotNull(inputStream);
		
		NavData data = rv.getData();
		
		try {
			// Deserialize the autodoc from text file.
			List<GenericItem> docItems = AutodocParser.parse(inputStream);
			// Get a NavData from the flat autodoc list.
			data.mergeAutodoc(docItems, null);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		return rv;
	}
	
	@Test
	public void testSerializeDeserialize() throws IOException {
		
		// Create a document with NavData, Maps, Points
		CorrelatorDocument doc = getTestData();
		
		// Get a temporary test file name.
		Path tempDirWithPrefix = Files.createTempDirectory("temp");
		File saveFile = tempDirWithPrefix.resolve("test.xml").toFile();
		
		// Serialize to disk
		try {
			CorrelatorDocument.serialize(doc, saveFile);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			CorrelatorDocument doc2 = CorrelatorDocument.deserialize(saveFile);
			assertFalse(doc2.isDirty());
			
			// Can we deserialize a map item?
			assertEquals(1, doc2.getData().getMapData().size());
			assertEquals(doc2.getData().getMapData().get(0).getAutoDoc().getValue(NavigatorKey.MapID), 370719668);
		
			// Can we deserialize a positiondata item?
			assertEquals(1, doc2.getData().getPositionData().size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}
	
}
