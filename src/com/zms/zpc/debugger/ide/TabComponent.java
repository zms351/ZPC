package com.zms.zpc.debugger.ide;

import com.zms.zpc.debugger.ZPC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by 张小美 on 17/七月/24.
 * Copyright 2002-2016
 */
public class TabComponent extends JPanel implements MouseListener, ActionListener {

    private JLabel label;
    private JButton closeButton;
    private FileEditorPane parent;

    public static Icon close, closeHovered, text;

    public TabComponent(FileEditorPane parent) {
        super(new BorderLayout());
        ZPC zpc = parent.getParentComponent().getFrame();
        if (close == null) {
            close = zpc.loadIcon("closeNew");
            closeHovered = zpc.loadIcon("closeNewHovered");
            text = zpc.loadIcon("text");
        }
        closeButton = new JButton(close);
        this.add(closeButton, BorderLayout.EAST);
        closeButton.setBorderPainted(false);
        closeButton.addMouseListener(this);
        this.parent = parent;
        label = new JLabel(parent.getDisplayTitle(), text, JLabel.CENTER);
        this.add(label, BorderLayout.CENTER);
        closeButton.addActionListener(this);

        this.setOpaque(false);
        label.setOpaque(false);
        closeButton.setOpaque(false);

        this.addMouseListener(this);
    }

    public JLabel getLabel() {
        return label;
    }

    public JButton getCloseButton() {
        return closeButton;
    }

    protected void close() {
        parent.getParentComponent().closeTab(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        close();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getComponent() == closeButton) {
            close();
        }
        if (e.getComponent() == this) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                close();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getComponent() == this) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                parent.getParentComponent().select(parent);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getComponent() == closeButton) {
            closeButton.setIcon(closeHovered);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getComponent() == closeButton) {
            closeButton.setIcon(close);
        }
    }

}
