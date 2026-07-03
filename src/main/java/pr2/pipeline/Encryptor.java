package pr2.pipeline;

import pr1.CryptoService;
import pr1.Packet;
import pr1.PacketComposer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Encryptor implements Runnable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id = NEXT_ID.getAndIncrement();
    private final BlockingQueue<Packet> input;
    private final BlockingQueue<byte[]> output;
    private final CryptoService crypto;

    public Encryptor(BlockingQueue<Packet> input, BlockingQueue<byte[]> output, CryptoService crypto) {
        this.input = input;
        this.output = output;
        this.crypto = crypto;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Packet data = input.take();
                try {
                    System.out.println("[Encryptor " + id + "] Encrypting message");
                    byte[] m = PacketComposer.compose(data, crypto);
                    output.put(m);
                } catch (RuntimeException e) {
                    System.err.println("[Encryptor] Bad packet: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}