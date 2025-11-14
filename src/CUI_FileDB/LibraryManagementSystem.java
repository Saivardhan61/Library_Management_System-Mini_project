package CUI_FileDB;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

// ===============================================================
//                LIBRARY MANAGEMENT SYSTEM
// ===============================================================
public class LibraryManagementSystem {
    private static Scanner sc = new Scanner(System.in);
    private static LibraryDatabase db = new LibraryDatabase();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("           LIBRARY MANAGEMENT SYSTEM          ");
        System.out.println("==============================================");

        while (true) {
            System.out.println("\n1. Login as Admin");
            System.out.println("2. Login as Student");
            System.out.println("3. Student Register");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    adminLogin();
                    break;
                case "2":
                    studentLogin();
                    break;
                case "3":
                    register();
                    break;
                case "4":
                    db.saveAll();
                    System.out.println("Data saved successfully. Exiting...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // ---------------- ADMIN LOGIN ----------------
    private static void adminLogin() {
        System.out.print("Enter admin username: ");
        String user = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String pass = sc.nextLine().trim();
        Admin admin = db.findAdmin(user, pass);
        if (admin == null) {
            System.out.println("Invalid admin credentials!");
            return;
        }

        System.out.println("\nWelcome, " + admin.getUsername());
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Issue Book to Student");
            System.out.println("4. Return Book from Student");
            System.out.println("5. View All Transactions");
            System.out.println("6. Logout");
            System.out.print("Choose: ");
            String ch = sc.nextLine();
            switch (ch) {
                case "1":
                    addBook();
                    break;
                case "2":
                    db.viewBooks();
                    break;
                case "3":
                    issueBookByAdmin();
                    break;
                case "4":
                    returnBookByAdmin();
                    break;
                case "5":
                    db.viewTransactions();
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ---------------- ADMIN ISSUE BOOK ----------------
    private static void issueBookByAdmin() {
        System.out.print("Enter Student Username: ");
        String studentUser = sc.nextLine().trim();
        Student s = db.findStudentByUsername(studentUser);
        if (s == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Book ID to issue: ");
        String bookId = sc.nextLine().trim();
        Book b = db.findBook(bookId);
        if (b == null) {
            System.out.println("Book not found!");
            return;
        }
        if (b.getAvailableCopies() <= 0) {
            System.out.println("No available copies!");
            return;
        }

        if (db.findActiveTransaction(s.getUsername(), bookId) != null) {
            System.out.println("This student already has this book issued!");
            return;
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(7);
        Transaction t = new Transaction(bookId, s.getUsername(), issueDate, dueDate);
        db.issueBook(t);
        b.decreaseAvailable();
        db.saveBooks();
        System.out.println("Book issued successfully to " + s.getUsername() + ". Due date: " + dueDate);
    }

    // ---------------- ADMIN RETURN BOOK ----------------
    private static void returnBookByAdmin() {
        System.out.print("Enter Student Username: ");
        String studentUser = sc.nextLine().trim();
        Student s = db.findStudentByUsername(studentUser);
        if (s == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Book ID to return: ");
        String bookId = sc.nextLine().trim();
        Transaction t = db.findActiveTransaction(s.getUsername(), bookId);
        if (t == null) {
            System.out.println("No active transaction for this student and book!");
            return;
        }

        LocalDate returnDate = LocalDate.now();
        t.setReturnDate(returnDate);

        long daysLate = ChronoUnit.DAYS.between(t.getDueDate(), returnDate);
        if (daysLate > 0) {
            double fine = daysLate * 10.0;
            t.setFine(fine);
            System.out.println("Returned late by " + daysLate + " days. Fine: ₹" + fine);
        } else {
            System.out.println("Book returned on time. No fine!");
        }

        Book b = db.findBook(bookId);
        if (b != null)
            b.increaseAvailable();
        db.updateTransaction(t);
    }

    // ---------------- ADD BOOK ----------------
    private static void addBook() {
        System.out.print("Enter Book ID: ");
        String id = sc.nextLine().trim();
        if (db.findBook(id) != null) {
            System.out.println("Book ID already exists!");
            return;
        }
        System.out.print("Enter Book Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Enter Author: ");
        String author = sc.nextLine().trim();
        System.out.print("Enter Total Copies: ");
        int copies;
        try {
            copies = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number!");
            return;
        }
        db.addBook(new Book(id, title, author, copies, copies));
        System.out.println("Book added successfully!");
    }

    // ---------------- STUDENT REGISTER ----------------
    private static void register() {
        System.out.print("Enter new username: ");
        String username = sc.nextLine().trim();
        if (db.findStudentByUsername(username) != null) {
            System.out.println("Username already exists!");
            return;
        }

        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();
        Student newStudent = new Student(username, password);
        db.addUser(newStudent);
        System.out.println("Registration successful! You can now log in.");
    }

    // ---------------- STUDENT LOGIN ----------------
    private static void studentLogin() {
        System.out.print("Enter student username: ");
        String user = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String pass = sc.nextLine().trim();
        Student s = db.findStudent(user, pass);
        if (s == null) {
            System.out.println("Invalid credentials!");
            return;
        }
        System.out.println("\nWelcome, " + s.getUsername());
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. View Available Books");
            System.out.println("2. Issue Book");
            System.out.println("3. Return Book");
            System.out.println("4. View My Transactions");
            System.out.println("5. Logout");
            System.out.print("Choose: ");
            String ch = sc.nextLine();
            switch (ch) {
                case "1":
                    db.viewBooks();
                    break;
                case "2":
                    issueBook(s);
                    break;
                case "3":
                    returnBook(s);
                    break;
                case "4":
                    db.viewStudentTransactions(s.getUsername());
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // ---------------- STUDENT ISSUE BOOK ----------------
    private static void issueBook(Student s) {
        System.out.print("Enter Book ID to issue: ");
        String bookId = sc.nextLine().trim();
        Book b = db.findBook(bookId);
        if (b == null) {
            System.out.println("Book not found!");
            return;
        }
        if (b.getAvailableCopies() <= 0) {
            System.out.println("No available copies!");
            return;
        }
        if (db.findActiveTransaction(s.getUsername(), bookId) != null) {
            System.out.println("You already have this book issued!");
            return;
        }
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(7);
        Transaction t = new Transaction(bookId, s.getUsername(), issueDate, dueDate);
        db.issueBook(t);
        b.decreaseAvailable();
        db.saveBooks();
        System.out.println("Book issued successfully! Due date: " + dueDate);
    }

    // ---------------- STUDENT RETURN BOOK ----------------
    private static void returnBook(Student s) {
        System.out.print("Enter Book ID to return: ");
        String bookId = sc.nextLine().trim();
        Transaction t = db.findActiveTransaction(s.getUsername(), bookId);
        if (t == null) {
            System.out.println("No record of issuing this book!");
            return;
        }
        LocalDate returnDate = LocalDate.now();
        t.setReturnDate(returnDate);

        long daysLate = ChronoUnit.DAYS.between(t.getDueDate(), returnDate);
        if (daysLate > 0) {
            double fine = daysLate * 10.0;
            t.setFine(fine);
            System.out.println("Returned late by " + daysLate + " days. Fine: ₹" + fine);
        } else {
            System.out.println("Book returned on time. No fine!");
        }

        Book b = db.findBook(bookId);
        if (b != null)
            b.increaseAvailable();
        db.updateTransaction(t);
    }
}

// ===============================================================
//                       BOOK CLASS
// ===============================================================
class Book {
    private String id;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public Book(String id, String title, String author, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void decreaseAvailable() {
        if (availableCopies > 0)
            availableCopies--;
    }

    public void increaseAvailable() {
        if (availableCopies < totalCopies)
            availableCopies++;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-30s %-20s Total: %d | Available: %d",
                id, title, author, totalCopies, availableCopies);
    }

    public String serialize() {
        return id + "," + title.replace(",", ";") + "," + author.replace(",", ";") + "," + totalCopies + "," + availableCopies;
    }

    public static Book deserialize(String line) {
        String[] p = line.split(",");
        if (p.length < 5)
            return null;
        return new Book(p[0], p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]));
    }
}

// ===============================================================
//                       USER CLASSES
// ===============================================================
abstract class User {
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }

    public boolean checkCredentials(String u, String p) {
        return username.equals(u) && password.equals(p);
    }

    public abstract String getType();

    public String serialize() {
        return getType() + "," + username + "," + password;
    }

    public static User deserialize(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3)
            return null;
        if (parts[0].equals("ADMIN"))
            return new Admin(parts[1], parts[2]);
        else
            return new Student(parts[1], parts[2]);
    }
}

class Admin extends User {
    public Admin(String username, String password) { super(username, password); }
    @Override public String getType() { return "ADMIN"; }
}

class Student extends User {
    public Student(String username, String password) { super(username, password); }
    @Override public String getType() { return "STUDENT"; }
}

// ===============================================================
//                     TRANSACTION CLASS
// ===============================================================
class Transaction {
    private String bookId;
    private String username;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;

    public Transaction(String bookId, String username, LocalDate issueDate, LocalDate dueDate) {
        this.bookId = bookId;
        this.username = username;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.fine = 0.0;
    }

    public String getBookId() { return bookId; }
    public String getUsername() { return username; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFine() { return fine; }

    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setFine(double fine) { this.fine = fine; }
    public boolean isReturned() { return returnDate != null; }

    public String serialize() {
        return bookId + "," + username + "," + issueDate + "," + dueDate + "," +
                (returnDate == null ? "null" : returnDate.toString()) + "," + fine;
    }

    public static Transaction deserialize(String line) {
        try {
            String[] p = line.split(",");
            if (p.length < 6)
                return null;
            Transaction t = new Transaction(p[0], p[1], LocalDate.parse(p[2]), LocalDate.parse(p[3]));
            if (!p[4].equals("null"))
                t.returnDate = LocalDate.parse(p[4]);
            t.fine = Double.parseDouble(p[5]);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%-10s %-10s Issue: %s Due: %s Return: %s Fine: ₹%.2f",
                bookId, username, issueDate, dueDate, (returnDate == null ? "-" : returnDate.toString()), fine);
    }
}

// ===============================================================
//                 LIBRARY DATABASE CLASS
// ===============================================================
class LibraryDatabase {
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    private static final String BOOK_FILE = "books.txt";
    private static final String USER_FILE = "users.txt";
    private static final String TRANS_FILE = "transactions.txt";

    public LibraryDatabase() {
        loadAll();
        ensureDefaultUsers();
    }

    private void ensureDefaultUsers() {
        boolean hasAdmin = false, hasStudent = false;
        for (User u : users) {
            if (u instanceof Admin && u.getUsername().equals("admin"))
                hasAdmin = true;
            if (u instanceof Student && u.getUsername().equals("student"))
                hasStudent = true;
        }
        if (!hasAdmin)
            users.add(new Admin("admin", "admin"));
        if (!hasStudent)
            users.add(new Student("student", "1234"));
        saveUsers();
    }

    // ---------- BOOK ----------
    public void addBook(Book b) {
        books.add(b);
        saveBooks();
    }

    public Book findBook(String id) {
        for (Book b : books)
            if (b.getId().equals(id))
                return b;
        return null;
    }

    public void viewBooks() {
        System.out.println("\n--- BOOK LIST ---");
        if (books.isEmpty())
            System.out.println("No books available.");
        else
            for (Book b : books)
                System.out.println(b);
    }

    // ---------- USER ----------
    public void addUser(User u) {
        users.add(u);
        saveUsers();
    }

    public Admin findAdmin(String user, String pass) {
        for (User u : users)
            if (u instanceof Admin && u.checkCredentials(user, pass))
                return (Admin) u;
        return null;
    }

    public Student findStudent(String user, String pass) {
        for (User u : users)
            if (u instanceof Student && u.checkCredentials(user, pass))
                return (Student) u;
        return null;
    }

    public Student findStudentByUsername(String username) {
        for (User u : users)
            if (u instanceof Student && u.getUsername().equals(username))
                return (Student) u;
        return null;
    }

    // ---------- TRANSACTION ----------
    public void issueBook(Transaction t) {
        transactions.add(t);
        saveTransactions();
    }

    public Transaction findActiveTransaction(String user, String bookId) {
        for (Transaction t : transactions)
            if (t.getUsername().equals(user) && t.getBookId().equals(bookId) && !t.isReturned())
                return t;
        return null;
    }

    public void updateTransaction(Transaction t) {
        saveTransactions();
    }

    public void viewTransactions() {
        System.out.println("\n--- ALL TRANSACTIONS ---");
        if (transactions.isEmpty())
            System.out.println("No transactions yet.");
        else
            for (Transaction t : transactions)
                System.out.println(t);
    }

    public void viewStudentTransactions(String username) {
        System.out.println("\n--- MY TRANSACTIONS ---");
        boolean any = false;
        for (Transaction t : transactions)
            if (t.getUsername().equals(username)) {
                System.out.println(t);
                any = true;
            }
        if (!any)
            System.out.println("No transactions found.");
    }

    // ---------- FILE I/O ----------
    public void loadAll() {
        loadBooks();
        loadUsers();
        loadTransactions();
    }

    public void saveAll() {
        saveBooks();
        saveUsers();
        saveTransactions();
    }

    private void loadBooks() {
        books.clear();
        File f = new File(BOOK_FILE);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                Book b = Book.deserialize(line);
                if (b != null)
                    books.add(b);
            }
        } catch (Exception e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
    }

    public void saveBooks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOK_FILE))) {
            for (Book b : books)
                pw.println(b.serialize());
        } catch (Exception e) {
            System.out.println("Error saving books: " + e.getMessage());
        }
    }

    private void loadUsers() {
        users.clear();
        File f = new File(USER_FILE);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                User u = User.deserialize(line);
                if (u != null)
                    users.add(u);
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE))) {
            for (User u : users)
                pw.println(u.serialize());
        } catch (Exception e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private void loadTransactions() {
        transactions.clear();
        File f = new File(TRANS_FILE);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                Transaction t = Transaction.deserialize(line);
                if (t != null)
                    transactions.add(t);
            }
        } catch (Exception e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
    }

    private void saveTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TRANS_FILE))) {
            for (Transaction t : transactions)
                pw.println(t.serialize());
        } catch (Exception e) {
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }
}
