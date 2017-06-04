package com.zms.zpc.debugger.util;

import com.zms.zpc.debugger.ZPC;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by 张小美 on 17/六月/4.
 * Copyright 2002-2016
 */
public class JIconButton extends JButton {

    private String iconCommand;
    private ZPC frame;

    public JIconButton(String text) {
        super(text);
        init();
    }

    public String getIconCommand() {
        return iconCommand;
    }

    public void setIconCommand(String iconCommand) {
        this.iconCommand = iconCommand;
    }

    protected void init() {
        setRolloverEnabled(true);
        setRequestFocusEnabled(false);
        addMouseListener(ToolbarMouseListener);
        setBorderPainted(false);
    }

    public ZPC getFrame() {
        return frame;
    }

    public void setFrame(ZPC frame) {
        this.frame = frame;
    }

    public boolean isFocusable() {
        return isRequestFocusEnabled();
    }

    public static MouseListener ToolbarMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            JIconButton button = (JIconButton) e.getComponent();
            button.setBorderPainted(true);
            if (button.frame != null && button.getToolTipText() != null) {
                button.frame.showMainStatus(button.getToolTipText());
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JIconButton button = (JIconButton) e.getComponent();
            button.setBorderPainted(false);
            if (button.frame != null && button.getToolTipText() != null) {
                button.frame.hideMainStatus(button.getToolTipText());
            }
        }
    };

}
