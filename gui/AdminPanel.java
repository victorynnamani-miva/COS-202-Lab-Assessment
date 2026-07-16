package gui;

import controller.LibraryManager;
import model.*;
import utils.FileHandler;
import utils.IDGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * "Admin" tab. Demonstrates: dynamic components (fields are actually added
 * to / removed from the form at runtime depending on chosen item type - see
 * rebuildDynamicFields), live text-field-update validation, input validation
 * with dialog popups, a file chooser for import/export, and the undo Stack
 * action.
 */
public class AdminPanel extends JPanel {

    private final LibraryManager manager;
    private final MainWindow mainWindow;

    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
    private final JTextField titleField = new JTextField(15);
    private final JTextField authorField = new JTextField(15);
    private final JTextField yearField = new JTextField(6);
    private final JLabel extraLabel = new JLabel("ISBN:");
    private final JTextField extraField = new JTextField(15);
    private final JTextField deleteIdField = new JTextField(8);

    // Dynamic-components requirement: this sub-panel's children are actually
    // added/removed at runtime (not just relabelled) depending on item type.
    private final JPanel dynamicFieldsPanel = new JPanel(new GridBagLayout());
    private final JCheckBox peerReviewedCheck = new JCheckBox("Peer-reviewed?");

    public AdminPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildAddItemForm(), BorderLayout.NORTH);
        add(buildActionsPanel(), BorderLayout.CENTER);
    }

    private JPanel buildAddItemForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Library Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        panel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        panel.add(yearField, gbc);

        // Dynamic component area: its contents are added/removed at runtime
        // (see rebuildDynamicFields) rather than just relabelled.
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(dynamicFieldsPanel, gbc);
        gbc.gridwidth = 1;

        typeCombo.addActionListener(e -> rebuildDynamicFields());
        rebuildDynamicFields(); // build the initial set of fields for "Book"

        // Event-driven "text field update" requirement: live validation as the
        // admin types, turning the border red on non-numeric input.
        yearField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { validateYearFieldLive(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { validateYearFieldLive(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { validateYearFieldLive(); }
        });

        JButton addButton = new JButton("Add Item");
        addButton.setMnemonic('A');
        addButton.setToolTipText("Validates the fields, then adds the new item to the catalogue");
        addButton.addActionListener(e -> handleAddItem());
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(addButton, gbc);

        return panel;
    }

    private void validateYearFieldLive() {
        String text = yearField.getText().trim();
        boolean valid = text.isEmpty() || text.matches("\\d{1,4}");
        yearField.setBorder(valid
                ? UIManager.getBorder("TextField.border")
                : BorderFactory.createLineBorder(new Color(200, 40, 40), 1));
    }

    /**
     * Rebuilds the dynamic-fields panel from scratch for the currently
     * selected item type. Book/Magazine show one extra field; Journal adds
     * a second component (the peer-reviewed checkbox) that does not exist
     * for the other types - a genuine runtime add/remove of components,
     * not just a relabelled field.
     */
    private void rebuildDynamicFields() {
        String type = (String) typeCombo.getSelectedItem();
        if ("Book".equals(type)) extraLabel.setText("ISBN:");
        else if ("Magazine".equals(type)) extraLabel.setText("Issue #:");
        else extraLabel.setText("Volume:");

        dynamicFieldsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        dynamicFieldsPanel.add(extraLabel, gbc);
        gbc.gridx = 1;
        dynamicFieldsPanel.add(extraField, gbc);

        if ("Journal".equals(type)) {
            peerReviewedCheck.setSelected(false);
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
            dynamicFieldsPanel.add(peerReviewedCheck, gbc);
        }

        dynamicFieldsPanel.revalidate();
        dynamicFieldsPanel.repaint();
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Admin Actions"));

        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deleteRow.add(new JLabel("Item ID to delete:"));
        deleteRow.add(deleteIdField);
        JButton deleteButton = new JButton("Delete Item");
        deleteButton.setMnemonic('D');
        deleteButton.addActionListener(e -> handleDelete());
        deleteRow.add(deleteButton);
        panel.add(deleteRow);

        JPanel undoRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton undoButton = new JButton("Undo Last Action");
        undoButton.setMnemonic('U');
        undoButton.setToolTipText("Reverts the most recent add or delete operation");
        undoButton.addActionListener(e -> {
            boolean undone = manager.undoLastAction();
            mainWindow.setStatus(undone ? "Last admin action undone." : "Nothing to undo.");
            mainWindow.refreshAll();
        });
        undoRow.add(undoButton);
        panel.add(undoRow);

        JPanel fileRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton exportButton = new JButton("Export Catalogue...");
        exportButton.setToolTipText("Save the catalogue to a JSON-lines text file");
        exportButton.addActionListener(e -> handleExport());
        JButton importButton = new JButton("Import Catalogue...");
        importButton.setToolTipText("Load a catalogue from a previously exported file");
        importButton.addActionListener(e -> handleImport());
        fileRow.add(exportButton);
        fileRow.add(importButton);
        panel.add(fileRow);

        JPanel reportRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reportButton = new JButton("Generate Reports");
        reportButton.setMnemonic('G');
        reportButton.addActionListener(e -> showReports());
        reportRow.add(reportButton);
        panel.add(reportRow);

        return panel;
    }

    private void handleAddItem() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearText = yearField.getText().trim();
        String extra = extraField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();

        // Input validation with dialog popups
        if (title.isEmpty() || author.isEmpty() || yearText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, author and year are all required.",
                    "Validation error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a whole number, e.g. 2023.",
                    "Validation error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LibraryItem item;
        try {
            switch (type) {
                case "Book" -> item = new Book(IDGenerator.nextItemId("B"), title, author, year, extra);
                case "Magazine" -> {
                    int issue = extra.isEmpty() ? 0 : Integer.parseInt(extra);
                    item = new Magazine(IDGenerator.nextItemId("M"), title, author, year, issue);
                }
                default -> item = new Journal(IDGenerator.nextItemId("J"), title, author, year, extra,
                        peerReviewedCheck.isSelected());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Issue number must be numeric.",
                    "Validation error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        manager.addItem(item);
        mainWindow.setStatus("Added: " + item.describe());
        mainWindow.refreshAll();

        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
        extraField.setText("");
    }

    private void handleDelete() {
        String id = deleteIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the ID of the item to delete.",
                    "Validation error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean removed = manager.deleteItem(id);
        mainWindow.setStatus(removed ? "Deleted item " + id : "No item found with ID " + id);
        mainWindow.refreshAll();
        deleteIdField.setText("");
    }

    private void handleExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export catalogue as JSON-lines text file");
        chooser.setSelectedFile(new File("catalogue_export.json"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                FileHandler.saveItems(manager.getDatabase().getItems(), chooser.getSelectedFile().getAbsolutePath());
                mainWindow.setStatus("Catalogue exported to " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save file: " + ex.getMessage(),
                        "File error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import catalogue from JSON-lines text file");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                List<LibraryItem> loaded = FileHandler.loadItems(chooser.getSelectedFile().getAbsolutePath());
                for (LibraryItem item : loaded) {
                    manager.getDatabase().addItem(item);
                }
                mainWindow.setStatus("Imported " + loaded.size() + " item(s) from "
                        + chooser.getSelectedFile().getName());
                mainWindow.refreshAll();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read file: " + ex.getMessage(),
                        "File error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReports() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== MOST BORROWED ITEMS ===\n");
        List<LibraryItem> items = manager.getDatabase().getItems();
        items.stream()
                .sorted((a, b) -> b.getTimesBorrowed() - a.getTimesBorrowed())
                .limit(5)
                .forEach(i -> sb.append(String.format("  %-25s borrowed %d time(s)%n", i.getTitle(), i.getTimesBorrowed())));

        sb.append("\n=== USERS WITH OVERDUE ITEMS ===\n");
        boolean anyOverdue = false;
        for (UserAccount user : manager.getDatabase().getUsers().values()) {
            for (BorrowRecord record : user.getBorrowingHistory()) {
                if (!record.isReturned() && record.isOverdue(LocalDate.now())) {
                    sb.append("  ").append(user.getName()).append(" - \"")
                            .append(record.getItemTitle()).append("\" due ").append(record.getDueDate()).append("\n");
                    anyOverdue = true;
                }
            }
        }
        if (!anyOverdue) sb.append("  (none)\n");

        sb.append("\n=== CATEGORY DISTRIBUTION (recursive count) ===\n");
        for (String category : new String[]{"Book", "Magazine", "Journal"}) {
            sb.append(String.format("  %-10s %d item(s)%n", category, manager.countByCategory(category)));
        }

        JTextArea area = new JTextArea(sb.toString(), 20, 50);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Library Reports", JOptionPane.INFORMATION_MESSAGE);
    }
}
