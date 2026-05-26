package pr2.transport;

public interface MessageReceiver {
    byte[] receive() throws InterruptedException;
}
