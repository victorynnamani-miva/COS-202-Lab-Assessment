package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.*;
import java.awt.*;
import java.util.Queue;

/**
 * "Borrow/Return" tab. Uses GridBagLayout for precise field alignment plus
 * combo boxes, buttons and a queue viewer.
 */
public class BorrowPanel extends JPanel {

    private final LibraryManager manager;
    private final BorrowController borrowController;
    private final MainWindow mainWindow;

    private final JComboBox<String> itemCombo = new JComboBox<>();
    private final JComboBox<String> userCombo = new JComboBox<>();
    private final JTextArea queueArea = new JTextArea(6, 30);

    public BorrowPanel(LibraryManager manager, BorrowController borrowController, MainWindow mainWindow) {
        this.manager = manager;
        this.borrowController = borrowController;
        this.mainWindow = mainWindow;

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Item:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(itemCombo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("User:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(userCombo, gbc);

        gbc.gridwidth = 1;
        JButton borrowButton = new JButton("Borrow");
        borrowButton.setMnemonic('B');
        borrowButton.setToolTipText("Borrow the selected item for the selected user");
        gbc.gridx = 1; gbc.gridy = 2;
        add(borrowButton, gbc);

        JButton returnButton = new JButton("Return");
        returnButton.setMnemonic('T');
        returnButton.setToolTipText("Return the selected item on behalf of the selected user");
        gbc.gridx = 2; gbc.gridy = 2;
        add(returnButton, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        add(new JLabel("Reservation queue for selected item:"), gbc);

        queueArea.setEditable(false);
        gbc.gridy = 4;
        add(new JScrollPane(queueArea), gbc);

        borrowButton.addActionListener(e -> doBorrow());
        returnButton.addActionListener(e -> doReturn());
        itemCombo.addActionListener(e -> refreshQueueView());

        refreshCombos();
    }

    public void refreshCombos() {
        itemCombo.removeAllItems();
        for (LibraryItem item : manager.getDatabase().getItems()) {
            itemCombo.addItem(item.getId() + " - " + item.getTitle());
        }
        userCombo.removeAllItems();
        for (UserAccount user : manager.getDatabase().getUsers().values()) {
            userCombo.addItem(user.getUserId() + " - " + user.getName());
        }
        refreshQueueView();
    }

    private String extractId(Object comboValue) {
        if (comboValue == null) return null;
        return comboValue.toString().split(" - ")[0];
    }

    private void doBorrow() {
        String itemId = extractId(itemCombo.getSelectedItem());
        String userId = extractId(userCombo.getSelectedItem());
        if (itemId == null || userId == null) {
            JOptionPane.showMessageDialog(this, "Please select both an item and a user.",
                    "Input required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String message = borrowController.borrow(itemId, userId);
        mainWindow.setStatus(message);
        mainWindow.refreshAll();
        refreshQueueView();
    }

    private void doReturn() {
        String itemId = extractId(itemCombo.getSelectedItem());
        String userId = extractId(userCombo.getSelectedItem());
        if (itemId == null || userId == null) {
            JOptionPane.showMessageDialog(this, "Please select both an item and a user.",
                    "Input required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String message = borrowController.returnItem(itemId, userId);
        mainWindow.setStatus(message);
        mainWindow.refreshAll();
        refreshQueueView();
    }

    private void refreshQueueView() {
        String itemId = extractId(itemCombo.getSelectedItem());
        queueArea.setText("");
        if (itemId == null) return;
        Queue<String> queue = borrowController.getReservationQueue(itemId);
        if (queue.isEmpty()) {
            queueArea.setText("(no reservations)");
            return;
        }
        int position = 1;
        for (String userId : queue) {
            UserAccount user = manager.getDatabase().findUser(userId);
            queueArea.append(position++ + ". " + (user != null ? user.getName() : userId) + "\n");
        }
    }
}
