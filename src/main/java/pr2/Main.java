package pr2;

import pr1.CryptoService;
import pr1.Packet;
import pr2.pipeline.*;
import pr2.transport.FakeMessageReceiver;
import pr2.transport.MessageReceiver;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CryptoService crypto = new CryptoService("ConcurrencyTest1".getBytes(StandardCharsets.UTF_8));

        BlockingQueue<byte[]> rawIncoming = new LinkedBlockingQueue<>();
        BlockingQueue<Packet> decoded = new LinkedBlockingQueue<>();
        BlockingQueue<Packet> responses = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> rawOutgoing = new LinkedBlockingQueue<>();

        MessageReceiver source = new FakeMessageReceiver(crypto);

        ExecutorService pool = Executors.newFixedThreadPool(16);

        for (int i = 0; i < 2; i++)
            pool.submit(new Receiver(source, rawIncoming));

        for (int i = 0; i < 2; i++)
            pool.submit(new Decryptor(rawIncoming, decoded, crypto));

        for (int i = 0; i < 4; i++)
            pool.submit(new Processor(decoded, responses));

        for (int i = 0; i < 3; i++)
            pool.submit(new Encryptor(responses, rawOutgoing, crypto));

        for (int i = 0; i < 5; i++)
            pool.submit(new Sender(rawOutgoing));

        Runtime.getRuntime().addShutdownHook(new Thread(pool::shutdownNow));

        System.out.println("[Main] Pipeline started. Running for 5 seconds...");
        Thread.sleep(3000);

        System.out.println("[Main] Shutting down...");
        pool.shutdownNow();
        boolean clean = pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("[Main] Shutdown " + (clean ? "clean" : "with timeout"));
    }
}