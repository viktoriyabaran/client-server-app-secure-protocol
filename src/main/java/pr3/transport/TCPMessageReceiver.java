package pr3.transport;

import pr2.transport.MessageReceiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPMessageReceiver implements MessageReceiver {
    private final DataInputStream input;

    public TCPMessageReceiver(Socket socket) throws IOException {
        this.input = new DataInputStream(socket.getInputStream());
    }

    @Override
    public byte[] receive() throws InterruptedException {
        try {
            int length = input.readInt();
            byte[] data = new byte[length];
            input.readFully(data);
            return data;
        } catch (IOException e) {
            throw new InterruptedException("Connection closed: " + e.getMessage());
        }
    }
}