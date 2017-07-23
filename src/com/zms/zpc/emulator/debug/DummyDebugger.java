package com.zms.zpc.emulator.debug;

import com.zms.zpc.support.BaseObj;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class DummyDebugger extends BaseObj implements IDebugger {

    @Override
    public void onMessage(int type, String message, Object... params) {
        if(type==DEBUG) {
            if(Debug==1) {
                System.err.printf(message, params);
            }
        } else {
            System.err.printf("%d:\t%s\n", type, message);
        }
    }

}
