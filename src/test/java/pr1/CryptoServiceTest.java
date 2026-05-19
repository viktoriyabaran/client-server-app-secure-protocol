package pr1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class CryptoServiceTest {
    private static final byte[] KEY_16 = "cryptoKeyTest123".getBytes(StandardCharsets.UTF_8);

    @Test
    void shouldRoundTripText() {
        CryptoService crypto = new CryptoService(KEY_16);
        byte[] plaintext = "hello world".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = crypto.encrypt(plaintext);
        byte[] decrypted = crypto.decrypt(ciphertext);

        Assertions.assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void encryptedBytesShouldDifferFromText() {
        CryptoService crypto = new CryptoService(KEY_16);
        byte[] plaintext = "some readable data".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = crypto.encrypt(plaintext);

        Assertions.assertFalse(java.util.Arrays.equals(plaintext, ciphertext));
    }

    @Test
    void shouldRejectInvalidKeyLength() {
        byte[] badKey = "invalid".getBytes(StandardCharsets.UTF_8);

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CryptoService(badKey)
        );
        Assertions.assertTrue(ex.getMessage().contains("16, 24, or 32"));
    }
}