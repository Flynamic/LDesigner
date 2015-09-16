package org.flynamic.ldesigner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

public class ToolsPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 728321576713554071L;
	public DesignerPanel designerPanel;
	private JButton btnRemove;
	
	private DesignerEntity object = null;
	private JButton btnClone;

	/**
	 * Create the panel.
	 */
	public ToolsPane() {
		setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		add(toolBar);
		
		btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeObject();
			}
		});
		btnRemove.setEnabled(false);
        ImageIcon remove = new ImageIcon(DesignerPanel.class.getResource("/org/flynamic/ldesigner/resources/remove.png"));
        ImageIcon removeIcon = new ImageIcon(remove.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        btnRemove.setIcon(removeIcon);
		toolBar.add(btnRemove);
		
		btnClone = new JButton("Clone");
		btnClone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cloneObject();
			}
		});
		ImageIcon clone = new ImageIcon(DesignerPanel.class.getResource("/org/flynamic/ldesigner/resources/clone.png"));
        ImageIcon cloneIcon = new ImageIcon(clone.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        btnClone.setIcon(cloneIcon);
        btnClone.setEnabled(false);
		toolBar.add(btnClone);

	}
	
	public void updateServices() {
		if (object == null) {
			this.btnRemove.setEnabled(false);
			this.btnClone.setEnabled(false);
		} else {
			this.btnRemove.setEnabled(object.isRemovable());
			this.btnClone.setEnabled(object.isCloneable());
		}
	}
	
	public void removeObject() {
		if (object != null) {
			this.designerPanel.getDesignerPane().removeObject(object);
			this.setObject(null);
		}
	}
	
	public void cloneObject() {
		if (object != null) {
			this.designerPanel.getDesignerPane().cloneObject(object);
		}
	}
	
	/**
	 * @return the object
	 */
	public DesignerEntity getObject() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(DesignerEntity object) {
		this.object = object;
		this.updateServices();
	}
}
