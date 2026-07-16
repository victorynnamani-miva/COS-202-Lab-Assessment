package model;

/**
 * Concrete LibraryItem representing an academic Journal.
 * Journals are reference-only in many libraries, but here they may be
 * borrowed for a very short period to demonstrate polymorphic behaviour.
 */
public class Journal extends LibraryItem implements Borrowable {

    private String volume;
    private boolean peerReviewed;
    private String borrowedByUserId;

    public Journal(String id, String title, String author, int year, String volume) {
        this(id, title, author, year, volume, false);
    }

    public Journal(String id, String title, String author, int year, String volume, boolean peerReviewed) {
        super(id, title, author, year, "Journal");
        this.volume = volume;
        this.peerReviewed = peerReviewed;
    }

    public String getVolume() { return volume; }
    public boolean isPeerReviewed() { return peerReviewed; }

    @Override
    public String describe() {
        return "Journal: \"" + title + "\" Vol. " + volume + " (" + year + ")"
                + (peerReviewed ? " [Peer-reviewed]" : "");
    }

    @Override
    public int getLoanPeriodDays() {
        return 3; // journals are short-loan, reference-heavy items
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
