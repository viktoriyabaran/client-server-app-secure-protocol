package pr1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PacketRoundTripTest
{
    private static final CryptoService crypto = new CryptoService("testKey543216789".getBytes(StandardCharsets.UTF_8));

    private void assertPacketEquality(Packet original, Packet parsed) {
        Assertions.assertEquals(original.getbSrc(), parsed.getbSrc());
        Assertions.assertEquals(original.getbPktId(), parsed.getbPktId());
        Assertions.assertEquals(original.getbMsg().getcType(), parsed.getbMsg().getcType());
        Assertions.assertEquals(original.getbMsg().getbUserId(), parsed.getbMsg().getbUserId());
        Assertions.assertArrayEquals(original.getbMsg().getMessage(), parsed.getbMsg().getMessage());
    }

    @Test
    void shouldRoundTripPacket(){
        byte[] payload = "hello world".getBytes(StandardCharsets.UTF_8);
        Message originalMessage = new Message(100, 42, payload);
        Packet original = new Packet((byte) 7, originalMessage);

        byte[] wire = PacketComposer.compose(original, crypto);
        Packet parsed = PacketDecomposer.decompose(wire, crypto);
        assertPacketEquality(original, parsed);
    }

    @Test
    void shouldRoundTripEmptyPacket(){
        Message originalMessage = new Message(0, 0, new byte[0]);
        Packet original = new Packet((byte) 0, originalMessage);

        byte[] wire = PacketComposer.compose(original, crypto);
        Packet parsed = PacketDecomposer.decompose(wire, crypto);

        assertPacketEquality(original, parsed);
    }

    @Test
    void shouldRoundTripLargePayload() {
        byte[] bigPayload = new byte[100_000];
        new Random(42).nextBytes(bigPayload);
        Message originalMessage = new Message(1, 1, bigPayload);
        Packet original = new Packet((byte) 0, originalMessage);

        byte[] wire = PacketComposer.compose(original, crypto);
        Packet parsed = PacketDecomposer.decompose(wire, crypto);

        assertPacketEquality(original, parsed);
    }

    @Test
    void shouldRoundTripMaxNegativeIntFields() {
        Message originalMessage = new Message(Integer.MIN_VALUE, Integer.MAX_VALUE, new byte[] {1, 2, 3});
        Packet original = new Packet((byte) 7, originalMessage);

        byte[] wire = PacketComposer.compose(original, crypto);
        Packet parsed = PacketDecomposer.decompose(wire, crypto);
        assertPacketEquality(original, parsed);
    }

    @Test
    void shouldRoundTripMaxBytesAndLong() {
        Message originalMessage = new Message(100, 42, new byte[]{9, 9, 9, 0, 3, 4});
        Packet original = new Packet((byte) 0xFF, Long.MAX_VALUE, originalMessage);

        byte[] wire = PacketComposer.compose(original, crypto);
        Packet parsed = PacketDecomposer.decompose(wire, crypto);
        assertPacketEquality(original, parsed);
    }
}