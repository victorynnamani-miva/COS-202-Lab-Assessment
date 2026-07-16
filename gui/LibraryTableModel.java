package gui;

import model.LibraryItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model backing the item tables shown across several tabs.
 */
public class LibraryTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Title", "Author", "Year", "Category", "Status"};
    private List<LibraryItem> rows = new ArrayList<>();

    public void setItems(List<LibraryItem> items) {
        this.rows = new ArrayList<>(items);
        fireTableDataChanged();
    }

    public LibraryItem getItemAt(int row) { return rows.get(row); }

    @Override
    public int getRowCount() { return rows.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LibraryItem item = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getId();
            case 1 -> item.getTitle();
            case 2 -> item.getAuthor();
            case 3 -> item.getYear();
            case 4 -> item.getCategory();
            case 5 -> item.isAvailable() ? "Available" : "Borrowed";
            default -> "";
        };
    }
}
