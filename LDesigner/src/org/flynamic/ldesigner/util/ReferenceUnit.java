package org.flynamic.ldesigner.util;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class ReferenceUnit {
	/* Real screen coordinates for drawing of the reference unit */
	private double x = 0;
	private double y = 0;
	private double width = 0;
	private double height = 0;
	
	/* Parent reference unit */
	private ReferenceUnit unit = null;
	
	/* Relative values (relative to parent reference unit) */
	private Point2D anchor = new Point2D.Double(0.5, 0.5);
	private double relativeX = 0;
	private double relativeY = 0;
	private double relativeWidth = 0;
	private double relativeHeight = 0;
	
	private boolean hasAbsoluteSize = false;
	
	public ReferenceUnit(double screenX, double screenY, double screenWidth, double screenHeight) {
		this.x = screenX;
		this.y = screenY;
		this.width = screenWidth;
		this.height = screenHeight;
	}
	
	public ReferenceUnit(ReferenceUnit unit, double relativeX, double relativeY, double relativeWidth, double relativeHeight) {
		this.unit = unit;
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.relativeWidth = relativeWidth;
		this.relativeHeight = relativeHeight;
		this.calculate();
	}
	
	public ReferenceUnit(ReferenceUnit unit) {
		this.unit = unit;
	}
	
	public void calculate() {
		// Calculate real frame
		if (!this.hasAbsoluteSize()) {
			this.width = relativeWidth * unit.width;
			this.height = relativeHeight * unit.height;
		}
		this.x = unit.x + this.unit.getAnchor().getX() * this.unit.getWidth() - (anchor.getX() * this.width) + (relativeX * unit.width);
		this.y = unit.y + this.unit.getAnchor().getY() * this.unit.getHeight() - (anchor.getY() * this.height) - (relativeY * unit.height);
	}
	
	public void setRealFrame(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rectangle getRealFrame() {
		return new Rectangle((int)x, (int)y, (int)width, (int)height);
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
		if (this.unit != null) {
			double referenceX = this.unit.x + this.unit.getAnchor().getX() * this.unit.getWidth();
			double alignedX = (this.x - referenceX + (anchor.getX() * this.width));
			this.relativeX = alignedX / this.unit.getWidth();
		}
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
		if (this.unit != null) {
			double referenceY = this.unit.y + this.unit.getAnchor().getY() * this.unit.getHeight();
			double alignedY = -((this.y - referenceY) + (anchor.getY() * this.height));
			this.relativeY = alignedY / this.unit.getHeight();
		}
	}
	
	/**
	 * Returns the "normalized" x, that is, the absolute x in the parent unit's coordinate system.
	 * @return Normalized x
	 */
	public double getNormalX() {
		return x - unit.x;
	}
	
	/**
	 * Returns the "normalized" y, that is, the absolute y in the parent unit's coordinate system.
	 * @return Normalized y
	 */
	public double getNormalY() {
		return y - unit.y;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
		if (this.unit != null) {
			this.relativeWidth = this.width / unit.width;
		}
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
		if (this.unit != null) {
			this.relativeHeight = this.height / unit.height;
		}
	}

	/**
	 * @return the relativeX
	 */
	public double getRelativeX() {
		return relativeX;
	}

	/**
	 * @param relativeX the relativeX to set
	 */
	public void setRelativeX(double relativeX) {
		this.relativeX = relativeX;
	}

	/**
	 * @return the relativeY
	 */
	public double getRelativeY() {
		return relativeY;
	}

	/**
	 * @param relativeY the relativeY to set
	 */
	public void setRelativeY(double relativeY) {
		this.relativeY = relativeY;
	}

	/**
	 * @return the relativeWidth
	 */
	public double getRelativeWidth() {
		return relativeWidth;
	}

	/**
	 * @param relativeWidth the relativeWidth to set
	 */
	public void setRelativeWidth(double relativeWidth) {
		this.relativeWidth = relativeWidth;
	}

	/**
	 * @return the relativeHeight
	 */
	public double getRelativeHeight() {
		return relativeHeight;
	}

	/**
	 * @param relativeHeight the relativeHeight to set
	 */
	public void setRelativeHeight(double relativeHeight) {
		this.relativeHeight = relativeHeight;
	}
	
	public ReferenceUnit getUnit() {
		return unit;
	}

	/**
	 * @return the anchor
	 */
	public Point2D getAnchor() {
		return anchor;
	}

	/**
	 * @param anchor the anchor to set
	 */
	public void setAnchor(Point2D anchor) {
		this.anchor = anchor;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(ReferenceUnit unit) {
		this.unit = unit;
	}

	/**
	 * @return the hasAbsoluteSize
	 */
	public boolean hasAbsoluteSize() {
		return hasAbsoluteSize;
	}

	/**
	 * @param hasAbsoluteSize the hasAbsoluteSize to set
	 */
	public void setHasAbsoluteSize(boolean hasAbsoluteSize) {
		this.hasAbsoluteSize = hasAbsoluteSize;
	}
}
