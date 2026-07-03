package pr3.client;

import pr1.*;
import pr2.contracts.CommandType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class StoreClientUDP {
    private static final String HOST = "localhost";
    private static final int PORT = 4445;
    private static final int BUFFER_SIZE = 65535;
    private static final int SOCKET_TIMEOUT_MS = 2000;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final CryptoService crypto =
            new CryptoService("StoreServerTest1".getBytes(StandardCharsets.UTF_8));

    public static void main(String[] args) throws Exception {
        InetAddress serverAddress = InetAddress.getByName(HOST);
        byte clientId = args.length > 0
                ? (byte) Integer.parseInt(args[0])
                : (byte) new Random().nextInt(256);
        int requestCounter = 0;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            System.out.println("[Client " + (clientId & 0xFF) + "] Ready to talk to " + HOST + ":" + PORT);

            while (!Thread.currentThread().isInterrupted()) {
                sendAndReceive(socket, serverAddress, clientId, requestCounter++);
                Thread.sleep(2000);
            }
        }
    }

    private static void sendAndReceive(DatagramSocket socket, InetAddress serverAddress, byte clientId, int i)
            throws IOException {
        Message msg = new Message(
                CommandType.GET_STOCK.code(),
                1000 + i,
                ("request №" + i).getBytes(StandardCharsets.UTF_8)
        );
        Packet packet = new Packet(clientId, msg);
        byte[] wireData = PacketComposer.compose(packet, crypto);

        DatagramPacket request = new DatagramPacket(wireData, wireData.length, serverAddress, PORT);

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            socket.send(request);
            System.out.println("[Client] Sent request " + i + (attempt > 1 ? " (attempt " + attempt + ")" : ""));

            try {
                byte[] buf = new byte[BUFFER_SIZE];
                DatagramPacket response = new DatagramPacket(buf, buf.length);
                socket.receive(response);

                byte[] responseBytes = Arrays.copyOfRange(buf, 0, response.getLength());
                Packet decoded = PacketDecomposer.decompose(responseBytes, crypto);

                String responseBody = new String(decoded.getbMsg().getMessage(), StandardCharsets.UTF_8);
                System.out.println("[Client] Got response: " + responseBody);
                return;
            } catch (SocketTimeoutException e) {
                System.out.println("[Client] No response for request " + i
                        + " (" + attempt + "/" + MAX_RETRY_ATTEMPTS + "), retrying...");
            }
        }

        System.err.println("[Client] Giving up on request " + i + " after " + MAX_RETRY_ATTEMPTS + " attempts");
    }
}
