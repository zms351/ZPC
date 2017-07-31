package com.zms.zpc.debugger.ide;

import com.zms.zpc.debugger.IDEFrame;
import com.zms.zpc.support.GarUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.io.File;

/**
 * Created by 张小美 on 17/七月/24.
 * Copyright 2002-2016
 */
public class FileEditorPane extends JScrollPane implements DocumentListener, KeyListener {

    private DefaultStyledDocument doc;
    private IDEFrame parent;
    private String docName;
    private JTextPane pane;

    public FileEditorPane(IDEFrame parent) {
        super(new JTextPane(new DefaultStyledDocument()), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane = (JTextPane) getViewport().getView();
        doc = (DefaultStyledDocument) pane.getDocument();
        doc.addDocumentListener(this);
        this.parent = parent;
        pane.addKeyListener(this);
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
        setText(s, true, 2);
    }

    /**
     * @param caretPosition cursor pos parameter 0 none  1  header 2  original  3  tail
     */
    public void setText(String text, boolean silent, int caretPosition) {
        int n1 = pane.getSelectionStart();
        int n2 = pane.getSelectionEnd();
        if (silent) {
            modifed = true;
            setText(text);
            setModifed(false);
        } else {
            setText(text);
        }
        int len = text.length();
        switch (caretPosition) {
            case 1:
                pane.setSelectionStart(0);
                pane.setSelectionEnd(0);
                break;
            case 3:
                pane.setSelectionStart(len);
                pane.setSelectionEnd(len);
                break;
            case 2:
                int n3 = n2;
                if (n3 > len) {
                    n3 = len;
                }
                n1 = n3 - (n2 - n1);
                if (n1 < 0) {
                    n1 = 0;
                }
                pane.setSelectionStart(n1);
                pane.setSelectionEnd(n2);
                break;
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

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public JTextPane getPane() {
        return pane;
    }

    public int getCurrentLine() {
        int pos = pane.getSelectionEnd();
        int rn = (pos == 0) ? 1 : 0;
        try {
            int offs = pos;
            while (offs > 0) {
                offs = Utilities.getRowStart(pane, offs) - 1;
                rn++;
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        return rn;
    }

}
