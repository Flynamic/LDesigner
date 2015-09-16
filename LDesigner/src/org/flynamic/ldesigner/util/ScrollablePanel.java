package org.flynamic.ldesigner.util;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollablePanel extends JPanel implements Scrollable {
	public ScrollablePanel() {
		super();
	}
	
    @Override
	public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
       return 10;
    }

    @Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
    }

    @Override
	public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
	public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}