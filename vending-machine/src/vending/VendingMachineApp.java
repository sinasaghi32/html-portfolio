package vending;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

/** Swing GUI entry point. Sale and manager screens are independent modes. */
public class VendingMachineApp {
    private final VendingMachineModel model;
    private final JFrame frame = new JFrame("자판기 관리 프로그램");
    private final JPanel drinkPanel = new JPanel(new GridLayout(2, 4, 8, 8));
    private final JLabel insertedLabel = new JLabel("투입 금액: 0원");
    private final JTextArea statusArea = new JTextArea(8, 40);
    private volatile boolean adminMode;

    public VendingMachineApp() throws IOException {
        model = new VendingMachineModel(new LocalDatabase(Path.of("vending-machine", "data")));
        new Thread(new SocketSyncClient(model.getSyncQueue(), "127.0.0.1", 5555), "socket-sync-client").start();
        new Thread(this::lowStockMonitor, "low-stock-monitor").start();
        new Thread(this::autoSnapshot, "auto-snapshot").start();
    }

    public void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        statusArea.setEditable(false);
        JPanel moneyPanel = new JPanel();
        int[] denoms = {10, 50, 100, 500, 1000};
        for (int denom : denoms) {
            JButton button = new JButton(denom + "원 투입");
            button.addActionListener(e -> insertMoney(denom));
            moneyPanel.add(button);
        }
        JButton refund = new JButton("화폐 반환");
        refund.addActionListener(e -> safe(() -> status(model.refund())));
        JButton admin = new JButton("관리자 메뉴");
        admin.addActionListener(e -> openAdminLogin());
        moneyPanel.add(refund);
        moneyPanel.add(admin);
        frame.add(insertedLabel, BorderLayout.NORTH);
        frame.add(drinkPanel, BorderLayout.CENTER);
        frame.add(moneyPanel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(statusArea), BorderLayout.EAST);
        refreshDrinks();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void refreshDrinks() {
        drinkPanel.removeAll();
        int inserted = model.getInsertedTotal();
        for (Beverage b : model.getBeverages()) {
            String text = "<html>" + b.getName() + "<br>" + b.getPrice() + "원<br>재고 " + b.getStock() + (b.isSoldOut() ? "<br><b>품절</b>" : "") + "</html>";
            JButton button = new JButton(text);
            button.setEnabled(!adminMode && !b.isSoldOut() && inserted >= b.getPrice());
            button.addActionListener(e -> safe(() -> status(model.vend(b.getId()))));
            drinkPanel.add(button);
        }
        insertedLabel.setText("투입 금액: " + inserted + "원" + (inserted > 0 ? " / 구매 가능 음료가 활성화됩니다." : ""));
        drinkPanel.revalidate();
        drinkPanel.repaint();
    }

    private void insertMoney(int denom) { safe(() -> { model.insertMoney(denom); status(denom + "원을 투입했습니다."); }); }

