package org.cemrc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.cemrc.autodoc.AutodocWriter;
import org.cemrc.autodoc.GenericItem;
import org.cemrc.autodoc.NavigatorKey;
import org.cemrc.autodoc.Vector2;
import org.cemrc.autodoc.Vector3;
import org.cemrc.autodoc.Vector4;
import org.cemrc.data.BasicMap;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Java proof-of-function for converting pixel positions to stage positions
 * @author larso
 *
 */
public class Points2Nav {
	
	/**
	 * Parse the CSV into float values.
	 * @param file
	 * @return
	 */
	public static List<Vector2<Float>> parseCSV(File file, int columnX, int columnY) {
		List<Vector2<Float>> rv = new ArrayList<Vector2<Float>>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				String [] parts = line.split(",");
				
				if (parts.length > columnX && parts.length > columnY) {
					try {
						rv.add(new Vector2<Float>(Float.parseFloat(parts[columnX]), Float.parseFloat(parts[columnY])));
					} catch (NumberFormatException e) {
						// TODO : skip header line
					}
				}
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} 
		
		return rv;
	}
	
	/**
	 * Adjust the pixel position origin to be relative to center of the image.
	 * @param positions
	 * @param mapwidth
	 * @param mapheight
	 * @return
	 */
	private static List<Vector2<Float>> movePositions(List<Vector2<Float>> positions, int mapwidth, int mapheight) {
		List<Vector2<Float>> rv = new ArrayList<Vector2<Float>>();
		
		float halfWidth = (float) mapwidth / 2.0f;
		float halfHeight = (float) mapheight / 2.0f;
		
		for (Vector2<Float> position : positions) {
			rv.add(new Vector2<Float>(position.x - halfWidth, position.y - halfHeight));
		}
		
		return rv;
	}
	
	/**
	 * Build GenericItems from a list of StagePositions.
	 * @param template
	 * @param stagePositions
	 * @return
	 */
	private static List<GenericItem> buildItems(GenericItem template, List<Vector2<Float>> stagePositions) {
		List<GenericItem> rv = new ArrayList<GenericItem>();
		
		for (Vector2<Float> position : stagePositions) {
			GenericItem item = template.clone();
			item.addNavigatorField(NavigatorKey.PtsX, position.x);
			item.addNavigatorField(NavigatorKey.PtsY, position.y);
			item.addNavigatorField(NavigatorKey.StageXYZ, new Vector3<Float>(position.x, position.y, 0f));
			rv.add(item);
		}
		
		return rv;
	}
	
	public static void main(String [] args) {
				
		// parse arguments 
		ArgumentParser parser = ArgumentParsers.newFor("Points2Nav").build()
				.defaultHelp(true)
				.description("A tool for converting .csv pixel positions to stage positions as a Navigator(.nav) file");
		
		parser.addArgument("--csv").help("Path to CSV file containing Pixels(x,y) in 2nd and 3rd column.").required(true);

		parser.addArgument("--item").help("Set index for the starting item").required(true).type(Integer.class);
		parser.addArgument("--mapwidth").help("Width of the map image in pixels").required(true).type(Integer.class);
		parser.addArgument("--mapheight").help("Height of the map image in pixels").required(true).type(Integer.class);
		parser.addArgument("--mapscalemat").help("2D matrix, example: X1,X2,Y1,Y2").required(true);

		parser.addArgument("--color").help("Color id to for point Items").required(true).type(Integer.class);
		parser.addArgument("--regis").help("Registration id for point Items").required(true).type(Integer.class);
		parser.addArgument("--drawn").help("DrawnID for the point Items").required(true).type(Integer.class);
		parser.addArgument("--output").help("Name of the output").required(true);
		
		// Optional args
		parser.addArgument("--imported").help("Imported value for Items").type(Integer.class).setDefault(0);
		parser.addArgument("--backlash").help("Backlash XY values").setDefault("0,0");

		
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
		
		// Columns based on 0 numbering, 2nd and 3rd columns are expected to have the X, Y pixel positions.
		int columnPixelX = 1;
		int columnPixelY = 2;
		
		String filename_csv = ns.getString("csv");
		String filename_output = ns.getString("output");
		System.out.println("Input csv: " + filename_csv);
		System.out.println("Output will be the SerialEM .nav file: " + filename_output);
		
		// Check to prevent overwrite.
		Path path = Paths.get(filename_output);
		if (Files.exists(path)) {
		  System.err.println("Output file exists " + filename_output);
		  System.exit(1);
		}
		
		int colorId = ns.getInt("color");
		int drawnId = ns.getInt("drawn");
		int regisId = ns.getInt("regis");
		int mapwidth = ns.getInt("mapwidth");
		int mapheight = ns.getInt("mapheight");
		int item = ns.getInt("item");
		int imported = ns.getInt("imported");
		
		String parts[] = ns.getString("backlash").split("\\w+");
		Vector2<Integer> backlash = new Vector2<Integer>(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		
		// Parse the CSV file into a list of X, Y points.
		File csvFile = new File(filename_csv);
		List<Vector2<Float>> pixelPositions = parseCSV(csvFile, columnPixelX, columnPixelY);
		
		// Retrieve the MapScaleMat
		String msm = ns.getString("mapscalemat").replace(",", " ");
		String msmparts[] = msm.split("\\w+");
		
		Vector4<Float> map = new Vector4<Float>(Float.parseFloat(msmparts[0]), Float.parseFloat(msmparts[1]), Float.parseFloat(msmparts[2]), Float.parseFloat(msmparts[3]));
		
		// Find the Inv(MapScaleMat)
		double matrix [][] = { {map.x, map.y}, {map.z, map.w } };
		
		BasicMap virtualMap = new BasicMap();
		virtualMap.setMapScaleMat(matrix);
		virtualMap.setDimensions(new Vector2<Integer>(mapwidth, mapheight));
		
		List<Vector2<Float>> stagePositions = new ArrayList<Vector2<Float>>();
		for (Vector2<Float> pixel : pixelPositions) {
			// Here is where could eventually use RawScaleXY and affine matrices.
			stagePositions.add(virtualMap.getStageFromPixel(pixel, false));
		}
		
		// Create a .nav prototype Item
		GenericItem genericItem = new GenericItem();
		// Set as a Point item type.
		genericItem.addNavigatorField(NavigatorKey.Type, 0);
		genericItem.addNavigatorField(NavigatorKey.NumPts, 1);
		// Set provided values.
		genericItem.addNavigatorField(NavigatorKey.Color, colorId);
		genericItem.addNavigatorField(NavigatorKey.DrawnID, drawnId);
		genericItem.addNavigatorField(NavigatorKey.Regis, regisId);
		genericItem.addNavigatorField(NavigatorKey.BklshXY, backlash);
		if (imported != 0) {
			genericItem.addNavigatorField(NavigatorKey.Imported, imported);
		}
		
		// Renumber
		List<GenericItem> outputItems = buildItems(genericItem, stagePositions);
		for (GenericItem i : outputItems) {
			i.setName(Integer.toString(item++));
		}
		
		// write to a new file
		try (FileOutputStream os = new FileOutputStream(new File(filename_output))) {
			AutodocWriter.write(outputItems, os);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
