package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        java.util.List<JMenu> menus;
        menus = new ArrayList<>();

        JMenu menu;
        JIconMenuItem item;
        String command;
        {
            menu = new JMenu("File");
            menus.add(menu);
            {
                command = "Open";
                item = new JIconMenuItem("Open...");
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("menu-open");
            }
            {
                command = "Save All";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("menu-saveall");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
            }
        }
        {
            menu = new JMenu("Run");
            menus.add(menu);
            {
                command = "Start Debug";
                item = new JIconMenuItem("Debug...");
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("startDebugger");
            }
            {
                command = "Resume";
                item = new JIconMenuItem("Resume Program");
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("resume");
            }
        }
        Map<String, JIconMenuItem> menuItems = new HashMap<>();
        getFrame().addMenus(menuBar, menus, menuItems);
        for (JIconMenuItem one : menuItems.values()) {
            getFrame().designIcon(one);
        }
    }

    private void designToolbar() {
        java.util.List<JIconButton> toolButtons = new ArrayList<>();

        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        this.getContentPane().add(toolBar, BorderLayout.NORTH);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        JIconButton button;

        {
            button = new JIconButton("Open");
            toolButtons.add(button);
            button.setIconCommand("menu-open");
        }
        {
            button = new JIconButton("Save All");
            toolButtons.add(button);
            button.setIconCommand("menu-saveall");
        }
        toolButtons.add(null);
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
