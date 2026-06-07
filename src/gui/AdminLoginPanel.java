package gui;
import manager.VendingMachineManager; import javax.swing.*; import java.awt.*;
public class AdminLoginPanel extends JPanel{ public AdminLoginPanel(VendingMachineManager m,Runnable onLogin){ setLayout(new GridBagLayout()); JPasswordField pw=new JPasswordField(15); JButton login=new JButton("관리자 로그인"); login.addActionListener(e->{ try{ m.login(new String(pw.getPassword())); onLogin.run(); }catch(Exception ex){ JOptionPane.showMessageDialog(this,ex.getMessage()); }}); add(new JLabel("비밀번호:")); add(pw); add(login); } }
