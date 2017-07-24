package com.zms.zpc.debugger.ide;

import com.zms.zpc.debugger.*;
import com.zms.zpc.support.GarUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.DefaultStyledDocument;
import java.io.File;

/**
 * Created by 张小美 on 17/七月/24.
 * Copyright 2002-2016
 */
public class FileEditorPane extends JScrollPane implements DocumentListener {

    private DefaultStyledDocument doc;
    private IDEFrame parent;
    private String docName;
    private JTextPane pane;

    public FileEditorPane(IDEFrame parent) {
        super(new JTextPane(new DefaultStyledDocument()),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane= (JTextPane) getViewport().getView();
        doc = (DefaultStyledDocument) pane.getDocument();
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

    public String getDocName() {
        return docName;
    }
    
    public void setDocName(String name) {
        this.docName = name;
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
            setText(text);
            setModifed(false);
        } else {
            setText(text);
        }
    }

    private TabComponent tabComponent;

    public TabComponent getTabComponent() {
        if (tabComponent == null) {
            tabComponent = new TabComponent(this);
        }
        tabComponent.getLabel().setText(getDisplayTitle());
        return tabComponent;
    }

    public IDEFrame getParentComponent() {
        return parent;
    }

    public String getText() {
        return pane.getText();
    }

    public void setText(String t) {
        pane.setText(t);
    }

}
