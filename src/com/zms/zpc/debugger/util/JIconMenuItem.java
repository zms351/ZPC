package com.zms.zpc.debugger.util;

import javax.swing.*;

/**
 * Created by 张小美 on 17/六月/4.
 * Copyright 2002-2016
 */
public class JIconMenuItem extends JMenuItem {

    private String iconCommand;

    public JIconMenuItem(String text) {
        super(text);
    }

    public String getIconCommand() {
        return iconCommand;
    }

    public void setIconCommand(String iconCommand) {
        this.iconCommand = iconCommand;
    }

}
