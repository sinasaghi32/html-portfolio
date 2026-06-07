package vending;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Central synchronized model shared by GUI, persistence, and socket threads. */
public class VendingMachineModel {
    public static final String MACHINE_ID = "VM-001";
    private final LocalDatabase database;
    private final CashBox cashBox = new CashBox();
    private final CustomQueue<String> syncQueue = new CustomQueue<>();
    private final CustomStack<String> auditStack = new CustomStack<>();
    private CustomLinkedList<Beverage> beverages;
    private SalesTree salesTree = new SalesTree();
    private MoneyInput currentInput; // Allocated when first money is inserted; released by setting null.

    public VendingMachineModel(LocalDatabase database) throws IOException {
        this.database = database;
        database.initialize();
        beverages = database.loadBeverages();
        for (SalesRecord record : database.loadSales()) salesTree.insert(record);
    }

    public synchronized CustomLinkedList<Beverage> getBeverages() { return beverages; }
    public CashBox getCashBox() { return cashBox; }
    public CustomQueue<String> getSyncQueue() { return syncQueue; }

    public synchronized int getInsertedTotal() { return currentInput == null ? 0 : currentInput.getTotal(); }

    public synchronized void insertMoney(int denom) {
        if (currentInput == null) currentInput = new MoneyInput();
        int newBillTotal = currentInput.getBillTotal() + (denom == 1000 ? denom : 0);
        int newInsertedTotal = currentInput.getTotal() + denom;
        if (!cashBox.canAccept(denom, newBillTotal, newInsertedTotal)) throw new IllegalArgumentException("허용되지 않는 화폐이거나 한도(지폐 5,000원 / 총 7,000원)를 초과했습니다.");
        currentInput.add(denom);
        cashBox.add(denom);
        queue("MONEY," + MACHINE_ID + "," + denom + "," + newInsertedTotal);
    }

    public synchronized String refund() {
        if (currentInput == null || currentInput.getTotal() == 0) return "반환할 금액이 없습니다.";
        int amount = currentInput.getTotal();
        if (cashBox.makeChange(amount) == null) throw new IllegalStateException("반환 가능한 화폐가 부족합니다. 관리자를 호출하세요.");
        currentInput = null;
        queue("REFUND," + MACHINE_ID + "," + amount);
        return amount + "원을 반환했습니다.";
    }

    public synchronized String vend(int beverageId) throws IOException {
        Beverage beverage = (Beverage) beverages.findById(beverageId);
        if (beverage == null) throw new IllegalArgumentException("음료를 찾을 수 없습니다.");
        if (beverage.isSoldOut()) throw new IllegalStateException("품절된 음료입니다.");
        if (currentInput == null || currentInput.getTotal() < beverage.getPrice()) throw new IllegalStateException("투입 금액이 부족합니다.");
        int changeAmount = currentInput.getTotal() - beverage.getPrice();
        if (!cashBox.hasChangeFor(changeAmount)) throw new IllegalStateException("거스름돈 없음: 정확한 금액을 넣거나 다른 음료를 선택하세요.");
        cashBox.makeChange(changeAmount);
        beverage.vendOne();
        SalesRecord record = new SalesRecord(LocalDate.now(), beverage.getId(), beverage.getName(), 1, beverage.getPrice(), MACHINE_ID);
        database.appendSale(record);
        database.saveBeverages(beverages);
        salesTree.insert(record);
        currentInput = null;
        String message = beverage.getName() + " 배출 완료, 거스름돈 " + changeAmount + "원";
        audit(message);
        queue("SALE," + record.toCsv() + ",stock=" + beverage.getStock());
        return message;
    }

    public synchronized void restock(int id, int amount) throws IOException {
        Beverage b = (Beverage) beverages.findById(id);
        if (b == null) throw new IllegalArgumentException("음료를 찾을 수 없습니다.");
        b.restock(amount);
        database.saveBeverages(beverages);
        audit(b.getName() + " 재고 보충 +" + amount);
        queue("STOCK," + MACHINE_ID + "," + b.getId() + "," + b.getStock());
    }

    public synchronized void updateBeverage(int id, String name, int price) throws IOException {
        Beverage b = (Beverage) beverages.findById(id);
        if (b == null) throw new IllegalArgumentException("음료를 찾을 수 없습니다.");
        b.rename(name);
        b.changePrice(price);
        database.saveBeverages(beverages);
        audit("음료 정보 변경: " + id + " " + name + " " + price);
        queue("RENAME," + MACHINE_ID + "," + id + "," + name + "," + price);
    }

    public synchronized int collectCash() throws IOException {
        int amount = cashBox.collectKeepingMinimum();
        audit("수금 " + amount + "원");
        queue("COLLECT," + MACHINE_ID + "," + amount);
        return amount;
    }

    public synchronized Map<String, Integer> dailySales(LocalDate date) { return aggregate(salesTree.searchByDate(date)); }
    public synchronized Map<String, Integer> monthlySales(YearMonth month) { return aggregate(salesTree.searchByMonth(month)); }

    public boolean validPassword(String password) throws IOException { return database.loadPassword().equals(password); }
    public void changePassword(String password) throws IOException {
        if (!password.matches("^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$")) throw new IllegalArgumentException("비밀번호는 8자리 이상이며 숫자와 특수문자를 각각 1개 이상 포함해야 합니다.");
        database.savePassword(password);
        audit("관리자 비밀번호 변경");
    }

    public synchronized String recentAudit() { return auditStack.peek() == null ? "없음" : auditStack.peek(); }

    public synchronized String stockSnapshot() {
        StringBuilder sb = new StringBuilder("STOCKSNAP,").append(MACHINE_ID);
        for (Beverage b : beverages) sb.append(',').append(b.getId()).append(':').append(b.getStock()).append(':').append(b.getName());
        return sb.toString();
    }

    public synchronized List<Beverage> sortedByPrice() { return SearchSortUtil.selectionSortByPrice(beverages); }
    public synchronized Beverage searchByName(String keyword) { return SearchSortUtil.linearSearchByName(beverages, keyword); }

    private Map<String, Integer> aggregate(List<SalesRecord> records) {
        Map<String, Integer> map = new LinkedHashMap<>();
        int total = 0;
        for (SalesRecord r : records) { map.merge(r.beverageName(), r.amount(), Integer::sum); total += r.amount(); }
        map.put("전체", total);
        return map;
    }

    private void audit(String message) throws IOException { auditStack.push(message); database.audit(message); }
    private void queue(String event) { syncQueue.enqueue(event); }
}
