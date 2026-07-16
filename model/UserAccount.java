package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a library patron. Demonstrates composition: a UserAccount
 * "has-a" list of BorrowRecord objects (its borrowing history).
 */
public class UserAccount implements Serializable {

    private String userId;
    private String name;
    private List<BorrowRecord> borrowingHistory;

    public UserAccount(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.borrowingHistory = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public List<BorrowRecord> getBorrowingHistory() { return borrowingHistory; }

    public void addRecord(BorrowRecord record) {
        borrowingHistory.add(record);
    }

    /** Number of items currently out (not yet returned). */
    public int currentlyBorrowedCount() {
        int count = 0;
        for (BorrowRecord r : borrowingHistory) {
            if (!r.isReturned()) count++;
        }
        return count;
    }

    /** Total overdue charge accumulated across all records. */
    public double totalOverdueCharges() {
        double total = 0;
        for (BorrowRecord r : borrowingHistory) {
            total += r.getOverdueCharge();
        }
        return total;
    }

    @Override
    public String toString() {
        return userId + " - " + name + " (" + borrowingHistory.size() + " loans on record)";
    }
}
