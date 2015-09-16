package org.flynamic.ldesigner.util;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class DNDTree extends JTree {
	Insets autoscrollInsets = new Insets(20, 20, 20, 20); // insets

	public DNDTree(DefaultMutableTreeNode root) {
		this();
		DefaultTreeModel treemodel = new DefaultTreeModel(root);
		setModel(treemodel);
	}

	public DNDTree() {
		setAutoscrolls(true);
		setRootVisible(true);
		setShowsRootHandles(false);//to show the root icon
		setEditable(false);
		new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
	}

	public void autoscroll(Point cursorLocation)  {
		Insets insets = getAutoscrollInsets();
		Rectangle outer = getVisibleRect();
		Rectangle inner = new Rectangle(outer.x+insets.left, outer.y+insets.top, outer.width-(insets.left+insets.right), outer.height-(insets.top+insets.bottom));
		if (!inner.contains(cursorLocation))  {
			Rectangle scrollRect = new Rectangle(cursorLocation.x-insets.left, cursorLocation.y-insets.top,     insets.left+insets.right, insets.top+insets.bottom);
			scrollRectToVisible(scrollRect);
		}
	}

	public Insets getAutoscrollInsets()  {
		return (autoscrollInsets);
	}

	public static DefaultMutableTreeNode makeDeepCopy(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node.getUserObject());
		for (Enumeration e = node.children(); e.hasMoreElements();) {     
			copy.add(makeDeepCopy((DefaultMutableTreeNode)e.nextElement()));
		}
		return(copy);
	}

	public static class TransferableNode implements Transferable {
		public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Node");
		private DefaultMutableTreeNode node;
		private DataFlavor[] flavors = { NODE_FLAVOR };

		public TransferableNode(DefaultMutableTreeNode nd) {
			node = nd;
		}  

		@Override
		public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (flavor == NODE_FLAVOR) {
				return node;
			}
			else {
				throw new UnsupportedFlavorException(flavor);     
			}               
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return Arrays.asList(flavors).contains(flavor);
		}
	}

	public static abstract class AbstractTreeTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener {

		private DNDTree tree;
		private DragSource dragSource; // dragsource
		private DropTarget dropTarget; //droptarget
		private static DefaultMutableTreeNode draggedNode; 
		private DefaultMutableTreeNode draggedNodeParent; 
		private static BufferedImage image = null; //buff image
		private Rectangle rect2D = new Rectangle();
		private boolean drawImage;

		protected AbstractTreeTransferHandler(DNDTree tree, int action, boolean drawIcon) {
			this.tree = tree;
			drawImage = drawIcon;
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(tree, action, this);
			dropTarget = new DropTarget(tree, action, this);
		}

		/* Methods for DragSourceListener */
		@Override
		public void dragDropEnd(DragSourceDropEvent dsde) {
			if (dsde.getDropSuccess() && dsde.getDropAction()==DnDConstants.ACTION_MOVE && draggedNodeParent != null) {
				((DefaultTreeModel)tree.getModel()).nodeStructureChanged(draggedNodeParent);                    
			}
		}
		@Override
		public final void dragEnter(DragSourceDragEvent dsde)  {
			int action = dsde.getDropAction();
			if (action == DnDConstants.ACTION_COPY)  {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			} 
			else {
				if (action == DnDConstants.ACTION_MOVE) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} 
				else {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
		}
		@Override
		public final void dragOver(DragSourceDragEvent dsde) {
			int action = dsde.getDropAction();
			if (action == DnDConstants.ACTION_COPY) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			} 
			else  {
				if (action == DnDConstants.ACTION_MOVE) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} 
				else  {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
		}
		@Override
		public final void dropActionChanged(DragSourceDragEvent dsde)  {
			int action = dsde.getDropAction();
			if (action == DnDConstants.ACTION_COPY) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			}
			else  {
				if (action == DnDConstants.ACTION_MOVE) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} 
				else {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
		}
		@Override
		public final void dragExit(DragSourceEvent dse) {
			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}     

		/* Methods for DragGestureListener */
		@Override
		public final void dragGestureRecognized(DragGestureEvent dge) {
			TreePath path = tree.getSelectionPath(); 
			if (path != null) { 
				draggedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
				draggedNodeParent = (DefaultMutableTreeNode)draggedNode.getParent();
				if (drawImage) {
					Rectangle pathBounds = tree.getPathBounds(path); //getpathbounds of selectionpath
					Component lbl = tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode, false, tree.isExpanded(path), ((DefaultTreeModel)tree.getModel()).isLeaf(path.getLastPathComponent()), 0, false);//returning the label
					lbl.setBounds(pathBounds);//setting bounds to lbl
					image = new BufferedImage(lbl.getWidth(), lbl.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);//buffered image reference passing the label's ht and width
					Graphics2D graphics = image.createGraphics();//creating the graphics for buffered image
					graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));     //Sets the Composite for the Graphics2D context
					//lbl.setOpaque(false);
					lbl.paint(graphics); //painting the graphics to label
					graphics.dispose(); 
				}
				dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop , image, new Point(0,0), new TransferableNode(draggedNode), this);               
			}      
		}

		/* Methods for DropTargetListener */

		@Override
		public final void dragEnter(DropTargetDragEvent dtde) {
			Point pt = dtde.getLocation();
			int action = dtde.getDropAction();
			if (drawImage) {
				paintImage(pt);
			}
			if (canPerformAction(tree, draggedNode, action, pt)) {
				dtde.acceptDrag(action);
			}
			else {
				dtde.rejectDrag();
			}
		}

		@Override
		public final void dragExit(DropTargetEvent dte) {
			if (drawImage) {
				clearImage();
			}
		}

		@Override
		public final void dragOver(DropTargetDragEvent dtde) {
			Point pt = dtde.getLocation();
			int action = dtde.getDropAction();
			tree.autoscroll(pt);
			if (drawImage) {
				paintImage(pt);
			}
			if (canPerformAction(tree, draggedNode, action, pt)) {
				dtde.acceptDrag(action);               
			}
			else {
				dtde.rejectDrag();
			}
		}

		@Override
		public final void dropActionChanged(DropTargetDragEvent dtde) {
			Point pt = dtde.getLocation();
			int action = dtde.getDropAction();
			if (drawImage) {
				paintImage(pt);
			}
			if (canPerformAction(tree, draggedNode, action, pt)) {
				dtde.acceptDrag(action);               
			}
			else {
				dtde.rejectDrag();
			}
		}

		@Override
		public final void drop(DropTargetDropEvent dtde) {
			try {
				if (drawImage) {
					clearImage();
				}
				int action = dtde.getDropAction();
				Transferable transferable = dtde.getTransferable();
				Point pt = dtde.getLocation();
				if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR) && canPerformAction(tree, draggedNode, action, pt)) {
					TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) transferable.getTransferData(TransferableNode.NODE_FLAVOR);
					DefaultMutableTreeNode newParentNode =(DefaultMutableTreeNode)pathTarget.getLastPathComponent();
					if (executeDrop(tree, node, newParentNode, action)) {
						dtde.acceptDrop(action);                    
						dtde.dropComplete(true);
						return;                         
					}
				}
				dtde.rejectDrop();
				dtde.dropComplete(false);
			}

			catch (Exception e) {     
				System.out.println(e);
				dtde.rejectDrop();
				dtde.dropComplete(false);
			}     
		}

		private final void paintImage(Point pt) {
			tree.paintImmediately(rect2D.getBounds());
			rect2D.setRect((int) pt.getX(),(int) pt.getY(),image.getWidth(),image.getHeight());
			tree.getGraphics().drawImage(image,(int) pt.getX(),(int) pt.getY(),tree);
		}

		private final void clearImage() {
			tree.paintImmediately(rect2D.getBounds());
		}

		public abstract boolean canPerformAction(DNDTree target, DefaultMutableTreeNode draggedNode, int action, Point location);

		public abstract boolean executeDrop(DNDTree tree, DefaultMutableTreeNode draggedNode, DefaultMutableTreeNode newParentNode, int action);
	}

	public static class DefaultTreeTransferHandler extends AbstractTreeTransferHandler {

		public DefaultTreeTransferHandler(DNDTree tree, int action) {
			super(tree, action, true);
		}

		@Override
		public boolean canPerformAction(DNDTree target, DefaultMutableTreeNode draggedNode, int action, Point location) {
			TreePath pathTarget = target.getPathForLocation(location.x, location.y);
			if (pathTarget == null) {
				target.setSelectionPath(null);
				return(false);
			}
			target.setSelectionPath(pathTarget);
			if(action == DnDConstants.ACTION_COPY) {
				return(true);
			}
			else
				if(action == DnDConstants.ACTION_MOVE) {     
					DefaultMutableTreeNode parentNode =(DefaultMutableTreeNode)pathTarget.getLastPathComponent();                    
					if (draggedNode.isRoot() || parentNode == draggedNode.getParent() || draggedNode.isNodeDescendant(parentNode)) {                         
						return(false);     
					}
					else {
						return(true);
					}                     
				}
				else {          
					return(false);     
				}
		}

		@Override
		public boolean executeDrop(DNDTree target, DefaultMutableTreeNode draggedNode, DefaultMutableTreeNode newParentNode, int action) { 
			if (action == DnDConstants.ACTION_COPY) {
				DefaultMutableTreeNode newNode = DNDTree.makeDeepCopy(draggedNode);
				((DefaultTreeModel)target.getModel()).insertNodeInto(newNode,newParentNode,newParentNode.getChildCount());
				TreePath treePath = new TreePath(newNode.getPath());
				target.scrollPathToVisible(treePath);
				target.setSelectionPath(treePath);
				return(true);
			}
			if (action == DnDConstants.ACTION_MOVE) {
				MutableTreeNode parent = (MutableTreeNode)draggedNode.getParent();
				MutableTreeNode otherParent = (MutableTreeNode)newParentNode.getParent();
				int newIndex = 0;
				if (otherParent != null) {
					int otherNodeIndex = otherParent.getIndex(newParentNode);
					newIndex = otherNodeIndex;
				}
				draggedNode.removeFromParent();
				((DefaultTreeModel)target.getModel()).insertNodeInto(draggedNode, otherParent, newIndex);
				//((DefaultTreeModel)target.getModel()).insertNodeInto(draggedNode,newParentNode,newParentNode.getChildCount());
				TreePath treePath = new TreePath(draggedNode.getPath());
				target.scrollPathToVisible(treePath);
				target.setSelectionPath(treePath);
				return(true);
			}
			return(false);
		}
	}

	public final class ObjectsTransferHandler extends TransferHandler {

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
					!support.isDrop()) {
				return false;
			}

			JTree.DropLocation dropLocation =
					(JTree.DropLocation)support.getDropLocation();
			return dropLocation.getPath() != null;
		}
	}
}