package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Advanced GUI technique: a custom renderer that colours the "Status"
 * column green for Available and red/orange for Borrowed items.
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if ("Available".equals(value)) {
            c.setForeground(isSelected ? Color.WHITE : new Color(20, 130, 40));
        } else if ("Borrowed".equals(value)) {
            c.setForeground(isSelected ? Color.WHITE : new Color(180, 60, 20));
        } else {
            c.setForeground(isSelected ? Color.WHITE : Color.BLACK);
        }
        setHorizontalAlignment(CENTER);
        return c;
    }
}
