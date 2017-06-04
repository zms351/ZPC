package com.zms.zpc.debugger.util;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by 张小美 on 17/六月/4.
 * Copyright 2002-2016
 */
public class JIconButton extends JButton {

    private String iconCommand;

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

    public boolean isFocusable() {
        return isRequestFocusEnabled();
    }

    public static MouseListener ToolbarMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            JButton button = (JButton) e.getComponent();
            button.setBorderPainted(true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JButton button = (JButton) e.getComponent();
            button.setBorderPainted(false);
        }
    };

}
