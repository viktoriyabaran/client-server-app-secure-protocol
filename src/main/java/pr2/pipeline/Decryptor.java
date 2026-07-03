package pr2.pipeline;

import pr1.CryptoService;
import pr1.Packet;
import pr1.PacketDecomposer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Decryptor implements Runnable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id = NEXT_ID.getAndIncrement();
    private final BlockingQueue<byte[]> input;
    private final BlockingQueue<Packet> output;
    private final CryptoService crypto;

    public Decryptor(BlockingQueue<byte[]> input, BlockingQueue<Packet> output, CryptoService crypto) {
        this.input = input;
        this.output = output;
        this.crypto = crypto;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] data = input.take();
                try {
                    System.out.println("[Decryptor " + id + "] Decrypting message");
                    Packet p = PacketDecomposer.decompose(data, crypto);
                    output.put(p);
                } catch (RuntimeException e) {
                    System.err.println("[Decryptor] Bad packet: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}