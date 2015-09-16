package org.flynamic.ldesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

public class DesignerPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -3448237196151170932L;
    private InfoPane infoPane;
    private DesignerPane designerPane;
    private ObjectsPane objectsPane;
    private JSplitPane splitPane;
    private JSplitPane splitPane_1;
    
    private Class<? extends DesignerEntityContainer<?>> containerClass;
    
    private ToolsPane toolsPane;
    private JSplitPane splitPane_2;
    private LayersPane layersPane;

    /**
     * Create the frame.
     */
    public DesignerPanel(Class<? extends DesignerEntityContainer<?>> containerClass) {
        this();
        setContainerClass(containerClass);
    }
    
    public DesignerPanel() {
        setBounds(100, 100, 450, 300);

        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout(0, 0));

        splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.1);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);
        
        splitPane_1 = new JSplitPane();
        splitPane_1.setResizeWeight(0.9);
        splitPane_1.setContinuousLayout(true);
        splitPane.setRightComponent(splitPane_1);
        
        designerPane = new DesignerPane();
        splitPane_1.setLeftComponent(designerPane);
        designerPane.designerPanel = this;
        
        splitPane_2 = new JSplitPane();
        splitPane_2.setResizeWeight(0.5);
        splitPane_2.setContinuousLayout(true);
        splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane_1.setRightComponent(splitPane_2);
        
        objectsPane = new ObjectsPane();
        splitPane_2.setLeftComponent(objectsPane);
        objectsPane.setDesignerWindow(this);
        
        layersPane = new LayersPane();
        layersPane.setDesignerWindow(this);
        splitPane_2.setRightComponent(layersPane);
        
                infoPane = new InfoPane();
                splitPane.setLeftComponent(infoPane);
                infoPane.designerPanel = this;
                infoPane.setPreferredSize(new Dimension(200,0));
        
        toolsPane = new ToolsPane();
        add(toolsPane, BorderLayout.NORTH);
        toolsPane.designerPanel = this;
    }

	public InfoPane getInfoPane() {
		return infoPane;
	}
	
	public DesignerPane getDesignerPane() {
		return designerPane;
	}
	
	public ObjectsPane getObjectsPane() {
		return objectsPane;
	}
	
	public ToolsPane getToolsPane() {
		return toolsPane;
	}
	public LayersPane getLayersPane() {
		return layersPane;
	}

	/**
	 * @return the containerClass
	 */
	public Class<? extends DesignerEntityContainer<?>> getContainerClass() {
		return containerClass;
	}

	/**
	 * @param containerClass the containerClass to set
	 */
	public void setContainerClass(Class<? extends DesignerEntityContainer<?>> containerClass) {
		this.containerClass = containerClass;
        
        try {
			setEntityContainer(getContainerClass().newInstance());
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
	}
	
	public DesignerEntityContainer<?> getEntityContainer() {
		return this.getDesignerPane().getEntityContainer();
	}
	
	public void setEntityContainer(DesignerEntityContainer<?> container) {
		this.getDesignerPane().setEntityContainer(container);
	}
}
