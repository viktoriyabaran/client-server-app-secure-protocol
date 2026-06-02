package pr3.transport;

import pr2.transport.MessageSender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;

public class UDPMessageSender implements MessageSender {
    private final DatagramSocket socket;
    private final Map<Long, SocketAddress> pendingReplies;

    public UDPMessageSender(DatagramSocket socket, Map<Long, SocketAddress> pendingReplies) {
        this.socket = socket;
        this.pendingReplies = pendingReplies;
    }

    @Override
    public void send(byte[] message) {
        long key = UDPMessageReceiver.replyKey(message);
        SocketAddress destination = pendingReplies.remove(key);
        if (destination == null) {
            System.err.println("[UDPMessageSender] No known sender for response, dropping");
            return;
        }

        try {
            DatagramPacket packet = new DatagramPacket(message, message.length, destination);
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException("Send failed: " + e.getMessage(), e);
        }
    }
}
