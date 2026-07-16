package controller;

import model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Handles the borrow / return workflow, the reservation waitlist Queue for
 * items that are currently unavailable, and the recursive overdue charge
 * calculation.
 */
public class BorrowController {

    private static final double DAILY_LATE_FEE = 50.0; // currency units per overdue day

    private final LibraryDatabase database;

    // One reservation Queue per item id - required data structure: Queue.
    private final Map<String, Queue<String>> reservationQueues = new HashMap<>();

    public BorrowController(LibraryDatabase database) {
        this.database = database;
    }

    /**
     * Attempts to borrow an item for a user. If the item is unavailable the
     * user is instead placed on the item's reservation queue.
     * @return a status message describing what happened.
     */
    public String borrow(String itemId, String userId) {
        LibraryItem item = database.findById(itemId);
        UserAccount user = database.findUser(userId);
        if (item == null) return "Item not found.";
        if (user == null) return "User not found.";

        if (item instanceof Borrowable borrowable) {
            if (borrowable.isAvailable()) {
                borrowable.borrowItem(userId);
                LocalDate today = LocalDate.now();
                LocalDate due = today.plusDays(item.getLoanPeriodDays());
                user.addRecord(new BorrowRecord(item.getId(), item.getTitle(), today, due));
                return "\"" + item.getTitle() + "\" borrowed successfully. Due back " + due;
            } else {
                reservationQueues.computeIfAbsent(itemId, k -> new LinkedList<>()).add(userId);
                return "\"" + item.getTitle() + "\" is currently unavailable. "
                        + user.getName() + " added to reservation queue.";
            }
        }
        return "This item type cannot be borrowed.";
    }

    /**
     * Returns an item. If a reservation queue exists for it, the item is
     * automatically re-issued to the next user in line (FIFO).
     */
    public String returnItem(String itemId, String returningUserId) {
        LibraryItem item = database.findById(itemId);
        UserAccount returningUser = database.findUser(returningUserId);
        if (item == null) return "Item not found.";
        if (!(item instanceof Borrowable borrowable)) return "This item type cannot be borrowed.";

        // close the borrowing record for the returning user
        if (returningUser != null) {
            for (BorrowRecord record : returningUser.getBorrowingHistory()) {
                if (record.getItemId().equals(itemId) && !record.isReturned()) {
                    record.setReturnDate(LocalDate.now());
                    long overdueDays = computeOverdueDaysRecursive(record, 0);
                    record.setOverdueCharge(overdueDays * DAILY_LATE_FEE);
                    break;
                }
            }
        }
        borrowable.returnItem();

        Queue<String> queue = reservationQueues.get(itemId);
        if (queue != null && !queue.isEmpty()) {
            String nextUserId = queue.poll(); // dequeue next reservation
            UserAccount nextUser = database.findUser(nextUserId);
            if (nextUser != null) {
                borrowable.borrowItem(nextUserId);
                LocalDate today = LocalDate.now();
                LocalDate due = today.plusDays(item.getLoanPeriodDays());
                nextUser.addRecord(new BorrowRecord(item.getId(), item.getTitle(), today, due));
                return "\"" + item.getTitle() + "\" returned and automatically issued to "
                        + nextUser.getName() + " from the reservation queue.";
            }
        }
        return "\"" + item.getTitle() + "\" returned successfully.";
    }

    public Queue<String> getReservationQueue(String itemId) {
        return reservationQueues.getOrDefault(itemId, new LinkedList<>());
    }

    /**
     * Recursive overdue-days calculator. Rather than a single subtraction,
     * it recursively "walks" from the due date to today one day at a time,
     * satisfying the recursion requirement (recursive overdue charge
     * computation) while remaining easy to trace/debug.
     */
    private long computeOverdueDaysRecursive(BorrowRecord record, long accumulatedDays) {
        LocalDate checkDate = record.getDueDate().plusDays(accumulatedDays);
        LocalDate endDate = record.getReturnDate() != null ? record.getReturnDate() : LocalDate.now();
        if (!checkDate.isBefore(endDate)) {
            return accumulatedDays; // base case: no more overdue days remain
        }
        return computeOverdueDaysRecursive(record, accumulatedDays + 1); // recursive case
    }
}
