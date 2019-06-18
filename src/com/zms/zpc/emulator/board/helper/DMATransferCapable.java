package com.zms.zpc.emulator.board.helper;

import com.zms.zpc.emulator.board.DMAController;

/**
 * Created by 张小美 on 2019-06-19.
 * Copyright 2002-2016
 */
public interface DMATransferCapable {

    int handleTransfer(DMAController.DMAChannel channel, int position, int size);

}
