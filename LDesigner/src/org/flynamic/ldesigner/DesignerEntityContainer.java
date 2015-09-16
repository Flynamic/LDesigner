package org.flynamic.ldesigner;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.flynamic.ldesigner.InfoPane.Inspector;
import org.flynamic.ldesigner.ObjectsPane.Group;
import org.flynamic.ldesigner.util.ReferenceUnit;

public abstract class DesignerEntityContainer<T extends DesignerEntity> implements Inspector {
	private DesignerPane pane;
	private ReferenceUnit unit;
	
	private LinkedList<DropListener> dropListeners = new LinkedList<>();
	
	public DesignerEntityContainer() {
		
	}
	
	public abstract Dimension getDimensions();
	
	/**
	 * Adds a drop listener to the designer entity. When the user drops something into the designer pane, all listeners are informed. 
	 * @param a DesignerEntity DropListener
	 */
	public void addDropListener(DropListener listener) {
		this.getDropListeners().add(listener);
	}
	
	/**
	 * Repaint the designer pane.
	 */
	public void repaint() {
		this.getPane().repaint();
	}
	
	/**
	 * Select a designer entity.
	 * @param entity
	 */
	public void select(T entity) {
		this.getPane().select(entity);
	}
	
	/**
	 * Return all entities that should show up in the designer as entities.
	 * @return
	 */
	public abstract LinkedList<T> getEntities();
	
	/**
	 * Return groups that contain certain sets of entity classes that can be added to the designer pane by the user.
	 * @return
	 */
	public abstract LinkedList<Group> getEntityClasses();
	
	/**
	 * Set the entities.
	 * @param entities
	 */
	public abstract void setEntities(LinkedList<T> entities);
	
	/**
	 * Called when an entity is added.
	 * @param entity
	 */
	public abstract void addEntity(T entity);
	/**
	 * Called when an entity is removed.
	 * @param entity
	 */
	public abstract void removeEntity(T entity);
	/**
	 * Called when an entity is cloned.
	 * @param entity
	 */
	public abstract void cloneEntity(T entity);
	
	/**
	 * Render the container in the designer pane. Entities should be rendered here. It is up to the container if and how to display its entities.
	 * Any outline for editing purposes is rendered in the DesignerPane seperately.
	 * @param g
	 */
	public abstract void renderContainer(Graphics2D g);
	
	public abstract void load(File importFile);
	
	public abstract void export(File exportFile);
	
	public abstract String getFileExtension();
	
	/**
	 * The name displayed in the inspector.
	 */
	@Override
	public abstract String getInspectorName();
	
	/**
	 * Tabs and properties that are edited through the inspector.
	 */
	@Override
	public abstract LinkedList<Tab> getTabs();
	
	/**
	 * Save properties that are edited through the inspector.
	 */
	@Override
	public abstract void setProperties(Tab tab, LinkedHashMap<String, Object> properties);
	
	
	public ReferenceUnit getUnit() {
		return unit;
	}
	
	public void setUnit(ReferenceUnit unit) {
		this.unit = unit;
	}
	
	/**
	 * @return the dropListeners
	 */
	public LinkedList<DropListener> getDropListeners() {
		return dropListeners;
	}

	/**
	 * @param dropListeners the dropListeners to set
	 */
	public void setDropListeners(LinkedList<DropListener> dropListeners) {
		this.dropListeners = dropListeners;
	}

	/**
	 * @return the pane
	 */
	public DesignerPane getPane() {
		return pane;
	}

	/**
	 * @param pane the pane to set
	 */
	public void setPane(DesignerPane pane) {
		this.pane = pane;
	}

	public static interface DropListener {
		public void dropFiles(List<File> files, Point location);
	}
}