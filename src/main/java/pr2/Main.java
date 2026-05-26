package pr2;

import pr1.CryptoService;
import pr1.Packet;
import pr2.pipeline.*;
import pr2.transport.BasicMessageSender;
import pr2.transport.FakeMessageReceiver;
import pr2.transport.MessageReceiver;
import pr2.transport.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CryptoService crypto = new CryptoService("ConcurrencyMain1".getBytes(StandardCharsets.UTF_8));

        BlockingQueue<byte[]> incoming = new LinkedBlockingQueue<>();
        BlockingQueue<Packet> decoded = new LinkedBlockingQueue<>();
        BlockingQueue<Packet> responses = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> outgoing = new LinkedBlockingQueue<>();

        MessageReceiver source = new FakeMessageReceiver(crypto);
        MessageSender sink = new BasicMessageSender();

        ExecutorService pool = Executors.newFixedThreadPool(16);

        for (int i = 0; i < 2; i++)
            pool.submit(new Receiver(source, incoming));

        for (int i = 0; i < 2; i++)
            pool.submit(new Decryptor(incoming, decoded, crypto));

        for (int i = 0; i < 4; i++)
            pool.submit(new Processor(decoded, responses));

        for (int i = 0; i < 3; i++)
            pool.submit(new Encryptor(responses, outgoing, crypto));

        for (int i = 0; i < 5; i++)
            pool.submit(new Sender(sink, outgoing));

        Runtime.getRuntime().addShutdownHook(new Thread(pool::shutdownNow));

        System.out.println("[Main] Pipeline started. Running for 5 seconds...");
        Thread.sleep(5000);

        System.out.println("[Main] Shutting down...");
        pool.shutdownNow();
        boolean clean = pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("[Main] Shutdown " + (clean ? "clean" : "with timeout"));
    }
}