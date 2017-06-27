package com.zms.zpc.emulator.debug;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class DummyDebugger implements IDebugger {

    @Override
    public void onMessage(int type, String message, Object... params) {
        System.err.printf("%d:\t%s\n",type,message);
    }

}
