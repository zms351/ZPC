package com.zms.zpc.emulator.board.time;

/**
 * Created by 张小美 on 2019-06-15.
 * Copyright 2002-2016
 */
public interface TimerResponsive {

    /**
     * Called after a timer registered to this object has expired.
     */
    public void callback();

    public int getType();

}
