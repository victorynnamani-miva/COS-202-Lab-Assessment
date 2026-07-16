package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implements the required sorting algorithms from scratch (no
 * Collections.sort / Arrays.sort shortcuts) so students can compare their
 * behaviour and complexity. The GUI lets the user pick both the algorithm
 * and the field (title / author / year).
 */
public class SortEngine {

    public enum Field { TITLE, AUTHOR, YEAR }
    public enum Algorithm { SELECTION, INSERTION, MERGE }

    public void sort(List<LibraryItem> items, Field field, Algorithm algorithm) {
        Comparator<LibraryItem> comparator = comparatorFor(field);
        switch (algorithm) {
            case SELECTION -> selectionSort(items, comparator);
            case INSERTION -> insertionSort(items, comparator);
            case MERGE -> {
                List<LibraryItem> sorted = mergeSort(items, comparator);
                items.clear();
                items.addAll(sorted);
            }
        }
    }

    private Comparator<LibraryItem> comparatorFor(Field field) {
        return switch (field) {
            case TITLE -> Comparator.comparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);
            case AUTHOR -> Comparator.comparing(LibraryItem::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case YEAR -> Comparator.comparingInt(LibraryItem::getYear);
        };
    }

    // ---------------- 1. SELECTION SORT  O(n^2) ----------------
    private void selectionSort(List<LibraryItem> items, Comparator<LibraryItem> cmp) {
        int n = items.size();
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(items.get(j), items.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                LibraryItem temp = items.get(i);
                items.set(i, items.get(minIndex));
                items.set(minIndex, temp);
            }
        }
    }

    // ---------------- 2. INSERTION SORT  O(n^2), fast on nearly-sorted data ----------------
    private void insertionSort(List<LibraryItem> items, Comparator<LibraryItem> cmp) {
        int n = items.size();
        for (int i = 1; i < n; i++) {
            LibraryItem key = items.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(items.get(j), key) > 0) {
                items.set(j + 1, items.get(j));
                j--;
            }
            items.set(j + 1, key);
        }
    }

    // ---------------- 3. MERGE SORT  O(n log n), stable, recursive ----------------
    private List<LibraryItem> mergeSort(List<LibraryItem> items, Comparator<LibraryItem> cmp) {
        if (items.size() <= 1) return new ArrayList<>(items); // base case
        int mid = items.size() / 2;
        List<LibraryItem> left = mergeSort(new ArrayList<>(items.subList(0, mid)), cmp);
        List<LibraryItem> right = mergeSort(new ArrayList<>(items.subList(mid, items.size())), cmp);
        return merge(left, right, cmp);
    }

    private List<LibraryItem> merge(List<LibraryItem> left, List<LibraryItem> right, Comparator<LibraryItem> cmp) {
        List<LibraryItem> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }
}
