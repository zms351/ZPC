package com.zms.zpc.debugger.util;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class CenterTableCellRenderer implements TableCellRenderer {

    private TableCellRenderer parent=new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = parent.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(c instanceof JLabel) {
            JLabel label= (JLabel) c;
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return c;
    }

}
