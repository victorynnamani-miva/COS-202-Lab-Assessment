package model;

import java.io.Serializable;

/**
 * Abstract base class representing any item held by the library.
 * Concrete subclasses: Book, Magazine, Journal.
 */
public abstract class LibraryItem implements Serializable {

    protected String id;
    protected String title;
    protected String author;
    protected int year;
    protected String category;
    protected boolean available;
    protected int timesAccessed;   // used for "most frequently accessed" cache
    protected int timesBorrowed;   // used for "most borrowed items" report

    public LibraryItem(String id, String title, String author, int year, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.available = true;
        this.timesAccessed = 0;
        this.timesBorrowed = 0;
    }

    // Polymorphic method - every subclass must describe itself differently
    public abstract String describe();

    // Polymorphic method - every subclass returns its loan period in days
    public abstract int getLoanPeriodDays();

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getTimesAccessed() { return timesAccessed; }
    public void registerAccess() { timesAccessed++; }
    public int getTimesBorrowed() { return timesBorrowed; }
    public void registerBorrow() { timesBorrowed++; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %d | %s | %s",
                id, title, author, year, category, available ? "Available" : "Borrowed");
    }
}
