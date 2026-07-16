package gui;

import controller.LibraryManager;
import controller.SearchEngine;
import controller.SortEngine;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * "Search & Sort" tab. Lets the user choose the search algorithm and the
 * sort algorithm/field from dropdowns, then applies it live to the table.
 */
public class SearchSortPanel extends JPanel {

    private final LibraryManager manager;
    private final SearchEngine searchEngine = new SearchEngine();
    private final SortEngine sortEngine = new SortEngine();
    private final LibraryTableModel tableModel = new LibraryTableModel();
    private final MainWindow mainWindow;

    private final JTextField queryField = new JTextField(15);
    private final JComboBox<String> searchFieldCombo = new JComboBox<>(new String[]{"Title", "Author", "Type"});
    private final JComboBox<String> searchAlgoCombo = new JComboBox<>(new String[]{"Linear", "Binary (needs sorted list)", "Recursive"});

    private final JComboBox<String> sortFieldCombo = new JComboBox<>(new String[]{"Title", "Author", "Year"});
    private final JComboBox<String> sortAlgoCombo = new JComboBox<>(new String[]{"Selection Sort", "Insertion Sort", "Merge Sort"});

    private boolean listCurrentlySorted = false;

    public SearchSortPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildControls(), BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event-driven "text field update" requirement: live match-count feedback
        // as the user types, independent of the explicit Search button/algorithm.
        queryField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { liveMatchPreview(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { liveMatchPreview(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { liveMatchPreview(); }
        });

        refresh();
    }

    /** Fires on every keystroke in the query field; gives instant feedback without altering the table. */
    private void liveMatchPreview() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            mainWindow.setStatus("Ready.");
            return;
        }
        SearchEngine.Field field = switch (searchFieldCombo.getSelectedIndex()) {
            case 0 -> SearchEngine.Field.TITLE;
            case 1 -> SearchEngine.Field.AUTHOR;
            default -> SearchEngine.Field.CATEGORY;
        };
        long matchCount = manager.getDatabase().getItems().stream()
                .filter(i -> fieldText(i, field).toLowerCase().contains(query.toLowerCase()))
                .count();
        mainWindow.setStatus(matchCount + " item(s) match \"" + query + "\" so far. Press Search to apply the chosen algorithm.");
    }

    private String fieldText(LibraryItem item, SearchEngine.Field field) {
        return switch (field) {
            case TITLE -> item.getTitle();
            case AUTHOR -> item.getAuthor();
            case CATEGORY -> item.getCategory();
        };
    }

    private JPanel buildControls() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Search for:"));
        searchRow.add(queryField);
        searchRow.add(new JLabel("in"));
        searchRow.add(searchFieldCombo);
        searchRow.add(new JLabel("using"));
        searchRow.add(searchAlgoCombo);
        JButton searchButton = new JButton("Search");
        searchButton.setMnemonic('S');
        searchButton.addActionListener(e -> doSearch());
        searchRow.add(searchButton);
        JButton resetButton = new JButton("Show All");
        resetButton.addActionListener(e -> refresh());
        searchRow.add(resetButton);
        container.add(searchRow);

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortRow.add(new JLabel("Sort by:"));
        sortRow.add(sortFieldCombo);
        sortRow.add(new JLabel("algorithm:"));
        sortRow.add(sortAlgoCombo);
        JButton sortButton = new JButton("Sort");
        sortButton.setMnemonic('O');
        sortButton.addActionListener(e -> doSort());
        sortRow.add(sortButton);
        container.add(sortRow);

        return container;
    }

    public void refresh() {
        tableModel.setItems(manager.getDatabase().getItems());
        listCurrentlySorted = false;
    }

    private void doSearch() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type something to search for.",
                    "Validation error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SearchEngine.Field field = switch (searchFieldCombo.getSelectedIndex()) {
            case 0 -> SearchEngine.Field.TITLE;
            case 1 -> SearchEngine.Field.AUTHOR;
            default -> SearchEngine.Field.CATEGORY;
        };
        String algo = (String) searchAlgoCombo.getSelectedItem();

        List<LibraryItem> items = manager.getDatabase().getItems();
        LibraryItem found;

        if (algo.startsWith("Binary")) {
            if (!listCurrentlySorted) {
                String fieldName = switch (field) {
                    case TITLE -> "Title";
                    case AUTHOR -> "Author";
                    case CATEGORY -> "Type";
                };
                JOptionPane.showMessageDialog(this,
                        "Binary search requires the list to be sorted by the same field first.\n"
                                + "Please Sort by " + fieldName + " first"
                                + (field == SearchEngine.Field.CATEGORY
                                        ? " (Type isn't a sort option, so use Linear or Recursive search instead)." : "."),
                        "Binary search requires sorted data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            found = searchEngine.binarySearch(items, query, field);
        } else if (algo.startsWith("Recursive")) {
            found = searchEngine.recursiveSearch(items, query, field);
        } else {
            found = searchEngine.linearSearch(items, query, field);
        }

        if (found != null) {
            manager.registerAccess(found);
            List<LibraryItem> single = new ArrayList<>();
            single.add(found);
            tableModel.setItems(single);
            mainWindow.setStatus("Found: " + found.describe());
        } else {
            tableModel.setItems(new ArrayList<>());
            mainWindow.setStatus("No item matched \"" + query + "\".");
        }
    }

    private void doSort() {
        SortEngine.Field field = switch (sortFieldCombo.getSelectedIndex()) {
            case 0 -> SortEngine.Field.TITLE;
            case 1 -> SortEngine.Field.AUTHOR;
            default -> SortEngine.Field.YEAR;
        };
        SortEngine.Algorithm algorithm = switch (sortAlgoCombo.getSelectedIndex()) {
            case 0 -> SortEngine.Algorithm.SELECTION;
            case 1 -> SortEngine.Algorithm.INSERTION;
            default -> SortEngine.Algorithm.MERGE;
        };

        List<LibraryItem> items = new ArrayList<>(manager.getDatabase().getItems());
        sortEngine.sort(items, field, algorithm);
        tableModel.setItems(items);
        manager.getDatabase().setItems(new ArrayList<>(items));
        listCurrentlySorted = (field != SortEngine.Field.YEAR); // binary search only supports title/author here
        mainWindow.setStatus("Sorted " + items.size() + " items by " + sortFieldCombo.getSelectedItem()
                + " using " + sortAlgoCombo.getSelectedItem() + ".");
    }
}
