package thread;
import datastructure.MyQueue; import network.VendingClient;
/** 3학년 멀티스레딩 3: custom Queue에 쌓인 서버 전송 대기 메시지를 Socket으로 보낸다. */
public class ServerSendThread extends Thread{ private final MyQueue<String> queue; private final VendingClient client=new VendingClient(); private volatile boolean running=true; public ServerSendThread(MyQueue<String> q){super("ServerSendThread");queue=q;setDaemon(true);} public void close(){running=false;interrupt();} public void run(){ while(running){ try{ String msg=queue.dequeue(); if(msg!=null) client.send(msg); else Thread.sleep(1000); }catch(Exception e){ try{Thread.sleep(3000);}catch(InterruptedException ignored){} } } } }
