package org.flynamic.ldesigner;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.flynamic.ldesigner.DesignerEntityContainer.DropListener;
import org.flynamic.ldesigner.util.ReferenceUnit;

public class DesignerPane extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DesignerPanel designerPanel;
    
    public ReferenceUnit panelRect;
    public ReferenceUnit sceneRect;
    
    private DesignerEntityContainer<DesignerEntity> container;
    
    private EntityPicker entityPicker = null;
    
    private boolean absoluteScene = false;
        
    /**
     * Create the panel.
     */
    public DesignerPane() {
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(1334, 750));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
		this.setFocusable(true);
		
        this.panelRect = new ReferenceUnit(0, 0, this.getWidth(), this.getHeight());
        this.sceneRect = new ReferenceUnit(panelRect);
        this.sceneRect.setAnchor(new Point2D.Double(0, 1));
        
        new DropTarget(this, new PaneDropTargetListener());        
    }
    
    public void calculateScene() {
    	double x = 10;
    	double y = 10;
    	double width = 0;
    	double height = 0;
    	if (getEntityContainer() != null) {
        	width = getEntityContainer().getDimensions().getWidth();
        	height = getEntityContainer().getDimensions().getHeight();
    	}
    	
    	int padding = 10;
    	
    	int w = 0, h = 0;
    	
    	if (isAbsoluteScene()) {
    		w = (int) width;
    		h = (int) height;
    	} else {
	    	w = (int) (this.getWidth()-(x + padding));
	    	h = (int) Math.round((height/width)*w);
	    	if (h > this.getHeight()-(y + padding)) {
	    		h = (int) (this.getHeight()-(y + padding));
	    		w = (int) Math.round((width/height)*h);
	    	}
    	}
    	this.sceneRect.setRealFrame(x, y, w, h);
    }
    
    @Override
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D)g;
    	this.calculateScene();
    	g2.setColor(Color.BLACK);
    	g2.draw(sceneRect.getRealFrame());
    	
    	g2.translate(sceneRect.getX(), sceneRect.getY());
    	if (getEntityContainer() != null) {
    		getEntityContainer().renderContainer(g2);
    	}
    	g2.translate(-sceneRect.getX(), -sceneRect.getY());
    	
    	for (DesignerEntity object : getObjects()) {
    		//g2.translate(object.getUnit().getX(), object.getUnit().getY());
    		g2.scale(object.getUnit().getUnit().getWidth(), object.getUnit().getUnit().getHeight());
    		if (getEntityPicker() != null) {
        		g2.scale(1/object.getUnit().getUnit().getWidth(), 1/object.getUnit().getUnit().getHeight());
    			this.renderEntityPicker(g2, object);
        		g2.scale(object.getUnit().getUnit().getWidth(), object.getUnit().getUnit().getHeight());
    		}
    		if (object.editMode) {
        		g2.scale(1/object.getUnit().getUnit().getWidth(), 1/object.getUnit().getUnit().getHeight());
    			this.renderEditMode(g2, object);
        		if (object.isResizable()) {
    				this.renderResizing(g2, object);
    			}
        		g2.scale(object.getUnit().getUnit().getWidth(), object.getUnit().getUnit().getHeight());
    		}
    		g2.scale(1/object.getUnit().getUnit().getWidth(), 1/object.getUnit().getUnit().getHeight());
    		//g2.translate(-object.getUnit().getX(), -object.getUnit().getY());
    	}
    	//g2.translate(-sceneRect.getX(), -sceneRect.getY());
    	if (addingObject != null) {
    		g2.translate(addingObject.getUnit().getX(), addingObject.getUnit().getY());
    		addingObject.render(g2);
    		g2.translate(-addingObject.getUnit().getX(), -addingObject.getUnit().getY());
    	}
    }
    
    private Rectangle2D[][] corners = new Rectangle2D[2][2];
    private Ellipse2D anchor;
    
    /**
     * Rotate point around point.
     * @param point
     * @param angle
     * @param origin
     * @return
     */
	private double[] rotatePoint(double[] point, double angle, double[] origin) {
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		
		// translate point back to origin:
		point[0] -= origin[0];
		point[1] -= origin[1];
		
		// rotate point
		double xnew = point[0] * c - point[1] * s;
		double ynew = point[0] * s + point[1] * c;
		
		// translate point back:
		point[0] = xnew + origin[0];
		point[1] = ynew + origin[1];
		return point;
	}
    
    /**
     * Calculates the relative anchor (e.g. P(0.5, 0.5)) for an object from their absolute frames,
     * taking into account the real path of the object.
     * @param absoluteAnchor
     * @param object
     * @return
     */
    private Point2D getRelativeAnchorFromAbsolute(Point2D absoluteAnchor, DesignerEntity object) {
    	PathIterator iterator = object.getRealPath().getPathIterator(null);
    	final double[] coords=new double[6];
    	while (!iterator.isDone()) {
    		iterator.currentSegment(coords);
    		iterator.next();
    	}
    	double rotation = Math.toRadians(object.getRotation());
    	double[] first = new double[]{coords[0], coords[1]};
    	double[] origin = new double[]{object.getUnit().getX(), object.getUnit().getY()};
    	double[] anch = new double[]{
    			origin[0] + object.getUnit().getAnchor().getX() * object.getUnit().getWidth(),
    			origin[1] + object.getUnit().getAnchor().getY() * object.getUnit().getHeight()};
    	double[] widthPoint = new double[]{origin[0] + object.getUnit().getWidth(), origin[1]};
    	double[] rotatedWithPoint = this.rotatePoint(widthPoint, rotation, anch);
    	
    	return null;
    }
    
    /**
     * Calculates the absolute anchor for an object using its absolute frames and relative anchor,
     * taking into account the real path of the object.
     * @param relativeAnchor
     * @param object
     * @return
     */
    private Point2D getAbsoluteAnchorFromRelative(Point2D relativeAnchor, DesignerEntity object) {
    	return null;
    }
    
    public void renderEntityPicker(Graphics2D g, DesignerEntity object) {
    	if (this.hoveringObject == object) {
    		if (getEntityPicker().acceptsEntity(object)) {
		    	Path2D path = object.getRealPath();
		    	Rectangle2D bounding = path.getBounds2D();
		    	g.setColor(Color.MAGENTA);
		    	g.draw(bounding);
    		} else {
		    	Path2D path = object.getRealPath();
		    	Rectangle2D bounding = path.getBounds2D();
    			double w = object.getUnit().getWidth();
    			double h = object.getUnit().getHeight();
    			g.setColor(Color.RED);
    			g.drawLine((int)(bounding.getX()), (int)(bounding.getY()), 
    	    			(int)(bounding.getX() + w), (int)(bounding.getY() + h));
    	    	g.drawLine((int)(bounding.getX()), (int)(bounding.getY() + h), 
    	    			(int)(bounding.getX() + w), (int)(bounding.getY()));
    		}
    	}
    }
    
    public void renderEditMode(Graphics2D g, DesignerEntity object) {
    	Path2D path = object.getRealPath();
    	Rectangle2D bounding = path.getBounds2D();
    	g.setColor(Color.GREEN);
    	g.draw(bounding);
    }
    
    public void renderResizing(Graphics2D g, DesignerEntity object) {
    	double c = 5;
    	Path2D path = object.getRealPath();
    	Rectangle2D bounding = path.getBounds2D();
    	double w = bounding.getWidth();
    	double h = bounding.getHeight();
    	for (int j = 0; j < corners.length; j++) {
	    	for (int i = 0; i < corners[j].length; i++) {
	    		Rectangle2D corner = new Rectangle2D.Double(
	    				bounding.getX() + (i == 0 ? -c : w+c) - c*i, bounding.getY() + (j == 0 ? -c : h+c) - c*j, c, c);
	    		g.setColor(Color.WHITE);
	    		g.fill(corner);
	    		g.setColor(Color.BLACK);
	    		g.draw(corner);
	    		corners[j][i] = corner;
	    	}
    	}
    	if (object.isAnchorEditable()) {
	    	PathIterator iterator = path.getPathIterator(null);
	    	final double[] coords=new double[6];
	    	while (!iterator.isDone()) {
	    		iterator.currentSegment(coords);
	    		iterator.next();
	    	}
	    	double rotation = Math.toRadians(object.getRotation());
	    	double[] first = new double[]{coords[0], coords[1]};
	    	double[] origin = new double[]{object.getUnit().getX(), object.getUnit().getY()};
	    	double[] anch = new double[]{
	    			origin[0] + object.getUnit().getAnchor().getX() * object.getUnit().getWidth(),
	    			origin[1] + object.getUnit().getAnchor().getY() * object.getUnit().getHeight()};
	    	
	    	double d = 10;
	    	anchor = new Ellipse2D.Double(anch[0] - d/2, 
	    			anch[1] - d/2, d, d);
	    	g.setColor(Color.GREEN);
	    	g.draw(anchor);
    	} else {
    		anchor = null;
    	}
    }
    
    public DesignerEntity addObject(Class<? extends DesignerEntity> objectClass) {
    	DesignerEntity object = createObject(objectClass);
    	addObject(object);
    	return object;
    }
    
    public void addObject(DesignerEntity object) {
    	object = createObject(object);
	    this.getEntityContainer().addEntity(object);
		this.designerPanel.getLayersPane().update();
    	this.repaint();
    }
    
    public DesignerEntity createObject(Class<? extends DesignerEntity> objectClass) {
    	DesignerEntity object;
		try {
			object = objectClass.getConstructor().newInstance();
	    	return createObject(object);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public DesignerEntity createObject(DesignerEntity object) {
    	ReferenceUnit unit = sceneRect;
    	Point2D anch = null;
    	if (object.getUnit() != null)
    		anch = object.getUnit().getAnchor();
		object.getUnit().setUnit(unit);
		if (anch != null)
			object.getUnit().setAnchor(anch);
		object.wasCreated();
		return object;
    }
    
    public void removeObject(DesignerEntity object) {
	    this.getEntityContainer().removeEntity(object);
		this.designerPanel.getLayersPane().update();
    	this.repaint();
    }
    
    public void removeSelected() {
    	if (this.selectedObject != null) {
			this.removeObject(selectedObject);
			this.deselectAll();
		}
    }
    
    public void cloneObject(DesignerEntity object) {
    	DesignerEntity cloned = object.clone();
    	cloned.getUnit().setX(object.getUnit().getX() + 10);
    	cloned.getUnit().setY(object.getUnit().getY() + 10);
    	this.addObject(cloned);
    	this.deselectAll();
    	this.select(cloned);
    }
    
    public void setEditMode(DesignerEntity object, boolean editMode) {
    	object.setEditMode(editMode);
    }
    
    public void select(DesignerEntity object) {
    	for (DesignerEntity entity : getObjects()) {
    		entity.setEditMode(false);
    	}
    	this.designerPanel.getInfoPane().setInspector(object);
		object.setEditMode(true);
		this.selectedObject = object;
		this.designerPanel.getToolsPane().setObject(object);
		this.designerPanel.getLayersPane().select(object);
		this.repaint();
    }
        
    public void deselectAll() {
		this.corners = new Rectangle2D[2][2];
		this.anchor = null;
		this.selectedObject = null;
		this.designerPanel.getInfoPane().setInspector(this.getEntityContainer());
		this.designerPanel.getToolsPane().setObject(null);
		this.designerPanel.getLayersPane().deselectAll();
    	for (DesignerEntity object : getObjects()) {
			setEditMode(object, false);
		}
    }
    
    public DesignerEntity getSelection() {
    	return this.selectedObject;
    }
    
    public void stopAdding() {
		this.designerPanel.getObjectsPane().deselectAll();
		this.addingObject = null;
		this.repaint();
    }
    
    public void placeObject() {
    	addObject(addingObject);
    	this.select(addingObject);
		this.addingObject = null;
		this.stopAdding();
    }
    
    public void removeAdding() {
		addingObject = null;
    }
        
    private void goUp() {
    	getSelection().move(0, -1);
    }
    
    private void goDown() {
    	getSelection().move(0, 1);
    }
    
    private void goRight() {
    	getSelection().move(1, 0);
    }
    
    private void goLeft() {
    	getSelection().move(-1, 0);
    }
        
    public enum Corner {
    	TopLeft, TopRight, BottomLeft, BottomRight
    }
    
    private DesignerEntity selectedObject = null;
    private DesignerEntity hoveringObject = null;
    private DesignerEntity draggingObject = null;
    private DesignerEntity resizingObject = null;
    private Point2D currentPoint = null;
    private Point2D selectedObjectOldPoint = null;
    private Corner corner = null;
    private boolean dragAnchor = false;

    private boolean keepProportions = false;
    
    public DesignerEntity entityAtPosition(Point2D position) {
    	for (DesignerEntity object : getObjects()) {
			object.getUnit().calculate();
			Path2D o = object.getRealPath();
			if (o.contains(position)) {
				return object;
			}
		}
    	return null;
    }
    
	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragAnchor && anchor != null) {
			int x = e.getX();
			int y = e.getY();
			Point2D p = new Point2D.Double(x, y);
			Point2D n = new Point2D.Double(currentPoint.getX(), currentPoint.getY());
			double xd = (p.getX() - n.getX());
			double yd = (p.getY() - n.getY());
			Rectangle2D bounding = selectedObject.getRealPath().getBounds2D();
			
			anchor.setFrame(anchor.getX() + xd, anchor.getY() + yd, anchor.getWidth(), anchor.getHeight());
		
			selectedObject.getUnit().getAnchor().setLocation(
					(anchor.getX() - bounding.getX() + anchor.getWidth()/2) / selectedObject.getUnit().getWidth(), 
					(anchor.getY() - bounding.getY() + anchor.getHeight()/2) / selectedObject.getUnit().getHeight());
			
			selectedObject.getUnit().setX(selectedObjectOldPoint.getX());
			selectedObject.getUnit().setY(selectedObjectOldPoint.getY());
			currentPoint.setLocation(x, y);
			this.repaint();
		}
		if (draggingObject != null && currentPoint != null) {
			int x = e.getX();
			int y = e.getY();
			Point2D p = new Point2D.Double(x, y);
			Point2D n = new Point2D.Double(currentPoint.getX(), currentPoint.getY());
			
			draggingObject.getUnit().setX(draggingObject.getUnit().getX() + (p.getX() - n.getX()));
			draggingObject.getUnit().setY(draggingObject.getUnit().getY() + (p.getY() - n.getY()));
			currentPoint.setLocation(x, y);
			this.repaint();
		}
		if (corner != null && resizingObject != null && currentPoint != null) {
			int x = e.getX();
			int y = e.getY();
			Point2D p = new Point2D.Double(x, y);
			Point2D n = new Point2D.Double(currentPoint.getX(), currentPoint.getY());
			Point2D anch = resizingObject.getUnit().getAnchor();
			
			double dx = p.getX() - n.getX();
			double dy = p.getY() - n.getY();
			
			double ow = resizingObject.getUnit().getWidth();
			double oh = resizingObject.getUnit().getHeight();
			double hprop = oh / ow;
			double wprop = ow / oh;
			
			double nw = 0;
			double nh = 0;
			
			double pdx = ((hprop>wprop) ? wprop : hprop)*dx;
			double pdy = ((hprop>wprop) ? wprop : hprop)*dy;
			
			if (corner == Corner.TopLeft) {
				if (keepsProportions() || resizingObject.keepsProportions()) {
					if (Math.abs(dx) >= Math.abs(dy)) {
						nw = ow - pdx;
						nh = hprop * nw;
					} else {
						nh = oh - pdy;
						nw = wprop * nh;
					}
				} else {
					nw = ow - dx;
					nh = oh - dy;
				}
				resizingObject.move((ow - nw) * anch.getX(), (oh - nh) * anch.getY());
				resizingObject.getUnit().setWidth(nw);
				resizingObject.getUnit().setHeight(nh);
			}
			if (corner == Corner.TopRight) {
				if (keepsProportions() || resizingObject.keepsProportions()) {
					if (Math.abs(dx) >= Math.abs(dy)) {
						nw = ow + pdx;
						nh = hprop * nw;
					} else {
						nh = oh - pdy;
						nw = wprop * nh;
					}
				} else {
					nw = ow + dx;
					nh = oh - dy;
				}
				resizingObject.move(-(ow - nw) * anch.getX(), (oh - nh) * anch.getY());
				resizingObject.getUnit().setWidth(nw);
				resizingObject.getUnit().setHeight(nh);
			}
			if (corner == Corner.BottomLeft) {
				if (keepsProportions() || resizingObject.keepsProportions()) {
					if (Math.abs(dx) >= Math.abs(dy)) {
						nw = ow - pdx;
						nh = hprop * nw;
					} else {
						nh = oh + pdy;
						nw = wprop * nh;
					}
				} else {
					nw = ow - dx;
					nh = oh + dy;
				}
				resizingObject.move((ow - nw) * anch.getX(), -(oh - nh) * anch.getY());
				resizingObject.getUnit().setWidth(nw);
				resizingObject.getUnit().setHeight(nh);
			}
			if (corner == Corner.BottomRight) {
				if (keepsProportions() || resizingObject.keepsProportions()) {
					if (Math.abs(dx) >= Math.abs(dy)) {
						nw = ow + pdx;
						nh = hprop * nw;
					} else {
						nh = oh + pdy;
						nw = wprop * nh;
					}
				} else {
					nw = ow + dx;
					nh = oh + dy;
				}
				resizingObject.move(-(ow - nw) * anch.getX(), -(oh - nh) * anch.getY());
				resizingObject.getUnit().setWidth(nw);
				resizingObject.getUnit().setHeight(nh);
			}
			
			currentPoint.setLocation(x, y);
			this.repaint();
		}
	}
	
	private DesignerEntity addingObject = null;

	@Override
	public void mouseMoved(MouseEvent e) {
		this.hoveringObject = entityAtPosition(e.getPoint());
		if (this.getEntityPicker() != null) {
			this.requestFocus();
			repaint();
			return;
		}
		int x = e.getX();
        int y = e.getY();
        Point2D point = new Point2D.Double(x, y);
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		/* Adding objects */
		if (this.designerPanel.getObjectsPane().selectedObject != null && addingObject == null) {
			addingObject = createObject(this.designerPanel.getObjectsPane().selectedObject);
		} else if (addingObject != null){
			if (this.designerPanel.getObjectsPane().selectedObject == null) {
				removeAdding();
			} else if (this.designerPanel.getObjectsPane().selectedObject != addingObject.getClass()) {
				removeAdding();
				addingObject = createObject(this.designerPanel.getObjectsPane().selectedObject);
			}
		}
		if (addingObject != null) {
			this.requestFocus();
			addingObject.getUnit().calculate();
			addingObject.getUnit().setX(x - addingObject.getUnit().getAnchor().getX() * addingObject.getUnit().getWidth());
			addingObject.getUnit().setY(y - addingObject.getUnit().getAnchor().getY() * addingObject.getUnit().getHeight());
			this.repaint();
		}
		
        for (DesignerEntity object : getObjects()) {
        	object.getUnit().calculate();
			Rectangle r = object.getUnit().getRealFrame();
			for (int i = 0; i < corners.length; i++) {
	        	for (int j = 0; j < corners[i].length; j++) {
	        		Rectangle2D rect = corners[i][j];
	        		if (rect == null) break;
	        		rect = corners[i][j].getFrame();
	        		if (rect.contains(point)) {
	        			int cursor = Cursor.DEFAULT_CURSOR;
	        			if (i == 0) {
	        				if (j == 0) {
	        					cursor = Cursor.NW_RESIZE_CURSOR;
	        				} else {
	        					cursor = Cursor.NE_RESIZE_CURSOR;
	        				}
	        			}
	        			if (i == 1) {
	        				if (j == 0) {
	        					cursor = Cursor.SW_RESIZE_CURSOR;
	        				} else {
	        					cursor = Cursor.SE_RESIZE_CURSOR;
	        				}
	        			}
	        			setCursor(Cursor.getPredefinedCursor(cursor));
	        		}
	        	}
			}
        }
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point2D click = new Point2D.Double(e.getX(), e.getY());
		this.requestFocus();
		
		if (this.getEntityPicker() == null) {
			/* Handling entity selection */
			this.deselectAll();
			if (this.addingObject != null) {
				this.placeObject();
				this.repaint();
				return;
			}
			for (DesignerEntity object : getObjects()) {
				object.getUnit().calculate();
				Path2D o = object.getRealPath();
				this.setEditMode(object, false);
				if (o.contains(click)) {
					this.deselectAll();
					this.select(object);
				}
			}
		} else {
			/* Handling entity picker */
			DesignerEntity selected = null;
			for (DesignerEntity object : getObjects()) {
				object.getUnit().calculate();
				Path2D o = object.getRealPath();
				if (o.contains(click)) {
					selected = object;
				}
			}
			if (selected != null) {
				if (this.getEntityPicker().acceptsEntity(selected)) {
					this.getEntityPicker().entityPicked(selected);
					this.setEntityPicker(null);
				}
			}
		}
		
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (this.getEntityPicker() != null) {
			/* Stopping any other event when an entity picker is active */
			return;
		}
        currentPoint = new Point2D.Double(e.getX(), e.getY());
        for (DesignerEntity object : getObjects()) {
        	object.getUnit().calculate();
			Path2D path = object.getRealPath();
			/* Dragging anchor if anchor is visible / object is selected, and return */
			if (anchor != null) {
				Rectangle2D rect = anchor.getBounds2D();
        		if (rect.contains(currentPoint) && this.selectedObject == object) {
        			dragAnchor = true;
        			this.selectedObjectOldPoint = new Point2D.Double(object.getUnit().getX(), object.getUnit().getY());
        			return;
        		}
			}
			/* Dragging object if point lies within real object path, and return */
			if (path.contains(currentPoint) && (this.selectedObject == null || this.selectedObject == object)) {
				this.draggingObject = object;
				return;
			}
			/* For resizing, find corner that is clicked on */
			for (int i = 0; i < corners.length; i++) {
	        	for (int j = 0; j < corners[i].length; j++) {
	        		Rectangle2D rect = corners[i][j];
	        		if (rect == null) break;
	        		rect = corners[i][j].getBounds2D();
	        		if (rect.contains(currentPoint)) {
	        			this.resizingObject = this.selectedObject;
	        			if (i == 0) {
	        				if (j == 0) {
	        					this.corner = Corner.TopLeft;
	        				} else {
	        					this.corner = Corner.TopRight;
	        				}
	        			}
	        			if (i == 1) {
	        				if (j == 0) {
	        					this.corner = Corner.BottomLeft;
	        				} else {
	        					this.corner = Corner.BottomRight;
	        				}
	        			}
	        		}
	        	}
	        }
        }
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Clear all temporary objects on mouse release */
		this.draggingObject = null;
		this.resizingObject = null;
		this.currentPoint = null;
		this.corner = null;
		this.dragAnchor = false;
		this.selectedObjectOldPoint = null;
		this.anchor = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// nothing ...
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// nothing ...
	}

	/**
	 * @return the container
	 */
	public DesignerEntityContainer<DesignerEntity> getEntityContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	@SuppressWarnings("unchecked")
	public void setEntityContainer(DesignerEntityContainer<? extends DesignerEntity> container) {
		this.container = (DesignerEntityContainer<DesignerEntity>) container;
		this.container.setPane(this);
		this.container.setUnit(sceneRect);
		for (DesignerEntity object : container.getEntities()) {
			object.getUnit().setUnit(sceneRect);
		}
		if (this.designerPanel != null) {
			if (this.designerPanel.getLayersPane() != null) {
				this.designerPanel.getLayersPane().update();
			}
			if (this.designerPanel.getInfoPane() != null) {
				this.designerPanel.getInfoPane().setInspector(container);
			}
			if (this.designerPanel.getObjectsPane() != null) {
				this.designerPanel.getObjectsPane().update();
			}
		}
		this.repaint();
	}

	/**
	 * @return the objects
	 */
	public LinkedList<DesignerEntity> getObjects() {
		LinkedList<DesignerEntity> entities = new LinkedList<DesignerEntity>();
		if (this.getEntityContainer() != null) {
			return this.getEntityContainer().getEntities();
		}
		return entities;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// nothing ...
	}

	@Override
	public void keyPressed(KeyEvent e) {
		/* Moving selection and keeping proportions when resizing */
		if (getSelection() != null) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				goLeft();
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				goRight();
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				goUp();
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				goDown();
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				setKeepProportions(true);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Removing selection on back space and delete keys */
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
			this.removeSelected();
		}
		/* Stop any action */
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.removeAdding();
			this.stopAdding();
			this.setEntityPicker(null);
		}
		/* Cloning selections */
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_J) {
			if (this.getSelection() != null) {
				this.cloneObject(this.getSelection());
			}
		}
		/* Stop keeping proportions on shift release */
		if (getSelection() != null) {
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				setKeepProportions(false);
			}
		}
	}
	
	/**
	 * @return the entityPicker
	 */
	public EntityPicker getEntityPicker() {
		return entityPicker;
	}

	/**
	 * @param entityPicker the entityPicker to set
	 */
	public void setEntityPicker(EntityPicker entityPicker) {
		if (entityPicker != null) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (this.entityPicker != null) {
				this.entityPicker.pickerCancelled();
			}
		}
		this.entityPicker = entityPicker;
	}

	/**
	 * @return the keepProportions
	 */
	private boolean keepsProportions() {
		return keepProportions;
	}

	/**
	 * @param keepProportions the keepProportions to set
	 */
	private void setKeepProportions(boolean keepProportions) {
		this.keepProportions = keepProportions;
	}

	/**
	 * @return the absoluteScene
	 */
	public boolean isAbsoluteScene() {
		return absoluteScene;
	}

	/**
	 * @param absoluteScene the absoluteScene to set
	 */
	public void setAbsoluteScene(boolean absoluteScene) {
		this.absoluteScene = absoluteScene;
	}

	public interface EntityPicker {
		public void entityPicked(DesignerEntity entity);
		public boolean acceptsEntity(DesignerEntity entity);
		public void pickerCancelled();
	}
	
	public class PaneDropTargetListener implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drop(DropTargetDropEvent event) {
	        // Accept copy drops
			event.acceptDrop(DnDConstants.ACTION_COPY);
			
	        // Get the transfer which can provide the dropped item data
			Transferable transferable = event.getTransferable();
			
			// Get the data formats of the dropped item
	        DataFlavor[] flavors = transferable.getTransferDataFlavors();
	        	        
	        // Loop through the flavors
	        for (DataFlavor flavor : flavors) {
	            try {
	                // If the drop items are files
	                if (flavor.isFlavorJavaFileListType()) {

	                    // Get all of the dropped files
	                    List<File> files = (List<File>) transferable.getTransferData(flavor);

	                    // Loop them through
	                    for (File file : files) {
	                        // Print out the file path
	                    }
	                    
	                    for (DropListener listener : getEntityContainer().getDropListeners()) {
	                    	listener.dropFiles(files, event.getLocation());
                        }
	                }
	            } catch (Exception e) {
	                // Print out the error stack
	                e.printStackTrace();
	            }
	        }

	        // Inform that the drop is complete
	        event.dropComplete(true);
		}
		
	}
}
