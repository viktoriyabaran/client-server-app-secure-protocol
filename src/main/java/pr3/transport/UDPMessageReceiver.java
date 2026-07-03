package pr3.transport;

import pr2.transport.MessageReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;

public class UDPMessageReceiver implements MessageReceiver {
    private static final int BUFFER_SIZE = 65535;
    private static final int SRC_OFFSET = 1;
    private static final int PKT_ID_OFFSET = 2;

    private final DatagramSocket socket;
    private final Map<Long, SocketAddress> pendingReplies;

    public UDPMessageReceiver(DatagramSocket socket, Map<Long, SocketAddress> pendingReplies) {
        this.socket = socket;
        this.pendingReplies = pendingReplies;
    }

    @Override
    public byte[] receive() throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        byte[] data = Arrays.copyOfRange(buf, 0, packet.getLength());

        if (data.length >= PKT_ID_OFFSET + Long.BYTES) {
            pendingReplies.put(replyKey(data), packet.getSocketAddress());
        }

        return data;
    }

    static long replyKey(byte[] data) {
        int src = data[SRC_OFFSET] & 0xFF;
        long pktId = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getLong(PKT_ID_OFFSET);
        return ((long) src << 56) | (pktId & 0x00FFFFFFFFFFFFFFL);
    }
}
