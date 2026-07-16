package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.BorrowRecord;
import model.UserAccount;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Application shell. Uses CardLayout to switch between a splash/welcome
 * screen and the main tabbed dashboard, BorderLayout for the overall frame,
 * a status bar, a menu bar with mnemonics, and a javax.swing.Timer that
 * periodically checks for overdue items (event-driven, timer-triggered
 * reminder requirement).
 */
public class MainWindow extends JFrame {

    private static final String CARD_WELCOME = "welcome";
    private static final String CARD_MAIN = "main";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardContainer = new JPanel();
    private final JLabel statusBar = new JLabel("Ready.");

    private final LibraryManager manager;
    private final BorrowController borrowController;

    private ViewItemsPanel viewItemsPanel;
    private BorrowPanel borrowPanel;
    private AdminPanel adminPanel;
    private SearchSortPanel searchSortPanel;

    public MainWindow(LibraryManager manager, BorrowController borrowController) {
        super("Smart Library Circulation & Automation System (SLCAS)");
        this.manager = manager;
        this.borrowController = borrowController;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveAndExit();
            }
        });
        setSize(950, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardContainer.setLayout(cardLayout);
        cardContainer.add(buildWelcomeCard(), CARD_WELCOME);
        cardContainer.add(buildMainCard(), CARD_MAIN);

        add(cardContainer, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(buildMenuBar());

        cardLayout.show(cardContainer, CARD_WELCOME);

        startOverdueTimer();
    }

    private JPanel buildWelcomeCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Smart Library Circulation & Automation System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, gbc);

        gbc.gridy = 1;
        JLabel subtitle = new JLabel("Victory Nnamani - COS 202 Lab Assessment");
        panel.add(subtitle, gbc);

        gbc.gridy = 2;
        JButton enterButton = new JButton("Enter System");
        enterButton.setMnemonic('E');
        enterButton.addActionListener(e -> cardLayout.show(cardContainer, CARD_MAIN));
        panel.add(enterButton, gbc);

        return panel;
    }

    private JPanel buildMainCard() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        viewItemsPanel = new ViewItemsPanel(manager, this);
        borrowPanel = new BorrowPanel(manager, borrowController, this);
        adminPanel = new AdminPanel(manager, this);
        searchSortPanel = new SearchSortPanel(manager, this);

        tabs.addTab("View Items", viewItemsPanel);
        tabs.addTab("Borrow/Return", borrowPanel);
        tabs.addTab("Admin", adminPanel);
        tabs.addTab("Search & Sort", searchSortPanel);

        tabs.setMnemonicAt(0, java.awt.event.KeyEvent.VK_1);
        tabs.setMnemonicAt(1, java.awt.event.KeyEvent.VK_2);
        tabs.setMnemonicAt(2, java.awt.event.KeyEvent.VK_3);
        tabs.setMnemonicAt(3, java.awt.event.KeyEvent.VK_4);

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        bar.add(statusBar, BorderLayout.WEST);
        return bar;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem saveItem = new JMenuItem("Save Now");
        saveItem.setMnemonic('S');
        saveItem.setToolTipText("Persist the catalogue and user data to the data/ folder immediately");
        saveItem.addActionListener(e -> {
            saveData();
            setStatus("Data saved to " + gui.Main.ITEMS_FILE + " and " + gui.Main.USERS_FILE);
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        exitItem.addActionListener(e -> saveAndExit());
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.setMnemonic('R');
        refreshItem.addActionListener(e -> refreshAll());
        viewMenu.add(refreshItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        return menuBar;
    }

    /** Timer-triggered event: checks for overdue items every 30 seconds and pops a dialog. */
    private void startOverdueTimer() {
        Timer timer = new Timer(30_000, e -> checkOverdueItems());
        timer.setRepeats(true);
        timer.start();
    }

    private void checkOverdueItems() {
        StringBuilder overdueMessage = new StringBuilder();
        for (UserAccount user : manager.getDatabase().getUsers().values()) {
            for (BorrowRecord record : user.getBorrowingHistory()) {
                if (!record.isReturned() && record.isOverdue(LocalDate.now())) {
                    overdueMessage.append(user.getName()).append(" - \"")
                            .append(record.getItemTitle()).append("\" was due ")
                            .append(record.getDueDate()).append("\n");
                }
            }
        }
        if (overdueMessage.length() > 0) {
            statusBar.setText("Overdue reminder issued at " + java.time.LocalTime.now().withNano(0));
            JOptionPane.showMessageDialog(this, overdueMessage.toString(),
                    "Overdue Reminder", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void setStatus(String message) {
        statusBar.setText(message);
    }

    /** Persists the catalogue and users to the data/ folder (auto-persistence requirement). */
    private void saveData() {
        try {
            utils.FileHandler.saveAll(manager.getDatabase(), gui.Main.ITEMS_FILE, gui.Main.USERS_FILE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data on exit: " + ex.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndExit() {
        saveData();
        dispose();
        System.exit(0);
    }

    public void refreshAll() {
        viewItemsPanel.refresh();
        borrowPanel.refreshCombos();
        searchSortPanel.refresh();
    }
}
