package com.zms.zpc.debugger.util;

import com.zms.zpc.debugger.ZPC;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class UtilityFrame extends JInternalFrame {

    private ZPC frame;
    private boolean showStatus;

    public UtilityFrame(ZPC pc, String title) {
        this(pc, title, false);
    }

    public UtilityFrame(ZPC pc, String title, boolean showStatus) {
        super(title, true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.frame = pc;
        this.showStatus = showStatus;
        this.design();
        //this.setToolTipText(title);
    }

    protected JLabel status;

    private void design() {
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        status = new JLabel("Ready");
        if (showStatus) {
            content.add(status, BorderLayout.SOUTH);
        }
        status.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }

    public static final Random random = new Random(System.currentTimeMillis());

    public void show(JDesktopPane desktop) {
        UtilityFrame frame = this;
        if (!frame.isVisible()) {
            frame.pack();
            frame.setVisible(true);
        }
        DesktopManager dm = desktop.getDesktopManager();
        dm.activateFrame(frame);
        int a = desktop.getWidth() - frame.getWidth() - 20;
        int b = desktop.getHeight() - frame.getHeight() - 20;
        if (a > 0 && b > 0 && frame.getWidth() > 0 && frame.getHeight() > 0) {
            synchronized (random) {
                a = random.nextInt(a);
                b = random.nextInt(b);
                dm.resizeFrame(frame, a, b, frame.getWidth(), frame.getHeight());
            }
        }
    }

    public ZPC getFrame() {
        return frame;
    }

    public static File LastDir;

    public File openDialog() {
        return _dialog(false);
    }

    public File saveDialog() {
        return _dialog(true);
    }

    public File _dialog(boolean save) {
        FileDialog dialog = new FileDialog(getFrame());
        if (LastDir != null) {
            dialog.setDirectory(LastDir.getPath());
        }
        dialog.setMultipleMode(false);
        if (save) {
            dialog.setMode(FileDialog.SAVE);
            dialog.setTitle("另存为");
        } else {
            dialog.setMode(FileDialog.LOAD);
            dialog.setTitle("打开");
        }
        dialog.setLocale(Locale.CHINA);
        dialog.setVisible(true);
        String path = dialog.getFile();
        if (path != null && path.length() > 0) {
            File file = new File(dialog.getDirectory(), path);
            LastDir = file.getParentFile();
            return file;
        }
        return null;
    }

    public String loadFile(File file) {
        long len = file.length();
        assert len >= 0 && len < Integer.MAX_VALUE;
        byte[] bytes = new byte[(int) len];
        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            input.readFully(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public Object saveFile(File file, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

}
