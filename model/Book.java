package model;

/**
 * Concrete LibraryItem representing a Book.
 */
public class Book extends LibraryItem implements Borrowable {

    private String isbn;
    private String borrowedByUserId;

    public Book(String id, String title, String author, int year, String isbn) {
        super(id, title, author, year, "Book");
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }

    @Override
    public String describe() {
        return "Book: \"" + title + "\" by " + author + " (" + year + ") ISBN:" + isbn;
    }

    @Override
    public int getLoanPeriodDays() {
        return 14; // books can be borrowed for 2 weeks
    }

    @Override
    public boolean borrowItem(String userId) {
        if (!available) return false;
        available = false;
        borrowedByUserId = userId;
        registerBorrow();
        return true;
    }

    @Override
    public boolean returnItem() {
        if (available) return false;
        available = true;
        borrowedByUserId = null;
        return true;
    }

    @Override
    public boolean isAvailable() { return available; }
}
