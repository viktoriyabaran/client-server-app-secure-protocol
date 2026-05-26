package pr2.transport;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

public class CountingMessageSender implements MessageSender {
    private final AtomicInteger count = new AtomicInteger(0);
    private final List<byte[]> sent = new CopyOnWriteArrayList<>();

    @Override
    public void send(byte[] message) {
        count.incrementAndGet();
        sent.add(message);
    }

    public int getCount() {
        return count.get();
    }

    public List<byte[]> getSent() {
        return sent;
    }

    public void reset() {
        count.set(0);
        sent.clear();
    }
}