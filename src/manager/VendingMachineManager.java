package manager;

import database.LocalDatabaseManager;
import datastructure.DrinkLinkedList;
import datastructure.MyQueue;
import exception.ChangeNotAvailableException;
import exception.FileDataException;
import exception.InvalidMoneyException;
import exception.InvalidPasswordException;
import exception.NotEnoughMoneyException;
import exception.SoldOutException;
import file.DrinkFileManager;
import file.MoneyFileManager;
import file.StockHistoryFileManager;
import model.CoinStorage;
import model.Drink;
import model.MoneyInput;
import model.SaleRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 기본 요구사항 + 3학년(파일/DB/스레드/소켓 연결용 큐)을 관리하는 중심 클래스. */
public class VendingMachineManager {
    public static final String MACHINE_ID = "VM001";

    private DrinkLinkedList drinks;
    private CoinStorage coins;

    // 요구사항 B: new로 동적 할당하고, 환불/구매 완료 후 null로 참조를 끊는다.
    private MoneyInput moneyInput;

    private final DrinkFileManager drinkFile = new DrinkFileManager();
    private final MoneyFileManager moneyFile = new MoneyFileManager();
    private final StockHistoryFileManager stockFile = new StockHistoryFileManager();
    private final SalesManager sales = new SalesManager();
    private final AdminManager admin;
    private final SortSearchManager sortSearch = new SortSearchManager();
    private final LocalDatabaseManager db = new LocalDatabaseManager();

    // 요구사항 H/K: 직접 만든 Queue를 서버 전송 대기열로 사용한다.
    private final MyQueue<String> serverQueue = new MyQueue<>();

    public VendingMachineManager() throws FileDataException {
        drinks = drinkFile.load();
        coins = moneyFile.load();
        admin = new AdminManager();
    }

    public synchronized List<Drink> getDrinks() {
        return drinks.toList();
    }

    public synchronized int getInsertedTotal() {
        return moneyInput == null ? 0 : moneyInput.getTotal();
    }

    public CoinStorage getCoins() { return coins; }
    public SalesManager getSales() { return sales; }
    public AdminManager getAdmin() { return admin; }
    public SortSearchManager getSortSearch() { return sortSearch; }
    public MyQueue<String> getServerQueue() { return serverQueue; }

    public synchronized void insertMoney(int unit) throws InvalidMoneyException, FileDataException {
        if (moneyInput == null) {
            moneyInput = new MoneyInput();
        }
        moneyInput.insert(unit);
        if (unit != 1000) {
            coins.addCoin(unit);
        }
        moneyFile.save(coins);
    }

    public synchronized Map<Integer, Integer> refund() throws ChangeNotAvailableException, FileDataException {
        int amount = getInsertedTotal();
        Map<Integer, Integer> out = coins.returnChange(amount);
        moneyInput = null; // Java GC가 이후 사용하지 않는 MoneyInput 객체를 정리한다.
        moneyFile.save(coins);
        return out;
    }

    public synchronized Map<Integer, Integer> buy(int id) throws Exception {
        if (moneyInput == null) {
            throw new NotEnoughMoneyException("먼저 돈을 투입하세요.");
        }

        Drink d = drinks.findById(id);
        if (d == null) throw new SoldOutException("없는 음료입니다.");
        if (d.isSoldOut()) throw new SoldOutException("품절: " + d.getName());
        if (moneyInput.getTotal() < d.getPrice()) throw new NotEnoughMoneyException("금액 부족");

        int change = moneyInput.getTotal() - d.getPrice();
        if (!coins.canMakeChange(change)) {
            throw new ChangeNotAvailableException("거스름돈 없음");
        }

        d.decreaseStock();
        d.increaseSoldCount();
        SaleRecord r = new SaleRecord(LocalDateTime.now(), d.getName(), d.getPrice(), 1);
        sales.record(r);
        db.syncSale(r);

        Map<Integer, Integer> returned = coins.returnChange(change);
        moneyInput = null; // 구매 후 투입금 상태 초기화
        saveAll();

        queue("SALE|" + MACHINE_ID + "|" + r.day() + "|" + d.getName() + "|" + d.getPrice() + "|1");
        queue("STOCK|" + MACHINE_ID + "|" + d.getName() + "|" + d.getStock());
        if (d.isSoldOut()) {
            stockFile.add("SOLD_OUT," + d.getName());
        }
        return returned;
    }

    public synchronized void refill(int id, int amount) throws FileDataException {
        Drink d = drinks.findById(id);
        if (d == null) return;

        d.addStock(amount);
        drinkFile.save(drinks);
        String msg = "REFILL," + d.getName() + "," + amount + ",stock=" + d.getStock();
        stockFile.add(msg);
        db.syncStock(msg);
        queue("REFILL|" + MACHINE_ID + "|" + LocalDateTime.now() + "|" + d.getName() + "|" + amount);
    }

    public synchronized void changeName(int id, String newName) throws FileDataException {
        Drink d = drinks.findById(id);
        if (d == null) return;

        String old = d.getName();
        d.setName(newName);
        drinkFile.save(drinks);
        stockFile.add("CHANGE_NAME," + old + "," + newName);
        queue("CHANGE_NAME|" + MACHINE_ID + "|" + old + "|" + newName);
    }

    public synchronized void changePrice(int id, int price) throws FileDataException {
        Drink d = drinks.findById(id);
        if (d == null) return;

        d.setPrice(price);
        drinkFile.save(drinks);
        stockFile.add("CHANGE_PRICE," + d.getName() + "," + price);
        queue("CHANGE_PRICE|" + MACHINE_ID + "|" + d.getName() + "|" + price);
    }

    public synchronized int collectMoney() throws FileDataException {
        int value = coins.collectKeepMinimum();
        moneyFile.save(coins);
        db.syncMoney(coins);
        return value;
    }

    public synchronized void saveAll() throws FileDataException {
        drinkFile.save(drinks);
        moneyFile.save(coins);
        db.syncMoney(coins);
    }

    public String stockHistory() throws FileDataException {
        return stockFile.readText();
    }

    public void login(String password) throws InvalidPasswordException {
        admin.login(password);
    }

    public void queue(String message) {
        serverQueue.enqueue(message);
    }
}
