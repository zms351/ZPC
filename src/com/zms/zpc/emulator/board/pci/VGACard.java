package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.NotImplException;

import java.awt.*;
import java.io.*;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public abstract class VGACard extends BasePCIDevice {

    public MotherBoard mb;

    private static final DummyDebugger LOGGING = DummyDebugger.getInstance();

    //VGA_RAM_SIZE must be a power of two
    private static final int VGA_RAM_SIZE = 16 * 1024 * 1024;
    private static final int INIT_VGA_RAM_SIZE = 64 * 1024;
    private static final int PAGE_SHIFT = 12;

    private static final int[] expand4 = new int[256];

    static {
        for (int i = 0; i < expand4.length; i++) {
            int v = 0;
            for (int j = 0; j < 8; j++)
                v |= ((i >>> j) & 1) << (j * 4);
            expand4[i] = v;
        }
    }

    private static final int[] expand2 = new int[256];

    static {
        for (int i = 0; i < expand2.length; i++) {
            int v = 0;
            for (int j = 0; j < 4; j++)
                v |= ((i >>> (2 * j)) & 3) << (j * 4);
            expand2[i] = v;
        }
    }

    private static final int[] expand4to8 = new int[16];

    static {
        for (int i = 0; i < expand4to8.length; i++) {
            int v = 0;
            for (int j = 0; j < 4; j++) {
                int b = ((i >>> j) & 1);
                v |= b << (2 * j);
                v |= b << (2 * j + 1);
            }
            expand4to8[i] = v;
        }
    }

    private static final int MOR_COLOR_EMULATION = 0x01;
    private static final int ST01_V_RETRACE = 0x08;
    private static final int ST01_DISP_ENABLE = 0x01;
    private static final int VBE_DISPI_MAX_XRES = 1600;
    private static final int VBE_DISPI_MAX_YRES = 1200;

    private static final int VBE_DISPI_INDEX_ID = 0x0;
    private static final int VBE_DISPI_INDEX_XRES = 0x1;
    private static final int VBE_DISPI_INDEX_YRES = 0x2;
    private static final int VBE_DISPI_INDEX_BPP = 0x3;
    private static final int VBE_DISPI_INDEX_ENABLE = 0x4;
    private static final int VBE_DISPI_INDEX_BANK = 0x5;
    private static final int VBE_DISPI_INDEX_VIRT_WIDTH = 0x6;
    private static final int VBE_DISPI_INDEX_VIRT_HEIGHT = 0x7;
    private static final int VBE_DISPI_INDEX_X_OFFSET = 0x8;
    private static final int VBE_DISPI_INDEX_Y_OFFSET = 0x9;
    private static final int VBE_DISPI_INDEX_NB = 0xa;

    private static final int VBE_DISPI_ID0 = 0xB0C0;
    private static final int VBE_DISPI_ID1 = 0xB0C1;
    private static final int VBE_DISPI_ID2 = 0xB0C2;

    private static final int VBE_DISPI_DISABLED = 0x00;
    private static final int VBE_DISPI_ENABLED = 0x01;
    private static final int VBE_DISPI_LFB_ENABLED = 0x40;
    private static final int VBE_DISPI_NOCLEARMEM = 0x80;
    private static final int VBE_DISPI_LFB_PHYSICAL_ADDRESS = 0xE0000000;

    private static final int GMODE_TEXT = 0;
    private static final int GMODE_GRAPH = 1;
    private static final int GMODE_BLANK = 2;

    private static final int CH_ATTR_SIZE = (160 * 100);
    private static final int VGA_MAX_HEIGHT = 1024;

    private static final int GR_INDEX_SETRESET = 0x00;
    private static final int GR_INDEX_ENABLE_SETRESET = 0x01;
    private static final int GR_INDEX_COLOR_COMPARE = 0x02;
    private static final int GR_INDEX_DATA_ROTATE = 0x03;
    private static final int GR_INDEX_READ_MAP_SELECT = 0x04;
    private static final int GR_INDEX_GRAPHICS_MODE = 0x05;
    private static final int GR_INDEX_MISC = 0x06;
    private static final int GR_INDEX_COLOR_DONT_CARE = 0x07;
    private static final int GR_INDEX_BITMASK = 0x08;

    private static final int SR_INDEX_RESET = 0x00;
    private static final int SR_INDEX_CLOCKING_MODE = 0x01;
    private static final int SR_INDEX_MAP_MASK = 0x02;
    private static final int SR_INDEX_CHAR_MAP_SELECT = 0x03;
    private static final int SR_INDEX_SEQ_MEMORY_MODE = 0x04;

    private static final int AR_INDEX_PALLETE_MIN = 0x00;
    private static final int AR_INDEX_PALLETE_MAX = 0x0F;
    private static final int AR_INDEX_ATTR_MODE_CONTROL = 0x10;
    private static final int AR_INDEX_OVERSCAN_COLOR = 0x11;
    private static final int AR_INDEX_COLOR_PLANE_ENABLE = 0x12;
    private static final int AR_INDEX_HORIZ_PIXEL_PANNING = 0x13;
    private static final int AR_INDEX_COLOR_SELECT = 0x14;

    private static final int CR_INDEX_HORZ_DISPLAY_END = 0x01;
    private static final int CR_INDEX_VERT_TOTAL = 0x06;
    private static final int CR_INDEX_OVERFLOW = 0x07;
    private static final int CR_INDEX_MAX_SCANLINE = 0x09;
    private static final int CR_INDEX_CURSOR_START = 0x0a;
    private static final int CR_INDEX_CURSOR_END = 0x0b;
    private static final int CR_INDEX_START_ADDR_HIGH = 0x0c;
    private static final int CR_INDEX_START_ADDR_LOW = 0x0d;
    private static final int CR_INDEX_CURSOR_LOC_HIGH = 0x0e;
    private static final int CR_INDEX_CURSOR_LOC_LOW = 0x0f;
    private static final int CR_INDEX_VERT_RETRACE_END = 0x11;
    private static final int CR_INDEX_VERT_DISPLAY_END = 0x12;
    private static final int CR_INDEX_OFFSET = 0x13;
    private static final int CR_INDEX_CRTC_MODE_CONTROL = 0x17;
    private static final int CR_INDEX_LINE_COMPARE = 0x18;

    private static final int[] sequencerRegisterMask = new int[]{
            0x03, //~0xfc,
            0x3d, //~0xc2,
            0x0f, //~0xf0,
            0x3f, //~0xc0,
            0x0e, //~0xf1,
            0x00, //~0xff,
            0x00, //~0xff,
            0xff //~0x00
    };

    private static final int[] graphicsRegisterMask = new int[]{
            0x0f, //~0xf0
            0x0f, //~0xf0
            0x0f, //~0xf0
            0x1f, //~0xe0
            0x03, //~0xfc
            0x7b, //~0x84
            0x0f, //~0xf0
            0x0f, //~0xf0
            0xff, //~0x00
            0x00, //~0xff
            0x00, //~0xff
            0x00, //~0xff
            0x00, //~0xff
            0x00, //~0xff
            0x00, //~0xff
            0x00 //~0xff
    };

    private static final int[] mask16 = new int[]{
            0x00000000,
            0x000000ff,
            0x0000ff00,
            0x0000ffff,
            0x00ff0000,
            0x00ff00ff,
            0x00ffff00,
            0x00ffffff,
            0xff000000,
            0xff0000ff,
            0xff00ff00,
            0xff00ffff,
            0xffff0000,
            0xffff00ff,
            0xffffff00,
            0xffffffff
    };

    private static final int[] dmask16 = new int[]{
            0x00000000,
            0xff000000,
            0x00ff0000,
            0xffff0000,
            0x0000ff00,
            0xff00ff00,
            0x00ffff00,
            0xffffff00,
            0x000000ff,
            0xff0000ff,
            0x00ff00ff,
            0xffff00ff,
            0x0000ffff,
            0xff00ffff,
            0x00ffffff,
            0xffffffff
    };

    private static final int[] dmask4 = new int[]{
            0x00000000,
            0xffff0000,
            0x0000ffff,
            0xffffffff
    };

    private static final int[] cursorGlyph = new int[]{
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff,
            0xffffffff, 0xffffffff};

    private final GraphicsUpdater VGA_DRAW_LINE2;
    private final GraphicsUpdater VGA_DRAW_LINE2D2;
    private final GraphicsUpdater VGA_DRAW_LINE4;
    private final GraphicsUpdater VGA_DRAW_LINE4D2;
    private final GraphicsUpdater VGA_DRAW_LINE8D2;
    private final GraphicsUpdater VGA_DRAW_LINE8;
    private final GraphicsUpdater VGA_DRAW_LINE15;
    private final GraphicsUpdater VGA_DRAW_LINE16;
    private final GraphicsUpdater VGA_DRAW_LINE24;
    private final GraphicsUpdater VGA_DRAW_LINE32;

    private int latch;
    private int sequencerRegisterIndex, graphicsRegisterIndex, attributeRegisterIndex, crtRegisterIndex;
    private int[] sequencerRegister, graphicsRegister, attributeRegister, crtRegister;

    private boolean attributeRegisterFlipFlop;
    private int miscellaneousOutputRegister;
    private int featureControlRegister;
    private int st00, st01; // status 0 and 1
    private int dacReadIndex, dacWriteIndex, dacSubIndex, dacState;
    private int shiftControl, doubleScan;
    private int[] dacCache;
    private int[] palette;
    private int bankOffset;

    private int vbeIndex;
    private int[] vbeRegs;
    private int vbeStartAddress;
    private int vbeLineOffset;
    private int vbeBankMask;

    private int[] fontOffset;
    private int graphicMode;
    private int lineOffset;
    private int lineCompare;
    private int startAddress;
    private int planeUpdated;
    private int lastCW, lastCH;
    private int lastWidth, lastHeight;
    private int lastScreenWidth, lastScreenHeight;
    private int cursorStart, cursorEnd;
    private int cursorOffset;
    private final int[] lastPalette;
    private int[] lastChar;

    private boolean ioportRegistered;
    private boolean pciRegistered;
    private boolean memoryRegistered;

    private boolean updatingScreen;

    private VGARAMIORegion ioRegion;

    private VGALowMemoryRegion lowIORegion;

    public VGACard(MotherBoard mb) {
        super(mb.pciBus);

        VGA_DRAW_LINE2 = new DrawLine2();
        VGA_DRAW_LINE2D2 = new DrawLine2d2();
        VGA_DRAW_LINE4 = new DrawLine4();
        VGA_DRAW_LINE4D2 = new DrawLine4d2();
        VGA_DRAW_LINE8D2 = new DrawLine8d2();
        VGA_DRAW_LINE8 = new DrawLine8();
        VGA_DRAW_LINE15 = new DrawLine15();
        VGA_DRAW_LINE16 = new DrawLine16();
        VGA_DRAW_LINE24 = new DrawLine24();
        VGA_DRAW_LINE32 = new DrawLine32();

        lastPalette = new int[256];
        this.mb = mb;

        this.init();

        this.internalReset();

    }

    public String getText() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < lastChar.length / 80; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < 80; j++)
                row.append((char) lastChar[j + 80 * i]);
            b.append(row.toString().trim() + "\n");
        }
        return b.toString();
    }

    public abstract void saveScreenshot();

    public abstract void resizeDisplay(int w, int h);

    protected abstract int[] getDisplayBuffer();

    protected abstract int rgbToPixel(int r, int g, int b);

    protected abstract void dirtyDisplayRegion(int x, int y, int w, int h);

    public abstract Dimension getDisplaySize();

    public void dirtyScreen() {
        Dimension size = getDisplaySize();
        dirtyDisplayRegion(0, 0, size.width, size.height);
    }

    public void setOriginalDisplaySize() {
        resizeDisplay(lastScreenWidth, lastScreenHeight);
    }

    public IORegion[] getIORegions() {
        return new IORegion[]{ioRegion};
    }

    public IORegion getIORegion(int index) {
        if (index == 0)
            return ioRegion;
        else
            return null;
    }

    //IODevice Methods
    public void ioPortWrite8(int address, int data) {
        //all byte accesses are vgaIOPort ones
        vgaIOPortWriteByte(address, data);
    }

    public void ioPortWrite16(int address, int data) {
        switch (address) {
            case 0x1ce:
            case 0xff80:
                vbeIOPortWriteIndex(data);
                break;
            case 0x1cf:
            case 0xff81:
                vbeIOPortWriteData(data);
                break;
            default:
                ioPortWrite8(address, 0xFF & data);
                ioPortWrite8(address + 1, 0xFF & (data >>> 8));
                break;
        }
    }

    public void ioPortWrite32(int address, int data) {
        ioPortWrite16(address, 0xFFFF & data);
        ioPortWrite16(address + 2, data >>> 16);
    }

    public int ioPortRead8(int address) {
        //all byte accesses are vgaIOPort ones
        return vgaIOPortReadByte(address);
    }

    public int ioPortRead16(int address) {
        switch (address) {
            case 0x1ce:
            case 0xff80:
                return vbeIOPortReadIndex();
            case 0x1cf:
            case 0xff81:
                return vbeIOPortReadData();
            default:
                int b0 = 0xFF & ioPortRead8(address);
                int b1 = 0xFF & ioPortRead8(address + 1);
                return b0 | (b1 << 8);
        }
    }

    public int ioPortRead32(int address) {
        int b0 = 0xFFFF & ioPortRead16(address);
        int b1 = 0xFFFF & ioPortRead16(address + 2);
        return b0 | (b1 << 16);
    }

    public int[] ioPorts = new int[]{0x3b4, 0x3b5, 0x3ba,
            0x3d4, 0x3d5, 0x3da,
            0x3c0, 0x3c1, 0x3c2, 0x3c3,
            0x3c4, 0x3c5, 0x3c6, 0x3c7,
            0x3c8, 0x3c9, 0x3ca, 0x3cb,
            0x3cc, 0x3cd, 0x3ce, 0x3cf,
            0x1ce, 0x1cf, 0xff80, 0xff81
    };

    public void init() {
        for (int port : ioPorts) {
            mb.ios.register(port, this);
        }

        reset();
        mb.pciBus.addDevice(this);
    }

    private void vgaIOPortWriteByte(int address, int data) {
        if ((address >= 0x3b0 && address <= 0x3bf && ((miscellaneousOutputRegister & MOR_COLOR_EMULATION) != 0)) ||
                (address >= 0x3d0 && address <= 0x3df && ((miscellaneousOutputRegister & MOR_COLOR_EMULATION) == 0))) {
            return;
        }

        if ((data & ~0xff) != 0) {
            mb.pc.getDebugger().onMessage(WARN, "possible int/byte register problem\n");
        }
        switch (address) {
            case 0x3b4:
            case 0x3d4:
                crtRegisterIndex = data;
                break;
            case 0x3b5:
            case 0x3d5:
                if (crtRegisterIndex <= 7 && (crtRegister[CR_INDEX_VERT_RETRACE_END] & 0x80) != 0) {
                            /* can always write bit 4 of CR_INDEX_OVERFLOW */
                    if (crtRegisterIndex == CR_INDEX_OVERFLOW)
                        crtRegister[CR_INDEX_OVERFLOW] = (crtRegister[CR_INDEX_OVERFLOW] & ~0x10) | (data & 0x10);
                    return;
                }
                crtRegister[crtRegisterIndex] = data;
                break;
            case 0x3ba:
            case 0x3da:
                featureControlRegister = data & 0x10;
                break;
            case 0x3c0:
                if (!attributeRegisterFlipFlop) {
                    data &= 0x3f;
                    attributeRegisterIndex = data;
                } else {
                    int index = attributeRegisterIndex & 0x1f;
                    switch (index) {
                        case AR_INDEX_PALLETE_MIN:
                        case 0x01:
                        case 0x02:
                        case 0x03:
                        case 0x04:
                        case 0x05:
                        case 0x06:
                        case 0x07:
                        case 0x08:
                        case 0x09:
                        case 0x0a:
                        case 0x0b:
                        case 0x0c:
                        case 0x0d:
                        case 0x0e:
                        case AR_INDEX_PALLETE_MAX:
                            attributeRegister[index] = data & 0x3f;
                            break;
                        case AR_INDEX_ATTR_MODE_CONTROL:
                            attributeRegister[AR_INDEX_ATTR_MODE_CONTROL] = data & ~0x10;
                            break;
                        case AR_INDEX_OVERSCAN_COLOR:
                            attributeRegister[AR_INDEX_OVERSCAN_COLOR] = data;
                            break;
                        case AR_INDEX_COLOR_PLANE_ENABLE:
                            attributeRegister[AR_INDEX_COLOR_PLANE_ENABLE] = data & ~0xc0;
                            break;
                        case AR_INDEX_HORIZ_PIXEL_PANNING:
                            attributeRegister[AR_INDEX_HORIZ_PIXEL_PANNING] = data & ~0xf0;
                            break;
                        case AR_INDEX_COLOR_SELECT:
                            attributeRegister[AR_INDEX_COLOR_SELECT] = data & ~0xf0;
                            break;
                        default:
                            break;
                    }
                }
                attributeRegisterFlipFlop = !attributeRegisterFlipFlop;
                break;
            case 0x3c2:
                miscellaneousOutputRegister = data & ~0x10;
                break;
            case 0x3c4:
                sequencerRegisterIndex = data & 0x7;
                break;
            case 0x3c5:
                sequencerRegister[sequencerRegisterIndex] = data & sequencerRegisterMask[sequencerRegisterIndex];
                break;
            case 0x3c7:
                dacReadIndex = data;
                dacSubIndex = 0;
                dacState = 3;
                break;
            case 0x3c8:
                dacWriteIndex = data;
                dacSubIndex = 0;
                dacState = 0;
                break;
            case 0x3c9:
                dacCache[dacSubIndex] = data;
                if (++dacSubIndex == 3) {
                    System.arraycopy(dacCache, 0, palette, ((0xff & dacWriteIndex) * 3), 3);
                    dacSubIndex = 0;
                    dacWriteIndex++;
                }
                break;
            case 0x3ce:
                graphicsRegisterIndex = data & 0x0f;
                break;
            case 0x3cf:
                graphicsRegister[graphicsRegisterIndex] = data & graphicsRegisterMask[graphicsRegisterIndex];
                break;
        }
    }

    private int vgaIOPortReadByte(int address) {
        if ((address >= 0x3b0 && address <= 0x3bf && ((miscellaneousOutputRegister & MOR_COLOR_EMULATION) != 0)) ||
                (address >= 0x3d0 && address <= 0x3df && ((miscellaneousOutputRegister & MOR_COLOR_EMULATION) == 0)))
            return 0xff;

        switch (address) {
            case 0x3c0:
                if (!attributeRegisterFlipFlop) {
                    return attributeRegisterIndex;
                } else {
                    return 0;
                }
            case 0x3c1:
                int index = attributeRegisterIndex & 0x1f;
                if (index < 21) {
                    return attributeRegister[index];
                } else {
                    return 0;
                }
            case 0x3c2:
                return st00;
            case 0x3c4:
                return sequencerRegisterIndex;
            case 0x3c5:
                return sequencerRegister[sequencerRegisterIndex];
            case 0x3c7:
                return dacState;
            case 0x3c8:
                return dacWriteIndex;
            case 0x3c9:
                int val = palette[dacReadIndex * 3 + dacSubIndex];
                if (++dacSubIndex == 3) {
                    dacSubIndex = 0;
                    dacReadIndex++;
                }
                return val;
            case 0x3ca:
                return featureControlRegister;
            case 0x3cc:
                return miscellaneousOutputRegister;
            case 0x3ce:
                return graphicsRegisterIndex;
            case 0x3cf:
                return graphicsRegister[graphicsRegisterIndex];
            case 0x3b4:
            case 0x3d4:
                return crtRegisterIndex;
            case 0x3b5:
            case 0x3d5:
                return crtRegister[crtRegisterIndex];
            case 0x3ba:
            case 0x3da:
                attributeRegisterFlipFlop = false;
                if (updatingScreen) {
                    st01 &= ~ST01_V_RETRACE; //claim we are not in vertical retrace (in the process of screen refresh)
                    st01 &= ~ST01_DISP_ENABLE; //is set when in h/v retrace (i.e. if e-beam is off, but we claim always on)
                } else {
                    st01 ^= (ST01_V_RETRACE | ST01_DISP_ENABLE); //if not updating toggle to fool polling in some vga code
                }
                return st01;
            default:
                return 0x00;
        }
    }

    private void vbeIOPortWriteIndex(int data) {
        vbeIndex = data;
    }

    private void vbeIOPortWriteData(int data) {
        if (vbeIndex <= VBE_DISPI_INDEX_NB) {
            switch (vbeIndex) {
                case VBE_DISPI_INDEX_ID:
                    if (data == VBE_DISPI_ID0 || data == VBE_DISPI_ID1 || data == VBE_DISPI_ID2)
                        vbeRegs[vbeIndex] = data;
                    break;
                case VBE_DISPI_INDEX_XRES:
                    if ((data <= VBE_DISPI_MAX_XRES) && ((data & 7) == 0))
                        vbeRegs[vbeIndex] = data;
                    break;
                case VBE_DISPI_INDEX_YRES:
                    if (data <= VBE_DISPI_MAX_YRES)
                        vbeRegs[vbeIndex] = data;
                    break;
                case VBE_DISPI_INDEX_BPP:
                    if (data == 0)
                        data = 8;
                    if (data == 4 || data == 8 || data == 15 ||
                            data == 16 || data == 24 || data == 32) {
                        vbeRegs[vbeIndex] = data;
                    }
                    break;
                case VBE_DISPI_INDEX_BANK:
                    data &= vbeBankMask;
                    vbeRegs[vbeIndex] = data;
                    bankOffset = data << 16;
                    break;
                case VBE_DISPI_INDEX_ENABLE:
                    if ((data & VBE_DISPI_ENABLED) != 0) {
                        vbeRegs[VBE_DISPI_INDEX_VIRT_WIDTH] = vbeRegs[VBE_DISPI_INDEX_XRES];
                        vbeRegs[VBE_DISPI_INDEX_VIRT_HEIGHT] = vbeRegs[VBE_DISPI_INDEX_YRES];
                        vbeRegs[VBE_DISPI_INDEX_X_OFFSET] = 0;
                        vbeRegs[VBE_DISPI_INDEX_Y_OFFSET] = 0;

                        if (vbeRegs[VBE_DISPI_INDEX_BPP] == 4)
                            vbeLineOffset = vbeRegs[VBE_DISPI_INDEX_XRES] >>> 1;
                        else
                            vbeLineOffset = vbeRegs[VBE_DISPI_INDEX_XRES] * ((vbeRegs[VBE_DISPI_INDEX_BPP] + 7) >>> 3);

                        vbeStartAddress = 0;

                        /* clear the screen (should be done in BIOS) */
                        if ((data & VBE_DISPI_NOCLEARMEM) == 0) {
                            int limit = vbeRegs[VBE_DISPI_INDEX_YRES] * vbeLineOffset;
                            for (int i = 0; i < limit; i++) {
                                ioRegion.setByte(i, (byte) 0);
                            }
                        }

                        /* we initialise the VGA graphic mode */
                        /* (should be done in BIOS) */
                        /* graphic mode + memory map 1 */
                        graphicsRegister[GR_INDEX_MISC] = (graphicsRegister[GR_INDEX_MISC] & ~0x0c) | 0x05;
                        crtRegister[CR_INDEX_CRTC_MODE_CONTROL] |= 0x3; /* no CGA modes */
                        crtRegister[CR_INDEX_OFFSET] = (vbeLineOffset >>> 3);
                        /* width */
                        crtRegister[CR_INDEX_HORZ_DISPLAY_END] = (vbeRegs[VBE_DISPI_INDEX_XRES] >>> 3) - 1;
                        /* height */
                        int h = vbeRegs[VBE_DISPI_INDEX_YRES] - 1;
                        crtRegister[CR_INDEX_VERT_DISPLAY_END] = h;
                        crtRegister[CR_INDEX_OVERFLOW] = (crtRegister[CR_INDEX_OVERFLOW] & ~0x42) | ((h >>> 7) & 0x02) | ((h >>> 3) & 0x40);
                        /* line compare to 1023 */
                        crtRegister[CR_INDEX_LINE_COMPARE] = 0xff;
                        crtRegister[CR_INDEX_OVERFLOW] |= 0x10;
                        crtRegister[CR_INDEX_MAX_SCANLINE] |= 0x40;

                        int shiftControl;
                        if (vbeRegs[VBE_DISPI_INDEX_BPP] == 4) {
                            shiftControl = 0;
                            sequencerRegister[SR_INDEX_CLOCKING_MODE] &= ~0x8; /* no double line */
                        } else {
                            shiftControl = 2;
                            sequencerRegister[SR_INDEX_SEQ_MEMORY_MODE] |= 0x08; /* set chain 4 mode */
                            sequencerRegister[SR_INDEX_MAP_MASK] |= 0x0f; /* activate all planes */
                        }
                        graphicsRegister[GR_INDEX_GRAPHICS_MODE] = (graphicsRegister[GR_INDEX_GRAPHICS_MODE] & ~0x60) | (shiftControl << 5);
                        crtRegister[CR_INDEX_MAX_SCANLINE] &= ~0x9f; /* no double scan */
                    } else {
                        /* XXX: the bios should do that */
                        bankOffset = 0;
                    }
                    vbeRegs[vbeIndex] = data;
                    break;
                case VBE_DISPI_INDEX_VIRT_WIDTH: {
                    if (data < vbeRegs[VBE_DISPI_INDEX_XRES])
                        return;
                    int w = data;
                    int lineOffset;
                    if (vbeRegs[VBE_DISPI_INDEX_BPP] == 4) {
                        lineOffset = data >>> 1;
                    } else {
                        lineOffset = data * ((vbeRegs[VBE_DISPI_INDEX_BPP] + 7) >>> 3);
                    }
                    int h = VGA_RAM_SIZE / lineOffset;
                    /* XXX: support wierd bochs semantics ? */
                    if (h < vbeRegs[VBE_DISPI_INDEX_YRES])
                        return;
                    vbeRegs[VBE_DISPI_INDEX_VIRT_WIDTH] = w;
                    vbeRegs[VBE_DISPI_INDEX_VIRT_HEIGHT] = h;
                    vbeLineOffset = lineOffset;
                }
                break;
                case VBE_DISPI_INDEX_X_OFFSET:
                case VBE_DISPI_INDEX_Y_OFFSET: {
                    vbeRegs[vbeIndex] = data;
                    vbeStartAddress = vbeLineOffset * vbeRegs[VBE_DISPI_INDEX_Y_OFFSET];
                    int x = vbeRegs[VBE_DISPI_INDEX_X_OFFSET];
                    if (vbeRegs[VBE_DISPI_INDEX_BPP] == 4) {
                        vbeStartAddress += x >>> 1;
                    } else {
                        vbeStartAddress += x * ((vbeRegs[VBE_DISPI_INDEX_BPP] + 7) >>> 3);
                    }
                    vbeStartAddress >>>= 2;
                }
                break;
                default:
                    DummyDebugger.getInstance().onMessage(WARN, "Invalid VBE write mode: vbeIndex=" + vbeIndex + "\n");
                    break;
            }
        }

    }

    private int vbeIOPortReadIndex() {
        return vbeIndex;
    }

    private int vbeIOPortReadData() {
        if (vbeIndex <= VBE_DISPI_INDEX_NB) {
            return vbeRegs[vbeIndex];
        } else {
            return 0;
        }
    }

    @Override
    public void write(int address, long v, int width) {
        switch (width) {
            case 8:
                vgaIOPortWriteByte(address, (int) v);
                break;
            case 16: {
                ioPortWrite16(address, (int) v);
            }
            break;
            case 32: {
                ioPortWrite32(address, (int) v);
            }
            break;
            default:
                throw new NotImplException();
        }
    }

    @Override
    public long read(int address, int width) {
        switch (width) {
            case 8:
                return vgaIOPortReadByte(address);
            case 16:
                return ioPortRead16(address);
            case 32:
                return ioPortRead32(address);
            default:
                throw new NotImplException();
        }
    }


    @Override
    public void reset() {
        ioportRegistered = false;
        memoryRegistered = false;
        pciRegistered = false;

        setFuncNum(-1);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x1234, 16); // Dummy
        putConfig(PCI_CONFIG_DEVICE_ID, 0x1111, 16);
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0300, 16); // VGA Controller
        putConfig(PCI_CONFIG_HEADER, 0x00, 8); // header_type

        if (sequencerRegister == null) {
            sequencerRegister = new int[256];
            graphicsRegister = new int[256];
            attributeRegister = new int[256];
            crtRegister = new int[256];

            dacCache = new int[3];
            palette = new int[768];

            lastChar = new int[CH_ATTR_SIZE];
            fontOffset = new int[2];

            ioRegion = new VGARAMIORegion();
            lowIORegion = new VGALowMemoryRegion();

            vbeRegs = new int[VBE_DISPI_INDEX_NB + 1];
        }

        vbeRegs[VBE_DISPI_INDEX_ID] = VBE_DISPI_ID0;
        vbeBankMask = ((VGA_RAM_SIZE >>> 16) - 1);
        vbeRegs[VBE_DISPI_INDEX_XRES] = 1600;
        vbeRegs[VBE_DISPI_INDEX_YRES] = 1200;
        vbeRegs[VBE_DISPI_INDEX_BPP] = 32;

        this.internalReset();
    }


    private final void internalReset() {
        latch = 0;
        sequencerRegisterIndex = graphicsRegisterIndex = attributeRegisterIndex = crtRegisterIndex = 0;
        attributeRegisterFlipFlop = false;
        miscellaneousOutputRegister = 0;
        featureControlRegister = 0;
        st00 = st01 = 0; // status 0 and 1
        dacState = dacSubIndex = dacReadIndex = dacWriteIndex = 0;
        shiftControl = doubleScan = 0;
        bankOffset = 0;
        vbeIndex = 0;
        vbeStartAddress = 0;
        vbeLineOffset = 0;
        vbeBankMask = 0;
        graphicMode = 0;
        lineOffset = 0;
        lineCompare = 0;
        startAddress = 0;
        planeUpdated = 0;
        lastCW = lastCH = 0;
        lastWidth = lastHeight = 0;
        lastScreenWidth = lastScreenHeight = 0;
        cursorStart = cursorEnd = 0;
        cursorOffset = 0;

        graphicMode = -1;
        bankOffset = 0;
    }

    public class VGALowMemoryRegion {

        public void lock(int addr) {
        }

        public void unlock(int addr) {
        }

        public void copyContentsIntoArray(int address, byte[] buffer, int off, int len) {
            throw new IllegalStateException("copyContentsInto: Invalid Operation for VGA Card");
        }

        public void copyArrayIntoContents(int address, byte[] buffer, int off, int len) {
            throw new IllegalStateException("copyContentsFrom: Invalid Operation for VGA Card");
        }

        public long getSize() {
            return 0x20000;
        }

        public boolean isAllocated() {
            return false;
        }

        public byte getByte(int offset) {
                    /* convert to VGA memory offset */
            int memoryMapMode = (graphicsRegister[GR_INDEX_MISC] >>> 2) & 3;
            offset &= 0x1ffff;
            switch (memoryMapMode) {
                case 0:
                    break;
                case 1:
                    if (offset >= 0x10000)
                        return (byte) 0xff;
                    offset += bankOffset;
                    break;
                case 2:
                    offset -= 0x10000;
                    if ((offset >= 0x8000) || (offset < 0))
                        return (byte) 0xff;
                    break;
                default:
                case 3:
                    offset -= 0x18000;
                    if (offset < 0)
                        return (byte) 0xff;
                    break;
            }

            if ((sequencerRegister[SR_INDEX_SEQ_MEMORY_MODE] & 0x08) != 0) {
                        /* chain 4 mode : simplest access */
                //return vramPtr[address];
                return ioRegion.getByte(offset);
            } else if ((graphicsRegister[GR_INDEX_GRAPHICS_MODE] & 0x10) != 0) {
                        /* odd/even mode (aka text mode mapping) */
                int plane = (graphicsRegister[GR_INDEX_READ_MAP_SELECT] & 2) | (offset & 1);
                return ioRegion.getByte(((offset & ~1) << 1) | plane);
            } else {
                        /* standard VGA latched access */
                latch = ioRegion.getDoubleWord(4 * offset);

                if ((graphicsRegister[GR_INDEX_GRAPHICS_MODE] & 0x08) == 0) {
                            /* read mode 0 */
                    return (byte) (latch >>> (graphicsRegister[GR_INDEX_READ_MAP_SELECT] * 8));
                } else {
                            /* read mode 1 */
                    int ret = (latch ^ mask16[graphicsRegister[GR_INDEX_COLOR_COMPARE]])
                            & mask16[graphicsRegister[GR_INDEX_COLOR_DONT_CARE]];
                    ret |= ret >>> 16;
                    ret |= ret >>> 8;
                    return (byte) (~ret);
                }
            }
        }

        public short getWord(int offset) {
            int v = 0xFF & getByte(offset);
            v |= getByte(offset + 1) << 8;
            return (short) v;
        }

        public int getDoubleWord(int offset) {
            int v = 0xFF & getByte(offset);
            v |= (0xFF & getByte(offset + 1)) << 8;
            v |= (0xFF & getByte(offset + 2)) << 16;
            v |= (0xFF & getByte(offset + 3)) << 24;
            return v;
        }

        public long getQuadWord(int offset) {
            long v = 0xFFl & getByte(offset);
            v |= (0xFFl & getByte(offset + 1)) << 8;
            v |= (0xFFl & getByte(offset + 2)) << 16;
            v |= (0xFFl & getByte(offset + 3)) << 24;
            v |= (0xFFl & getByte(offset + 4)) << 32;
            v |= (0xFFl & getByte(offset + 5)) << 40;
            v |= (0xFFl & getByte(offset + 6)) << 48;
            v |= (0xFFl & getByte(offset + 7)) << 56;
            return v;
        }

        public long getLowerDoubleQuadWord(int offset) {
            return getQuadWord(offset);
        }

        public long getUpperDoubleQuadWord(int offset) {
            return getQuadWord(offset + 8);
        }

        public void setByte(int offset, byte data) {
                    /* convert to VGA memory offset */
            int memoryMapMode = (graphicsRegister[GR_INDEX_MISC] >>> 2) & 3;
            offset &= 0x1ffff;
            switch (memoryMapMode) {
                case 0:
                    break;
                case 1:
                    if (offset >= 0x10000)
                        return;
                    offset += bankOffset;
                    break;
                case 2:
                    offset -= 0x10000;
                    if ((offset >= 0x8000) || (offset < 0))
                        return;
                    break;
                default:
                case 3:
                    offset -= 0x18000;
                    //should be (unsigned) if (offset >= 0x8000) but anding above "offset &= 0x1ffff;" means <=> the below
                    if (offset < 0)
                        return;
                    break;
            }

            if ((sequencerRegister[SR_INDEX_SEQ_MEMORY_MODE] & 0x08) != 0) {
                        /* chain 4 mode : simplest access */
                int plane = offset & 3;
                int mask = 1 << plane;
                if ((sequencerRegister[SR_INDEX_MAP_MASK] & mask) != 0) {
                    ioRegion.setByte(offset, data);
                    planeUpdated |= mask; // only used to detect font change
                    //cpu_physical_memory_set_dirty
                }
            } else if ((graphicsRegister[GR_INDEX_GRAPHICS_MODE] & 0x10) != 0) {
                        /* odd/even mode (aka text mode mapping) */
                int plane = (graphicsRegister[GR_INDEX_READ_MAP_SELECT] & 2) | (offset & 1);
                int mask = 1 << plane;
                if ((sequencerRegister[SR_INDEX_MAP_MASK] & mask) != 0) {
                    ioRegion.setByte(((offset & ~1) << 1) | plane, data);
                    planeUpdated |= mask; // only used to detect font change
                    //cpu_physical_memory_set_dirty
                }
            } else {
                        /* standard VGA latched access */
                int bitMask = 0;
                int writeMode = graphicsRegister[GR_INDEX_GRAPHICS_MODE] & 3;
                int intData = 0xff & data;
                switch (writeMode) {
                    default:
                    case 0:
                                /* rotate */
                        int b = graphicsRegister[GR_INDEX_DATA_ROTATE] & 7;
                        intData |= intData << 8;
                        intData |= intData << 16;
                        intData = (intData >>> b) | (intData << -b);
                        //Integer.rotateRight(intData, b);

                                /* apply set/reset mask */
                        int setMask = mask16[graphicsRegister[GR_INDEX_ENABLE_SETRESET]];
                        intData = (intData & ~setMask) | (mask16[graphicsRegister[GR_INDEX_SETRESET]] & setMask);
                        bitMask = graphicsRegister[GR_INDEX_BITMASK];
                        break;
                    case 1:
                        intData = latch;
                        int mask = sequencerRegister[SR_INDEX_MAP_MASK];
                        planeUpdated |= mask; // only used to detect font change
                        int writeMask = mask16[mask];
                        //check address being used here;
                        offset <<= 2;
                        ioRegion.setDoubleWord(offset, (ioRegion.getDoubleWord(offset) & ~writeMask) | (intData & writeMask));
                        return;
                    case 2:
                        intData = mask16[intData & 0x0f];
                        bitMask = graphicsRegister[GR_INDEX_BITMASK];
                        break;
                    case 3:
                                /* rotate */
                        b = graphicsRegister[GR_INDEX_DATA_ROTATE] & 7;
                        intData = ((intData >>> b) | (intData << (8 - b)));
                        bitMask = graphicsRegister[GR_INDEX_BITMASK] & intData;
                        intData = mask16[graphicsRegister[GR_INDEX_SETRESET]];
                        break;
                }

                        /* apply logical operation */
                int funcSelect = graphicsRegister[GR_INDEX_DATA_ROTATE] >>> 3;
                switch (funcSelect) {
                    default:
                    case 0:
                                /* nothing to do */
                        break;
                    case 1:
                                /* and */
                        intData &= latch;
                        break;
                    case 2:
                                /* or */
                        intData |= latch;
                        break;
                    case 3:
                                /* xor */
                        intData ^= latch;
                        break;
                }

                        /* apply bit mask */
                bitMask |= bitMask << 8;
                bitMask |= bitMask << 16;
                intData = (intData & bitMask) | (latch & ~bitMask);

                        /* mask data according to sequencerRegister[SR_INDEX_MAP_MASK] */
                int mask = sequencerRegister[SR_INDEX_MAP_MASK];
                planeUpdated |= mask; // only used to detect font change
                int writeMask = mask16[mask];
                offset <<= 2;
                //check address being used here;
                ioRegion.setDoubleWord(offset, (ioRegion.getDoubleWord(offset) & ~writeMask) | (intData & writeMask));
            }
        }

        public void setWord(int offset, short data) {
            setByte(offset++, (byte) data);
            data >>>= 8;
            setByte(offset, (byte) data);
        }

        public void setDoubleWord(int offset, int data) {
            setByte(offset++, (byte) data);
            data >>>= 8;
            setByte(offset++, (byte) data);
            data >>>= 8;
            setByte(offset++, (byte) data);
            data >>>= 8;
            setByte(offset, (byte) data);
        }

        public void setQuadWord(int offset, long data) {
            setDoubleWord(offset, (int) data);
            setDoubleWord(offset + 4, (int) (data >> 32));
        }

        public void setLowerDoubleQuadWord(int offset, long data) {
            setDoubleWord(offset, (int) data);
            setDoubleWord(offset + 4, (int) (data >> 32));
        }

        public void setUpperDoubleQuadWord(int offset, long data) {
            offset += 8;
            setDoubleWord(offset, (int) data);
            setDoubleWord(offset + 4, (int) (data >> 32));
        }

        public void clear() {
            internalReset();
        }

        public void clear(int start, int length) {
            clear();
        }

        public void loadInitialContents(int address, byte[] buf, int off, int len) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class VGARAMIORegion implements IORegion {
        private byte[] buffer;
        private int startAddress;
        private boolean[] dirtyPages;

        public VGARAMIORegion() {
            buffer = new byte[INIT_VGA_RAM_SIZE];
            dirtyPages = new boolean[(VGA_RAM_SIZE >>> PAGE_SHIFT) + 1];
            for (int i = 0; i < dirtyPages.length; i++)
                dirtyPages[i] = false;

            startAddress = -1;
        }

        public void dumpState(DataOutput output) throws IOException {
            output.writeInt(startAddress);
            output.writeInt(buffer.length);
            output.write(buffer);
            output.writeInt(dirtyPages.length);
            for (int i = 0; i < dirtyPages.length; i++)
                output.writeBoolean(dirtyPages[i]);
        }

        private void increaseVGARAMSize(int offset) {
            if ((offset < 0) || (offset >= VGA_RAM_SIZE))
                throw new ArrayIndexOutOfBoundsException("tried to access outside of memory bounds");

            int newSize = buffer.length;
            while (newSize <= offset)
                newSize = newSize << 1;

            if (newSize > VGA_RAM_SIZE)
                newSize = VGA_RAM_SIZE;

            byte[] newBuf = new byte[newSize];
            System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
            buffer = newBuf;
        }

        public void copyContentsIntoArray(int address, byte[] buf, int off, int len) {
            System.arraycopy(buffer, address, buf, off, len);
        }

        public void copyArrayIntoContents(int address, byte[] buf, int off, int len) {
            System.arraycopy(buf, off, buffer, address, len);
        }

        public void clear() {
            for (int i = 0; i < buffer.length; i++)
                buffer[i] = 0;

            for (int i = 0; i < dirtyPages.length; i++)
                dirtyPages[i] = false;
        }

        public void clear(int start, int length) {
            int limit = start + length;
            if (limit > getSize()) throw new ArrayIndexOutOfBoundsException("Attempt to clear outside of memory bounds");
            try {
                for (int i = start; i < limit; i++)
                    buffer[i] = 0;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            int pageStart = start >>> PAGE_SHIFT;
            int pageLimit = (limit - 1) >>> PAGE_SHIFT;
            for (int i = pageStart; i <= pageLimit; i++)
                dirtyPages[i] = true;
        }

        public boolean pageIsDirty(int i) {
            return dirtyPages[i];
        }

        public void cleanPage(int i) {
            dirtyPages[i] = false;
        }

        //IORegion Methods
        public int getAddress() {
            return startAddress;
        }

        public long getSize() {
            return VGA_RAM_SIZE;
        }

        public int getType() {
            return PCI_ADDRESS_SPACE_MEM_PREFETCH;
        }

        public int getRegionNumber() {
            return 0;
        }

        public void setAddress(int address) {
            this.startAddress = address;
        }

        public void setByte(int offset, byte data) {
            try {
                dirtyPages[offset >>> PAGE_SHIFT] = true;
                buffer[offset] = data;
            } catch (ArrayIndexOutOfBoundsException e) {
                increaseVGARAMSize(offset);
                setByte(offset, data);
            }
        }

        public byte getByte(int offset) {
            try {
                return buffer[offset];
            } catch (ArrayIndexOutOfBoundsException e) {
                increaseVGARAMSize(offset);
                return getByte(offset);
            }
        }

        public void setWord(int offset, short data) {
            try {
                buffer[offset] = (byte) data;
                dirtyPages[offset >>> PAGE_SHIFT] = true;
                offset++;
                buffer[offset] = (byte) (data >> 8);
                dirtyPages[offset >>> PAGE_SHIFT] = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                increaseVGARAMSize(offset);
                setWord(offset, data);
            }
        }

        public short getWord(int offset) {
            try {
                int result = 0xFF & buffer[offset];
                offset++;
                result |= buffer[offset] << 8;
                return (short) result;
            } catch (ArrayIndexOutOfBoundsException e) {
                increaseVGARAMSize(offset);
                return getWord(offset);
            }
        }

        public void setDoubleWord(int offset, int data) {
            try {
                dirtyPages[offset >>> PAGE_SHIFT] = true;
                buffer[offset] = (byte) data;
                offset++;
                data >>= 8;
                buffer[offset] = (byte) (data);
                offset++;
                data >>= 8;
                buffer[offset] = (byte) (data);
                offset++;
                data >>= 8;
                buffer[offset] = (byte) (data);
                dirtyPages[offset >>> PAGE_SHIFT] = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                increaseVGARAMSize(offset);
                setDoubleWord(offset, data);
            }
        }

        public int getDoubleWord(int offset) {
            try {
                int result = 0xFF & buffer[offset];
                offset++;
                result |= (0xFF & buffer[offset]) << 8;
                offset++;
                result |= (0xFF & buffer[offset]) << 16;
                offset++;
                result |= (buffer[offset]) << 24;
                return result;
            } catch (ArrayIndexOutOfBoundsException e) {
                increaseVGARAMSize(offset);
                return getDoubleWord(offset);
            }
        }

        public String toString() {
            return "VGA RAM ByteArray[" + getSize() + "]";
        }

        public boolean isAllocated() {
            return true;
        }

        public void loadInitialContents(int address, byte[] buf, int off, int len) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    public void updateDisplay() {
        // note this causes NON-deterministic execution because of thread scheduling
        updatingScreen = true;

        boolean fullUpdate = false;
        int detGraphicMode;
        if ((attributeRegisterIndex & 0x20) == 0)
            detGraphicMode = GMODE_BLANK;
        else
            detGraphicMode = graphicsRegister[GR_INDEX_MISC] & 1;

        if (detGraphicMode != this.graphicMode) {
            this.graphicMode = detGraphicMode;
            fullUpdate = true;
        }

        switch (graphicMode) {
            case GMODE_TEXT:
                drawText(fullUpdate);
                break;
            case GMODE_GRAPH:
                drawGraphic(fullUpdate);
                break;
            case GMODE_BLANK:
            default:
                drawBlank(fullUpdate);
                break;
        }

        updatingScreen = false;
    }

    private void drawText(boolean fullUpdate) {
        boolean temp = updatePalette16();
        fullUpdate |= temp;
        int[] palette = lastPalette;

                /* compute font data address (in plane 2) */
        int v = this.sequencerRegister[SR_INDEX_CHAR_MAP_SELECT];

        int offset = (((v >>> 4) & 1) | ((v << 1) & 6)) * 8192 * 4 + 2;
        if (offset != this.fontOffset[0]) {
            this.fontOffset[0] = offset;
            fullUpdate = true;
        }


        offset = (((v >>> 5) & 1) | ((v >>> 1) & 6)) * 8192 * 4 + 2;
        if (offset != this.fontOffset[1]) {
            this.fontOffset[1] = offset;
            fullUpdate = true;
        }

        if ((this.planeUpdated & (1 << 2)) != 0) {
                    /* if the plane 2 was modified since the last display, it
                      indicates the font may have been modified */
            this.planeUpdated = 0;
            fullUpdate = true;
        }

        temp = updateBasicParameters();
        fullUpdate |= temp;

        int srcIndex = this.startAddress * 4;

                /* total width and height */
        int charHeight = (crtRegister[CR_INDEX_MAX_SCANLINE] & 0x1f) + 1;
        int charWidth = 8;
        if ((sequencerRegister[SR_INDEX_CLOCKING_MODE] & 0x01) == 0)
            charWidth = 9;
        if ((sequencerRegister[SR_INDEX_CLOCKING_MODE] & 0x08) != 0)
            charWidth = 16; /* NOTE: no 18 pixel wide */

        int width = crtRegister[CR_INDEX_HORZ_DISPLAY_END] + 1;
        int height;
        if (crtRegister[CR_INDEX_VERT_TOTAL] == 100) {
                    /* ugly hack for CGA 160x100x16 */
            height = 100;
        } else {
            height = crtRegister[CR_INDEX_VERT_DISPLAY_END] | ((crtRegister[CR_INDEX_OVERFLOW] & 0x02) << 7) | ((crtRegister[CR_INDEX_OVERFLOW] & 0x40) << 3);
            height = (height + 1) / charHeight;
        }

        if ((height * width) > CH_ATTR_SIZE) {
                    /* better than nothing: exit if transient size is too big */
            return;
        }

        if ((width != this.lastWidth) || (height != this.lastHeight) || (charWidth != this.lastCW) || (charHeight != this.lastCH)) {
            this.lastScreenWidth = width * charWidth;
            this.lastScreenHeight = height * charHeight;
            resizeDisplay(this.lastScreenWidth, this.lastScreenHeight);
            this.lastWidth = width;
            this.lastHeight = height;
            this.lastCH = charHeight;
            this.lastCW = charWidth;
            fullUpdate = true;
        }

        int curCursorOffset = ((crtRegister[CR_INDEX_CURSOR_LOC_HIGH] << 8) | crtRegister[CR_INDEX_CURSOR_LOC_LOW]) - this.startAddress;

        if ((curCursorOffset != this.cursorOffset) || (crtRegister[CR_INDEX_CURSOR_START] != this.cursorStart) || (crtRegister[CR_INDEX_CURSOR_END] != this.cursorEnd)) {
                    /* if the cursor position changed, we updated the old and new
                      chars */
            if ((this.cursorOffset < CH_ATTR_SIZE) && (this.cursorOffset >= 0))
                this.lastChar[this.cursorOffset] = -1;
            if ((curCursorOffset < CH_ATTR_SIZE) && (curCursorOffset >= 0))
                this.lastChar[curCursorOffset] = -1;

            this.cursorOffset = curCursorOffset;
            this.cursorStart = crtRegister[CR_INDEX_CURSOR_START];
            this.cursorEnd = crtRegister[CR_INDEX_CURSOR_END];
        }

        int cursorIndex = (this.startAddress + this.cursorOffset) * 4;
        int lastCharOffset = 0;

        switch (charWidth) {
            case 8:
                for (int charY = 0; charY < height; charY++) {
                    int srcOffset = srcIndex;
                    for (int charX = 0; charX < width; charX++) {
                        int charShort = 0xffff & ioRegion.getWord(srcOffset);
                        if (fullUpdate || (charShort != this.lastChar[lastCharOffset])) {
                            this.lastChar[lastCharOffset] = charShort;

                            int character = 0xff & charShort;
                            int characterAttribute = charShort >>> 8;

                            int glyphOffset = fontOffset[(characterAttribute >>> 3) & 1] + 32 * 4 * character;
                            int backgroundColor = palette[characterAttribute >>> 4];
                            int foregroundColor = palette[characterAttribute & 0xf];

                            drawGlyph8(getDisplayBuffer(), charY * charHeight * lastScreenWidth + charX * 8,
                                    lastScreenWidth, glyphOffset, charHeight, foregroundColor, backgroundColor);
                            dirtyDisplayRegion(charX * 8, charY * charHeight, 8, charHeight);

                            if ((srcOffset == cursorIndex) && ((crtRegister[CR_INDEX_CURSOR_START] & 0x20) == 0)) {
                                int lineStart = crtRegister[CR_INDEX_CURSOR_START] & 0x1f;
                                int lineLast = crtRegister[CR_INDEX_CURSOR_END] & 0x1f;
                                        /* XXX: check that */
                                if (lineLast > charHeight - 1)
                                    lineLast = charHeight - 1;

                                if ((lineLast >= lineStart) && (lineStart < charHeight)) {
                                    int tempHeight = lineLast - lineStart + 1;
                                    drawCursorGlyph8(getDisplayBuffer(), (charY * charHeight + lineStart) * lastScreenWidth + charX * 8,
                                            lastScreenWidth, tempHeight, foregroundColor, backgroundColor);
                                    dirtyDisplayRegion(charX * 8, charY * charHeight + lineStart, 8, tempHeight);
                                }
                            }
                        }
                        srcOffset += 4;
                        lastCharOffset++;
                    }
                    srcIndex += lineOffset;
                }
                return;
            case 9:
                for (int charY = 0; charY < height; charY++) {
                    int srcOffset = srcIndex;
                    for (int charX = 0; charX < width; charX++) {
                        int charShort = 0xffff & ioRegion.getWord(srcOffset);
                        if (fullUpdate || (charShort != this.lastChar[lastCharOffset])) {
                            this.lastChar[lastCharOffset] = charShort;

                            int character = 0xff & charShort;
                            int characterAttribute = charShort >>> 8;

                            int glyphOffset = fontOffset[(characterAttribute >>> 3) & 1] + 32 * 4 * character;
                            int backgroundColor = palette[characterAttribute >>> 4];
                            int foregroundColor = palette[characterAttribute & 0xf];

                            boolean dup9 = ((character >= 0xb0) && (character <= 0xdf) && ((attributeRegister[AR_INDEX_ATTR_MODE_CONTROL] & 0x04) != 0));
                            drawGlyph9(getDisplayBuffer(), charY * charHeight * lastScreenWidth + charX * 9, lastScreenWidth,
                                    glyphOffset, charHeight, foregroundColor, backgroundColor, dup9);
                            dirtyDisplayRegion(charX * 9, charY * charHeight, 9, charHeight);

                            if ((srcOffset == cursorIndex) && ((crtRegister[CR_INDEX_CURSOR_START] & 0x20) == 0)) {
                                int lineStart = crtRegister[CR_INDEX_CURSOR_START] & 0x1f;
                                int lineLast = crtRegister[CR_INDEX_CURSOR_END] & 0x1f;
                                        /* XXX: check that */
                                if (lineLast > charHeight - 1)
                                    lineLast = charHeight - 1;

                                if ((lineLast >= lineStart) && (lineStart < charHeight)) {
                                    int tempHeight = lineLast - lineStart + 1;
                                    drawCursorGlyph9(getDisplayBuffer(), (charY * charHeight + lineStart) * lastScreenWidth + charX * 9,
                                            lastScreenWidth, tempHeight, foregroundColor, backgroundColor);
                                    dirtyDisplayRegion(charX * 9, charY * charHeight + lineStart, 9, tempHeight);
                                }
                            }
                        }
                        srcOffset += 4;
                        lastCharOffset++;
                    }
                    srcIndex += lineOffset;
                }
                return;
            case 16:
                for (int charY = 0; charY < height; charY++) {
                    int srcOffset = srcIndex;
                    for (int charX = 0; charX < width; charX++) {
                        int charShort = 0xffff & ioRegion.getWord(srcOffset);
                        if (fullUpdate || (charShort != this.lastChar[lastCharOffset])) {
                            this.lastChar[lastCharOffset] = charShort;

                            int character = 0xff & charShort;
                            int characterAttribute = charShort >>> 8;

                            int glyphOffset = fontOffset[(characterAttribute >>> 3) & 1] + 32 * 4 * character;
                            int backgroundColor = palette[characterAttribute >>> 4];
                            int foregroundColor = palette[characterAttribute & 0xf];

                            drawGlyph16(getDisplayBuffer(), charY * charHeight * lastScreenWidth + charX * 16,
                                    lastScreenWidth, glyphOffset, charHeight, foregroundColor, backgroundColor);
                            dirtyDisplayRegion(charX * 16, charY * charHeight, 16, charHeight);

                            if ((srcOffset == cursorIndex) && ((crtRegister[CR_INDEX_CURSOR_START] & 0x20) == 0)) {
                                int lineStart = crtRegister[CR_INDEX_CURSOR_START] & 0x1f;
                                int lineLast = crtRegister[CR_INDEX_CURSOR_END] & 0x1f;
                                        /* XXX: check that */
                                if (lineLast > charHeight - 1)
                                    lineLast = charHeight - 1;

                                if ((lineLast >= lineStart) && (lineStart < charHeight)) {
                                    int tempHeight = lineLast - lineStart + 1;
                                    drawCursorGlyph16(getDisplayBuffer(), (charY * charHeight + lineStart) * lastScreenWidth + charX * 16,
                                            lastScreenWidth, tempHeight, foregroundColor, backgroundColor);
                                    dirtyDisplayRegion(charX * 16, charY * charHeight + lineStart, 16, tempHeight);
                                }
                            }
                        }
                        srcOffset += 4;
                        lastCharOffset++;
                    }
                    srcIndex += lineOffset;
                }
                return;
            default:
                mb.pc.getDebugger().onMessage(WARN, "Unknown character width %d\n", charWidth);
                return;
        }
    }

    abstract class GraphicsUpdater {

        abstract int byteWidth(int width);

        abstract void drawLine(int offset, int width, int y, int dispWidth);

        void updateDisplay(int width, int height, int dispWidth, boolean fullUpdate, int multiScan) {
            int multiRun = multiScan;
            int addr1 = 4 * startAddress;
            //int lineSize = width; // get the line size from the display device??

            // if the "cursor_invalidate" function pointer is not null, then call it here.
            //if (s->cursor_invalidate)
            //s->cursor_invalidate(s);

            int y1 = 0;
            boolean addrMunge1 = (crtRegister[CR_INDEX_CRTC_MODE_CONTROL] & 1) == 0;
            boolean addrMunge2 = (crtRegister[CR_INDEX_CRTC_MODE_CONTROL] & 2) == 0;
            boolean addrMunge = addrMunge1 || addrMunge2;
            int mask = (crtRegister[CR_INDEX_CRTC_MODE_CONTROL] & 3) ^ 3;

            int pageMin = Integer.MAX_VALUE;
            int pageMax = Integer.MIN_VALUE;

            for (int y = 0; y < height; y++) {
                int addr = addr1;

                if (addrMunge) {
                    if (addrMunge1) {
                                /* CGA compatibility handling */
                        int shift = 14 + ((crtRegister[CR_INDEX_CRTC_MODE_CONTROL] >>> 6) & 1);
                        addr = (addr & ~(1 << shift)) | ((y1 & 1) << shift);
                    }

                    if (addrMunge2)
                        addr = (addr & ~0x8000) | ((y1 & 2) << 14);
                }

                int pageStart = addr >>> PAGE_SHIFT;
                int pageEnd = (addr + byteWidth(width) - 1) >>> PAGE_SHIFT;
                for (int i = pageStart; i <= pageEnd; i++) {
                    if (fullUpdate || ioRegion.pageIsDirty(i)) {
                        pageMin = Math.min(pageMin, pageStart);
                        pageMax = Math.max(pageMax, pageEnd);
                        drawLine(addr, width, y, dispWidth);
                        // if the "cursor_draw_line" function pointer is not null, then call it here.
                        //if (s->cursor_draw_line)
                        //   s->cursor_draw_line(s, d, y);
                        break;
                    }
                }

                if (multiRun == 0) {
                    if ((y1 & mask) == mask)
                        addr1 += lineOffset;
                    y1++;
                    multiRun = multiScan;
                } else
                    multiRun--;

                        /* line compare acts on the displayed lines */
                if (y == lineCompare)
                    addr1 = 0;
            }

            for (int i = pageMin; i <= pageMax; i++)
                ioRegion.cleanPage(i);
        }
    }

    class DrawLine2 extends GraphicsUpdater {
        int byteWidth(int width) {
            return (width / 2);
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();
            int index = y * dispWidth;

            int[] palette = lastPalette;
            int planeMask = mask16[attributeRegister[AR_INDEX_COLOR_PLANE_ENABLE] & 0xf];
            width >>>= 3;

            do {
                int data = ioRegion.getDoubleWord(offset);
                data &= planeMask;

                int v = expand2[data & 0xff];
                v |= expand2[(data >>> 16) & 0xff] << 2;
                dest[index++] = palette[v >>> 12];
                dest[index++] = palette[(v >>> 8) & 0xf];
                dest[index++] = palette[(v >>> 4) & 0xf];
                dest[index++] = palette[(v >>> 0) & 0xf];

                v = expand2[(data >>> 8) & 0xff];
                v |= expand2[(data >>> 24) & 0xff] << 2;
                dest[index++] = palette[v >>> 12];
                dest[index++] = palette[(v >>> 8) & 0xf];
                dest[index++] = palette[(v >>> 4) & 0xf];
                dest[index++] = palette[(v >>> 0) & 0xf];
                offset += 4;
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine2d2 extends GraphicsUpdater {
        int byteWidth(int width) {
            return (width / 2);
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();
            int index = y * dispWidth;

            int[] palette = lastPalette;
            int planeMask = mask16[attributeRegister[AR_INDEX_COLOR_PLANE_ENABLE] & 0xf];
            width >>>= 3;

            do {
                int data = ioRegion.getDoubleWord(offset);
                data &= planeMask;

                int v = expand2[data & 0xff];
                v |= expand2[(data >>> 16) & 0xff] << 2;
                dest[index++] = dest[index++] = palette[v >>> 12];
                dest[index++] = dest[index++] = palette[(v >>> 8) & 0xf];
                dest[index++] = dest[index++] = palette[(v >>> 4) & 0xf];
                dest[index++] = dest[index++] = palette[(v >>> 0) & 0xf];

                v = expand2[(data >>> 8) & 0xff];
                v |= expand2[(data >>> 24) & 0xff] << 2;
                dest[index++] = dest[index++] = palette[v >>> 12];
                dest[index++] = dest[index++] = palette[(v >>> 8) & 0xf];
                dest[index++] = dest[index++] = palette[(v >>> 4) & 0xf];
                dest[index++] = dest[index++] = palette[(v >>> 0) & 0xf];
                offset += 4;
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine4 extends GraphicsUpdater {
        int byteWidth(int width) {
            return (width / 2);
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();
            int index = y * dispWidth;

            int[] palette = lastPalette;
            int planeMask = mask16[attributeRegister[AR_INDEX_COLOR_PLANE_ENABLE] & 0xf];
            width >>>= 3;

            do {
                int data = ioRegion.getDoubleWord(offset) & planeMask;

                int v = expand4[data & 0xff];
                data >>>= 8;
                v |= expand4[data & 0xff] << 1;
                data >>>= 8;
                v |= expand4[data & 0xff] << 2;
                data >>>= 8;
                v |= expand4[data & 0xff] << 3;

                dest[index++] = palette[v >>> 28];
                dest[index++] = palette[(v >>> 24) & 0xF];
                dest[index++] = palette[(v >>> 20) & 0xF];
                dest[index++] = palette[(v >>> 16) & 0xF];
                dest[index++] = palette[(v >>> 12) & 0xF];
                dest[index++] = palette[(v >>> 8) & 0xF];
                dest[index++] = palette[(v >>> 4) & 0xF];
                dest[index++] = palette[(v >>> 0) & 0xF];
                offset += 4;
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine4d2 extends GraphicsUpdater {
        int byteWidth(int width) {
            return (width / 2);
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();
            int index = y * dispWidth;

            int[] palette = lastPalette;
            int planeMask = mask16[attributeRegister[AR_INDEX_COLOR_PLANE_ENABLE] & 0xf];
            width >>>= 3;

            do {
                int data = ioRegion.getDoubleWord(offset);
                data &= planeMask;

                int v = expand4[data & 0xff];
                v |= expand4[(data >>> 8) & 0xff] << 1;
                v |= expand4[(data >>> 16) & 0xff] << 2;
                v |= expand4[(data >>> 24) & 0xff] << 3;

                dest[index++] = dest[index++] = palette[v >>> 28];
                dest[index++] = dest[index++] = palette[(v >>> 24) & 0xF];
                dest[index++] = dest[index++] = palette[(v >>> 20) & 0xF];
                dest[index++] = dest[index++] = palette[(v >>> 16) & 0xF];
                dest[index++] = dest[index++] = palette[(v >>> 12) & 0xF];
                dest[index++] = dest[index++] = palette[(v >>> 8) & 0xF];
                dest[index++] = dest[index++] = palette[(v >>> 4) & 0xF];
                dest[index++] = dest[index++] = palette[(v >>> 0) & 0xF];
                offset += 4;
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine8d2 extends GraphicsUpdater {
        int byteWidth(int width) {
            return (width / 2);
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();
            int index = y * dispWidth;

            int[] palette = lastPalette;
            width >>>= 1;

            do {
                int val = palette[0xFF & ioRegion.getByte(offset++)];
                dest[index++] = val;
                dest[index++] = val;
                width--;
            }
            while (width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine8 extends GraphicsUpdater {
        int byteWidth(int width) {
            return width;
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();
            int index = y * dispWidth;

            int[] palette = lastPalette;
            do {
                dest[index] = palette[0xFF & ioRegion.getByte(offset++)];
                index++;
                width--;
            }
            while (width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine15 extends GraphicsUpdater {
        int byteWidth(int width) {
            return width * 2;
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();

            int i = y * dispWidth;
            do {
                int v = 0xffff & ioRegion.getWord(offset);
                int r = (v >>> 7) & 0xf8;
                int g = (v >>> 2) & 0xf8;
                int b = (v << 3) & 0xf8;
                dest[i] = rgbToPixel(r, g, b);
                offset += 2;
                i++;
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine16 extends GraphicsUpdater {
        int byteWidth(int width) {
            return width * 2;
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();

            int i = y * dispWidth;
            do {
                int v = 0xffff & ioRegion.getWord(offset);
                int r = (v >>> 8) & 0xf8;
                int g = (v >>> 3) & 0xfc;
                int b = (v << 3) & 0xf8;
                dest[i] = rgbToPixel(r, g, b);
                offset += 2;
                i++;
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine24 extends GraphicsUpdater {
        int byteWidth(int width) {
            return width * 3;
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();

            int i = y * dispWidth;
            do {
                int b = 0xFF & ioRegion.getByte(offset++);
                int g = 0xFF & ioRegion.getByte(offset++);
                int r = 0xFF & ioRegion.getByte(offset++);

                dest[i++] = rgbToPixel(r, g, b);
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    class DrawLine32 extends GraphicsUpdater {
        int byteWidth(int width) {
            return width * 4;
        }

        void drawLine(int offset, int width, int y, int dispWidth) {
            int[] dest = getDisplayBuffer();

            int i = y * dispWidth;
            do {
                int b = 0xff & ioRegion.getByte(offset++);
                int g = 0xff & ioRegion.getByte(offset++);
                int r = 0xff & ioRegion.getByte(offset++);
                offset++;

                dest[i++] = rgbToPixel(r, g, b);
            } while (--width != 0);

            dirtyDisplayRegion(0, y, dispWidth, 1);
        }
    }

    private void drawGraphic(boolean fullUpdate) {
        boolean temp = updateBasicParameters();
        fullUpdate |= temp;

        int width = (crtRegister[CR_INDEX_HORZ_DISPLAY_END] + 1) * 8;
        int height = (crtRegister[CR_INDEX_VERT_DISPLAY_END] | ((crtRegister[CR_INDEX_OVERFLOW] & 0x02) << 7) | ((crtRegister[CR_INDEX_OVERFLOW] & 0x40) << 3)) + 1;

        int dispWidth = width;
        int shiftControlBuffer = (graphicsRegister[GR_INDEX_GRAPHICS_MODE] >>> 5) & 3;
        int doubleScanBuffer = crtRegister[CR_INDEX_MAX_SCANLINE] >>> 7;

        int multiScan;
        if (shiftControlBuffer != 1)
            multiScan = (((crtRegister[CR_INDEX_MAX_SCANLINE] & 0x1f) + 1) << doubleScanBuffer) - 1;
        else {
                /* in CGA modes, multi_scan is ignored */
                /* XXX: is it correct ? */
            multiScan = doubleScanBuffer;
        }

        if (shiftControlBuffer != shiftControl || doubleScanBuffer != doubleScan) {
            fullUpdate = true;
            this.shiftControl = shiftControlBuffer;
            this.doubleScan = doubleScanBuffer;
        }

        GraphicsUpdater graphicUpdater = null;
        if (shiftControl == 0) {
            temp = updatePalette16();
            fullUpdate |= temp;
            if ((sequencerRegister[SR_INDEX_CLOCKING_MODE] & 8) != 0) {
                graphicUpdater = VGA_DRAW_LINE4D2;
                dispWidth <<= 1;
            } else
                graphicUpdater = VGA_DRAW_LINE4;
        } else if (shiftControl == 1) {
            temp = updatePalette16();
            fullUpdate |= temp;
            if ((sequencerRegister[SR_INDEX_CLOCKING_MODE] & 8) != 0) {
                graphicUpdater = VGA_DRAW_LINE2D2;
                dispWidth <<= 1;
            } else
                graphicUpdater = VGA_DRAW_LINE2;
        } else {
            int bpp = 0;
            if ((vbeRegs[VBE_DISPI_INDEX_ENABLE] & VBE_DISPI_ENABLED) != 0)
                bpp = vbeRegs[VBE_DISPI_INDEX_BPP];

            switch (bpp) {
                default:
                case 0:
                    temp = updatePalette256();
                    fullUpdate |= temp;
                    graphicUpdater = VGA_DRAW_LINE8D2;
                    break;
                case 8:
                    temp = updatePalette256();
                    fullUpdate |= temp;
                    graphicUpdater = VGA_DRAW_LINE8;
                    break;
                case 15:
                    graphicUpdater = VGA_DRAW_LINE15;
                    break;
                case 16:
                    graphicUpdater = VGA_DRAW_LINE16;
                    break;
                case 24:
                    graphicUpdater = VGA_DRAW_LINE24;
                    break;
                case 32:
                    graphicUpdater = VGA_DRAW_LINE32;
                    break;
            }
        }

        if ((dispWidth != lastWidth) || (height != lastHeight)) {
            fullUpdate = true;
            lastScreenWidth = lastWidth = dispWidth;
            lastScreenHeight = lastHeight = height;
            resizeDisplay(lastScreenWidth, lastScreenHeight);
        }

        graphicUpdater.updateDisplay(width, height, dispWidth, fullUpdate, multiScan);
    }

    private final void drawBlank(boolean fullUpdate) {
        if (!fullUpdate)
            return;
        if ((lastScreenWidth <= 0) || (lastScreenHeight <= 0))
            return;

        int[] rawBytes = getDisplayBuffer();
        int black = rgbToPixel(0, 0, 0);
        for (int i = rawBytes.length - 1; i >= 0; i--) {
            rawBytes[i] = black;
        }

        dirtyDisplayRegion(0, 0, lastScreenWidth, lastScreenHeight);
    }

    private final boolean updatePalette16() {
        boolean fullUpdate = false;
        int[] palette = lastPalette;

        for (int colorIndex = AR_INDEX_PALLETE_MIN; colorIndex <= AR_INDEX_PALLETE_MAX; colorIndex++) {
            int v = attributeRegister[colorIndex];
            if ((attributeRegister[AR_INDEX_ATTR_MODE_CONTROL] & 0x80) != 0)
                v = ((attributeRegister[AR_INDEX_COLOR_SELECT] & 0xf) << 4) | (v & 0xf);
            else
                v = ((attributeRegister[AR_INDEX_COLOR_SELECT] & 0xc) << 4) | (v & 0x3f);

            v *= 3;
            int col = rgbToPixel(c6to8(this.palette[v]),
                    c6to8(this.palette[v + 1]),
                    c6to8(this.palette[v + 2]));
            if (col != palette[colorIndex]) {
                fullUpdate = true;
                palette[colorIndex] = col;
            }
        }
        return fullUpdate;
    }

    private final boolean updatePalette256() {
        boolean fullUpdate = false;
        int[] palette = lastPalette;

        for (int i = 0, v = 0; i < 256; i++, v += 3) {
            int col = rgbToPixel(c6to8(this.palette[v]),
                    c6to8(this.palette[v + 1]),
                    c6to8(this.palette[v + 2]));
            if (col != palette[i]) {
                fullUpdate = true;
                palette[i] = col;
            }
        }
        return fullUpdate;
    }

    private final boolean updateBasicParameters() {
        int curStartAddress, curLineOffset;
        if ((vbeRegs[VBE_DISPI_INDEX_ENABLE] & VBE_DISPI_ENABLED) != 0) {
            curLineOffset = this.vbeLineOffset;
            curStartAddress = this.vbeStartAddress;
        } else {
                    /* compute curLineOffset in bytes */
            curLineOffset = crtRegister[CR_INDEX_OFFSET];
            curLineOffset <<= 3;

                    /* starting address */
            curStartAddress = crtRegister[CR_INDEX_START_ADDR_LOW] | (crtRegister[CR_INDEX_START_ADDR_HIGH] << 8);
        }

                /* line compare */
        int curLineCompare = crtRegister[CR_INDEX_LINE_COMPARE] | ((crtRegister[CR_INDEX_OVERFLOW] & 0x10) << 4) | ((crtRegister[CR_INDEX_MAX_SCANLINE] & 0x40) << 3);

        if ((curLineOffset != this.lineOffset) || (curStartAddress != this.startAddress) || (curLineCompare != this.lineCompare)) {
            this.lineOffset = curLineOffset;
            this.startAddress = curStartAddress;
            this.lineCompare = curLineCompare;
            return true;
        }

        return false;
    }

    private static final int c6to8(int v) {
        v &= 0x3f;
        int b = v & 1;
        return (v << 2) | (b << 1) | b;
    }

    private final void drawGlyph8(int[] buffer, int startOffset, int scanSize, int glyphOffset, int charHeight, int foregroundColor, int backgroundColor) {
        int xorColor = backgroundColor ^ foregroundColor;
        scanSize -= 8;

        do {
            int fontData = ioRegion.getByte(glyphOffset);
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            glyphOffset += 4;
            startOffset += scanSize;
        } while (--charHeight != 0);
    }

    private final void drawGlyph16(int[] buffer, int startOffset, int scanSize, int glyphOffset, int charHeight, int foregroundColor, int backgroundColor) {
        int xorColor = backgroundColor ^ foregroundColor;
        scanSize -= 16;

        do {
            int rawData = ioRegion.getByte(glyphOffset);
            int fontData = expand4to8[(rawData >>> 4) & 0x0f];
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            fontData = expand4to8[rawData & 0x0f];
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            glyphOffset += 4;
            startOffset += scanSize;
        } while (--charHeight != 0);
    }

    private final void drawGlyph9(int[] buffer, int startOffset, int scanSize, int glyphOffset, int charHeight, int foregroundColor, int backgroundColor, boolean dup9) {
        int xorColor = backgroundColor ^ foregroundColor;
        scanSize -= 9;

        if (dup9) {
            do {
                int fontData = ioRegion.getByte(glyphOffset);

                for (int i = 7; i >= 0; i--) {
                    int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                    buffer[startOffset++] = pixel;
                }

                buffer[startOffset++] = buffer[startOffset - 2];

                glyphOffset += 4;
                startOffset += scanSize;
            } while (--charHeight != 0);
        } else {
            do {
                int fontData = ioRegion.getByte(glyphOffset);

                for (int i = 7; i >= 0; i--) {
                    int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                    buffer[startOffset++] = pixel;
                }

                buffer[startOffset++] = backgroundColor;

                glyphOffset += 4;
                startOffset += scanSize;
            } while (--charHeight != 0);
        }
    }


    private final void drawCursorGlyph8(int[] buffer, int startOffset, int scanSize, int charHeight, int foregroundColor, int backgroundColor) {
        int xorColor = backgroundColor ^ foregroundColor;
        int glyphOffset = 0;
        scanSize -= 8;

        do {
            int fontData = cursorGlyph[glyphOffset];
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            glyphOffset += 4;
            startOffset += scanSize;
        }
        while (--charHeight != 0);
    }


    private final void drawCursorGlyph16(int[] buffer, int startOffset, int scanSize, int charHeight, int foregroundColor, int backgroundColor) {
        int glyphOffset = 0;
        int xorColor = backgroundColor ^ foregroundColor;
        scanSize -= 16;

        do {
            int rawData = cursorGlyph[glyphOffset];
            int fontData = expand4to8[(rawData >>> 4) & 0x0f];
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            fontData = expand4to8[rawData & 0x0f];
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            glyphOffset += 4;
            startOffset += scanSize;
        }
        while (--charHeight != 0);
    }

    private final void drawCursorGlyph9(int[] buffer, int startOffset, int scanSize, int charHeight, int foregroundColor, int backgroundColor) {
        int glyphOffset = 0;
        int xorColor = backgroundColor ^ foregroundColor;
        scanSize -= 9;

        do {
            int fontData = cursorGlyph[glyphOffset];
            for (int i = 7; i >= 0; i--) {
                int pixel = ((-((fontData >>> i) & 1)) & xorColor) ^ backgroundColor;
                buffer[startOffset++] = pixel;
            }
            buffer[startOffset++] = buffer[startOffset - 2];
            glyphOffset++;
            startOffset += scanSize;
        } while (--charHeight != 0);
    }

    public boolean initialised() {
        return ioportRegistered && pciRegistered && memoryRegistered;
    }

    public boolean updated() {
        return ioportRegistered && pciRegistered && memoryRegistered;
    }

    public String toString() {
        return "VGA Card [Mode: " + lastScreenWidth + " x " + lastScreenHeight + "]";
    }

}
