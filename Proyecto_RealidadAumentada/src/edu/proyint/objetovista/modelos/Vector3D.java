package edu.proyint.objetovista.modelos;

import java.io.Serializable;

/**
 * a three dimensional vector
 */
public class Vector3D implements Serializable {
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public float x=0;
	public float y=0;
	public float z=0;
	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}
	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}
	/**
	 * @return the z
	 */
	public float getZ() {
		return z;
	}
	/**
	 * @param z the z to set
	 */
	public void setZ(float z) {
		this.z = z;
	}

}
