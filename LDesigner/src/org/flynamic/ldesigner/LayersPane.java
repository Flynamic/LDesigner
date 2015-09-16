package org.flynamic.ldesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.flynamic.ldesigner.util.DNDTree;

public class LayersPane extends JPanel {
	private DesignerPanel designerPanel = null;
	private DNDTree tree;

	/**
	 * Create the panel.
	 */
	public LayersPane() {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		tree = new DNDTree();
		scrollPane.setViewportView(tree);
		tree.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				deselectAll();
			}
		});
	}

	public void update() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Objects");
		DefaultTreeModel model = new DefaultTreeModel(root);

		tree.setShowsRootHandles(true);
		tree.setBackground(UIManager.getColor("Button.background"));
		tree.setModel(model);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					nodeClicked(e);
				}
			}
		});
		tree.getModel().addTreeModelListener(new TreeModelListener(){

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				tree.expandPath(e.getTreePath());
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				LinkedList<DesignerEntity> list = new LinkedList<>();
				DesignerEntityContainer<DesignerEntity> container = getDesignerWindow().getDesignerPane().getEntityContainer();
				for (int i = 0; i < tree.getModel().getChildCount(tree.getModel().getRoot()); i++) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getChild(tree.getModel().getRoot(), i);
					if (node instanceof DesignerEntityNode) {
						DesignerEntity entity = ((DesignerEntityNode) node).getObject();
						list.add(entity);
					}
				}
				Collections.reverse(list);
				container.setEntities(list);
				container.repaint();
			}

		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
					TreePath currentSelection = tree.getSelectionPath();
					if (currentSelection != null) {
						DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) 
								(currentSelection.getLastPathComponent());
						MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
						if (parent != null) {
							((DefaultTreeModel) tree.getModel()).removeNodeFromParent(currentNode);
							if (currentNode instanceof DesignerEntityNode) {
								DesignerEntityNode node = (DesignerEntityNode) currentNode;
								designerPanel.getDesignerPane().removeObject(node.getObject());
							}
						}
					}
				}
			}
		});

		DesignerEntityContainer<DesignerEntity> container = getDesignerWindow().getDesignerPane().getEntityContainer();
		LinkedList<DesignerEntity> objects = getDesignerWindow().getDesignerPane().getObjects();
		Collections.reverse(objects);
		for (DesignerEntity object : objects) {
			root.add(new DesignerEntityNode(object.getInspectorName(), object));
		}

		tree.setCellRenderer(new ObjectsTreeCellRenderer());
		tree.expandRow(0);
	}

	public void nodeClicked(MouseEvent e) {
		TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
		if (tp.getLastPathComponent() instanceof DesignerEntityNode) {
			DesignerEntityNode node = (DesignerEntityNode) tp.getLastPathComponent();
			this.getDesignerWindow().getDesignerPane().select(node.getObject());
		}
	}

	public void deselectAll() {
		tree.clearSelection();
	}

	public void select(DesignerEntity object) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		for (int i = 0; i < tree.getModel().getChildCount(root); i++) {
			Object node = tree.getModel().getChild(root, i);
			if (node instanceof DesignerEntityNode) {
				DesignerEntityNode dnode = (DesignerEntityNode) node;
				if (dnode.getObject() == object) {
					TreePath tp = new TreePath(dnode.getPath());
					tree.setSelectionPath(tp);
				}
			}
		}
	}

	/**
	 * @return the designerWindow
	 */
	public DesignerPanel getDesignerWindow() {
		return designerPanel;
	}

	/**
	 * @param designerPanel the designerWindow to set
	 */
	public void setDesignerWindow(DesignerPanel designerPanel) {
		this.designerPanel = designerPanel;
		this.update();
	}

	public final class DesignerEntityNode extends DefaultMutableTreeNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DesignerEntity object;

		public DesignerEntityNode(String string, DesignerEntity object) {
			super(string);
			this.setObject(object);
		}

		/**
		 * @return the object
		 */
		DesignerEntity getObject() {
			return object;
		}

		/**
		 * @param object the object to set
		 */
		void setObject(DesignerEntity object) {
			this.object = object;
		}  
	}

	

	public final class ObjectsTreeCellRenderer implements TreeCellRenderer {
		/**
		 * 
		 */
		 private static final long serialVersionUID = 1885556391787976723L;

		 DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

		 JPanel renderer;

		 JLabel nodeIcon;

		 JLabel title;

		 public ObjectsTreeCellRenderer() {
			 renderer = new JPanel(new BorderLayout());
			 //((FlowLayout)renderer.getLayout()).setHgap(0);
			 //((FlowLayout)renderer.getLayout()).setVgap(0);
			 renderer.setBorder(new EmptyBorder(0,0,0,0));

			 nodeIcon = new JLabel();
			 renderer.add(nodeIcon, BorderLayout.WEST);

			 title = new JLabel();
			 renderer.add(title, BorderLayout.CENTER);


		 }

		 @Override
		 public Component getTreeCellRendererComponent(JTree tree,
				 Object value, boolean selected, boolean expanded,
				 boolean leaf, int row, boolean hasFocus) {
			 Component returnValue = null;

			 if (value instanceof DesignerEntityNode) {
				 DesignerEntityNode current = (DesignerEntityNode)value;
				 title.setText(current.getObject().getInspectorName());
				 if (selected) {
					 renderer.setBackground(new Color(50, 50, 250));
					 title.setForeground(Color.WHITE);
				 } else {
					 renderer.setBackground(Color.WHITE);
					 title.setForeground(Color.BLACK);
				 }

				 nodeIcon.addMouseListener(new MouseListener() {

					 @Override
					 public void mouseClicked(MouseEvent e) {
						 System.out.println("OOOAOA");
						 current.getObject().setVisible(false);
					 }

					 @Override
					 public void mousePressed(MouseEvent e) {
						 // TODO Auto-generated method stub

					 }

					 @Override
					 public void mouseReleased(MouseEvent e) {
						 // TODO Auto-generated method stub

					 }

					 @Override
					 public void mouseEntered(MouseEvent e) {
						 // TODO Auto-generated method stub

					 }

					 @Override
					 public void mouseExited(MouseEvent e) {
						 // TODO Auto-generated method stub

					 }

				 });
				 try {
					 ImageIcon icon = current.object.getMiniatureIcon(15, 15);
					 nodeIcon.setIcon(icon);
				 } catch (SecurityException | IllegalArgumentException e) {
					 e.printStackTrace();
				 }
				 returnValue = renderer;
			 }
			 if (returnValue == null) {
				 return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			 }
			 return returnValue;
		 }
	}
}
