package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.*;
import utils.FileHandler;
import utils.IDGenerator;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application entry point. Loads previously-saved data if it exists
 * (persistent storage requirement); otherwise seeds sample data on first
 * run. Launches the Swing GUI on the Event Dispatch Thread.
 */
public class Main {

    public static final String DATA_DIR = "data";
    public static final String ITEMS_FILE = DATA_DIR + "/catalogue.json";
    public static final String USERS_FILE = DATA_DIR + "/users.json";

    public static void main(String[] args) {
        LibraryDatabase database = new LibraryDatabase();
        LibraryManager manager = new LibraryManager(database);
        BorrowController borrowController = new BorrowController(database);

        boolean loaded = false;
        try {
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            loaded = FileHandler.loadAll(database, ITEMS_FILE, USERS_FILE);
        } catch (IOException e) {
            System.err.println("Could not load saved data (" + e.getMessage() + "); starting with sample data.");
        }

        if (!loaded) {
            seedSampleData(database);
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow(manager, borrowController);
            window.setVisible(true);
        });
    }

    private static void seedSampleData(LibraryDatabase database) {
        // Books
        database.addItem(new Book(IDGenerator.nextItemId("B"), "Things Fall Apart", "Chinua Achebe", 1958, "978-0385474542"));
        database.addItem(new Book(IDGenerator.nextItemId("B"), "Americanah", "Chimamanda Ngozi Adichie", 2013, "978-0307962126"));
        database.addItem(new Book(IDGenerator.nextItemId("B"), "The Lion and the Jewel", "Wole Soyinka", 1962, "978-0199110834"));
        database.addItem(new Book(IDGenerator.nextItemId("B"), "The Famished Road", "Ben Okri", 1991, "978-0099530510"));

        // Magazines
        database.addItem(new Magazine(IDGenerator.nextItemId("M"), "The Africa Report", "Various", 2025, 142));
        database.addItem(new Magazine(IDGenerator.nextItemId("M"), "New African", "Various", 2026, 615));
        database.addItem(new Magazine(IDGenerator.nextItemId("M"), "TechCabal Quarterly", "Various", 2025, 12));

        // Journals (all peer-reviewed)
        database.addItem(new Journal(IDGenerator.nextItemId("J"), "Nigerian Journal of Technological Research",
                "Federal University of Technology, Minna", 2024, "Vol. 19", true));
        database.addItem(new Journal(IDGenerator.nextItemId("J"), "Journal of African History",
                "Cambridge University Press", 2025, "Vol. 66", true));
        database.addItem(new Journal(IDGenerator.nextItemId("J"), "Ibadan Journal of the Social Sciences",
                "University of Ibadan", 2023, "Vol. 21", true));
        database.addItem(new Journal(IDGenerator.nextItemId("J"), "African Development Review",
                "African Development Bank", 2026, "Vol. 38", true));

        // Users
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Chidi Okafor"));
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Nneka Eze"));
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Blessing Johnson"));
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Tunde Bakare"));
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Olumide Awosika"));
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Zainab Umar"));
        database.addUser(new UserAccount(IDGenerator.nextUserId(), "Efe Omorodion"));
    }
}
