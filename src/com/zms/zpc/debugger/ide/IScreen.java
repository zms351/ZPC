package com.zms.zpc.debugger.ide;

import java.awt.*;

/**
 * Created by 张小美 on 17/八月/16.
 * Copyright 2002-2016
 */
public interface IScreen {

    void init();

    void resized(Dimension size);

    void setData(Object data);

    void repaint();

    Component getComponent();

}
