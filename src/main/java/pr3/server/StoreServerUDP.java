package pr3.server;

import pr1.CryptoService;
import pr1.Packet;
import pr2.pipeline.*;
import pr2.transport.MessageReceiver;
import pr2.transport.MessageSender;
import pr3.transport.UDPMessageReceiver;
import pr3.transport.UDPMessageSender;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

public class StoreServerUDP {
    private static final int PORT = 4445;

    public static void main(String[] args) throws Exception {
        CryptoService crypto = new CryptoService("StoreServerTest1".getBytes(StandardCharsets.UTF_8));
        ExecutorService pipeline = Executors.newFixedThreadPool(4);

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("[UDP Server] Listening on port " + PORT);

            Map<Long, SocketAddress> pendingReplies = new ConcurrentHashMap<>();

            MessageReceiver receiver = new UDPMessageReceiver(socket, pendingReplies);
            MessageSender sender = new UDPMessageSender(socket, pendingReplies);

            BlockingQueue<byte[]> incoming = new LinkedBlockingQueue<>();
            BlockingQueue<Packet> decoded = new LinkedBlockingQueue<>();
            BlockingQueue<Packet> responses = new LinkedBlockingQueue<>();
            BlockingQueue<byte[]> outgoing = new LinkedBlockingQueue<>();

            pipeline.submit(new Decryptor(incoming, decoded, crypto));
            pipeline.submit(new Processor(decoded, responses));
            pipeline.submit(new Encryptor(responses, outgoing, crypto));
            pipeline.submit(new Sender(sender, outgoing));

            new Receiver(receiver, incoming).run();
        } finally {
            pipeline.shutdownNow();
            System.out.println("[UDP Server] Stopped");
        }
    }
}