    private void openAdminLogin() {
        JPasswordField field = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(frame, field, "관리자 비밀번호", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        safe(() -> {
            if (!model.validPassword(new String(field.getPassword()))) throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
            adminMode = true;
            showAdminDialog();
        });
    }

    private void showAdminDialog() {
        JDialog dialog = new JDialog(frame, "관리자 전용 메뉴", true);
        JTextArea adminText = new JTextArea(18, 52);
        adminText.setEditable(false);
        JPanel buttons = new JPanel(new GridLayout(0, 2, 5, 5));
        JButton daily = new JButton("일별 매출");
        daily.addActionListener(e -> adminText.setText(formatSales(model.dailySales(LocalDate.now()))));
        JButton monthly = new JButton("월별 매출");
        monthly.addActionListener(e -> adminText.setText(formatSales(model.monthlySales(YearMonth.now()))));
        JButton cash = new JButton("화폐 현황");
        cash.addActionListener(e -> adminText.setText(model.getCashBox().status()));
        JButton collect = new JButton("수금");
        collect.addActionListener(e -> safe(() -> adminText.setText(model.collectCash() + "원을 수금했습니다.\n" + model.getCashBox().status())));
        JButton restock = new JButton("재고 보충");
        restock.addActionListener(e -> restockDialog(adminText));
        JButton edit = new JButton("이름/가격 변경");
        edit.addActionListener(e -> editDrinkDialog(adminText));
        JButton sort = new JButton("가격순 정렬");
        sort.addActionListener(e -> adminText.setText(sortedInventory()));
        JButton search = new JButton("이름 검색");
        search.addActionListener(e -> searchDialog(adminText));
        JButton password = new JButton("비밀번호 변경");
        password.addActionListener(e -> passwordDialog(adminText));
        JButton exit = new JButton("관리자 종료");
        exit.addActionListener(e -> dialog.dispose());
        for (JButton b : new JButton[]{daily, monthly, cash, collect, restock, edit, sort, search, password, exit}) buttons.add(b);
        dialog.add(new JScrollPane(adminText), BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() { public void windowClosed(java.awt.event.WindowEvent e) { adminMode = false; refreshDrinks(); } });
        adminText.setText("관리자 메뉴 활성화: 판매 화면 기능은 잠금 처리되었습니다.\n최근 감사: " + model.recentAudit());
        refreshDrinks();
        dialog.setVisible(true);
    }

    private void restockDialog(JTextArea out) { safe(() -> { int id = Integer.parseInt(JOptionPane.showInputDialog(frame, "음료 ID")); int amount = Integer.parseInt(JOptionPane.showInputDialog(frame, "보충 수량")); model.restock(id, amount); out.setText("재고 보충 완료"); }); }
    private void editDrinkDialog(JTextArea out) { safe(() -> { int id = Integer.parseInt(JOptionPane.showInputDialog(frame, "음료 ID")); String name = JOptionPane.showInputDialog(frame, "새 이름"); int price = Integer.parseInt(JOptionPane.showInputDialog(frame, "새 가격")); model.updateBeverage(id, name, price); out.setText("음료 정보 변경 완료"); }); }
    private void passwordDialog(JTextArea out) { safe(() -> { String password = JOptionPane.showInputDialog(frame, "새 비밀번호(숫자+특수문자 포함 8자리 이상)"); model.changePassword(password); out.setText("비밀번호 변경 완료"); }); }
    private void searchDialog(JTextArea out) { String keyword = JOptionPane.showInputDialog(frame, "검색어"); Beverage b = model.searchByName(keyword); out.setText(b == null ? "검색 결과 없음" : b.getId() + ": " + b.getName() + " " + b.getPrice() + "원 재고 " + b.getStock()); }

    private String sortedInventory() { StringBuilder sb = new StringBuilder("가격순 정렬 결과\n"); for (Beverage b : model.sortedByPrice()) sb.append(b.getId()).append(' ').append(b.getName()).append(' ').append(b.getPrice()).append("원 재고 ").append(b.getStock()).append('\n'); return sb.toString(); }
    private String formatSales(Map<String, Integer> sales) { StringBuilder sb = new StringBuilder(); sales.forEach((k, v) -> sb.append(k).append(": ").append(v).append("원\n")); return sb.toString(); }
    private void status(String message) { statusArea.append(message + "\n"); refreshDrinks(); }

    private void safe(ThrowingRunnable runnable) {
        try { runnable.run(); } catch (Exception e) { JOptionPane.showMessageDialog(frame, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); statusArea.append("오류: " + e.getMessage() + "\n"); } finally { refreshDrinks(); }
    }

    private void lowStockMonitor() {
        while (true) {
            try { Thread.sleep(10_000); model.getSyncQueue().enqueue(model.stockSnapshot()); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }

    private void autoSnapshot() {
        Timer timer = new Timer(30_000, e -> model.getSyncQueue().enqueue("PING," + VendingMachineModel.MACHINE_ID));
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { try { new VendingMachineApp().show(); } catch (Exception e) { JOptionPane.showMessageDialog(null, e.getMessage(), "시작 오류", JOptionPane.ERROR_MESSAGE); } });
    }

    @FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }
}
