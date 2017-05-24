package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;
import com.zms.zpc.emulator.processor.Reg;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class ProcessorRegistersFrame extends UtilityFrame {

    public static final String Title = "Processor Registers";

    public ProcessorRegistersFrame(ZPC pc) {
        super(pc, Title);
        this.design();
    }

    private JTable table;
    private DefaultTableModel model;
    private Object[] cols;
    private Object[][] data;

    private void design() {
        cols = new Object[]{"Register", "7", "6", "5", "4", "3", "2", "1", "0", "Hex"};
        Reg[] regs = getPc().getPc().getProcessor().getRegs().getRootRegs();
        data = new Object[regs.length][cols.length];
        for (int i = 0; i < regs.length; i++) {
            data[i][0] = regs[i].getName().toUpperCase();
        }
        model = new DefaultTableModel(data, cols);
        table = new JTable(model);

        table.setShowGrid(true);
        table.setGridColor(Color.DARK_GRAY);
        table.setAutoCreateColumnsFromModel(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.setDefaultRenderer(String.class, new CenterTableCellRenderer());
        table.setDefaultRenderer(Long.class, new CellRenderer());
        JScrollPane scroll = new JScrollPane(table);
        getContentPane().add(scroll, BorderLayout.CENTER);
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

    public Object[][] getData() {
        return data;
    }

    private static class CellRenderer extends CenterTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            long v = (Long) value;
            if (column > 0 && column <= 8) {
                value = Long.toBinaryString((v >>> (column * 8 - 8)) & 0xff);
            } else if (column == 9) {
                value = Long.toHexString(v);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

    }

    private static class HeaderRenderer extends CenterTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setBackground(Color.LIGHT_GRAY);
            }
            return c;
        }

    }

    public void refresh1() {
        Reg[] regs = getPc().getPc().getProcessor().getRegs().getRootRegs();
        for (int i = 0; i < regs.length; i++) {
            Long v = regs[i].getRv();
            for (int j = 1; j < 10; j++) {
                data[i][j] = v;
            }
        }
        model.fireTableDataChanged();
    }

}
