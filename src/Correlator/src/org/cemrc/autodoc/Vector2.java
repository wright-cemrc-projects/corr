package org.cemrc.autodoc;

import java.io.Serializable;

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

}
