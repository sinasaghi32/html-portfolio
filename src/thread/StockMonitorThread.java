package thread;
import manager.VendingMachineManager; import model.Drink;
/** 3학년 멀티스레딩 1: 5초마다 재고 <=2 확인 후 서버 큐에 LOW_STOCK 메시지를 넣는다. */
public class StockMonitorThread extends Thread{ private final VendingMachineManager manager; private volatile boolean running=true; public StockMonitorThread(VendingMachineManager m){super("StockMonitorThread");manager=m;setDaemon(true);} public void close(){running=false;interrupt();} public void run(){ while(running){ try{ for(Drink d:manager.getDrinks()) if(d.getStock()<=2) manager.queue("LOW_STOCK|"+VendingMachineManager.MACHINE_ID+"|"+d.getName()+"|"+d.getStock()); Thread.sleep(5000); }catch(Exception ignored){} } } }
