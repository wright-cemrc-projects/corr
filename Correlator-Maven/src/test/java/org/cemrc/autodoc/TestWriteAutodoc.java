package org.cemrc.autodoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestWriteAutodoc {

	@Test
	void testWriteNav() {
	
		// Test writing out an autodoc.
		List<GenericItem> items = new ArrayList<GenericItem>();
		
		GenericItem mapItem = new GenericItem();
		mapItem.setName("Leica");
		mapItem.addNavigatorField(NavigatorKey.Color, "0");
		mapItem.addNavigatorField(NavigatorKey.NumPts, "5");
		mapItem.addNavigatorField(NavigatorKey.StageXYZ, "0 0 0");
		
		items.add(mapItem);
		
		// For collecting the output 
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		try {
			AutodocWriter.write(items, stream);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		// System.out.println(new String(stream.toByteArray()));
		String testString = "AdocVersion = 2.00\n" + 
				"\n" + 
				"[Item = Leica]\n" + 
				"Color = 0\n" + 
				"StageXYZ = 0 0 0\n" + 
				"NumPts = 5\n" +
				"\n";
		
		// Check the output.
		assertEquals(new String(stream.toByteArray()), testString);
	}
	
}
