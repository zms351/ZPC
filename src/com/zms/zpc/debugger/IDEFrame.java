package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;
import com.zms.zpc.emulator.assembler.Assembler;
import com.zms.zpc.support.GarUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            }
            {
                command = "Save As";
                item = new JIconMenuItem("Save As...");
                item.setActionCommand(command);
                menu.add(item);
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
        return one;
    }

    public void showNew(String text, String title, boolean silent) {
        FileEditorPane tab = addNew();
        tabs.setSelectedComponent(tab);
        if (title != null) {
            tab.setDocTitle(title);
        }
        if (text != null) {
            tab.setText(text, silent);
        }
        refreshTitle(tab);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == null) {
            return;
        }
        switch (command) {
            case "New": {
                tabs.setSelectedComponent(addNew());
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
                    tabs.setSelectedComponent(tab);
                    tab.init(file);
                    refreshTitle(tab);
                }
            }
            break;
            case "Resume": {
                FileEditorPane pane = (FileEditorPane) tabs.getSelectedComponent();
                if (pane != null) {
                    Object o = new Assembler().assemble(pane.getText());
                    if (o != null) {
                        showNew(o.toString(), "本地编译结果", true);
                    }
                }
            }
            break;
            case "ReRun": {
                FileEditorPane tab = (FileEditorPane) tabs.getSelectedComponent();
                if (tab != null) {
                    Object o = Assembler.nativeAssemble(tab.getText());
                    if (o != null) {
                        showNew(o.toString(), "Native编译结果", true);
                    }
                }
            }
            break;
        }
    }

    protected void refreshTitle(FileEditorPane tab) {
        int index = tabs.indexOfComponent(tab);
        if (index >= 0) {
            tabs.setTitleAt(index, tab.getDisplayTitle());
        }
    }

}

class FileEditorPane extends JTextPane implements DocumentListener {

    private DefaultStyledDocument doc;
    private IDEFrame parent;

    public FileEditorPane(IDEFrame parent) {
        super(new DefaultStyledDocument());
        doc = (DefaultStyledDocument) getDocument();
        doc.addDocumentListener(this);
        this.parent = parent;
    }

    private String docTitle;
    private boolean modifed;
    private File file;

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public boolean isModifed() {
        return modifed;
    }

    public void setModifed(boolean modifed) {
        this.modifed = modifed;
    }

    public String getDisplayTitle() {
        if (isModifed()) {
            return getDocTitle() + "*";
        } else {
            return getDocTitle();
        }
    }

    public static FileEditorPane newDefault(IDEFrame parent) {
        FileEditorPane one = new FileEditorPane(parent);
        one.setDocTitle("新建文档");
        one.setModifed(false);
        return one;
    }

    public DefaultStyledDocument getDoc() {
        return doc;
    }

    public Object save() {
        if (file == null) {
            return saveAs();
        } else {
            Object r = GarUtils.saveFile(file, getText());
            setModifed(false);
            setDocTitle(file.getName());
            return r;
        }
    }

    public Object saveAs() {
        File file = parent.saveDialog();
        if (file != null) {
            this.file = file;
            return save();
        }
        return null;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        showModified();
        setModifed(true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        showModified();
        setModifed(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        showModified();
        setModifed(true);
    }

    protected void showModified() {
        if (!modifed) {
            modifed = true;
            parent.refreshTitle(this);
        }
    }

    public void init(File file) {
        this.file = file;
        this.docTitle = file.getName();
        String s = GarUtils.loadFile(file);
        setText(s, true);
    }

    public void setText(String text, boolean silent) {
        if (silent) {
            modifed = true;
            super.setText(text);
            setModifed(false);
        } else {
            super.setText(text);
        }
    }

}
