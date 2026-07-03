package pr2.transport;

public interface MessageSender {
    void send(byte[] message) throws InterruptedException;
}
