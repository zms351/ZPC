package com.zms.zpc.debugger.ide;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.zms.zpc.emulator.board.pci.DefaultVGACard;
import com.zms.zpc.support.GarUtils;

import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.nio.*;

/**
 * Created by 张小美 on 17/八月/15.
 * Copyright 2002-2016
 */
public class MonitorOpengl extends GLCanvas implements IScreen, GLEventListener, Runnable {

    static GLCapabilities cap;

    public synchronized static MonitorOpengl createOne(ComponentListener listener) {
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        cap = new GLCapabilities(profile);
        return new MonitorOpengl(listener, cap);
    }

    public DefaultVGACard vga;
    public volatile boolean clearBackground = true;
    public Object frame;

    public MonitorOpengl(ComponentListener listener, GLCapabilitiesImmutable glCapabilitiesImmutable) throws GLException {
        super(glCapabilitiesImmutable);
        this.addGLEventListener(this);
        if (listener != null) {
            frame = listener;
            this.addComponentListener(listener);
        }
    }

    @Override
    public void init() {
        this.requestFocusInWindow();
    }

    @Override
    public void resized(Dimension size) {
        this.setPreferredSize(size);
        this.setSize(size);
        this.clearBackground = true;
        GarUtils.runLater(this);
    }

    @Override
    public void setData(Object data) {
        vga = (DefaultVGACard) data;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    private BufferedImage image;
    private Object buffer;
    private Buffer buf;

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        if (clearBackground) {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            clearBackground = false;
        }
        if (buf != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            gl.glPixelZoom(2f, -2f);
            gl.glWindowPos2f(0, height * 2);
            gl.glDrawPixels(width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buf);
        }
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {
    }

    @Override
    public void paintData(Object context, BufferedImage image, Object buffer, Object data) {
        if (this.image != image || this.buffer != buffer) {
            this.image = image;
            this.buffer = buffer;
            this.buf = IntBuffer.wrap((int[]) buffer);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (vga != null) {
            vga.paintPCMonitor(this, this);
        }
        super.paint(g);
    }

    @Override
    public void moved(Object e) {
        GarUtils.runLater(this);
    }

    public void moved_(Object e) {
        if (frame instanceof Component) {
            Component parent = (Component) frame;
            this.setVisible(parent.isVisible());
        }
    }

    @Override
    public void run() {
        moved_(this);
    }

}
