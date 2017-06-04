package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by 张小美 on 17/六月/4.
 * Copyright 2002-2016
 */
public class IDEFrame extends UtilityFrame implements ActionListener {

    public static final String Title = "IDE";

    public IDEFrame(ZPC pc) {
        super(pc, Title);
        this.design();

    }

    private void design() {
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());

        this.designMenubar();
        this.designToolbar();
    }

    private void designMenubar() {

    }

    private void designToolbar() {
        java.util.List<JIconButton> toolButtons = new ArrayList<>();

        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        this.getContentPane().add(toolBar, BorderLayout.NORTH);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        JIconButton button;

        {
            button = new JIconButton("Start Debug");
            toolButtons.add(button);
            button.setIconCommand("startDebugger");
        }
        {
            button = new JIconButton("Run To Cursor");
            toolButtons.add(button);
            button.setIconCommand("runToCursor");
        }

        {
            button = new JIconButton("Step Out");
            toolButtons.add(button);
            button.setIconCommand("stepOut");
        }
        {
            button = new JIconButton("Resume");
            toolButtons.add(button);
            button.setIconCommand("resume");
        }
        {
            button = new JIconButton("Reset");
            toolButtons.add(button);
            button.setIconCommand("reset");
        }
        {
            button = new JIconButton("ReRun");
            toolButtons.add(button);
            button.setIconCommand("rerun");
        }

        getFrame().designToolbar(toolBar, toolButtons);

        for (JIconButton one : toolButtons) {
            getFrame().designIcon(one);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
