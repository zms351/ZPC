package com.zms.zpc.debugger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class ZPC extends JFrame {

    public ZPC() {
        super("ZPC Debugger");
    }

    protected void start() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(640,480);
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

    private void designMenu() {
        if (menus == null) {
            menus = new ArrayList<>();
            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu menu;
            {
                menu = new JMenu("Actions");
                menus.add(menu);
            }
            {
                menu = new JMenu("Windows");
                menus.add(menu);
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
                if(one==null) {
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

    public java.util.List<JMenu> getMenus() {
        return menus;
    }

    public java.util.List<AbstractButton> getToolButtons() {
        return toolButtons;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new ZPC().start();
    }

}
