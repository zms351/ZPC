package com.zms.zpc.emulator.debug;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public interface IDebugger {

    void onMessage(int type,String message,Object... params);

}
