package com.zms.zpc.emulator.board.pci;

/**
 * Created by 张小美 on 17/八月/6.
 * Copyright 2002-2016
 */
public interface IORegion {

    int PCI_ADDRESS_SPACE_MEM = 0x00;
    int PCI_ADDRESS_SPACE_IO = 0x01;
    int PCI_ADDRESS_SPACE_MEM_PREFETCH = 0x08;

    /**
     * Returns the starting address of the area that this region is mapped to.
     *
     * @return starting address of this region.
     */
    int getAddress();

    /**
     * Returns the length of this region in bytes.
     *
     * @return size in bytes.
     */
    long getSize();

    /**
     * Returns an integer representing the type of this region.
     *
     * @return integer type.
     */
    int getType();

    /**
     * Returns the region number or index of this region.
     * <p>
     * In any given PCI device, <code>IORegion</code>s are not required to be
     * contiguous.
     *
     * @return region number.
     */
    int getRegionNumber();

    /**
     * Tells this region that it has been mapped into it's associated address
     * space at the given address.
     *
     * @param address start address of the mapping.
     */
    void setAddress(int address);

}
