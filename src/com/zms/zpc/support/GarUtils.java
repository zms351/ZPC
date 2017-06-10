package com.zms.zpc.support;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class GarUtils {

    @SuppressWarnings("unchecked")
    public static <U, V> V convertAll(U u) {
        return (V) u;
    }

    public static File LastDir;

    public static File fileDialog(Frame parent, boolean save) {
        FileDialog dialog = new FileDialog(parent);
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

    public static byte[] readFile(File file) {
        long len = file.length();
        assert len >= 0 && len < Integer.MAX_VALUE;
        byte[] bytes = new byte[(int) len];
        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            input.readFully(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    public static String loadFile(File file) {
        return new String(readFile(file), StandardCharsets.UTF_8);
    }

    public static Object saveFile(File file, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static Object bytes2Plain(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        int size = bytes.length;
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                if ((i % 16) == 0) {
                    buffer.append('\n');
                } else {
                    buffer.append(' ');
                }
            }
            String token = Integer.toHexString(bytes[i] + 512);
            buffer.append(token.substring(token.length() - 2));
        }
        return buffer;
    }

}
