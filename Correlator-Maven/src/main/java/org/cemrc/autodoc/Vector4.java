package org.cemrc.autodoc;

import java.io.Serializable;

public class Vector4<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public T x;
	public T y;
	public T z;
	public T w;
	  
	public Vector4 () {
	    x = null;
	    y = null;
	    z = null;
	    w = null;
	}
	  
	public Vector4 (T x, T y, T z, T w) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    this.w = w;
	}
	
	@Override
	public String toString() {
		return x.toString() + " " + y.toString() + " " + z.toString() + " " + w.toString();
	}
}
