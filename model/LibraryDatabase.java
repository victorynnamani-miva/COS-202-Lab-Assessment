package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the entire library catalogue and user base.
 * Demonstrates encapsulation (private fields, controlled access) and
 * composition (LibraryDatabase "has-a" collection of LibraryItem and
 * UserAccount objects).
 */
public class LibraryDatabase {

    private ArrayList<LibraryItem> items;          // required: ArrayList of items
    private Map<String, UserAccount> users;        // userId -> UserAccount

    public LibraryDatabase() {
        items = new ArrayList<>();
        users = new HashMap<>();
    }

    // ---------- Item operations ----------
    public void addItem(LibraryItem item) {
        items.add(item);
    }

    public boolean removeItem(String itemId) {
        return items.removeIf(i -> i.getId().equals(itemId));
    }

    public LibraryItem findById(String itemId) {
        for (LibraryItem item : items) {
            if (item.getId().equals(itemId)) return item;
        }
        return null;
    }

    public ArrayList<LibraryItem> getItems() { return items; }

    public void setItems(ArrayList<LibraryItem> items) { this.items = items; }

    // ---------- User operations ----------
    public void addUser(UserAccount user) {
        users.put(user.getUserId(), user);
    }

    public UserAccount findUser(String userId) {
        return users.get(userId);
    }

    public Map<String, UserAccount> getUsers() { return users; }

    /**
     * Recursive method: computes the total number of items belonging to a
     * category by walking the list recursively instead of using a loop.
     * Demonstrates the "recursive component" requirement.
     */
    public int countItemsByCategoryRecursive(String category, int index) {
        if (index >= items.size()) {
            return 0; // base case
        }
        int countForCurrent = items.get(index).getCategory().equalsIgnoreCase(category) ? 1 : 0;
        return countForCurrent + countItemsByCategoryRecursive(category, index + 1); // recursive case
    }

    public int countItemsByCategoryRecursive(String category) {
        return countItemsByCategoryRecursive(category, 0);
    }

    /** Polymorphic function: processes ANY LibraryItem type identically. */
    public static String processItem(LibraryItem item) {
        item.registerAccess();
        return item.describe() + "  [Loan period: " + item.getLoanPeriodDays() + " days]";
    }
}
