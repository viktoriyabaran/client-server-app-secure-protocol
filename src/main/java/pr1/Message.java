package pr1;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Message {
    private final int cType;
    private final int bUserId;
    private final byte[] message;

    public Message(int cType, int bUserId, byte[] message) {
        this.cType = cType;
        this.bUserId = bUserId;
        this.message = message;
    }

    public int getcType() {
        return cType;
    }

    public int getbUserId() {
        return bUserId;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(this.message.length  + 8).order(ByteOrder.BIG_ENDIAN);;
        buf.putInt(this.cType);
        buf.putInt(this.bUserId);
        buf.put(this.message);
        return buf.array();
    }

    public static Message fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        int cType = buf.getInt();
        int bUserId = buf.getInt();
        byte[] message = new byte[bytes.length - 8];
        buf.get(message);
        return new Message(cType, bUserId, message);
    }
}
