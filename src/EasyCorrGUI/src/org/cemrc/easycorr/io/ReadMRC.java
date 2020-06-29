package org.cemrc.easycorr.io;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
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
	
	private static BufferedImage getGrayscale(int width, int height, byte[] buffer, int bitwidth) {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		int[] nBits = { bitwidth };
		ColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		SampleModel sm = cm.createCompatibleSampleModel(width, height);
		DataBufferByte db = new DataBufferByte(buffer, width * height);
		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		BufferedImage result = new BufferedImage(cm, raster, false, null);
		return result;
	}
	
	/**
	 * Parse the SerialEM (.st) file format into an Image.
	 * This is a variant of the MRC file format.
	 * 
	 * @param file
	 * @return
	 */
	public static Image parseSerialEM(File file) {
		Image rv = null;
		
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
			byte [] flippedByteBuffer = new byte[(int)imageByteSize];
			
			// Read the expected image data
			in.read(imageByteBuffer);
			
			// Flip the byte order
			for (int i =0; i < imageByteSize; i+=modeBytes) {
				for (int j = 0; j < modeBytes; j++) {
					flippedByteBuffer[i+j] = imageByteBuffer[i + (modeBytes - j - 1)];
				}
			}
			
			// TODO:
			// Flipping the byte-order (little endian) may be necessary
			// However the below is still likely incorrectly only using 1 byte, not 2 or 4 bytes.
			// Another idea might be to create an Int array and correctly handle the bytes to make a grayscale
			// with the correct dynamic range?
			
			// How to convert 8-bit grayscale in a bytearray to JavaFX Image?
			BufferedImage image = new BufferedImage(nx, ny, BufferedImage.TYPE_BYTE_GRAY);
			image.getRaster().setDataElements(0,  0, nx, ny, imageByteBuffer);
			
			ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
			WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(flippedByteBuffer, imageByteBuffer.length), nx, ny, nx*modeBytes, modeBytes, new int[] {0}, null);
			BufferedImage bImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
			
			rv = SwingFXUtils.toFXImage(bImage, null);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return rv;
	}
	
}
