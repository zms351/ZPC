package com.zms.zpc.debugger.util;

import com.zms.zpc.debugger.ZPC;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by 张小美 on 17/六月/4.
 * Copyright 2002-2016
 */
public class JIconMenuItem extends JMenuItem {

    private String iconCommand;
    private ZPC frame;

    public JIconMenuItem(String text) {
        super(text);
        init();
    }

    public String getIconCommand() {
        return iconCommand;
    }

    public void setIconCommand(String iconCommand) {
        this.iconCommand = iconCommand;
    }

    public ZPC getFrame() {
        return frame;
    }

    public void setFrame(ZPC frame) {
        this.frame = frame;
    }

    protected void init() {
        this.addMouseListener(MenuItemMouseListener);
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return null;
    }

    private String tooltip;

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(tooltip = text);
    }

    public static MouseListener MenuItemMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            JIconMenuItem item = (JIconMenuItem) e.getComponent();
            if (item.frame != null && item.tooltip != null) {
                item.frame.showMainStatus(item.tooltip);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JIconMenuItem item = (JIconMenuItem) e.getComponent();
            if (item.frame != null && item.tooltip != null) {
                item.frame.hideMainStatus(item.tooltip);
            }
        }
    };

}
