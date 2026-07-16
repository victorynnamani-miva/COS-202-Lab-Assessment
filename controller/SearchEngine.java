package controller;

import model.LibraryItem;

import java.util.List;

/**
 * Implements the three required search strategies. Binary search is only
 * valid on a list already sorted (ascending) by the same field being
 * searched, so callers must sort first (see SortEngine) or use linear /
 * recursive search on unsorted data.
 */
public class SearchEngine {

    public enum Field { TITLE, AUTHOR, CATEGORY }

    // ---------------- 1. LINEAR SEARCH  O(n) ----------------
    public LibraryItem linearSearch(List<LibraryItem> items, String query, Field field) {
        for (LibraryItem item : items) {
            if (matches(item, query, field)) return item;
        }
        return null;
    }

    // ---------------- 2. BINARY SEARCH  O(log n) ----------------
    // Requires items to be pre-sorted ascending on the chosen field.
    public LibraryItem binarySearch(List<LibraryItem> items, String query, Field field) {
        int low = 0;
        int high = items.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            String midValue = fieldValue(items.get(mid), field);
            int cmp = midValue.compareToIgnoreCase(query);
            if (cmp == 0) return items.get(mid);
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    // ---------------- 3. RECURSIVE SEARCH  O(n) ----------------
    public LibraryItem recursiveSearch(List<LibraryItem> items, String query, Field field, int index) {
        if (index >= items.size()) {
            return null; // base case: reached the end, not found
        }
        if (matches(items.get(index), query, field)) {
            return items.get(index); // base case: found it
        }
        return recursiveSearch(items, query, field, index + 1); // recursive case
    }

    public LibraryItem recursiveSearch(List<LibraryItem> items, String query, Field field) {
        return recursiveSearch(items, query, field, 0);
    }

    private boolean matches(LibraryItem item, String query, Field field) {
        String value = fieldValue(item, field);
        return value.equalsIgnoreCase(query) || value.toLowerCase().contains(query.toLowerCase());
    }

    private String fieldValue(LibraryItem item, Field field) {
        return switch (field) {
            case TITLE -> item.getTitle();
            case AUTHOR -> item.getAuthor();
            case CATEGORY -> item.getCategory();
        };
    }
}
