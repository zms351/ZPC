package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public abstract class VGACard extends BasePCIDevice {

    public MotherBoard mb;

    private static final int VGA_RAM_SIZE = 16 * 1024 * 1024;
    private static final int INIT_VGA_RAM_SIZE = 64 * 1024;
    private static final int PAGE_SHIFT = 12;

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

    public VGACard(MotherBoard mb) {
        super(mb.pciBus);

        lastPalette = new int[256];
        sequencerRegister = new int[256];
        graphicsRegister = new int[256];
        attributeRegister = new int[256];
        crtRegister = new int[256];

        dacCache = new int[3];
        palette = new int[768];


        vbeRegs = new int[VBE_DISPI_INDEX_NB + 1];

        fontOffset = new int[2];
        lastChar = new int[CH_ATTR_SIZE];

        this.internalReset();

        bankOffset = 0;

        vbeRegs[VBE_DISPI_INDEX_ID] = VBE_DISPI_ID0;
        vbeBankMask = ((VGA_RAM_SIZE >>> 16) - 1);

        this.mb = mb;
        this.init();
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
        setFuncNum(-1);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x1234, 16); // Dummy
        putConfig(PCI_CONFIG_DEVICE_ID, 0x1111, 16);
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0300, 16); // VGA Controller
        putConfig(PCI_CONFIG_HEADER, 0x00, 8); // header_type

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
                                //ioRegion.setByte(i, (byte) 0);
                                System.out.println("to impl");
                                //todo
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
                    DummyDebugger.getInstance().onMessage(WARN,"Invalid VBE write mode: vbeIndex=" + vbeIndex+"\n");
                    break;
            }
        }

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

    @Override
    public void write(int address, long v, int width) {
        switch (width) {
            case 8:
                vgaIOPortWriteByte(address, (int) v);
                break;
            case 16: {
                switch (address) {
                    case 0x1ce:
                    case 0xff80:
                        vbeIOPortWriteIndex((int) v);
                        break;
                    case 0x1cf:
                    case 0xff81:
                        vbeIOPortWriteData((int) v);
                        break;
                    default:
                        ioPortWrite8(address, 0xFF & ((int) v));
                        ioPortWrite8(address + 1, 0xFF & ((int) (v >>> 8)));
                        break;
                }
            }
            break;
            case 32: {
                ioPortWrite16(address, 0xFFFF & ((int) v));
                ioPortWrite16(address + 2, (int) (v >>> 16));
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
            case 32:
                int b0 = 0xFFFF & ioPortRead16(address);
                int b1 = 0xFFFF & ioPortRead16(address + 2);
                return b0 | (b1 << 16);
            default:
                throw new NotImplException();
        }
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

    private int vbeIOPortReadData() {
        if (vbeIndex <= VBE_DISPI_INDEX_NB) {
            return vbeRegs[vbeIndex];
        } else {
            return 0;
        }
    }

    private int vbeIOPortReadIndex() {
        return vbeIndex;
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

    @Override
    public void reset() {
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

        lastChar = new int[CH_ATTR_SIZE];
        fontOffset = new int[2];
        vbeRegs = new int[VBE_DISPI_INDEX_NB + 1];
        dacCache = new int[3];
        palette = new int[768];
        sequencerRegister = new int[256];
        graphicsRegister = new int[256];
        attributeRegister = new int[256];
        crtRegister = new int[256];

        graphicMode = -1;
    }

}
