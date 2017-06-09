package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.UtilityFrame;

import java.awt.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PCMonitorFrame extends UtilityFrame {

    public static final String Title = "PC Monitor";

    public PCMonitorFrame(ZPC pc) {
        super(pc,Title);
        this.design();
        setFrameIcon(pc.loadIcon("monitor"));
    }

    private void design() {
        this.setPreferredSize(new Dimension(640,480));
    }

}
