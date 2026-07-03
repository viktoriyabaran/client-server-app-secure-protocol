package pr2.transport;

import pr1.CryptoService;
import pr1.Message;
import pr1.Packet;
import pr1.PacketComposer;
import pr2.contracts.CommandType;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Random;

public class BasicMessageSender implements MessageSender {


    @Override
    public void send(byte[] message) {
        System.out.println("Sent " + message.length
                + " bytes: " + HexFormat.of().formatHex(message));
    }
}