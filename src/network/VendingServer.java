package network;
import java.io.IOException; import java.net.ServerSocket; import java.net.Socket;
/** ServerSocket 5000번 포트. ClientHandler 스레드로 최소 2개 이상의 클라이언트를 동시에 처리할 수 있다. */
public class VendingServer { public static void main(String[] args){ new VendingServer().start(); } public void start(){ try(ServerSocket server=new ServerSocket(5000)){ System.out.println("VendingServer started: port 5000"); while(true){ Socket s=server.accept(); new ClientHandler(s).start(); } }catch(IOException e){ System.err.println("Server error: "+e.getMessage()); } } }
