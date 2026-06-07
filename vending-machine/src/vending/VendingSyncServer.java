package vending;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import com.sun.net.httpserver.HttpServer;
import java.util.ArrayList;
import java.util.List;

/** TCP socket server aggregating events from multiple vending-machine clients. */
public class VendingSyncServer implements Runnable {
    private final int port;
    private volatile boolean running = true;
    private final List<String> eventLog = new ArrayList<>();

    public VendingSyncServer(int port) { this.port = port; }

    @Override
    public void run() {
        startWebAdmin(port + 2525);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Vending sync server started on port " + port);
            while (running) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handle(socket), "client-handler").start();
            }
        } catch (IOException e) {
            if (running) System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handle(Socket socket) {
        try (socket; BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String line;
            while ((line = in.readLine()) != null) {
                synchronized (eventLog) { eventLog.add(LocalDateTime.now() + " " + line); }
                if (line.startsWith("STOCK") && line.matches(".*,(0|1|2)$")) out.println("ALERT,LOW_STOCK," + line);
                else if (line.startsWith("PING")) out.println("PONG");
                else out.println("ACK," + line.hashCode());
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        }
    }

    private void startWebAdmin(int webPort) {
        try {
            HttpServer httpServer = HttpServer.create(new java.net.InetSocketAddress(webPort), 0);
            httpServer.createContext("/", exchange -> {
                String body;
                synchronized (eventLog) {
                    StringBuilder sb = new StringBuilder("<html><meta charset='UTF-8'><h1>Vending Server Admin</h1><pre>");
                    for (String event : eventLog) sb.append(event.replace("&", "&amp;").replace("<", "&lt;")).append("\n");
                    sb.append("</pre></html>");
                    body = sb.toString();
                }
                byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            httpServer.start();
            System.out.println("Web admin started on http://127.0.0.1:" + webPort);
        } catch (IOException e) {
            System.err.println("Web admin warning: " + e.getMessage());
        }
    }

    public void stop() { running = false; }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5555;
        new Thread(new VendingSyncServer(port), "sync-server").start();
    }
}
