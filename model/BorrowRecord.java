package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single borrow transaction, kept inside a UserAccount's history
 * and used for overdue / reporting calculations.
 */
public class BorrowRecord implements Serializable {

    private String itemId;
    private String itemTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null while item is still out
    private double overdueCharge;

    public BorrowRecord(String itemId, String itemTitle, LocalDate borrowDate, LocalDate dueDate) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.overdueCharge = 0.0;
    }

    public String getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public double getOverdueCharge() { return overdueCharge; }
    public void setOverdueCharge(double overdueCharge) { this.overdueCharge = overdueCharge; }

    public boolean isReturned() { return returnDate != null; }

    public boolean isOverdue(LocalDate today) {
        LocalDate reference = (returnDate != null) ? returnDate : today;
        return reference.isAfter(dueDate);
    }

    @Override
    public String toString() {
        return itemTitle + " | borrowed:" + borrowDate + " | due:" + dueDate
                + " | returned:" + (returnDate == null ? "NOT YET" : returnDate)
                + " | charge:" + String.format("%.2f", overdueCharge);
    }
}
