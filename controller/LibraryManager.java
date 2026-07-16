package controller;

import model.LibraryDatabase;
import model.LibraryItem;

import java.util.Stack;

/**
 * Coordinates catalogue-level admin operations: adding/removing items,
 * undoing the last admin action (Stack) and maintaining a small
 * fixed-size Array cache of the most frequently accessed items.
 */
public class LibraryManager {

    private static final int CACHE_SIZE = 5; // fixed-size array cache

    private final LibraryDatabase database;

    // Stack (java.util.Stack, as required by the spec) for undoing the last
    // admin operation. Each entry is the inverse action to run on undo.
    private final Stack<Runnable> undoStack = new Stack<>();

    // Fixed-size ARRAY (not a List) holding the most frequently accessed items.
    private final LibraryItem[] frequentCache = new LibraryItem[CACHE_SIZE];

    public LibraryManager(LibraryDatabase database) {
        this.database = database;
    }

    public LibraryDatabase getDatabase() { return database; }

    // ---------------- Admin operations with undo support ----------------

    public void addItem(LibraryItem item) {
        database.addItem(item);
        undoStack.push(() -> database.removeItem(item.getId())); // undo = remove it again
    }

    public boolean deleteItem(String itemId) {
        LibraryItem removed = database.findById(itemId);
        if (removed == null) return false;
        boolean ok = database.removeItem(itemId);
        if (ok) {
            undoStack.push(() -> database.addItem(removed)); // undo = re-add it
        }
        return ok;
    }

    /** Pops and executes the last admin action, if any. Returns true if something was undone. */
    public boolean undoLastAction() {
        if (undoStack.isEmpty()) return false;
        Runnable undoAction = undoStack.pop();
        undoAction.run();
        return true;
    }

    public boolean hasUndoableAction() { return !undoStack.isEmpty(); }

    // ---------------- Fixed-size ARRAY cache: most frequently accessed items ----------------

    /**
     * Called every time an item is viewed/processed. Keeps the cache array
     * sorted so index 0 is always the most accessed item (simple insertion
     * into a fixed-size array - no dynamic resizing).
     */
    public void registerAccess(LibraryItem item) {
        item.registerAccess();

        // is it already cached?
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (frequentCache[i] == item) {
                bubbleUp(i);
                return;
            }
        }
        // find an empty slot, or replace the least-accessed slot if the item beats it
        int weakestIndex = 0;
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (frequentCache[i] == null) {
                frequentCache[i] = item;
                bubbleUp(i);
                return;
            }
            if (frequentCache[i].getTimesAccessed() < frequentCache[weakestIndex].getTimesAccessed()) {
                weakestIndex = i;
            }
        }
        if (item.getTimesAccessed() > frequentCache[weakestIndex].getTimesAccessed()) {
            frequentCache[weakestIndex] = item;
            bubbleUp(weakestIndex);
        }
    }

    /** Moves a newly-updated slot toward index 0 while it out-ranks its left neighbour. */
    private void bubbleUp(int index) {
        while (index > 0 && frequentCache[index] != null && frequentCache[index - 1] != null
                && frequentCache[index].getTimesAccessed() > frequentCache[index - 1].getTimesAccessed()) {
            LibraryItem temp = frequentCache[index - 1];
            frequentCache[index - 1] = frequentCache[index];
            frequentCache[index] = temp;
            index--;
        }
    }

    public LibraryItem[] getFrequentCache() { return frequentCache; }

    // ---------------- Recursive category count (delegates to model layer) ----------------
    public int countByCategory(String category) {
        return database.countItemsByCategoryRecursive(category);
    }
}
