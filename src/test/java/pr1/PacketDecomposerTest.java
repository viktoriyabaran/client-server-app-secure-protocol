package pr1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketDecomposerTest
{
    private static final CryptoService crypto = new CryptoService("testKey987654321".getBytes(StandardCharsets.UTF_8));

    private byte[] sampleWire() {
        Message msg = new Message(100, 42, "{\"format\":\"json\",\"purpose\":\"test\"}".getBytes(StandardCharsets.UTF_8));
        Packet packet = new Packet((byte) 7, msg);
        return PacketComposer.compose(packet, crypto);
    }

    @Test
    void shouldRejectTooShortPayload() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, crypto)
        );
    }

    @Test
    void shouldDecomposerRejectTooLongPayload() {
        ByteBuffer buf = ByteBuffer.allocate(18).order(ByteOrder.BIG_ENDIAN);
        buf.put(Packet.B_MAGIC);
        buf.put((byte) 1);
        buf.putLong(1L);
        buf.putInt(Packet.MAX_MESSAGE_LENGTH + 100);
        buf.putShort((short) 0);

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(buf.array(), crypto)
        );
        Assertions.assertTrue(ex.getMessage().contains("length"));
    }

    @Test
    void shouldRejectCorruptMagicNumber() {
        ByteBuffer buf = ByteBuffer.allocate(18).order(ByteOrder.BIG_ENDIAN);
        buf.put((byte) 1);
        buf.put((byte) 1);
        buf.putLong(1L);
        buf.putInt(1);
        buf.putShort((short) 0);

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(buf.array(), crypto)
        );
        Assertions.assertTrue(ex.getMessage().contains("Magic"));
    }

    @Test
    void shouldRejectCorruptedHeaderByte() {
        byte[] wire = sampleWire();
        wire[5] ^= 0x01;  // flip a bit somewhere in bPktId

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(wire, crypto)
        );
        Assertions.assertTrue(ex.getMessage().contains("Header CRC"));
    }

    @Test
    void shouldRejectCorruptedHeaderCrc() {
        byte[] wire = sampleWire();
        wire[14] ^= 0x01;

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(wire, crypto)
        );
        Assertions.assertTrue(ex.getMessage().contains("Header CRC"));
    }

    @Test
    void shouldRejectCorruptedMessageBody() {
        byte[] wire = sampleWire();
        wire[20] ^= 0x01;

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(wire, crypto)
        );
        Assertions.assertTrue(ex.getMessage().contains("Message CRC"));
    }

    @Test
    void shouldRejectCorruptedMessageCrc() {
        byte[] wire = sampleWire();
        wire[wire.length - 1] ^= 0x01;

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketDecomposer.decompose(wire, crypto)
        );
        Assertions.assertTrue(ex.getMessage().contains("Message CRC"));
    }

    @Test
    void shouldFailDecryptionWithWrongKey() {
        CryptoService wrongKey = new CryptoService(
                "AnotherKey123456".getBytes(StandardCharsets.UTF_8));

        byte[] wire = sampleWire();

        Assertions.assertThrows(
                RuntimeException.class,
                () -> PacketDecomposer.decompose(wire, wrongKey)
        );
    }
}