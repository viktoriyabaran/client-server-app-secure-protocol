package pr1;


public class Packet {
    public static final byte B_MAGIC = 0x13;
    public static final int MAX_MESSAGE_LENGTH = 1024 * 1024; // exactly 1 MB
    private static long nextPktId = 1;

    private final byte bSrc;
    private final long bPktId;
    private final Message bMsg;

    public Packet(byte bSrc, Message bMsg) {
        this.bSrc = bSrc;
        this.bPktId = nextPktId++;
        this.bMsg = bMsg;
    }

    public Packet(byte bSrc, long bPktId,  Message bMsg) {
        this.bSrc = bSrc;
        this.bPktId = bPktId;
        this.bMsg = bMsg;
    }

    public byte getbSrc() {
        return bSrc;
    }

    public long getbPktId() {
        return bPktId;
    }

    public Message getbMsg() {
        return bMsg;
    }
}
