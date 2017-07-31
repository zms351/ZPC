package com.zms.zpc.debugger;

import com.zms.zpc.debugger.ide.FileEditorPane;
import com.zms.zpc.debugger.util.*;
import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.assembler.Assembler;
import com.zms.zpc.emulator.debug.*;
import com.zms.zpc.support.GarUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

/**
 * Created by 张小美 on 17/六月/4.
 * Copyright 2002-2016
 */
public class IDEFrame extends UtilityFrame implements ActionListener, IDebugger {

    public static final String Title = "IDE";

    public IDEFrame(ZPC pc) {
        super(pc, Title);
        this.design();
        setFrameIcon(pc.loadIcon("ide"));
    }

    private void design() {
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());

        this.designMenubar();
        this.designToolbar();
        this.designMain();
        this.checkNew();
        this.setPreferredSize(new Dimension(640, 480));
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
                command = "New";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("documentation");
            }
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
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
            }
            {
                command = "Save As";
                item = new JIconMenuItem("Save As...");
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("profileMemory");
            }
            {
                command = "Close Tab";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("closeActive");
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
            menu.addSeparator();
            {
                command = "Step Into";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("traceInto");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            }
            {
                command = "Step Over";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("traceOver");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
            }
            {
                command = "Step Out";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("stepOut");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
            }
            {
                command = "Run To Cursor";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("runToCursor");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
            }
            menu.addSeparator();
            {
                command = "Show Execution Point";
                item = new JIconMenuItem(command);
                item.setActionCommand(command);
                menu.add(item);
                item.setIconCommand("showCurrentFrame");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
            }
        }
        Map<String, JIconMenuItem> menuItems = new HashMap<>();
        getFrame().addMenus(menuBar, menus, menuItems);
        for (JIconMenuItem one : menuItems.values()) {
            getFrame().designIcon(one);
            one.removeActionListener(getFrame());
            one.addActionListener(this);
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
            button = new JIconButton("New");
            toolButtons.add(button);
            button.setIconCommand("documentation");
        }
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
        {
            button = new JIconButton("Close Tab");
            toolButtons.add(button);
            button.setIconCommand("closeActive");
        }
        toolButtons.add(null);
        {
            button = new JIconButton("Start Debug");
            toolButtons.add(button);
            button.setIconCommand("startDebugger");
        }
        {
            button = new JIconButton("Step Into");
            toolButtons.add(button);
            button.setIconCommand("traceInto");
        }
        {
            button = new JIconButton("Step Over");
            toolButtons.add(button);
            button.setIconCommand("traceOver");
        }
        {
            button = new JIconButton("Step Out");
            toolButtons.add(button);
            button.setIconCommand("stepOut");
        }
        {
            button = new JIconButton("Run To Cursor");
            toolButtons.add(button);
            button.setIconCommand("runToCursor");
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
        toolButtons.add(null);
        {
            button = new JIconButton("Compile Native");
            toolButtons.add(button);
            button.setIconCommand("compile");
        }
        {
            button = new JIconButton("Compile");
            toolButtons.add(button);
            button.setIconCommand("output");
        }
        {
            button = new JIconButton("Show Execution Point");
            toolButtons.add(button);
            button.setIconCommand("showCurrentFrame");
        }
        {
            button = new JIconButton("Write Instruction");
            toolButtons.add(button);
            button.setIconCommand("write");
        }
        getFrame().designToolbar(toolBar, toolButtons);

        for (JIconButton one : toolButtons) {
            if (one != null) {
                getFrame().designIcon(one);
                one.removeActionListener(getFrame());
                one.addActionListener(this);
            }
        }
    }

    private JTabbedPane tabs;

    private void designMain() {
        tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        this.getContentPane().add(tabs, BorderLayout.CENTER);
    }

    private void checkNew() {
        if (tabs.getTabCount() < 1) {
            addNew();
        }
    }

    private FileEditorPane addNew() {
        FileEditorPane one = FileEditorPane.newDefault(this);
        tabs.add(one.getDisplayTitle(), one);
        refreshTitle(one);
        return one;
    }

    public FileEditorPane findTabByName(String name) {
        if (name == null) {
            return null;
        }
        for (int i = 0; i < tabs.getTabCount(); i++) {
            FileEditorPane tab = (FileEditorPane) tabs.getComponentAt(i);
            if (name.equals(tab.getDocName())) {
                return tab;
            }
        }
        return null;
    }

    public void showNew(String text, String title, boolean silent) {
        FileEditorPane tab = addNew();
        select(tab);
        if (title != null) {
            tab.setDocTitle(title);
        }
        if (text != null) {
            tab.setText(text, silent, 2);
        }
        refreshTitle(tab);
    }

    public void closeTab(FileEditorPane tab) {
        tabs.remove(tab);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == null) {
            return;
        }
        switch (command) {
            case "New": {
                select(addNew());
            }
            break;
            case "Save All": {
                FileEditorPane tab = (FileEditorPane) tabs.getSelectedComponent();
                if (tab != null) {
                    tab.save();
                    refreshTitle(tab);
                }
            }
            break;
            case "Close Tab": {
                int index = tabs.getSelectedIndex();
                if (index >= 0) {
                    tabs.remove(index);
                }
            }
            break;
            case "Save As": {
                FileEditorPane tab = (FileEditorPane) tabs.getSelectedComponent();
                if (tab != null) {
                    tab.saveAs();
                    refreshTitle(tab);
                }
            }
            break;
            case "Open": {
                File file = openDialog();
                if (file != null) {
                    FileEditorPane tab = addNew();
                    select(tab);
                    tab.init(file);
                    refreshTitle(tab);
                }
            }
            break;
            case "Compile": {
                FileEditorPane pane = (FileEditorPane) tabs.getSelectedComponent();
                if (pane != null) {
                    Object o = new Assembler().assemble(pane.getText());
                    if (o != null) {
                        showNew(o.toString(), "本地编译结果", true);
                    }
                }
            }
            break;
            case "Compile Native": {
                FileEditorPane tab = (FileEditorPane) tabs.getSelectedComponent();
                if (tab != null) {
                    Object o = Assembler.nativeAssemble(tab.getText());
                    if (o != null) {
                        showNew(o.toString(), "Native编译结果", true);
                    }
                }
            }
            break;
            case "Step Into": {
                PC pc = getFrame().getPc();
                pc.setDebugger(this);
                pc.setPause(11, command);
            }
            break;
            case "Step Over": {
                PC pc = getFrame().getPc();
                pc.setDebugger(this);
                pc.setPause(14, command);
            }
            break;
            case "Step Out": {
                PC pc = getFrame().getPc();
                pc.setDebugger(this);
                pc.setPause(15, command);
            }
            break;
            case "Run To Cursor": {
                int gap=getRun2CursorGap();
                if(gap>0) {
                    PC pc = getFrame().getPc();
                    pc.setDebugger(this);
                    pc.intObj[0]=gap;
                    pc.intObj[1]=-1;
                    pc.setPause(17, pc.intObj);
                }
            }
            break;
            case "Show Execution Point": {
                PC pc = getFrame().getPc();
                pc.setDebugger(this);
                pc.setPause(12, command);
            }
            break;
            case "Write Instruction": {
                FileEditorPane tab = (FileEditorPane) tabs.getSelectedComponent();
                PC pc = getFrame().getPc();
                if (tab != null) {
                    byte[] bytes = GarUtils.plain2Bytes(tab.getText());
                    if (bytes != null && bytes.length > 0) {
                        pc.setPause(13, bytes);
                    }
                }
            }
            break;
        }
    }

    public int getRun2CursorGap() {
        FileEditorPane pane = (FileEditorPane) tabs.getSelectedComponent();
        if (pane == null) {
            return -1;
        }
        int line = pane.getCurrentLine();
        if (line < 1) {
            return -1;
        }
        if (line == 1) {
            return 0;
        }
        String[] lines = pane.getText().split("\n");
        if (line > lines.length) {
            return -1;
        }
        line--;
        int gap = 0;
        for (int i = 0; i < line; i++) {
            String s = lines[i];
            int index = s.indexOf(' ');
            if (index < 0) {
                return -1;
            }
            s = s.substring(index + 1).trim();
            index = s.indexOf(' ');
            if (index < 0) {
                return -1;
            }
            s = s.substring(0, index).trim();
            if ((s.length() % 2) == 0) {
                gap += s.length() / 2;
            }
        }
        return gap;
    }

    @Override
    public void onMessage(int type, String message, Object... params) {
        if (type >= LOG) {
            DummyDebugger.getInstance().onMessage(type, message, params);
            return;
        }
        final Object[] os = new Object[2];
        if (type == 12) {
            os[0] = "_internal__12__A_";
            os[1] = "当前代码反编译结果";
        }
        if (os[0] != null && os[1] != null) {
            SwingUtilities.invokeLater(() -> {
                String name = (String) os[0];
                FileEditorPane tab = findTabByName(name);
                if (tab == null) {
                    tab = addNew();
                }
                tab.setDocName(name);
                tab.setDocTitle((String) os[1]);
                tab.setText(message, true, 1);
                select(tab);
                refreshTitle(tab);
            });
        }
    }

    public void refreshTitle(FileEditorPane tab) {
        int index = tabs.indexOfComponent(tab);
        if (index >= 0) {
            tabs.setTitleAt(index, tab.getDisplayTitle());
            tabs.setTabComponentAt(index, tab.getTabComponent());
        }
    }

    public FileEditorPane select(FileEditorPane tab) {
        tabs.setSelectedComponent(tab);
        return tab;
    }

    public JTabbedPane getTabs() {
        return tabs;
    }

}
