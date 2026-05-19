package pr1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class PacketComposerTest
{
    private static final CryptoService crypto = new CryptoService("testKey123456789".getBytes(StandardCharsets.UTF_8));

    @Test
    void shouldComposerRejectTooLongPayload() {
        byte[] payload = new byte[Packet.MAX_MESSAGE_LENGTH];
        Message originalMessage = new Message(100, 42, payload);
        Packet original = new Packet((byte) 7, originalMessage);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PacketComposer.compose(original, crypto)
        );
    }

    @Test
    void wireBytesShouldNotContainPlaintext() {
        String secret = "TOPSUPERSECRETMESSAGE";
        Message msg = new Message(1, 1, secret.getBytes(StandardCharsets.UTF_8));
        Packet packet = new Packet((byte) 1, msg);
        byte[] wire = PacketComposer.compose(packet, crypto);

        String wireAsString = new String(wire, StandardCharsets.UTF_8);
        Assertions.assertFalse(wireAsString.contains(secret));
    }
}