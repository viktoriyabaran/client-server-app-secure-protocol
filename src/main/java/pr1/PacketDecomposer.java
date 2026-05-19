package pr1;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketDecomposer {
    public static Packet decompose(byte[] bytes, CryptoService crypto) {
        if (bytes.length < 18) {
            throw new IllegalArgumentException("Packet too short: " + bytes.length);
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);

        byte bMagic = buf.get();
        if (bMagic != Packet.B_MAGIC)
            throw new IllegalArgumentException("Magic Byte mismatch");

        byte bSrc = buf.get();
        long bPktId = buf.getLong();
        int wLen = buf.getInt();

        if (bytes.length != 18 + wLen) {
            throw new IllegalArgumentException("Packet length mismatch");
        }
        if (wLen > Packet.MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message length too big");
        }


        short wCrc16 = buf.getShort();
        byte[] header = new byte[14];
        System.arraycopy(bytes, 0, header, 0, 14);
        if (wCrc16 != Crc16.calculateCrc(header))
            throw new IllegalArgumentException("Header CRC mismatch");

        byte[] payload = new byte[wLen];
        buf.get(payload);

        short wCrc16M = buf.getShort();
        if (wCrc16M != Crc16.calculateCrc(payload))
            throw new IllegalArgumentException("Message CRC mismatch");

        payload = crypto.decrypt(payload);
        Message message = Message.fromBytes(payload);

        return new Packet(bSrc, bPktId, message);
    }
}
