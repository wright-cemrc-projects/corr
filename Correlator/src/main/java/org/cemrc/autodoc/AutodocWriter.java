package org.cemrc.autodoc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * A class for writing a List of GenericItems back 
 * @author larso
 *
 */
public class AutodocWriter {

	public static void write(List<GenericItem> items, OutputStream out) throws IOException {
		
		String header = "AdocVersion = 2.00\n\n";
		out.write(header.getBytes(Charset.forName("UTF-8")));
		
		for (GenericItem i : items) {
			i.write(out);
			out.write("\n".getBytes(Charset.forName("UTF-8")));
		}
	}
	
	
}
