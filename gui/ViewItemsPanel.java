package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;

/**
 * "View Items" tab - simple, read-only catalogue browser with a refresh
 * button and a live count of items, using BorderLayout as the outer layout.
 */
public class ViewItemsPanel extends JPanel {

    private final LibraryManager manager;
    private final LibraryTableModel tableModel = new LibraryTableModel();
    private final JLabel countLabel = new JLabel();
    private final MainWindow mainWindow;

    public ViewItemsPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        table.setRowHeight(24);
        table.setToolTipText("Double-click a row to mark it as accessed (updates frequency cache)");

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        LibraryItem item = tableModel.getItemAt(row);
                        manager.registerAccess(item);
                        mainWindow.setStatus("Accessed: " + item.getTitle()
                                + " (total accesses: " + item.getTimesAccessed() + ")");
                    }
                }
            }
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setMnemonic('R'); // keyboard shortcut: Alt+R
        refreshButton.setToolTipText("Reload the table from the library database");
        refreshButton.addActionListener(e -> refresh());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Library Catalogue"), BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(countLabel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        tableModel.setItems(manager.getDatabase().getItems());
        countLabel.setText("Total items: " + manager.getDatabase().getItems().size());
    }
}
