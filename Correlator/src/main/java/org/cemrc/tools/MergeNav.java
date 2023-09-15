package org.cemrc.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cemrc.autodoc.AutodocParser;
import org.cemrc.autodoc.AutodocWriter;
import org.cemrc.autodoc.FloatList;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Commandline function for combining autodoc information.
 * @author larso
 *
 */
public class MergeNav {
	
	/**
	 * Get an index of the map types.
	 * @param items
	 * @return
	 */
	private static Map<Integer, GenericItem> getMapDictionary(List<GenericItem> items) {
		Map <Integer, GenericItem> rv = new HashMap<Integer, GenericItem>();
		
		for (GenericItem item : items) {
			if (item.getValue(NavigatorKey.Type).equals(2)) {
				rv.put((Integer) item.getValue(NavigatorKey.MapID), item);
			}
		}
		
		return rv;
	}
	
	private static float getFirst(FloatList floats) {
		if (floats.getValues().size() > 0) return floats.getValues().get(0);
		return 0f;
	}
	
	private static List<GenericItem> consecutiveIndex(List<GenericItem> autodoc) {
		Integer index = 0;
		
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		for (GenericItem item : autodoc) {
			GenericItem c = item.clone();
			
			try { 
				Integer currentIndex = Integer.parseInt(c.getName());
				
				if (currentIndex <= index) {
					index++;
					c.setName(index.toString());
				} else {
					index = currentIndex;
				}
			} catch (NumberFormatException e) {}
			
			rv.add(c);
		}
		
		return rv;
	}

	public static void main(String [] args) {
		
		// parse arguments 
		ArgumentParser parser = ArgumentParsers.newFor("MergeNav").build()
				.defaultHelp(true)
				.description("A tool for merging Navigator(.nav) files");
		
		parser.addArgument("--ccd_nav").help("Path to CCD .nav, will be the Regis=1 points").required(true);
		parser.addArgument("--nav").help("Path to an additional .nav to merge").nargs("+");
		parser.addArgument("--output").help("Name of the output file").required(true);
		
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
		
		String ccd_nav = ns.getString("ccd_nav");
		String output_nav = ns.getString("output");
		List<String> append_nav = ns.getList("nav");
		
		// Check to prevent overwrite.
		Path path = Paths.get(output_nav);
		if (Files.exists(path)) {
		  System.err.println("Output file exists " + output_nav);
		  System.exit(1);
		}
		
		// parse the ccd_nav.
		List<GenericItem> ccdItems = new ArrayList<GenericItem>();
		try {
			// Parse the navFile into GenericItem list.
			File navFile = new File(ccd_nav);
			ccdItems = AutodocParser.parse(navFile);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		
		// create a lookup table for MapID -> Map items.
		Map<Integer, GenericItem> mapDictionary = getMapDictionary(ccdItems);
		
		// parse Nav files into NavData instances
		if (append_nav != null) {
			for (String filename : append_nav) {
				File navFile = new File(filename);
				
				try {
					// Parse the navFile into GenericItem list.
					List<GenericItem> items = AutodocParser.parse(navFile);
					
					// Update point items and append to the ccd_nav.
					for (GenericItem item : items) {
						if (item.getValue(NavigatorKey.Type).equals(0)) {
							
							// When adding items into the other GenericItem list, it should clone.
							GenericItem cItem = item.clone();
							
							if (cItem.hasKey(NavigatorKey.DrawnID) && 
									mapDictionary.containsKey(cItem.getValue(NavigatorKey.DrawnID))) {
								
								// Get the map item.
								GenericItem mapItem = mapDictionary.get(cItem.getValue(NavigatorKey.DrawnID));
								
								if (mapItem.hasKey(NavigatorKey.RawStageXY)) {
									Vector2<Float> stageXY = (Vector2<Float>) mapItem.getValue(NavigatorKey.RawStageXY);
									
									float ptsX = getFirst((FloatList) cItem.getValue(NavigatorKey.PtsX)) + stageXY.x;
									float ptsY = getFirst((FloatList) cItem.getValue(NavigatorKey.PtsY)) + stageXY.y;
									
									cItem.addNavigatorField(NavigatorKey.PtsX, ptsX);
									cItem.addNavigatorField(NavigatorKey.PtsY, ptsY);
									
									// Update StageXYZ.	
									Vector3<Float> stageXYZ = (Vector3<Float>) cItem.getValue(NavigatorKey.StageXYZ);
									stageXYZ.x = ptsX;
									stageXYZ.y = ptsY;
									cItem.addNavigatorField(NavigatorKey.StageXYZ, stageXYZ);
								}
							}
										
							// Merge into the output data.
							ccdItems.add(cItem);
						}
					}
					
				} catch (IOException ex) {}
			}
		}
		
		// Make the item numberings consecutive
		List<GenericItem> sanitizedItems = consecutiveIndex(ccdItems);
		
		// write to a new file
		try (FileOutputStream os = new FileOutputStream(new File(output_nav))) {
			AutodocWriter.write(sanitizedItems, os);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
