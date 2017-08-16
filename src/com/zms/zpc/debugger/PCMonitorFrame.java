package com.zms.zpc.debugger;

import com.zms.zpc.debugger.ide.IScreen;
import com.zms.zpc.debugger.util.UtilityFrame;
import com.zms.zpc.emulator.*;
import com.zms.zpc.emulator.board.pci.DefaultVGACard;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.*;

import java.awt.*;
import java.awt.event.*;

import static com.zms.zpc.support.Constants.WARN;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PCMonitorFrame extends UtilityFrame implements Runnable, ComponentListener {

    public static final String Title = "PC Monitor";

    public PCMonitorFrame(ZPC pc) {
        super(pc, Title);
        this.design();
        setFrameIcon(pc.loadIcon("monitor"));
        Thread thread = new Thread(this, this.getClass().getName() + " refresh thread");
        thread.setDaemon(true);
        thread.start();
        this.addComponentListener(this);
    }

    private Dimension size = new Dimension(640, 480);
    IScreen screen;

    public void design() {
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        if (screen == null) {
            screen = PlatformDepends.createScreen(this);
        }
        screen.resized(size);
        content.add(screen.getComponent(), BorderLayout.CENTER);
        screen.init();
    }

    @Override
    public void run() {
        PC pc = getFrame().getPc();
        DefaultVGACard vga = pc.board.pciBus.vga;
        PCState state;
        long t = System.currentTimeMillis();
        long gap;
        vga.frame = this.getFrame();
        screen.setData(vga);
        Runnable refresh = () -> {
            Dimension size = vga.getDisplaySize();
            if (size.width > 0 && size.height > 0 && (size.width != PCMonitorFrame.this.size.width || size.height != PCMonitorFrame.this.size.height)) {
                PCMonitorFrame.this.size.setSize(size);
                screen.resized(size);
                PCMonitorFrame.this.pack();
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
                        if (this.isVisible()) {
                            vga.prepareUpdate();
                            vga.updateDisplay();
                            GarUtils.runInUI(refresh);
                        }
                        break;
                    default:
                        Thread.sleep(20);
                        break;
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
        screen.resized(size);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        screen.moved(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

}
