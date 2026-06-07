package thread;
import manager.VendingMachineManager;
/** 3학년 멀티스레딩 2: 10초마다 파일/로컬DB 동기화를 수행한다. */
public class AutoSaveThread extends Thread{ private final VendingMachineManager manager; private volatile boolean running=true; public AutoSaveThread(VendingMachineManager m){super("AutoSaveThread");manager=m;setDaemon(true);} public void close(){running=false;interrupt();} public void run(){ while(running){ try{ manager.saveAll(); Thread.sleep(10000); }catch(Exception ignored){} } } }
