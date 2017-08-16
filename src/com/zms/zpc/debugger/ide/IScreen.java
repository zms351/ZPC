package com.zms.zpc.debugger.ide;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by 张小美 on 17/八月/16.
 * Copyright 2002-2016
 */
public interface IScreen {

    void init();

    void resized(Dimension size);

    void moved(Object e);

    void setData(Object data);

    void repaint();

    Component getComponent();

    void paintData(Object context, BufferedImage image,Object buffer,Object data);

}
