package gui;

import manager.VendingMachineManager;
import thread.AutoSaveThread;
import thread.ServerSendThread;
import thread.StockMonitorThread;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

/** GUI 요구사항: 제목은 정확히 '자판기 관리 프로그램', 판매/관리 화면 분리, 관리자 중 판매 비활성화. */
public class MainFrame extends JFrame {
    private final VendingMachineManager manager;
    private final CardLayout card = new CardLayout();
    private final JPanel root = new JPanel(card);
    private CustomerPanel customer;
    private boolean adminActive;

    public MainFrame() throws Exception {
        super("자판기 관리 프로그램");
        manager = new VendingMachineManager();

        customer = new CustomerPanel(manager, this::refresh);
        root.add(customer, "customer");
        root.add(new AdminLoginPanel(manager, this::showAdmin), "login");
        add(root);

        JPanel top = new JPanel();
        JButton admin = new JButton("관리자");
        admin.addActionListener(e -> {
            adminActive = true;
            refresh();
            card.show(root, "login");
        });
        top.add(admin);
        add(top, BorderLayout.NORTH);

        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 3학년 요구사항 I: GUI EDT 외에 직접 만든 3개의 의미 있는 스레드를 실행한다.
        new StockMonitorThread(manager).start();
        new AutoSaveThread(manager).start();
        new ServerSendThread(manager.getServerQueue()).start();
    }

    private void showAdmin() {
        root.add(new AdminPanel(manager, () -> {
            adminActive = false;
            refresh();
            card.show(root, "customer");
        }, this::refresh), "admin");
        card.show(root, "admin");
    }

    private void refresh() {
        customer.refresh(adminActive);
    }
}
