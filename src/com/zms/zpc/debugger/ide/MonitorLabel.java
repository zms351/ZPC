package com.zms.zpc.debugger.ide;

import com.zms.zpc.debugger.PCMonitorFrame;
import com.zms.zpc.emulator.board.pci.DefaultVGACard;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 张小美 on 17/八月/6.
 * Copyright 2002-2016
 */
public class MonitorLabel extends JLabel {

    public DefaultVGACard vga;
    public volatile boolean clearBackground = true;

    public MonitorLabel(PCMonitorFrame parent) {
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
        if(vga!=null) {
            vga.paintPCMonitor(g, this);
        }
    }

}
