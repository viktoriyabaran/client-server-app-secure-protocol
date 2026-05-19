package pr1;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketComposer {
    public static byte[] compose(Packet packet, CryptoService crypto) {
        byte[] messageEnc = crypto.encrypt(packet.getbMsg().toBytes());
        if (messageEnc.length > Packet.MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "Encrypted message exceeds maximum length: "
                            + messageEnc.length + " > " + Packet.MAX_MESSAGE_LENGTH);
        }
        int wLen = messageEnc.length;
        ByteBuffer buf = ByteBuffer.allocate(wLen + 18).order(ByteOrder.BIG_ENDIAN);

        buf.put(Packet.B_MAGIC);
        buf.put(packet.getbSrc());
        buf.putLong(packet.getbPktId());
        buf.putInt(wLen);

        byte[] headerBytes = new byte[14];
        buf.position(0);
        buf.get(headerBytes);

        buf.putShort(Crc16.calculateCrc(headerBytes));
        buf.put(messageEnc);
        buf.putShort(Crc16.calculateCrc(messageEnc));

        return buf.array();
    }
}
