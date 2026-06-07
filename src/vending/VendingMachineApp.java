package vending;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * VendingMachineApp is a complete Java/Swing vending-machine management project.
 * It demonstrates GUI operation, custom data structures, file-backed management data,
 * socket transmission to a server, multithreading, exception handling, and comments.
 */
public class VendingMachineApp {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && "--server".equals(args[0])) {
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 5050;
                new VendingServer(port, Paths.get("data/server-db")).start();
                return;
            }
            if (args.length > 0 && "--self-test".equals(args[0])) {
                SelfTest.run();
                return;
            }
            SwingUtilities.invokeLater(() -> new VendingFrame(new VendingMachine("VM-001", Paths.get("data/client-db"))).setVisible(true));
        } catch (Exception ex) {
            ex.printStackTrace(); // Final safety net for launcher-level exceptions.
        }
    }

    /** GUI with independent sales/admin screens; admin mode disables sales controls. */
    static final class VendingFrame extends JFrame {
        private final VendingMachine machine;
        private final CardLayout cards = new CardLayout();
        private final JPanel root = new JPanel(cards);
        private final JPanel drinkPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        private final JLabel balanceLabel = new JLabel();
        private final JLabel statusLabel = new JLabel("Insert money or open admin mode.");
        private final JTextArea adminArea = new JTextArea(14, 48);
        private final AtomicBoolean adminActive = new AtomicBoolean(false);

        VendingFrame(VendingMachine machine) {
            super("JAVA 자판기 관리 프로그램 - " + machine.machineId);
            this.machine = machine;
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(980, 680));
            root.add(buildSalesPanel(), "sales");
            root.add(buildAdminPanel(), "admin");
            add(root);
            machine.startBackgroundThreads(this::refreshAll);
            refreshAll();
        }

        private JPanel buildSalesPanel() {
            JPanel panel = new JPanel(new BorderLayout(12, 12));
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));
            JLabel title = new JLabel("판매 화면 (Sales Screen)", SwingConstants.CENTER);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
            panel.add(title, BorderLayout.NORTH);
            panel.add(new JScrollPane(drinkPanel), BorderLayout.CENTER);

            JPanel money = new JPanel(new GridLayout(0, 1, 6, 6));
            money.add(balanceLabel);
            int[] units = {10, 50, 100, 500, 1000};
            for (int unit : units) {
                JButton b = new JButton(unit + "원 투입");
                b.addActionListener(e -> safeRun(() -> machine.insertMoney(unit), "화폐 투입 실패"));
                money.add(b);
            }
            JButton refund = new JButton("반환");
            refund.addActionListener(e -> safeRun(machine::refund, "반환 실패"));
            JButton admin = new JButton("관리자 메뉴");
            admin.addActionListener(e -> openAdmin());
            money.add(refund);
            money.add(admin);
            money.add(statusLabel);
            panel.add(money, BorderLayout.EAST);
            return panel;
        }

        private JPanel buildAdminPanel() {
            JPanel panel = new JPanel(new BorderLayout(12, 12));
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));
            JLabel title = new JLabel("관리자 전용 화면 (Admin Only)", SwingConstants.CENTER);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
            panel.add(title, BorderLayout.NORTH);
            adminArea.setEditable(false);
            panel.add(new JScrollPane(adminArea), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new GridLayout(0, 2, 8, 8));
            addAdminButton(buttons, "현황 새로고침", () -> {});
            addAdminButton(buttons, "재고 보충", this::restockDialog);
            addAdminButton(buttons, "음료명/가격 변경", this::editDrinkDialog);
            addAdminButton(buttons, "수금", () -> statusLabel.setText(machine.collectCash()));
            addAdminButton(buttons, "비밀번호 변경", this::changePasswordDialog);
            addAdminButton(buttons, "서버 전송", machine::sendSnapshotNow);
            addAdminButton(buttons, "판매화면으로", this::closeAdmin);
            panel.add(buttons, BorderLayout.EAST);
            return panel;
        }

        private void addAdminButton(JPanel panel, String label, Runnable action) {
            JButton b = new JButton(label);
            b.addActionListener(e -> safeRun(action, label + " 실패"));
            panel.add(b);
        }

        private void openAdmin() {
            String password = JOptionPane.showInputDialog(this, "관리자 비밀번호 입력", "Admin Login", JOptionPane.QUESTION_MESSAGE);
            if (password != null && machine.checkPassword(password)) {
                adminActive.set(true); // Sales actions are now unavailable because the card is switched.
                cards.show(root, "admin");
                refreshAll();
            } else if (password != null) {
                JOptionPane.showMessageDialog(this, "비밀번호가 올바르지 않습니다.");
            }
        }

        private void closeAdmin() {
            adminActive.set(false);
            cards.show(root, "sales");
        }

        private void restockDialog() {
            Drink d = chooseDrink();
            if (d == null) return;
            String input = JOptionPane.showInputDialog(this, "보충 수량", "10");
            if (input != null) machine.restock(d.id, Integer.parseInt(input));
        }

        private void editDrinkDialog() {
            Drink d = chooseDrink();
            if (d == null) return;
            String name = JOptionPane.showInputDialog(this, "새 음료명", d.name);
            if (name == null || name.isBlank()) return;
            String price = JOptionPane.showInputDialog(this, "새 가격", d.price);
            if (price != null) machine.updateDrink(d.id, name.trim(), Integer.parseInt(price));
        }

        private void changePasswordDialog() {
            String next = JOptionPane.showInputDialog(this, "새 비밀번호(8자리 이상, 숫자/특수문자 포함)");
            if (next != null) machine.changePassword(next);
        }

        private Drink chooseDrink() {
            List<Drink> list = machine.drinks.toList();
            Object selected = JOptionPane.showInputDialog(this, "음료 선택", "Drink", JOptionPane.QUESTION_MESSAGE, null, list.toArray(), list.get(0));
            return (Drink) selected;
        }

        private void safeRun(Runnable action, String errorTitle) {
            try {
                action.run();
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), errorTitle, JOptionPane.ERROR_MESSAGE);
            }
        }

        private void refreshAll() {
            SwingUtilities.invokeLater(() -> {
                drinkPanel.removeAll();
                for (Drink d : machine.drinks.toList()) {
                    JButton b = new JButton("<html><b>" + d.name + "</b><br/>" + d.price + "원<br/>재고 " + d.stock + "개" + (d.stock <= 0 ? "<br/><font color='red'>품절</font>" : "") + "</html>");
                    b.setEnabled(!adminActive.get() && d.stock > 0 && machine.canBuy(d));
                    b.addActionListener(e -> safeRun(() -> statusLabel.setText(machine.buy(d.id)), "구매 실패"));
                    drinkPanel.add(b);
                }
                balanceLabel.setText("현재 투입금액: " + machine.currentBalance() + "원 / 지폐 " + machine.currentBillAmount() + "원");
                adminArea.setText(machine.adminSummary());
                drinkPanel.revalidate();
                drinkPanel.repaint();
            });
        }
    }

    /** Core business rules for money, sales, stock, persistence, and socket sync. */
    static final class VendingMachine {
        final String machineId;
        final InventoryLinkedList drinks = new InventoryLinkedList();
        final MoneyBox moneyBox = new MoneyBox();
        final SalesDatabase db;
        final EventQueue eventQueue = new EventQueue();
        final ActionStack undoStack = new ActionStack();
        final SalesTree salesTree = new SalesTree();
        private MoneySession session; // Dynamically allocated when first money is inserted and released after sale/refund.
        private String password = "Admin#123";
        private final Path dbDir;

        VendingMachine(String machineId, Path dbDir) {
            this.machineId = machineId;
            this.dbDir = dbDir;
            this.db = new SalesDatabase(dbDir);
            loadDefaults();
            loadState();
        }

        private void loadDefaults() {
            String[] names = {"믹스커피", "고급믹스커피", "물", "캔커피", "이온음료", "고급캔커피", "탄산음료", "특화음료"};
            int[] prices = {200, 300, 450, 500, 550, 700, 750, 800};
            for (int i = 0; i < names.length; i++) drinks.add(new Drink(i + 1, names[i], prices[i], 10));
        }

        private void loadState() {
            try {
                db.ensure();
                password = db.loadPassword(password);
                db.loadDrinks(drinks);
                db.loadSalesInto(salesTree);
            } catch (IOException ex) {
                throw new IllegalStateException("데이터베이스 로드 실패: " + ex.getMessage(), ex);
            }
        }

        synchronized void insertMoney(int unit) {
            if (!MoneyBox.isAccepted(unit)) throw new IllegalArgumentException("허용되지 않는 화폐 단위입니다.");
            if (session == null) session = new MoneySession(); // Dynamic allocation required by assignment.
            session.insert(unit);
            if (session.billAmount > 5000) { session.remove(unit); throw new IllegalArgumentException("지폐 투입 상한은 5,000원입니다."); }
            if (session.balance > 7000) { session.remove(unit); throw new IllegalArgumentException("총 투입 금액은 7,000원을 초과할 수 없습니다."); }
            moneyBox.add(unit, 1);
        }

        synchronized String refund() {
            if (session == null || session.balance == 0) return "반환할 금액이 없습니다.";
            String change = moneyBox.payChange(session.balance);
            session = null; // Dynamic allocation released after refund.
            return "반환 완료: " + change;
        }

        synchronized boolean canBuy(Drink d) {
            return session != null && session.balance >= d.price && d.stock > 0 && moneyBox.canMakeChange(session.balance - d.price);
        }

        synchronized String buy(int id) {
            Drink d = drinks.findByIdLinear(id);
            if (d == null) throw new IllegalArgumentException("음료가 없습니다.");
            if (!canBuy(d)) throw new IllegalStateException("잔액/재고/거스름돈을 확인하세요.");
            d.stock--;
            int change = session.balance - d.price;
            String changeText = moneyBox.payChange(change);
            SalesRecord record = new SalesRecord(LocalDate.now(), machineId, d.id, d.name, d.price, 1);
            try {
                db.appendSale(record);
                db.saveDrinks(drinks);
            } catch (IOException ex) {
                throw new IllegalStateException("판매 저장 실패: " + ex.getMessage(), ex);
            }
            salesTree.insert(record);
            undoStack.push("SALE:" + d.id + ":" + d.price); // Stack requirement: recent management/sales trace.
            eventQueue.offer("SALE|" + record.toWire()); // Queue requirement: socket transmission buffer.
            session = null; // Dynamic allocation released after sale.
            return d.name + " 배출 완료, 거스름돈 " + changeText;
        }

        synchronized int currentBalance() { return session == null ? 0 : session.balance; }
        synchronized int currentBillAmount() { return session == null ? 0 : session.billAmount; }
        synchronized boolean checkPassword(String input) { return password.equals(input); }

        synchronized void changePassword(String next) {
            if (!next.matches("^(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) throw new IllegalArgumentException("비밀번호 조건을 만족하지 않습니다.");
            password = next;
            try { db.savePassword(password); } catch (IOException ex) { throw new IllegalStateException(ex); }
        }

        synchronized void restock(int id, int amount) {
            if (amount <= 0) throw new IllegalArgumentException("보충 수량은 양수여야 합니다.");
            Drink d = drinks.findByIdLinear(id);
            d.stock += amount;
            try { db.saveDrinks(drinks); db.appendStockEvent(LocalDate.now() + ",RESTOCK," + d.id + "," + amount); }
            catch (IOException ex) { throw new IllegalStateException(ex); }
            undoStack.push("RESTOCK:" + id + ":" + amount);
        }

        synchronized void updateDrink(int id, String name, int price) {
            if (price < 10) throw new IllegalArgumentException("가격은 10원 이상이어야 합니다.");
            Drink d = drinks.findByIdLinear(id);
            d.name = name;
            d.price = price;
            try { db.saveDrinks(drinks); } catch (IOException ex) { throw new IllegalStateException(ex); }
            eventQueue.offer("RENAME|" + machineId + "|" + id + "|" + name + "|" + price);
        }

        synchronized String collectCash() {
            int collected = moneyBox.collectKeepingMinimum();
            try { db.appendCollection(LocalDate.now() + "," + collected); } catch (IOException ex) { throw new IllegalStateException(ex); }
            return collected + "원 수금 완료(거스름돈 최소 보유분 유지).";
        }

        void sendSnapshotNow() { eventQueue.offer("SNAPSHOT|" + snapshotWire()); }

        String snapshotWire() {
            StringBuilder sb = new StringBuilder(machineId);
            for (Drink d : drinks.toList()) sb.append('|').append(d.id).append(',').append(d.name).append(',').append(d.price).append(',').append(d.stock);
            return sb.toString();
        }

        String adminSummary() {
            Sorter.selectionSortByPrice(drinks.toList()); // Sort requirement: management price view.
            StringBuilder sb = new StringBuilder();
            sb.append("[자판기] ").append(machineId).append('\n');
            sb.append("[화폐현황] ").append(moneyBox).append('\n');
            sb.append("[일별매출] 오늘 ").append(salesTree.sumDay(LocalDate.now())).append("원\n");
            sb.append("[월별매출] 이번 달 ").append(salesTree.sumMonth(YearMonth.now())).append("원\n");
            sb.append("[음료 현황 - 이진검색 가능하도록 ID 정렬]\n");
            Drink[] sorted = drinks.toArraySortedById();
            for (Drink d : sorted) sb.append("  #").append(d.id).append(' ').append(d.name).append(' ').append(d.price).append("원 재고 ").append(d.stock).append('\n');
            sb.append("[Search 예] ID 1 이진검색 결과: ").append(Search.binarySearchById(sorted, 1)).append('\n');
            sb.append("[최근 Stack] ").append(undoStack.peek()).append('\n');
            sb.append("[전송 Queue] ").append(eventQueue.size()).append("건 대기\n");
            return sb.toString();
        }

        void startBackgroundThreads(Runnable refresh) {
            Thread sync = new Thread(new SocketSyncWorker(eventQueue, "127.0.0.1", 5050), "socket-sync-thread");
            sync.setDaemon(true);
            sync.start();
            Thread lowStock = new Thread(() -> {
                while (true) {
                    try {
                        for (Drink d : drinks.toList()) if (d.stock <= 2) eventQueue.offer("LOW_STOCK|" + machineId + "|" + d.id + "|" + d.name + "|" + d.stock);
                        refresh.run();
                        Thread.sleep(10_000); // Required periodic check interval.
                    } catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; }
                }
            }, "low-stock-monitor-thread");
            lowStock.setDaemon(true);
            lowStock.start();
        }
    }

    static final class Drink {
        final int id; String name; int price; int stock;
        Drink(int id, String name, int price, int stock) { this.id = id; this.name = name; this.price = price; this.stock = stock; }
        public String toString() { return id + ". " + name; }
    }

    /** Linked list required for drink stock management. */
    static final class InventoryLinkedList {
        private Node head;
        void add(Drink drink) { head = new Node(drink, head); }
        Drink findByIdLinear(int id) { for (Node n = head; n != null; n = n.next) if (n.drink.id == id) return n.drink; return null; }
        List<Drink> toList() { List<Drink> out = new ArrayList<>(); for (Node n = head; n != null; n = n.next) out.add(n.drink); return out; }
        Drink[] toArraySortedById() { Drink[] a = toList().toArray(new Drink[0]); Sorter.insertionSortById(a); return a; }
        private static final class Node { Drink drink; Node next; Node(Drink drink, Node next) { this.drink = drink; this.next = next; } }
    }

    /** Stack implemented directly, used for recent operations. */
    static final class ActionStack {
        private StackNode top;
        void push(String value) { top = new StackNode(value, top); }
        String peek() { return top == null ? "없음" : top.value; }
        private static final class StackNode { String value; StackNode next; StackNode(String value, StackNode next) { this.value = value; this.next = next; } }
    }

    /** Queue implemented directly, used as socket transmission buffer. */
    static final class EventQueue {
        private QueueNode head, tail; private int size;
        synchronized void offer(String value) { QueueNode n = new QueueNode(value); if (tail == null) head = tail = n; else { tail.next = n; tail = n; } size++; notifyAll(); }
        synchronized String poll(long waitMs) throws InterruptedException { if (head == null) wait(waitMs); if (head == null) return null; String v = head.value; head = head.next; if (head == null) tail = null; size--; return v; }
        synchronized int size() { return size; }
        private static final class QueueNode { String value; QueueNode next; QueueNode(String value) { this.value = value; } }
    }

    /** Binary tree used to aggregate sales by date. */
    static final class SalesTree {
        private TreeNode root;
        void insert(SalesRecord r) { root = insert(root, r); }
        private TreeNode insert(TreeNode node, SalesRecord r) { if (node == null) return new TreeNode(r.date, r.amount()); int cmp = r.date.compareTo(node.date); if (cmp == 0) node.total += r.amount(); else if (cmp < 0) node.left = insert(node.left, r); else node.right = insert(node.right, r); return node; }
        int sumDay(LocalDate date) { TreeNode n = root; while (n != null) { int cmp = date.compareTo(n.date); if (cmp == 0) return n.total; n = cmp < 0 ? n.left : n.right; } return 0; }
        int sumMonth(YearMonth month) { return sumMonth(root, month); }
        private int sumMonth(TreeNode n, YearMonth m) { return n == null ? 0 : (YearMonth.from(n.date).equals(m) ? n.total : 0) + sumMonth(n.left, m) + sumMonth(n.right, m); }
        private static final class TreeNode { LocalDate date; int total; TreeNode left, right; TreeNode(LocalDate date, int total) { this.date = date; this.total = total; } }
    }

    static final class Sorter {
        static void insertionSortById(Drink[] a) { for (int i = 1; i < a.length; i++) { Drink key = a[i]; int j = i - 1; while (j >= 0 && a[j].id > key.id) { a[j + 1] = a[j]; j--; } a[j + 1] = key; } }
        static void selectionSortByPrice(List<Drink> list) { for (int i = 0; i < list.size(); i++) { int min = i; for (int j = i + 1; j < list.size(); j++) if (list.get(j).price < list.get(min).price) min = j; Drink t = list.get(i); list.set(i, list.get(min)); list.set(min, t); } }
    }

    static final class Search {
        static Drink binarySearchById(Drink[] a, int id) { int l = 0, r = a.length - 1; while (l <= r) { int m = (l + r) >>> 1; if (a[m].id == id) return a[m]; if (a[m].id < id) l = m + 1; else r = m - 1; } return null; }
    }

    static final class MoneySession {
        int balance, billAmount;
        void insert(int unit) { balance += unit; if (unit >= 1000) billAmount += unit; }
        void remove(int unit) { balance -= unit; if (unit >= 1000) billAmount -= unit; }
    }

    static final class MoneyBox {
        private final int[] units = {1000, 500, 100, 50, 10};
        private final int[] counts = {0, 10, 10, 10, 10}; // Coins are initialized with 10 each; bills are accepted but not used as change.
        private final int[] minKeep = {0, 3, 3, 3, 3};
        static boolean isAccepted(int unit) { return unit == 10 || unit == 50 || unit == 100 || unit == 500 || unit == 1000; }
        void add(int unit, int count) { for (int i = 0; i < units.length; i++) if (units[i] == unit) counts[i] += count; }
        boolean canMakeChange(int amount) { int[] tmp = counts.clone(); return makeChange(amount, tmp) != null; }
        String payChange(int amount) { int[] tmp = counts.clone(); int[] paid = makeChange(amount, tmp); if (paid == null) throw new IllegalStateException("거스름돈 없음: 정확한 금액을 입력하세요."); System.arraycopy(tmp, 0, counts, 0, counts.length); return format(paid); }
        private int[] makeChange(int amount, int[] box) { int[] paid = new int[units.length]; for (int i = 1; i < units.length; i++) { int need = amount / units[i]; int use = Math.min(need, box[i]); paid[i] = use; box[i] -= use; amount -= use * units[i]; } return amount == 0 ? paid : null; }
        int collectKeepingMinimum() { int sum = 0; for (int i = 0; i < units.length; i++) { int removable = Math.max(0, counts[i] - minKeep[i]); sum += removable * units[i]; counts[i] -= removable; } return sum; }
        private String format(int[] paid) { int total = 0; StringBuilder sb = new StringBuilder(); for (int i = 0; i < units.length; i++) if (paid[i] > 0) { total += paid[i] * units[i]; sb.append(units[i]).append("원x").append(paid[i]).append(' '); } return total + "원 (" + (sb.isEmpty() ? "없음" : sb.toString().trim()) + ")"; }
        public String toString() { StringBuilder sb = new StringBuilder(); for (int i = 0; i < units.length; i++) sb.append(units[i]).append("원=").append(counts[i]).append(" "); return sb.toString(); }
    }

    record SalesRecord(LocalDate date, String machineId, int drinkId, String drinkName, int price, int qty) {
        int amount() { return price * qty; }
        String toWire() { return date + "|" + machineId + "|" + drinkId + "|" + drinkName + "|" + price + "|" + qty; }
    }

    /** File-backed database used by admin functions. */
    static final class SalesDatabase {
        private final Path dir, drinksFile, salesFile, passwordFile, stockFile, collectionFile;
        SalesDatabase(Path dir) { this.dir = dir; drinksFile = dir.resolve("drinks.csv"); salesFile = dir.resolve("sales.csv"); passwordFile = dir.resolve("password.txt"); stockFile = dir.resolve("stock-events.csv"); collectionFile = dir.resolve("collections.csv"); }
        void ensure() throws IOException { Files.createDirectories(dir); if (!Files.exists(salesFile)) Files.writeString(salesFile, "date,machine,id,name,price,qty\n"); }
        String loadPassword(String fallback) throws IOException { return Files.exists(passwordFile) ? Files.readString(passwordFile).trim() : fallback; }
        void savePassword(String p) throws IOException { Files.writeString(passwordFile, p); }
        void saveDrinks(InventoryLinkedList drinks) throws IOException { StringBuilder sb = new StringBuilder("id,name,price,stock\n"); for (Drink d : drinks.toList()) sb.append(d.id).append(',').append(d.name).append(',').append(d.price).append(',').append(d.stock).append('\n'); Files.writeString(drinksFile, sb.toString()); }
        void loadDrinks(InventoryLinkedList drinks) throws IOException { if (!Files.exists(drinksFile)) { saveDrinks(drinks); return; } for (String line : Files.readAllLines(drinksFile).subList(1, Files.readAllLines(drinksFile).size())) { String[] p = line.split(",", -1); Drink d = drinks.findByIdLinear(Integer.parseInt(p[0])); if (d != null) { d.name = p[1]; d.price = Integer.parseInt(p[2]); d.stock = Integer.parseInt(p[3]); } } }
        void appendSale(SalesRecord r) throws IOException { Files.writeString(salesFile, r.date + "," + r.machineId + "," + r.drinkId + "," + r.drinkName + "," + r.price + "," + r.qty + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND); }
        void loadSalesInto(SalesTree tree) throws IOException { if (!Files.exists(salesFile)) return; List<String> lines = Files.readAllLines(salesFile); for (int i = 1; i < lines.size(); i++) { String[] p = lines.get(i).split(",", -1); if (p.length >= 6) tree.insert(new SalesRecord(LocalDate.parse(p[0]), p[1], Integer.parseInt(p[2]), p[3], Integer.parseInt(p[4]), Integer.parseInt(p[5]))); } }
        void appendStockEvent(String row) throws IOException { Files.writeString(stockFile, row + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND); }
        void appendCollection(String row) throws IOException { Files.writeString(collectionFile, row + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND); }
    }

    /** Client worker sends queued data to a socket server; failures leave a visible warning but do not stop GUI. */
    static final class SocketSyncWorker implements Runnable {
        private final EventQueue queue; private final String host; private final int port;
        SocketSyncWorker(EventQueue queue, String host, int port) { this.queue = queue; this.host = host; this.port = port; }
        public void run() {
            while (true) {
                try {
                    String event = queue.poll(2000);
                    if (event == null) continue;
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(host, port), 1500);
                        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
                            out.write(event); out.newLine(); out.flush();
                        }
                    } catch (IOException ex) {
                        System.err.println("서버 전송 대기/실패: " + ex.getMessage());
                    }
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; }
            }
        }
    }

    /** Socket server aggregates events from several vending clients. */
    static final class VendingServer {
        private final int port; private final Path dir;
        VendingServer(int port, Path dir) { this.port = port; this.dir = dir; }
        void start() throws IOException {
            Files.createDirectories(dir);
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Vending server listening on " + port);
                while (true) {
                    Socket client = server.accept();
                    Thread t = new Thread(() -> handle(client), "server-client-thread");
                    t.start();
                }
            }
        }
        private void handle(Socket client) {
            try (client; BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
                String line = in.readLine();
                if (line != null) {
                    Files.writeString(dir.resolve("events.log"), LocalDate.now() + " " + line + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    if (line.startsWith("LOW_STOCK")) System.out.println("관리자 알림: " + line);
                }
            } catch (IOException ex) {
                System.err.println("client handler error: " + ex.getMessage());
            }
        }
    }

    static final class SelfTest {
        static void run() throws Exception {
            Path tmp = Files.createTempDirectory("vending-test");
            VendingMachine vm = new VendingMachine("TEST", tmp);
            vm.insertMoney(1000);
            String result = vm.buy(1);
            if (!result.contains("배출")) throw new AssertionError("sale failed");
            vm.restock(1, 1);
            vm.changePassword("New#1234");
            if (!vm.checkPassword("New#1234")) throw new AssertionError("password failed");
            System.out.println("SELF_TEST_OK " + tmp);
        }
    }
}
