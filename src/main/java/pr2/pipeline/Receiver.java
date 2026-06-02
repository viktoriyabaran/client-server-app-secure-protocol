package pr2.pipeline;

import pr2.transport.MessageReceiver;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Receiver implements Runnable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id = NEXT_ID.getAndIncrement();
    private final MessageReceiver source;
    private final BlockingQueue<byte[]> output;

    public Receiver(MessageReceiver source, BlockingQueue<byte[]> output) {
        this.source = source;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("[Receiver " + id + "] Receiving message");
                    byte[] data = source.receive();
                    output.put(data);
                } catch (RuntimeException e) {
                    System.err.println("[Receiver] Runtime error receiving: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("[Receiver] Error receiving: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}