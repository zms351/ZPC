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
    private String iconCommand2;
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

    public String getIconCommand2() {
        return iconCommand2;
    }

    public void setIconCommand2(String iconCommand2) {
        this.iconCommand2 = iconCommand2;
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

    public Icon icon1;
    public Icon icon2;

    public boolean refresh() {
        if(icon2!=null) {
            if(this.isSelected()) {
                setIcon(icon2);
                return true;
            }
        }
        if(icon1!=null) {
            setIcon(icon1);
            return true;
        }
        return false;
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

        @Override
        public void mouseClicked(MouseEvent e) {
            JIconButton button = (JIconButton) e.getComponent();
            if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==1) {
                if(button.icon2!=null) {
                    button.setSelected(!button.isSelected());
                    button.refresh();
                }
            }
        }

    };

}
