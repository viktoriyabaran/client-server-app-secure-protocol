package pr2.pipeline;

import java.util.HexFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender implements Runnable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id = NEXT_ID.getAndIncrement();
    private final BlockingQueue<byte[]> input;

    public Sender(BlockingQueue<byte[]> input) {
        this.input = input;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] data = input.take();
                System.out.println("[Sender " + id + "] Sending bytes");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}