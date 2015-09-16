package org.flynamic.ldesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class ObjectsPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 683840417561955999L;
	private DesignerPanel designerPanel;
	private JTree tree;
	
	public Class<? extends DesignerEntity> selectedObject;

	/**
	 * Create the panel.
	 */
	public ObjectsPane() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblObjects = new JLabel("Objects");
		lblObjects.setHorizontalAlignment(SwingConstants.CENTER);
		lblObjects.setForeground(Color.GRAY);
		lblObjects.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		panel.add(lblObjects, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);

		tree = new JTree();
		scrollPane.setViewportView(tree);

	}
	
	public void deselectAll() {
		tree.clearSelection();
		selectedObject = null;
	}
	
	public void select(TreePath path) {
		DesignerEntityNode node = (DesignerEntityNode) path.getLastPathComponent();
		tree.setSelectionPath(path);
	    this.selectedObject = node.objectClass;
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
				deselectAll();
		        int selRow = tree.getRowForLocation(e.getX(), e.getY());
		        if(selRow != -1) {
					nodeClicked(e, e.getClickCount());
		        }
			}
		});
		tree.setCellRenderer(new ObjectsTreeCellRenderer());
		
		if (getDesignerWindow().getDesignerPane().getEntityContainer() != null) {
			for (Group group : getDesignerWindow().getDesignerPane().getEntityContainer().getEntityClasses()) {
				DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
				root.add(groupNode);
				for (Class<? extends DesignerEntity> entityClass : group.getEntityClasses()) {
					try {
						Field field = entityClass.getField("name");
						String name = (String) field.get(null);
						groupNode.add(new DesignerEntityNode(name, entityClass));
					} catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchFieldException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}
		
	public void nodeClicked(MouseEvent e, int times) {
	    TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
	    if (tp.getLastPathComponent() instanceof DesignerEntityNode) {
	    	this.select(tp);
		    /*
		    if (times == 1) {
			    this.selectedObject = (Class<? extends GameObject>)node.objectClass;
		    }
		    if (times == 2) {
		    	this.designerWindow.getDesignerPane().addObject((Class<? extends GameObject>)node.objectClass);
		    }
		    */
	    }
	}
	
	protected JTree getTree() {
		return tree;
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
	
	public static final class Group {
		private String name;
		private LinkedList<Class<? extends DesignerEntity>> entityClasses = new LinkedList<>();
		
		public Group(String name) {
			this.setName(name);
		}
		
		@SafeVarargs
		public Group(String name, Class<? extends DesignerEntity>... entities) {
			this(name);
			for (Class<? extends DesignerEntity> e : entities) {
				add(e);
			}
		}
		
		public void add(Class<? extends DesignerEntity> entity) {
			this.getEntityClasses().add(entity);
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the entityClasses
		 */
		public LinkedList<Class<? extends DesignerEntity>> getEntityClasses() {
			return entityClasses;
		}

		/**
		 * @param entityClasses the entityClasses to set
		 */
		public void setEntityClasses(LinkedList<Class<? extends DesignerEntity>> entityClasses) {
			this.entityClasses = entityClasses;
		}
	}

	public final class DesignerEntityNode extends DefaultMutableTreeNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Class<? extends DesignerEntity> objectClass;

		public DesignerEntityNode(String string, Class<? extends DesignerEntity> objectClass) {
			super(string);
			this.objectClass = objectClass;
		}	    
	}
	
	public final class ObjectsTreeCellRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1885556391787976723L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree,
				Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			
	        Component c = super.getTreeCellRendererComponent(tree, value, selected,expanded, leaf, row, hasFocus);
	        
	        if (value instanceof DesignerEntityNode) {
	        	DesignerEntityNode current = (DesignerEntityNode)value;
		        	        
		        try {
			        ImageIcon img = (ImageIcon) current.objectClass.getField("icon").get(null);
			        Icon icon = new ImageIcon(img.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
			        this.setIcon(icon);
		        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
					e.printStackTrace();
				}
	        }
	        return this;
		}
	}
}
