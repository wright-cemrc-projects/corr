package org.cemrc.easycorr.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.sun.prism.PixelFormat;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

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
		WritableImage rv = null;
		
		// TODO: this is a basic example for parsing a binary image format.
		// With the MRC we would parse the 1024 byte header
		// -> Get the correct dimensions, etc
		// -> Use this to create Image correctly.
		// -> Not sure how to handle mode and multiple layers
		
		try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
			// MRC header size.
			byte [] buffer = new byte[1024];
			in.read(buffer);
			
			ByteBuffer bb = ByteBuffer.wrap(buffer);
			
			// Set endianness, if needed.
			
			// MRC 2014 file format header description
			// (https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4642651/)
			
			// MRC header consists of 4 byte long words.
			
			int numberOfColumns = bb.getInt(); // Long word # 1
			int numberOfRows = bb.getInt(); // Long word #2
			int numberOfSections = bb.getInt(); // Long word #3
			int mode = bb.getInt(); // Long word #4
			
			// Determine mode bytes.
			int modeBytes;
			switch (mode) {
			case 0:
				modeBytes = 1;
				break;
			case 1:
				modeBytes = 2;
				break;
			case 2:
				modeBytes = 4;
				break;
			case 3:
				modeBytes = 2;
				break;
			case 4:
				modeBytes = 4;
				break;
			default:
				modeBytes = 1;
				break;
			}
			
			// Mode 0 represents 1-byte integers
			// Mode 1 represents 2-byte integers 
			// Mode 2 represents 4-byte real
			// Mode 3 represents 2-byte integer complex number
			// Mode 4 represents 4-byte real complex number
			
			// After 1024 bytes read from input stream,
			// Next determine the image size in bytes and read that in.
			
			int imageByteSize = numberOfColumns * numberOfRows * modeBytes;
			byte [] imageByteBuffer = new byte[imageByteSize]; 
			
			// Read the expected image data
			in.read(imageByteBuffer);
			
			rv = new WritableImage(numberOfColumns, numberOfRows);
			PixelWriter pw = rv.getPixelWriter();
			WritablePixelFormat<IntBuffer> pf = WritablePixelFormat.getIntArgbInstance();
			
			// TODO: pause here Matt
			// IntBuffer imageIntBuffer = new IntBuffer(imageByteBuffer);	
			// pw.setPixels(0, 0, numberOfColumns, numberOfRows, pf, buffer, numberOfColumns);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return rv;
	}
	
}
