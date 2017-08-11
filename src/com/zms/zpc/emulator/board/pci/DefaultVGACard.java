package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class DefaultVGACard extends VGACard {

    private int[] rawImageData;
    private int xmin, xmax, ymin, ymax, width, height;
    private BufferedImage buffer;
    private Dimension size = new Dimension();

    public DefaultVGACard(MotherBoard mb) {
        super(mb);
    }

    public int getXMin() {
        return xmin;
    }

    public int getXMax() {
        return xmax;
    }

    public int getYMin() {
        return ymin;
    }

    public int getYMax() {
        return ymax;
    }

    protected int rgbToPixel(int red, int green, int blue) {
        return ((0xFF & red) << 16) | ((0xFF & green) << 8) | (0xFF & blue);
    }

    public Frame frame;

    public void resizeDisplay(int width, int height) {
        if ((width == 0) || (height == 0))
            return;
        this.width = width;
        this.height = height;
        if (frame != null) {
            buffer = frame.getGraphicsConfiguration().createCompatibleImage(width, height, Transparency.OPAQUE);
            //new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            buffer.setAccelerationPriority(1);
            DataBufferInt buf = (DataBufferInt) buffer.getRaster().getDataBuffer();
            rawImageData = buf.getData();
            size.setSize(width, height);
        }
    }

    public void saveScreenshot() {
        File out = new File("Screenshot.png");
        try {
            ImageIO.write(buffer, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Dimension getDisplaySize() {
        return size;
    }

    public int[] getDisplayBuffer() {
        return rawImageData;
    }

    protected void dirtyDisplayRegion(int x, int y, int w, int h) {
        xmin = Math.min(x, xmin);
        xmax = Math.max(x + w, xmax);
        ymin = Math.min(y, ymin);
        ymax = Math.max(y + h, ymax);
    }

    public void paintPCMonitor(Graphics g, ImageObserver io) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        if (buffer != null) {
            g.drawImage(buffer, 0, 0, io);
        }
    }

    public final void prepareUpdate() {
        xmin = width;
        xmax = 0;
        ymin = height;
        ymax = 0;
    }

}
