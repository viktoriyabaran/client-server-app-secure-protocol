package pr2.transport;

import pr1.CryptoService;
import pr1.Message;
import pr1.Packet;
import pr1.PacketComposer;
import pr2.contracts.CommandType;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class FakeMessageReceiver implements MessageReceiver {
    private final CryptoService crypto;
    private final Random random;

    public FakeMessageReceiver(CryptoService crypto) {
        this.crypto = crypto;
        this.random = new Random();
    }

    @Override
    public byte[] receive() throws InterruptedException {
        Thread.sleep(5 + random.nextInt(20)); //please sleep

        CommandType[] commands = CommandType.values();
        CommandType chosen = commands[random.nextInt(commands.length)];

        String payloadText = "{\"action\":\"Intending to perform " + chosen.name() + "\"}";
        byte[] payload = payloadText.getBytes(StandardCharsets.UTF_8);

        int userId = random.nextInt(100000);

        byte src = (byte) random.nextInt(256);

        Message msg = new Message(chosen.code(), userId, payload);
        Packet packet = new Packet(src, msg);

        return PacketComposer.compose(packet, crypto);
    }
}