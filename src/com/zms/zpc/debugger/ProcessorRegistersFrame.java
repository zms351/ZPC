package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.UtilityFrame;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class ProcessorRegistersFrame extends UtilityFrame {

    public static final String Title="Processor Registers";

    public ProcessorRegistersFrame(ZPC pc) {
        super(pc,Title);
        this.design();
    }

    private JTable table;
    private DefaultTableModel model;
    private Object[] cols;

    private void design() {
        cols=new Object[]{"Register","7","6","5","4","3","2","1","0","Hex"};
        model=new DefaultTableModel(cols,getPc().getPc().getProcessor().getRegs().getRootRegs().length);
        table=new JTable(model);

        table.setShowGrid(true);
        table.setAutoCreateColumnsFromModel(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        getContentPane().add(table, BorderLayout.CENTER);
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public Object[] getCols() {
        return cols;
    }

}
