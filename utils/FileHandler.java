package utils;

import model.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving/loading library data to plain text files using a simple,
 * hand-rolled JSON-Lines format (one JSON object per line). No external
 * library is required, which keeps the project dependency-free while still
 * satisfying the "persist data to text/JSON files" requirement.
 *
 * Example line for a Book:
 * {"type":"Book","id":"B0001","title":"Java","author":"Bloch","year":2018,"extra":"9780134685991","available":true}
 */
public class FileHandler {

    // ---------------- ITEMS ----------------

    public static void saveItems(List<LibraryItem> items, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            for (LibraryItem item : items) {
                writer.write(itemToJson(item));
                writer.newLine();
            }
        }
    }

    public static List<LibraryItem> loadItems(String filePath) throws IOException {
        List<LibraryItem> items = new ArrayList<>();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return items;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                LibraryItem item = jsonToItem(line);
                if (item != null) items.add(item);
            }
        }
        return items;
    }

    private static String itemToJson(LibraryItem item) {
        String type = item.getCategory();
        String extra;
        boolean peerReviewed = false;
        if (item instanceof Book) extra = ((Book) item).getIsbn();
        else if (item instanceof Magazine) extra = String.valueOf(((Magazine) item).getIssueNumber());
        else if (item instanceof Journal j) {
            extra = j.getVolume();
            peerReviewed = j.isPeerReviewed();
        } else extra = "";

        return "{\"type\":\"" + escape(type) + "\",\"id\":\"" + escape(item.getId())
                + "\",\"title\":\"" + escape(item.getTitle()) + "\",\"author\":\"" + escape(item.getAuthor())
                + "\",\"year\":" + item.getYear() + ",\"extra\":\"" + escape(extra)
                + "\",\"available\":" + item.isAvailable()
                + ",\"timesBorrowed\":" + item.getTimesBorrowed()
                + ",\"peerReviewed\":" + peerReviewed + "}";
    }

    private static LibraryItem jsonToItem(String json) {
        String type = extractString(json, "type");
        String id = extractString(json, "id");
        String title = extractString(json, "title");
        String author = extractString(json, "author");
        int year = (int) extractNumber(json, "year");
        String extra = extractString(json, "extra");
        boolean available = extractBoolean(json, "available");
        int timesBorrowed = (int) extractNumber(json, "timesBorrowed");
        boolean peerReviewed = extractBoolean(json, "peerReviewed");

        LibraryItem item;
        switch (type) {
            case "Book":
                item = new Book(id, title, author, year, extra);
                break;
            case "Magazine":
                int issue = 0;
                try { issue = Integer.parseInt(extra); } catch (NumberFormatException ignored) {}
                item = new Magazine(id, title, author, year, issue);
                break;
            case "Journal":
                item = new Journal(id, title, author, year, extra, peerReviewed);
                break;
            default:
                return null;
        }
        item.setAvailable(available);
        for (int i = 0; i < timesBorrowed; i++) item.registerBorrow();
        return item;
    }

    // ---------------- USERS (including full borrowing history) ----------------

    public static void saveUsers(List<UserAccount> users, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            for (UserAccount user : users) {
                StringBuilder records = new StringBuilder("[");
                List<BorrowRecord> history = user.getBorrowingHistory();
                for (int i = 0; i < history.size(); i++) {
                    if (i > 0) records.append(",");
                    records.append(recordToJson(history.get(i)));
                }
                records.append("]");

                writer.write("{\"userId\":\"" + escape(user.getUserId()) + "\",\"name\":\""
                        + escape(user.getName()) + "\",\"records\":" + records + "}");
                writer.newLine();
            }
        }
    }

    public static List<UserAccount> loadUsers(String filePath) throws IOException {
        List<UserAccount> users = new ArrayList<>();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return users;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String userId = extractString(line, "userId");
                String name = extractString(line, "name");
                UserAccount user = new UserAccount(userId, name);
                for (String recordJson : extractObjectArray(line, "records")) {
                    BorrowRecord record = jsonToRecord(recordJson);
                    if (record != null) user.addRecord(record);
                }
                users.add(user);
            }
        }
        return users;
    }

    private static String recordToJson(BorrowRecord r) {
        return "{\"itemId\":\"" + escape(r.getItemId()) + "\",\"itemTitle\":\"" + escape(r.getItemTitle())
                + "\",\"borrowDate\":\"" + r.getBorrowDate() + "\",\"dueDate\":\"" + r.getDueDate()
                + "\",\"returnDate\":" + (r.getReturnDate() == null ? "null" : "\"" + r.getReturnDate() + "\"")
                + ",\"overdueCharge\":" + r.getOverdueCharge() + "}";
    }

    private static BorrowRecord jsonToRecord(String json) {
        try {
            String itemId = extractString(json, "itemId");
            String itemTitle = extractString(json, "itemTitle");
            java.time.LocalDate borrowDate = java.time.LocalDate.parse(extractString(json, "borrowDate"));
            java.time.LocalDate dueDate = java.time.LocalDate.parse(extractString(json, "dueDate"));
            BorrowRecord record = new BorrowRecord(itemId, itemTitle, borrowDate, dueDate);

            String returnDateStr = extractNullableString(json, "returnDate");
            if (returnDateStr != null) record.setReturnDate(java.time.LocalDate.parse(returnDateStr));
            record.setOverdueCharge(extractNumber(json, "overdueCharge"));
            return record;
        } catch (Exception e) {
            return null; // skip any malformed record rather than crash the whole load
        }
    }

    // ---------------- Combined convenience methods used for auto persistence ----------------

    public static void saveAll(LibraryDatabase database, String itemsPath, String usersPath) throws IOException {
        saveItems(new ArrayList<>(database.getItems()), itemsPath);
        saveUsers(new ArrayList<>(database.getUsers().values()), usersPath);
    }

    /** @return true if any previously-saved data was found and loaded. */
    public static boolean loadAll(LibraryDatabase database, String itemsPath, String usersPath) throws IOException {
        List<LibraryItem> items = loadItems(itemsPath);
        List<UserAccount> users = loadUsers(usersPath);
        if (items.isEmpty() && users.isEmpty()) return false;

        for (LibraryItem item : items) database.addItem(item);
        for (UserAccount user : users) database.addUser(user);

        int maxItemSuffix = 0, maxUserSuffix = 0;
        for (LibraryItem item : items) maxItemSuffix = Math.max(maxItemSuffix, numericSuffix(item.getId()));
        for (UserAccount user : users) maxUserSuffix = Math.max(maxUserSuffix, numericSuffix(user.getUserId()));
        IDGenerator.fastForwardItemCounter(maxItemSuffix);
        IDGenerator.fastForwardUserCounter(maxUserSuffix);
        return true;
    }

    private static int numericSuffix(String id) {
        String digits = id.replaceAll("\\D", "");
        try { return digits.isEmpty() ? 0 : Integer.parseInt(digits); } catch (NumberFormatException e) { return 0; }
    }

    // ---------------- tiny JSON helpers (no external dependency) ----------------

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        while (end > 0 && json.charAt(end - 1) == '\\') { // skip escaped quotes
            end = json.indexOf("\"", end + 1);
        }
        return end == -1 ? "" : json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static double extractNumber(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return 0;
        start += pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean extractBoolean(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return false;
        return json.startsWith("true", start + pattern.length());
    }

    /** Returns the raw string value for a key that may legitimately be JSON null, or null if so. */
    private static String extractNullableString(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        if (json.startsWith("null", start)) return null;
        return extractString(json, key);
    }

    /** Splits a "key":[{...},{...}] array (one level of nested braces) into its raw object strings. */
    private static List<String> extractObjectArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String pattern = "\"" + key + "\":[";
        int start = json.indexOf(pattern);
        if (start == -1) return result;
        int i = start + pattern.length();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
                current.append(c);
            } else if (c == '}') {
                depth--;
                current.append(c);
                if (depth == 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
            } else if (c == ']' && depth == 0) {
                break;
            } else if (depth > 0) {
                current.append(c);
            }
            i++;
        }
        return result;
    }
}
