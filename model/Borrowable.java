package model;

/**
 * Interface implemented by any LibraryItem that can be borrowed.
 */
public interface Borrowable {
    boolean borrowItem(String userId);
    boolean returnItem();
    boolean isAvailable();
}
