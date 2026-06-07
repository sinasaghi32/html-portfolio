package network;
import exception.NetworkException;
import java.io.*; import java.net.Socket; import java.nio.charset.StandardCharsets;
/** Socket client. 다른 PC에서는 localhost 대신 서버 PC IP 주소로 변경하면 된다. */
public class VendingClient { private final String host; private final int port; public VendingClient(){this("localhost",5000);} public VendingClient(String host,int port){this.host=host;this.port=port;} public void send(String msg) throws NetworkException{ try(Socket s=new Socket(host,port); PrintWriter out=new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8),true)){ out.println(msg); }catch(IOException e){ throw new NetworkException("서버 연결 실패",e);} } }
