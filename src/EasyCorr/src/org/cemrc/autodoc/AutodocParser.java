package org.cemrc.autodoc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for parsing autodoc text files into GenericItems.
 * @author larso
 *
 */
public class AutodocParser {
	
	enum States {Begin, HeaderFound, Item, Done };
	
	/**
	 * Parse a file into a list of autodoc items.
	 * @param f
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static List<GenericItem> parse(File f) throws FileNotFoundException, IOException {
		try (BufferedInputStream ioStream = new BufferedInputStream(new FileInputStream(f))) {
			return parse(ioStream);
		}
	}
	
	/**
	 * Parse a file into a list of autodoc items.
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	public static List<GenericItem> parse(InputStream input) throws IOException {
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		States currentState = States.Begin;
		GenericItem current = null;
		 
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (reader.ready()) {
			String line = reader.readLine();
			
			if (line.startsWith("AdocVersion")) {
				currentState = States.HeaderFound;
				continue;
			}
			
			line = line.trim();
			
			if (line.isEmpty()) {
				continue;
			}
			
			if (line.startsWith("[Item")) {
				// Start a new GenericItem
				if (current != null) {
					rv.add(current);
				}
				
				current = new GenericItem();
				
				String[] parts= line.split("=");
				if (parts.length > 1) {
					String namePartRaw = parts[1].replace("]", "");
					String name = namePartRaw.trim();
					current.setName(name);
				}
				
				currentState = States.Item;
				continue;
			}
			
			if (currentState == States.Item) {
				// Parse a field and add it to get 
				String[] parts = line.split("=");
				
				try {
					if (parts.length > 1) {
						NavigatorKey key = NavigatorKey.fromString(parts[0].trim());
						String rawValue = parts[1].trim();
						
						// TODO: type cast to the correct type.
						switch (key.getType()) {
						case Integer:
							current.addNavigatorField(key, Integer.parseInt(rawValue));
							break;
						case Float:
							current.addNavigatorField(key, Float.parseFloat(rawValue));
							break;
						case Double:
							current.addNavigatorField(key, Double.parseDouble(rawValue));
							break;
						case TwoFloat:
							String[] twoParts = rawValue.split("\\s+");
							if (twoParts.length > 1) {
								current.addNavigatorField(key, new Vector2<Float>(Float.parseFloat(twoParts[0]), Float.parseFloat(twoParts[1])));
							}
							break;
						case FourFloat:
							String[] fourParts = rawValue.split("\\s+");
							if (fourParts.length > 3) {
								current.addNavigatorField(key, 
										new Vector4<Float>(Float.parseFloat(fourParts[0]),
										Float.parseFloat(fourParts[1]),
										Float.parseFloat(fourParts[2]),
										Float.parseFloat(fourParts[3])));
							}
							break;
						case TripleFloat:
							String[] floatParts = rawValue.split("\\s+");
							if (floatParts.length > 2) {
								current.addNavigatorField(key, new Vector3<Float>(Float.parseFloat(floatParts[0]), Float.parseFloat(floatParts[1]), Float.parseFloat(floatParts[2])));
							}
							break;
						case TwoInteger:
							String[] intParts = rawValue.split("\\s+");
							if (intParts.length > 1) {
								current.addNavigatorField(key, new Vector2<Integer>(Integer.parseInt(intParts[0]), Integer.parseInt(intParts[1])));
							}
							break;
						case FloatList:
							current.addNavigatorField(key, new FloatList(rawValue));
							break;
						case String:
							current.addNavigatorField(key, rawValue);
							break;
						default:
							// TODO: not implemented field yet.
							break;
						}
					}
				} catch (NumberFormatException e) {
					// TODO: write to error log.
				}
			}
		}
		
		// Clean-up
		if (current != null) {
			rv.add(current);
		}
		
		return rv;
	}
}
