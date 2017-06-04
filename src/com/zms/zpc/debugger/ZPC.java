package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;
import com.zms.zpc.emulator.PC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class ZPC extends JFrame implements ActionListener {

    private PC pc = new PC();

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
            designIcons();

            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
    }

    private void designIcons() {
        for (JIconMenuItem item : menuItems.values()) {
            designIcon(item);
        }
        for (JIconButton button : toolButtons) {
            designIcon(button);
        }
    }

    void designIcon(JIconMenuItem item) {
        String iconCommand = item.getIconCommand();
        if (iconCommand != null && iconCommand.length() > 0) {
            item.setIcon(loadIcon(iconCommand));
        }
        item.setToolTipText(item.getActionCommand());
        item.setFrame(this);
    }

    void designIcon(JIconButton button) {
        if (button != null) {
            button.setToolTipText(button.getText());
            String iconCommand = button.getIconCommand();
            if (iconCommand != null && iconCommand.length() > 0) {
                button.setIcon(loadIcon(iconCommand));
                button.setText(null);
            }
            button.setFrame(this);
        }
    }

    private Map<String, Icon> iconCache = new HashMap<>();

    private Icon loadIcon(String name) {
        return iconCache.computeIfAbsent(name, n -> new ImageIcon(this.getClass().getClassLoader().getResource("icons/" + name + ".png")));
    }

    private java.util.List<JMenu> menus;
    private Map<String, JIconMenuItem> menuItems;

    private void designMenu() {
        if (menus == null) {
            menus = new ArrayList<>();
            menuItems = new HashMap<>();
            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu menu;
            JIconMenuItem item;
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
                    item = new JIconMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                }
                {
                    command = ProcessorRegistersFrame.Title;
                    item = new JIconMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                    item.setIconCommand("watches");
                }
                {
                    command = IDEFrame.Title;
                    item = new JIconMenuItem(command);
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
            {
                menu = new JMenu("Help");
                menus.add(menu);
                {
                    command = "Help";
                    item = new JIconMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                    item.setIconCommand("help");
                }
            }
            addMenus(menuBar, menus, menuItems);
        }
    }

    void addMenus(JMenuBar menuBar, java.util.List<JMenu> menus, Map<String, JIconMenuItem> menuItems) {
        JIconMenuItem item;
        for (JMenu one : menus) {
            menuBar.add(one);
            for (int i = 0; i < one.getItemCount(); i++) {
                item = (JIconMenuItem) one.getItem(i);
                item.addActionListener(this);
                menuItems.put(item.getActionCommand(), item);
            }
        }
    }

    private java.util.List<JIconButton> toolButtons;

    private void designToolbar() {
        if (toolButtons == null) {
            toolButtons = new ArrayList<>();
            JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
            this.getContentPane().add(toolBar, BorderLayout.NORTH);
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            JIconButton button;
            {
                button = new JIconButton("Test1");
                toolButtons.add(button);
                button.setIconCommand("refresh");
            }
            {
                button = new JIconButton("Test2");
                toolButtons.add(button);
                button.setIconCommand("sync");
            }
            toolButtons.add(null);
            {
                button = new JIconButton("Test3");
                toolButtons.add(button);
                button.setIconCommand("magic");
            }
            designToolbar(toolBar, toolButtons);
        }
    }

    void designToolbar(JToolBar toolBar, java.util.List<JIconButton> toolButtons) {
        for (AbstractButton one : toolButtons) {
            if (one == null) {
                toolBar.addSeparator();
            } else {
                toolBar.add(one);
                one.setActionCommand(one.getText());
                one.addActionListener(this);
            }
        }
    }

    private java.util.List<JLabel> statusLabels;
    private int mainStatusIndex = 1;

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
            int main = mainStatusIndex;
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

    public void showMainStatus(String text) {
        JLabel label = statusLabels.get(mainStatusIndex);
        if (text == null || text.length() < 1) {
            text = " ";
        }
        label.setText(text);
    }

    public void hideMainStatus(String text) {
        JLabel label = statusLabels.get(mainStatusIndex);
        if (text != null && text.equals(label.getText())) {
            label.setText(" ");
        }
    }

    private Map<String, Object> frameObjs = new HashMap<>();

    protected UtilityFrame showUtilityFrame(Class<? extends UtilityFrame> klass) {
        UtilityFrame frame;
        try {
            String name = (String) klass.getDeclaredField("Title").get(null);
            frame = (UtilityFrame) frameObjs.get(name);
            if (frame == null) {
                Constructor<? extends UtilityFrame> con = klass.getDeclaredConstructor(this.getClass());
                frame = con.newInstance(this);
                frameObjs.put(name, frame);
                this.desktop.add(frame);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        frame.show(desktop);
        return frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == null) {
            return;
        }
        switch (command) {
            case PCMonitorFrame.Title: {
                showUtilityFrame(PCMonitorFrame.class);
            }
            break;
            case ProcessorRegistersFrame.Title: {
                showUtilityFrame(ProcessorRegistersFrame.class);
            }
            break;
            case IDEFrame.Title: {
                showUtilityFrame(IDEFrame.class).setVisible(true);
            }
            break;
            case "Test1":
                test1();
                break;
            case "Test2":
                test2();
                break;
            case "Test3":
                test3();
                break;

        }
    }

    public java.util.List<JMenu> getMenus() {
        return menus;
    }

    public java.util.List<JIconButton> getToolButtons() {
        return toolButtons;
    }

    public Map<String, JIconMenuItem> getMenuItems() {
        return menuItems;
    }

    public PC getPc() {
        return pc;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new ZPC().start();
    }

    public void test1() {
        ProcessorRegistersFrame frame = (ProcessorRegistersFrame) frameObjs.get(ProcessorRegistersFrame.Title);
        if (frame != null) {
            frame.refresh1();
        }
    }

    public void test2() {

    }

    public void test3() {

    }

}
