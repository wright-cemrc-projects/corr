package org.cemrc.easycorr.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javafx.scene.image.Image;

/**
 * A parser for SerialEM (.st/.mrc) file format.
 * @author mrlarson2
 *
 */
public class ReadMRC {

	/**
	 * Parse the SerialEM (.st) file format into an Image.
	 * This is a variant of the MRC file format.
	 * 
	 * @param file
	 * @return
	 */
	public static Image parseSerialEM(File file) {
		Image rv = null;
		
		// TODO: this is a basic example for parsing a binary image format.
		// With the MRC we would parse the 1024 byte header
		// -> Get the correct dimensions, etc
		// -> Use this to create Image correctly.
		// -> Not sure how to handle mode and multiple layers
		
		try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
			int version = in.readInt();
			byte type = in.readByte();
			int pixelSize = in.readInt();
			
			byte[] tempId = new byte[pixelSize];
			in.read(tempId, 0, pixelSize);
			
			// TODO: steps here to convert byte[] into Image
			rv = new Image(new ByteArrayInputStream(tempId));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return rv;
	}
	
}
