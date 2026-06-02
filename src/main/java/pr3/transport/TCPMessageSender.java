package pr3.transport;

import pr2.transport.MessageSender;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPMessageSender implements MessageSender {
    private final DataOutputStream output;

    public TCPMessageSender(Socket socket) throws IOException {
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void send(byte[] message) {
        try {
            output.writeInt(message.length);
            output.write(message);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException("Send failed: " + e.getMessage(), e);
        }
    }
}