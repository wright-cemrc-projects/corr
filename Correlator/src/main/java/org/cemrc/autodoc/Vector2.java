package org.cemrc.autodoc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic class with two values
 * @author larso
 *
 * @param <T>
 */
public class Vector2<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public T x;
	public T y;
	  
	public Vector2 () {
		x = null;
		y = null;
	}
	  
	public Vector2 (T x, T y) {
		this.x = x;
		this.y = y;
	}
	  
	@Override
	public String toString() {
		return x.toString() + " " + y.toString();
	}

	public static List<Vector2<Double>> toDouble(List<Vector2<Float>> in) {
		List<Vector2<Double>> rv = new ArrayList<Vector2<Double>>();
		
		for (Vector2<Float> f : in) {
			rv.add(new Vector2<Double>((double) f.x, (double) f.y));
		}
		
		return rv;
	}
}
