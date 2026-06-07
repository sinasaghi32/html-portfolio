package gui;

import manager.VendingMachineManager;
import model.Drink;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

/** 관리자 화면: 매출/재고/돈/상품/비밀번호/정렬/검색 기능을 한 화면에서 설명 가능하게 구성했다. */
public class AdminPanel extends JPanel {
    private final VendingMachineManager manager;
    private final JTextArea area = new JTextArea();

    public AdminPanel(VendingMachineManager manager, Runnable logout, Runnable refresh) {
        this.manager = manager;
        setLayout(new BorderLayout(8, 8));
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 3, 5, 5));
        addButton(buttons, "일일 매출", () -> area.setText(manager.getSales().dailySummary()));
        addButton(buttons, "월간 매출", () -> area.setText(manager.getSales().monthlySummary()));
        addButton(buttons, "최근 판매(Stack)", () -> area.setText(join(manager.getSales().recent(20))));
        addButton(buttons, "재고 보충", () -> refill(refresh));
        addButton(buttons, "돈 상태", () -> area.setText(manager.getCoins().toString()));
        addButton(buttons, "돈 수거(최소 5개 유지)", () -> area.setText("수거 금액: " + manager.collectMoney() + "원\n" + manager.getCoins()));
        addButton(buttons, "이름 변경", () -> changeName(refresh));
        addButton(buttons, "가격 변경", () -> changePrice(refresh));
        addButton(buttons, "품절/보충 기록", () -> area.setText(manager.stockHistory()));
        addButton(buttons, "가격 정렬", () -> show(manager.getSortSearch().sortByPrice(manager.getDrinks())));
        addButton(buttons, "재고 정렬", () -> show(manager.getSortSearch().sortByStock(manager.getDrinks())));
        addButton(buttons, "판매수 정렬", () -> show(manager.getSortSearch().sortBySoldCount(manager.getDrinks())));
        addButton(buttons, "이름 검색", this::searchName);
        addButton(buttons, "가격 이진검색", this::binarySearchPrice);
        addButton(buttons, "가격 트리검색", this::treeSearchPrice);
        addButton(buttons, "비밀번호 변경", this::changePassword);

        JButton out = new JButton("판매 화면으로");
        out.addActionListener(e -> logout.run());
        buttons.add(out);
        add(buttons, BorderLayout.NORTH);
    }

    private void refill(Runnable refresh) throws Exception {
        Drink d = chooseDrink();
        if (d == null) return;
        int amount = Integer.parseInt(JOptionPane.showInputDialog("보충 수량"));
        manager.refill(d.getId(), amount);
        area.setText("보충 완료");
        refresh.run();
    }

    private void changeName(Runnable refresh) throws Exception {
        Drink d = chooseDrink();
        if (d == null) return;
        String newName = JOptionPane.showInputDialog("새 이름");
        manager.changeName(d.getId(), newName);
        refresh.run();
    }

    private void changePrice(Runnable refresh) throws Exception {
        Drink d = chooseDrink();
        if (d == null) return;
        int newPrice = Integer.parseInt(JOptionPane.showInputDialog("새 가격"));
        manager.changePrice(d.getId(), newPrice);
        refresh.run();
    }

    private void searchName() {
        String keyword = JOptionPane.showInputDialog("검색 이름");
        Drink d = manager.getSortSearch().linearSearchByName(manager.getDrinks(), keyword);
        area.setText(d == null ? "없음" : d.toString());
    }

    private void binarySearchPrice() {
        int price = Integer.parseInt(JOptionPane.showInputDialog("가격"));
        Drink d = manager.getSortSearch().binarySearchByPrice(manager.getSortSearch().sortByPrice(manager.getDrinks()), price);
        area.setText(d == null ? "없음" : d.toString());
    }

    private void treeSearchPrice() {
        int price = Integer.parseInt(JOptionPane.showInputDialog("가격"));
        show(manager.getSortSearch().treeSearchByPrice(manager.getDrinks(), price));
    }

    private void changePassword() throws Exception {
        String newPassword = JOptionPane.showInputDialog("새 비밀번호");
        manager.getAdmin().changePassword(newPassword);
        area.setText("변경 완료");
    }

    private void addButton(JPanel panel, String name, ThrowingRunnable runnable) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        panel.add(button);
    }

    private Drink chooseDrink() {
        List<Drink> list = manager.getDrinks();
        String[] names = list.stream().map(d -> d.getId() + ":" + d.getName()).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, "음료 선택", "관리", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (selected == null) return null;
        return list.get(Integer.parseInt(selected.substring(0, selected.indexOf(':'))) - 1);
    }

    private void show(List<Drink> drinks) {
        area.setText(join(drinks));
    }

    private String join(List<?> list) {
        return String.join("\n", list.stream().map(Object::toString).toList());
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
