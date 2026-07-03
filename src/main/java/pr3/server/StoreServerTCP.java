package pr3.server;

import pr1.CryptoService;
import pr1.Packet;
import pr2.pipeline.*;
import pr2.transport.MessageReceiver;
import pr2.transport.MessageSender;
import pr3.transport.TCPMessageReceiver;
import pr3.transport.TCPMessageSender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class StoreServerTCP {
    private static final int PORT = 2503;

    public static void main(String[] args) throws IOException {
        CryptoService crypto = new CryptoService("StoreServerTest1".getBytes(StandardCharsets.UTF_8));
        ExecutorService clientThreads = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Listening on port " + PORT);

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Client connected: " + clientSocket.getRemoteSocketAddress());

                clientThreads.submit(() -> handleClient(clientSocket, crypto));
            }
        }
    }

    private static void handleClient(Socket socket, CryptoService crypto) {
        ExecutorService pipeline = Executors.newFixedThreadPool(4);
        try {
            MessageReceiver receiver = new TCPMessageReceiver(socket);
            MessageSender sender = new TCPMessageSender(socket);

            BlockingQueue<byte[]> incoming = new LinkedBlockingQueue<>();
            BlockingQueue<Packet> decoded = new LinkedBlockingQueue<>();
            BlockingQueue<Packet> responses = new LinkedBlockingQueue<>();
            BlockingQueue<byte[]> outgoing = new LinkedBlockingQueue<>();

            pipeline.submit(new Decryptor(incoming, decoded, crypto));
            pipeline.submit(new Processor(decoded, responses));
            pipeline.submit(new Encryptor(responses, outgoing, crypto));
            pipeline.submit(new Sender(sender, outgoing));

            new Receiver(receiver, incoming).run();

        } catch (IOException e) {
            System.err.println("[Server] Error: " + e.getMessage());
        } finally {
            pipeline.shutdownNow();
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("[Server] Client disconnected");
        }
    }
}