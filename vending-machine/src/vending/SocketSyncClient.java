package vending;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/** Background client thread that sends queued vending data to a designated server. */
public class SocketSyncClient implements Runnable {
    private final CustomQueue<String> queue;
    private final String host;
    private final int port;

    public SocketSyncClient(CustomQueue<String> queue, String host, int port) {
        this.queue = queue;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String event = queue.waitAndDequeue();
                send(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Socket sync warning: " + e.getMessage());
            }
        }
    }

    private void send(String event) throws Exception {
        try (Socket socket = new Socket(host, port); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(event);
            String response = in.readLine();
            if (response != null && response.startsWith("ALERT")) System.out.println("Server alert: " + response);
        }
    }
}
