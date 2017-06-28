package com.zms.zpc.debugger;

import com.zms.zpc.debugger.util.*;
import com.zms.zpc.emulator.reg.BaseReg;
import com.zms.zpc.support.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class ProcessorRegistersFrame extends UtilityFrame {

    public static final String Title = "Processor Registers";

    public ProcessorRegistersFrame(ZPC pc) {
        super(pc, Title);
        this.design();
        setFrameIcon(pc.loadIcon("variable"));
    }

    private JTable table;
    private DefaultTableModel model;
    private Vector data;

    private void design() {
        Object[] cols = new Object[]{"Register", "7", "6", "5", "4", "3", "2", "1", "0", "Hex"};
        BaseReg[] regs = getFrame().getPc().getProcessor().getRegs().getRootRegs();
        Object[][] data = new Object[regs.length][cols.length];
        for (int i = 0; i < regs.length; i++) {
            data[i][0] = regs[i].getName().toUpperCase();
        }
        model = new DefaultTableModel(data, cols);
        this.data = model.getDataVector();
        table = new JTable(model);

        table.setShowGrid(true);
        table.setGridColor(Color.DARK_GRAY);
        table.setAutoCreateColumnsFromModel(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        TableColumnModel cm = table.getColumnModel();
        TableColumn col=cm.getColumn(0);
        col.setCellRenderer(new CenterTableCellRenderer());
        col.setMinWidth(60);
        CellRenderer r1 = new CellRenderer();
        for (int i = 1; i < cols.length; i++) {
            col=cm.getColumn(i);
            col.setCellRenderer(r1);
            col.setMinWidth(80);
        }
        col.setMinWidth(150);

        JScrollPane scroll = new JScrollPane(table);
        getContentPane().add(scroll, BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(888,Math.round(888*0.618f)));
        table.setFont(new Font("Monospaced",Font.PLAIN,12));
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public Vector getData() {
        return data;
    }

    private static class CellRenderer extends CenterTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Long) {
                long v = (Long) value;
                if (column > 0 && column <= 8) {
                    value = NumberUtils.toBin((v >>> (8 * (8 - column))) & 0xff,8);
                } else if (column == 9) {
                    value = NumberUtils.toHex(v,16);
                }
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
        BaseReg[] regs = getFrame().getPc().getProcessor().getRegs().getRootRegs();
        for (int i = 0; i < regs.length; i++) {
            Long v = regs[i].getRv();
            //v= new Random().nextLong();
            Vector<Object> one = GarUtils.convertAll(data.get(i));
            for (int j = 1; j < 10; j++) {
                one.set(j, v);
            }
        }
        model.fireTableDataChanged();
    }

}
