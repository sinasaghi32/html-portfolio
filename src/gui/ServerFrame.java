package gui;
import network.VendingServer; import javax.swing.*; import java.awt.*;
public class ServerFrame extends JFrame{ public ServerFrame(){ super("Vending Server"); JButton start=new JButton("서버 시작(port 5000)"); start.addActionListener(e->new Thread(()->new VendingServer().start()).start()); add(start, BorderLayout.CENTER); setSize(300,120); } }
