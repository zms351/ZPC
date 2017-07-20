package com.zms.zpc.emulator.debug;

import com.zms.zpc.support.Constants;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public interface IDebugger extends Constants {

    void onMessage(int type,String message,Object... params);

}
