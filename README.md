# LDesigner
A Java Swing Panel to design basic scenes, add custom objects, 
edit their position, size and connecting them with other objects using a WYSIWYG design pane.

![](https://github.com/Flynamic/LDesigner/blob/master/ldesignerscreen.png)

## Installation
LDesigner requires JGoodies Forms (1.3.0+). I will remove this dependency in the future.
You can download the archives here: http://www.jgoodies.com/downloads/archive/

### Embedding
The core element of LDesigner is the class ```DesignerPanel```. You can embed this JPanel wherever you want.
Now the fun can begin: You need to subclass ```DesignerEntityContainer``` and ```DesignerEntity```, as these are the models 
representing your scene and objects you can move, resize and edit.
Check the (now not-existing-yet) wiki for more information on how you can use features like the **Inspector**.

Next, just add your ```DesignerEntityContainer``` subclass to your ```DesignerPanel```:

```
  DesignerPanel designerPanel = new DesignerPanel();
  designerPanel.setEntityContainer(new MyContainer());
```

That's it! Now, you can build your own scene/game level/whatever and add custom objects. You don't need to worry about position
and size management, yet you can access each objects position and size nontheless.
