package com.zms.zpc.debugger.util;

import com.zms.zpc.debugger.ZPC;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.Random;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class UtilityFrame extends JInternalFrame {

    private ZPC frame;
    private boolean showStatus;

    public UtilityFrame(ZPC pc,String title) {
        this(pc,title,false);
    }

    public UtilityFrame(ZPC pc,String title,boolean showStatus) {
        super(title, true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.frame =pc;
        this.showStatus=showStatus;
        this.design();
        //this.setToolTipText(title);
    }

    protected JLabel status;

    private void design() {
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        status=new JLabel("Ready");
        if(showStatus) {
            content.add(status, BorderLayout.SOUTH);
        }
        status.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }

    public static final Random random=new Random(System.currentTimeMillis());

    public void show(JDesktopPane desktop) {
        UtilityFrame frame = this;
        if (!frame.isVisible()) {
            frame.pack();
            frame.setVisible(true);
        }
        DesktopManager dm = desktop.getDesktopManager();
        dm.activateFrame(frame);
        int a=desktop.getWidth()-frame.getWidth()-20;
        int b=desktop.getHeight()-frame.getHeight()-20;
        if(a>0 && b>0 && frame.getWidth()>0 && frame.getHeight()>0) {
            synchronized (random) {
                a=random.nextInt(a);
                b=random.nextInt(b);
                dm.resizeFrame(frame,a,b,frame.getWidth(),frame.getHeight());
            }
        }
    }

    public ZPC getFrame() {
        return frame;
    }

}
