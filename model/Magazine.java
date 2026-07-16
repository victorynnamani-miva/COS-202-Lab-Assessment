package model;

/**
 * Concrete LibraryItem representing a Magazine.
 */
public class Magazine extends LibraryItem implements Borrowable {

    private int issueNumber;
    private String borrowedByUserId;

    public Magazine(String id, String title, String author, int year, int issueNumber) {
        super(id, title, author, year, "Magazine");
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() { return issueNumber; }

    @Override
    public String describe() {
        return "Magazine: \"" + title + "\" Issue #" + issueNumber + " (" + year + ")";
    }

    @Override
    public int getLoanPeriodDays() {
        return 7; // magazines only for 1 week
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
