import gui.MainFrame; import gui.ServerFrame;
import javax.swing.*;
/** 실행 진입점. java -cp out Main 또는 java -cp out Main server */
public class Main { public static void main(String[] args){ SwingUtilities.invokeLater(()->{ try{ if(args.length>0 && args[0].equalsIgnoreCase("server")) new ServerFrame().setVisible(true); else new MainFrame().setVisible(true); }catch(Exception e){ JOptionPane.showMessageDialog(null,e.getMessage()); } }); } }
