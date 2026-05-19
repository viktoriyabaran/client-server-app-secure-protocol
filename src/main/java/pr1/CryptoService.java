package pr1;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {
    private static final String ALGORITHM = "AES";
    private final SecretKeySpec key;

    public CryptoService(byte[] keyBytes) {
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES key must be 16, 24, or 32 bytes, got " + keyBytes.length);
        }
        this.key = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public byte[] encrypt(byte[] plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed");
        }
    }

    public byte[] decrypt(byte[] ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed");
        }
    }
}
