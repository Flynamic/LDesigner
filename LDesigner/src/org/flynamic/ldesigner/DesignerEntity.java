package org.flynamic.ldesigner;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.flynamic.ldesigner.InfoPane.Inspector;
import org.flynamic.ldesigner.util.ReferenceUnit;

public abstract class DesignerEntity implements Inspector {
	protected ReferenceUnit unit;
	protected double rotation = 0.0;
	
	protected boolean editMode = false;
	protected boolean visible = true;
	
	private boolean removable = true;
	private boolean cloneable = true;
	private boolean editable = true;
	private boolean resizable = true;
	private boolean anchorEditable = true;
	
	private boolean keepProportions = false;
	
	public static final ImageIcon icon = new ImageIcon();
	public static final String name = "DesignerEntity";
	
	public DesignerEntity() {
		this.unit = new ReferenceUnit(new ReferenceUnit(0, 0, 100, 100), 0, 0, 0.1, 0.1);
	}
	
	public ReferenceUnit getUnit() {
		return unit;
	}
	
	public void setUnit(ReferenceUnit unit) {
		this.unit = unit;
	}
				
	public Path2D getRealPath() {
		Rectangle2D r = new Rectangle2D.Double(getUnit().getX(), getUnit().getY(), getUnit().getWidth(), getUnit().getHeight());
		Path2D path = new Path2D.Double(r);
		AffineTransform t = new AffineTransform();
		t.rotate(Math.toRadians(getRotation()), getUnit().getX() + unit.getAnchor().getX() * getUnit().getWidth(), getUnit().getY() + unit.getAnchor().getY() * getUnit().getHeight());
	    path.transform(t);
		return path;
	}
	
	public void wasCreated() {
		
	}
	
	public abstract void render(Graphics2D g);
	
	public ImageIcon getMiniatureIcon(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.scale(width/getUnit().getWidth(), height/getUnit().getHeight());
        render(g);
    	ImageIcon icon = new ImageIcon(image);
    	return icon;
	}
	
	public boolean getEditMode() {
		return editMode;
	}
	
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}
	
	@Override
	public abstract DesignerEntity clone();
	
	public void move(double dx, double dy) {
		this.getUnit().setX(getUnit().getX() + dx);
		this.getUnit().setY(getUnit().getY() + dy);
	}

	/**
	 * @return the rotation
	 */
	public double getRotation() {
		return rotation;
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return the removable
	 */
	public boolean isRemovable() {
		return removable;
	}

	/**
	 * @param removable the removable to set
	 */
	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	/**
	 * @return the cloneable
	 */
	public boolean isCloneable() {
		return cloneable;
	}

	/**
	 * @param cloneable the cloneable to set
	 */
	public void setCloneable(boolean cloneable) {
		this.cloneable = cloneable;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @return the resizable
	 */
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * @param resizable the resizable to set
	 */
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the keepProportions
	 */
	public boolean keepsProportions() {
		return keepProportions;
	}

	/**
	 * @param keepProportions the keepProportions to set
	 */
	public void setKeepProportions(boolean keepProportions) {
		this.keepProportions = keepProportions;
	}

	/**
	 * @return the anchorEditable
	 */
	public boolean isAnchorEditable() {
		return anchorEditable;
	}

	/**
	 * @param anchorEditable the anchorEditable to set
	 */
	public void setAnchorEditable(boolean anchorEditable) {
		this.anchorEditable = anchorEditable;
	}
}