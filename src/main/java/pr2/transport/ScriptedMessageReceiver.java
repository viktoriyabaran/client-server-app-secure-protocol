package pr2.transport;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ScriptedMessageReceiver implements MessageReceiver {
    private final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();

    public void offer(byte[] message) {
        queue.offer(message);
    }

    @Override
    public byte[] receive() throws InterruptedException {
        return queue.take();
    }
}
