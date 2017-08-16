package com.zms.zpc.support;

import com.zms.zpc.debugger.ide.*;

import java.awt.event.ComponentListener;

/**
 * Created by 张小美 on 17/八月/16.
 * Copyright 2002-2016
 */
public class PlatformDepends {

    public static IScreen createScreen(ComponentListener frame) {
        return new MonitorLabel(frame);
    }

}
