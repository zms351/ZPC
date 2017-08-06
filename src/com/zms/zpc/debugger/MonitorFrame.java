package com.zms.zpc.debugger;

import com.zms.zpc.debugger.ide.MonitorLabel;
import com.zms.zpc.debugger.util.UtilityFrame;
import com.zms.zpc.emulator.*;
import com.zms.zpc.emulator.board.pci.DefaultVGACard;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.GarUtils;

import java.awt.*;
import java.awt.event.*;

import static com.zms.zpc.support.Constants.WARN;

/**
 * Created by 张小美 on 17/八月/6.
 * Copyright 2002-2016
 */
public class MonitorFrame extends UtilityFrame implements Runnable, ComponentListener {

    public static final String Title = "Monitor";

    public MonitorFrame(ZPC pc) {
        super(pc, Title);
        this.design();
        setFrameIcon(pc.loadIcon("grid"));
        Thread thread = new Thread(this, this.getClass().getName() + " refresh thread");
        thread.setDaemon(true);
        thread.start();
        this.addComponentListener(this);
    }

    private Dimension size = new Dimension(640, 480);
    private MonitorLabel screen;

    public void design() {
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        if (screen == null) {
            screen = new MonitorLabel(this);
        }
        screen.setPreferredSize(size);
        screen.setSize(size);
        content.add(screen, BorderLayout.CENTER);
        screen.setDoubleBuffered(false);
        screen.requestFocusInWindow();
    }

    @Override
    public void run() {
        PC pc = getFrame().getPc();
        DefaultVGACard vga = pc.board.pciBus.vga;
        PCState state;
        long t = System.currentTimeMillis();
        long gap;
        screen.vga = vga;
        Runnable refresh = () -> {
            Dimension size = vga.getDisplaySize();
            if (size.width > 0 && size.height > 0 && (size.width != MonitorFrame.this.size.width || size.height != MonitorFrame.this.size.height)) {
                MonitorFrame.this.size.setSize(size);
                screen.clearBackground = true;
                screen.setPreferredSize(size);
                screen.setSize(size);
                MonitorFrame.this.pack();
            }
            screen.repaint();
        };
        try {
            while ((state = pc.getState()) != null) {
                switch (state) {
                    case Running:
                    case Pause:
                        gap = System.currentTimeMillis() - t;
                        if (gap >= 10) {
                            Thread.sleep(1);
                        } else {
                            Thread.sleep(10 - gap);
                        }
                        t = System.currentTimeMillis();
                        vga.prepareUpdate();
                        vga.updateDisplay();
                        GarUtils.runInUI(refresh);
                        break;
                    default:
                        Thread.sleep(20);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            DummyDebugger.getInstance().onMessage(WARN, Thread.currentThread().getName() + " exited!\n");
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        screen.getSize(size);
        screen.clearBackground = true;
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

}
