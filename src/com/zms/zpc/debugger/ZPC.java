package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;
import com.zms.zpc.emulator.*;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.util.*;

import static com.zms.zpc.support.Constants.WARN;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class ZPC extends JFrame implements ActionListener, Runnable {

    private final PC pc = new PC();

    public ZPC() {
        super("ZPC Debugger");
    }

    protected void start() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(640, 480);
        this.setExtendedState(Frame.NORMAL);
        this.setVisible(true);
        SwingUtilities.invokeLater(this::design0);
        setIconImage(loadIcon("pc").getImage());
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

            Thread thread = new Thread(this, this.getClass().getName() + " refresh thread");
            thread.setDaemon(true);
            thread.start();
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
                button.icon1 = loadIcon(iconCommand);
            }
            iconCommand = button.getIconCommand2();
            if (iconCommand != null && iconCommand.length() > 0) {
                button.icon2 = loadIcon(iconCommand);
            }
            if (button.refresh()) {
                button.setText(null);
            }
            button.setFrame(this);
        }
    }

    private Map<String, ImageIcon> iconCache = new HashMap<>();

    public ImageIcon loadIcon(String name) {
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
                    item.setIconCommand("monitor");
                }
                {
                    command = ProcessorRegistersFrame.Title;
                    item = new JIconMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                    item.setIconCommand("variable");
                }
                {
                    command = IDEFrame.Title;
                    item = new JIconMenuItem(command);
                    item.setActionCommand(command);
                    menu.add(item);
                    item.setIconCommand("ide");
                }
            }
            {
                menu = new JMenu("Run");
                menus.add(menu);
                {
                    command = "CPUPowerOn";
                    item = new JIconMenuItem("Start");
                    item.setActionCommand(command);
                    menu.add(item);
                }
                {
                    command = "CPUPowerOff";
                    item = new JIconMenuItem("Stop");
                    item.setActionCommand(command);
                    menu.add(item);
                }
                {
                    command = "CPUReset";
                    item = new JIconMenuItem("Reset");
                    item.setActionCommand(command);
                    menu.add(item);
                }
                {
                    command = "CPUPause";
                    item = new JIconMenuItem("Pause");
                    item.setActionCommand(command);
                    menu.add(item);
                }
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
                if(item==null) {
                    continue;
                }
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
            {
                button = new JIconButton("Test3");
                toolButtons.add(button);
                button.setIconCommand("magic");
            }
            toolButtons.add(null);
            {
                button = new JIconButton("Show Debug Msg");
                toolButtons.add(button);
                button.setIconCommand("console");
                button.setIconCommand2("console_log");
                button.setSelected(true);
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
        setStatus(mainStatusIndex, text);
    }

    public void hideMainStatus(String text) {
        JLabel label = statusLabels.get(mainStatusIndex);
        if (text != null && text.equals(label.getText())) {
            label.setText(" ");
        }
    }

    public void setStatus(int index, String text) {
        JLabel label = statusLabels.get(index);
        if (text == null || text.length() < 1) {
            text = " ";
        }
        if (!text.equals(label.getText())) {
            label.setText(text);
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
            case "CPUPowerOn":
                pc.powerOn(false);
                break;
            case "CPUPowerOff":
                pc.powerOff();
                break;
            case "CPUReset":
                pc.reset();
                break;
            case "CPUPause":
                pc.pause();
                break;
            case "Show Debug Msg":
                if (e.getSource() instanceof AbstractButton) {
                    final AbstractButton button = (AbstractButton) e.getSource();
                    GarUtils.runLater(() -> BaseObj.Debug = button.isSelected() ? 1 : 0);
                }
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

    public void test11() {
        ProcessorRegistersFrame frame = (ProcessorRegistersFrame) frameObjs.get(ProcessorRegistersFrame.Title);
        if (frame != null) {
            frame.refresh1();
        }
    }

    public void test1() {
        showUtilityFrame(MonitorFrame.class);
    }

    public void test2() {
        showUtilityFrame(IDEFrame.class);
    }

    public void test3() {
        showUtilityFrame(ProcessorRegistersFrame.class);
    }

    @Override
    public void run() {
        try {
            final Object[] vs = new Object[10];
            Runnable run1 = () -> setStatus(2, String.valueOf(vs[0]));
            while (this.isVisible() || this.isShowing()) {
                Thread.sleep(200);
                {
                    PCState v = pc.getState();
                    if (v != vs[0]) {
                        vs[0] = v;
                        GarUtils.runInUI(run1);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            pc.powerOff();
            DummyDebugger.getInstance().onMessage(WARN,Thread.currentThread().getName() + " exited!\n");
        }
    }

}
