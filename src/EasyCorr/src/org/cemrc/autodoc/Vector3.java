package org.cemrc.autodoc;

import java.io.Serializable;

public class Vector3<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public T x;
	public T y;
	public T z;
	  
	public Vector3 () {
	    x = null;
	    y = null;
	    z = null;
	}
	  
	public Vector3 (T x, T y, T z) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}
	
	@Override
	public String toString() {
		return x.toString() + " " + y.toString() + " " + z.toString();
	}
}