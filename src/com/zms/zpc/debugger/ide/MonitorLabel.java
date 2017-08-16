package com.zms.zpc.debugger.ide;

import com.zms.zpc.emulator.board.pci.DefaultVGACard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

/**
 * Created by 张小美 on 17/八月/6.
 * Copyright 2002-2016
 */
public class MonitorLabel extends JLabel implements IScreen {

    public DefaultVGACard vga;
    public volatile boolean clearBackground = true;

    public MonitorLabel(ComponentListener parent) {
        this.addComponentListener(parent);
    }

    @Override
    protected void paintComponent(Graphics g) {
        paint(g);
    }

    @Override
    public void paintAll(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        if (clearBackground) {
            g.setColor(Color.white);
            Dimension s1 = getSize();
            if (s1.getWidth() > 0 && s1.getHeight() > 0) {
                g.fillRect(0, 0, s1.width, s1.height);
            }
            clearBackground = false;
        }
        if (vga != null) {
            vga.paintPCMonitor(this, g);
        }
    }

    @Override
    public void paintData(Object context, BufferedImage buffer, Object data1, Object data2) {
        Graphics g = (Graphics) context;
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        if (buffer != null) {
            g.drawImage(buffer, 0, 0, this);
        }
    }

    @Override
    public void init() {
        this.setDoubleBuffered(false);
        this.requestFocusInWindow();
    }

    @Override
    public void resized(Dimension size) {
        this.setPreferredSize(size);
        this.getSize(size);
        this.clearBackground = true;
    }

    @Override
    public void setData(Object data) {
        vga = (DefaultVGACard) data;
    }

    @Override
    public Component getComponent() {
        return this;
    }

}
