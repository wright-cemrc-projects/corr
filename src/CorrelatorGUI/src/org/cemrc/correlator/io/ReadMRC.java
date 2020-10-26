package org.cemrc.correlator.io;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import javafx.embed.swing.SwingFXUtils;
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
	public static BufferedImage parseSerialEM(File file) {
		BufferedImage rv = null;
		
		try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
			
			// MRC header size.
			byte [] buffer = new byte[1024];
			in.read(buffer);
			
			ByteBuffer bb = ByteBuffer.wrap(buffer);
			
			//ByteBuffer bb = new ByteBuffer(1024);
			
			// Set endianness, if needed.
			
			// MRC 2014 file format header description
			// (https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4642651/)
			// https://bio3d.colorado.edu/imod/doc/mrc_format.txt
			
			// MRC header consists of 4 byte words.
			// Endianness is likely a problem here, as well as unsigned vs signed.
			
			Integer nx = Integer.reverseBytes(bb.getInt());
			Integer ny = Integer.reverseBytes(bb.getInt());
			Integer ns = Integer.reverseBytes(bb.getInt());
			Integer nmode = Integer.reverseBytes(bb.getInt());
			
			// Bytes 93-96 contain size for extended header.
			bb.position(93); // 92?
			Integer extendedHeader = Integer.reverseBytes(bb.getInt());
			// Need to see if the extended header is included in the MRC images that parse incorrectly [TODO]
			
			// Determine mode bytes.
			int modeBytes;
			switch (nmode) {
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
			
			int nBits = modeBytes * 4;
			
			// Mode 0 represents 1-byte integers
			// Mode 1 represents 2-byte integers 
			// Mode 2 represents 4-byte real
			// Mode 3 represents 2-byte integer complex number
			// Mode 4 represents 4-byte real complex number
			
			// After 1024 bytes read from input stream,
			// Next determine the image size in bytes and read that in.
			
			long imageByteSize = nx * ny * modeBytes;
			byte [] imageByteBuffer = new byte[(int)imageByteSize]; 
			
			// Read the expected image data
			in.read(imageByteBuffer);
	
			// Normalize to 8-bit greyscale
			byte [] normalizedValues = getNormalizedByteArray(imageByteBuffer, nx, ny, modeBytes, true);
			
			// Create 8-bit greyscale
			ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
			int byteSize = 1;
			WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(normalizedValues, normalizedValues.length), nx, ny, nx*byteSize, 1, new int[] {0}, null);
			BufferedImage bImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
			rv = bImage;
			// rv = SwingFXUtils.toFXImage(bImage, null);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return rv;
	}
	
	private static byte [] getNormalizedByteArray(byte [] bytes, int nx, int ny, int modeBytes, boolean flip) {
		byte [] rv = new byte[nx*ny];
		
		// High value to scale by.
		int maxValue = 1;
		int buffersize = nx * ny * modeBytes;
		
		byte [] buffer = new byte[4];
		
		// Pass #1 to find max sized integer in little-endian
		for (int i =0; i < buffersize; i+=modeBytes) {
			
			for (int j = 0; j < modeBytes; j++) {
				buffer[modeBytes - j - 1] = bytes[i + j];
			}
			
			// Get integer
			int result = ByteBuffer.wrap(buffer).getInt();
			if (result > maxValue) maxValue = result;
		}
		
		// Pass #2 normalize to 8-bit image.
		
		int deltaY = flip ? -1 : 1;
		
		int x = 0;
		int y = flip ? ny - 1 : 0;
		
		for (int i = 0; i < buffersize; i+=modeBytes) {
			
			for (int j = 0; j < modeBytes; j++) {
				buffer[modeBytes - j - 1] = bytes[i + j];
			}
			
			// Get integer
			int result = ByteBuffer.wrap(buffer).getInt();
			byte resultByte = new Integer( (int)(255.0f * ((float) result / (float) maxValue)) ).byteValue();
			
			int index = nx * y + x;
			rv[index] = resultByte;
			
			x++;
			if (x >= nx) {
				x = 0;
				y += deltaY;
			}
		}
		
		return rv;
	}
}
