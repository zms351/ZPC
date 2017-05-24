package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.UtilityFrame;
import com.zms.zpc.emulator.PC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class ZPC extends JFrame implements ActionListener {

    private PC pc=new PC();

    public ZPC() {
        super("ZPC Debugger");
    }

    protected void start() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(640, 480);
        this.setExtendedState(Frame.NORMAL);
        this.setVisible(true);
        SwingUtilities.invokeLater(this::design0);
    }

    private JDesktopPane desktop;
    private JPanel statusBar;

    private void design0() {
        if (desktop == null) {
            Container content = this.getContentPane();
            content.setLayout(new BorderLayout());

            desktop = new JDesktopPane();
            content.add(desktop, BorderLayout.CENTER);

            designMenu();

            statusBar = new JPanel(new BorderLayout());
            content.add(statusBar, BorderLayout.SOUTH);
            designStatus();

            designToolbar();

            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
    }

    private java.util.List<JMenu> menus;
    private Map<String, JMenuItem> menuItems;

    private void designMenu() {
        if (menus == null) {
            menus = new ArrayList<>();
            menuItems = new HashMap<>();
            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu menu;
            JMenuItem item;
            String command;
            {
                menu = new JMenu("Actions");
                menus.add(menu);
            }
            {
                menu = new JMenu("Windows");
                menus.add(menu);
                {
                    command = PCMonitorFrame.Title;
                    item = new JMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                }
                {
                    command = ProcessorRegistersFrame.Title;
                    item = new JMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                }
            }
            {
                menu = new JMenu("Run");
                menus.add(menu);
            }
            {
                menu = new JMenu("Tools");
                menus.add(menu);
            }
            {
                menu = new JMenu("Disks");
                menus.add(menu);
            }
            for (JMenu one : menus) {
                menuBar.add(one);
                for (int i = 0; i < one.getItemCount(); i++) {
                    item = one.getItem(i);
                    item.addActionListener(this);
                    menuItems.put(item.getActionCommand(), item);
                }
            }
        }
    }

    private java.util.List<AbstractButton> toolButtons;

    private void designToolbar() {
        if (toolButtons == null) {
            toolButtons = new ArrayList<>();
            JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
            this.getContentPane().add(toolBar, BorderLayout.NORTH);
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            JButton button;
            {
                button = new JButton("Test1");
                toolButtons.add(button);
            }
            {
                button = new JButton("Test2");
                toolButtons.add(button);
            }
            toolButtons.add(null);
            {
                button = new JButton("Test3");
                toolButtons.add(button);
            }
            for (AbstractButton one : toolButtons) {
                if (one == null) {
                    toolBar.addSeparator();
                } else {
                    toolBar.add(one);
                }
            }
        }
    }

    private java.util.List<JLabel> statusLabels;

    private void designStatus() {
        if (statusLabels == null) {
            statusLabels = new ArrayList<>();
            JLabel label;
            {
                label = new JLabel("1");
                label.setPreferredSize(new Dimension(32, 1));
                statusLabels.add(label);
            }
            {
                label = new JLabel("Ready");
                statusLabels.add(label);
            }
            {
                label = new JLabel("A");
                label.setPreferredSize(new Dimension(100, 1));
                statusLabels.add(label);
            }
            {
                label = new JLabel("B");
                label.setPreferredSize(new Dimension(100, 1));
                statusLabels.add(label);
            }
            {
                label = new JLabel("C");
                label.setPreferredSize(new Dimension(100, 1));
                statusLabels.add(label);
            }
            int main = 1;
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
            this.statusBar.add(panel, BorderLayout.CENTER);
            for (int i = 0; i < main; i++) {
                JPanel one = new JPanel(new BorderLayout());
                panel.add(one, BorderLayout.CENTER);
                panel.add(statusLabels.get(i), BorderLayout.WEST);
                panel = one;
            }
            for (int i = statusLabels.size() - 1; i > main; i--) {
                JPanel one = new JPanel(new BorderLayout());
                panel.add(one, BorderLayout.CENTER);
                panel.add(statusLabels.get(i), BorderLayout.EAST);
                panel = one;
            }
            panel.add(statusLabels.get(main), BorderLayout.CENTER);
        }
    }

    private Map<String, Object> frameObjs = new HashMap<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == null) {
            return;
        }
        switch (command) {
            case PCMonitorFrame.Title: {
                UtilityFrame frame = (UtilityFrame) frameObjs.get(command);
                if (frame == null) {
                    frame = new PCMonitorFrame(this);
                    frameObjs.put(command, frame);
                    this.desktop.add(frame);
                }
                frame.show(desktop);
            }
            break;
            case ProcessorRegistersFrame.Title: {
                UtilityFrame frame = (UtilityFrame) frameObjs.get(command);
                if (frame == null) {
                    frame = new ProcessorRegistersFrame(this);
                    frameObjs.put(command, frame);
                    this.desktop.add(frame);
                }
                frame.show(desktop);
            }
            break;
        }
    }

    public java.util.List<JMenu> getMenus() {
        return menus;
    }

    public java.util.List<AbstractButton> getToolButtons() {
        return toolButtons;
    }

    public Map<String, JMenuItem> getMenuItems() {
        return menuItems;
    }

    public PC getPc() {
        return pc;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new ZPC().start();
    }

}
