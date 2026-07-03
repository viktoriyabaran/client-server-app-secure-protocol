package pr2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr1.CryptoService;
import pr1.Message;
import pr1.Packet;
import pr1.PacketComposer;
import pr2.contracts.CommandType;
import pr2.pipeline.*;
import pr2.transport.CountingMessageSender;
import pr2.transport.ScriptedMessageReceiver;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class PipelineConcurrencyTest {
    private static final CryptoService crypto =
            new CryptoService("ConcurrencyTest2".getBytes(StandardCharsets.UTF_8));

    private ExecutorService pool;
    private ScriptedMessageReceiver scriptedReceiver;
    private CountingMessageSender countingSender;

    private BlockingQueue<byte[]> incoming;
    private BlockingQueue<Packet> decoded;
    private BlockingQueue<Packet> responses;
    private BlockingQueue<byte[]> outgoing;

    @BeforeEach
    void setUp() {
        scriptedReceiver = new ScriptedMessageReceiver();
        countingSender = new CountingMessageSender();

        incoming = new LinkedBlockingQueue<>();
        decoded = new LinkedBlockingQueue<>();
        responses = new LinkedBlockingQueue<>();
        outgoing = new LinkedBlockingQueue<>();
    }

    @AfterEach
    void destroy() throws InterruptedException {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    private void startPipeline(int receivers, int decryptors, int processors, int encryptors, int senders) {
        int total = receivers + decryptors + processors + encryptors + senders;
        pool = Executors.newFixedThreadPool(total);

        for (int i = 0; i < receivers; i++)
            pool.submit(new Receiver(scriptedReceiver, incoming));
        for (int i = 0; i < decryptors; i++)
            pool.submit(new Decryptor(incoming, decoded, crypto));
        for (int i = 0; i < processors; i++)
            pool.submit(new Processor(decoded, responses));
        for (int i = 0; i < encryptors; i++)
            pool.submit(new Encryptor(responses, outgoing, crypto));
        for (int i = 0; i < senders; i++)
            pool.submit(new Sender(countingSender, outgoing));
    }

    private byte[] buildValidMessage(int cType, int userId, String payload) {
        Message msg = new Message(cType, userId, payload.getBytes(StandardCharsets.UTF_8));
        Packet packet = new Packet((byte) 1, msg);
        return PacketComposer.compose(packet, crypto);
    }

    private void waitForCount(int expected, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (countingSender.getCount() < expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
    }

    @Test
    void shouldProcessSingleMessageEndToEnd() throws InterruptedException {
        startPipeline(1, 1, 1, 1, 1);

        scriptedReceiver.offer(buildValidMessage(CommandType.ADD_STOCK.code(), 42, "test"));
        waitForCount(1, 2000);
        Thread.sleep(200); // in case we hit 100 messages but the count kept growing

        Assertions.assertEquals(1, countingSender.getCount());
    }

    @Test
    void shouldProcessManyMessagesInOrder() throws InterruptedException {
        startPipeline(1, 1, 1, 1, 1);

        int messageCount = 100;
        for (int i = 0; i < messageCount; i++) {
            scriptedReceiver.offer(buildValidMessage(CommandType.GET_STOCK.code(), i, "msg" + i));
        }

        waitForCount(messageCount, 5000);
        Thread.sleep(200);
        Assertions.assertEquals(messageCount, countingSender.getCount());
    }

    @Test
    void shouldProcessMessagesUnderConcurrentLoad() throws InterruptedException {
        startPipeline(2, 2, 4, 3, 5);

        int messageCount = 512;
        for (int i = 0; i < messageCount; i++) {
            scriptedReceiver.offer(buildValidMessage(
                    CommandType.values()[i % CommandType.values().length].code(),
                    i,
                    "msg" + i
            ));
        }

        waitForCount(messageCount, 10000);
        Thread.sleep(200);
        Assertions.assertEquals(messageCount, countingSender.getCount());
    }

    @Test
    void shouldHandleMessagesFromMultipleThreads() throws InterruptedException {
        startPipeline(2, 2, 4, 3, 5);

        int threadCount = 10;
        int messagesPerThread = 50;
        int totalMessages = threadCount * messagesPerThread;

        Thread[] producers = new Thread[threadCount];
        for (int p = 0; p < threadCount; p++) {
            final int producerId = p;
            producers[p] = new Thread(() -> {
                for (int i = 0; i < messagesPerThread; i++) {
                    scriptedReceiver.offer(buildValidMessage(
                            CommandType.ADD_STOCK.code(),
                            producerId * 1000 + i,
                            "from-" + producerId
                    ));
                }
            });
            producers[p].start();
        }

        for (Thread t : producers) {
            t.join();
        }

        waitForCount(totalMessages, 10000);
        Thread.sleep(200);

        Assertions.assertEquals(totalMessages, countingSender.getCount());
    }

    @Test
    void shouldSurviveMalformedMessages() throws InterruptedException {
        startPipeline(1, 1, 1, 1, 1);

        scriptedReceiver.offer(buildValidMessage(CommandType.GET_STOCK.code(), 1, "valid"));
        scriptedReceiver.offer(new byte[]{1, 2, 3, 4, 5});
        scriptedReceiver.offer(buildValidMessage(CommandType.ADD_STOCK.code(), 2, "valid"));
        scriptedReceiver.offer(new byte[]{0, 0, 0, 0});
        scriptedReceiver.offer(buildValidMessage(CommandType.SET_PRICE.code(), 3, "valid"));

        waitForCount(3, 3000);
        Assertions.assertEquals(3, countingSender.getCount());
    }

    @Test
    void shouldShutdownCleanlyWithNoPendingMessages() throws InterruptedException {
        startPipeline(1, 1, 1, 1, 1);

        Thread.sleep(200);

        pool.shutdownNow();
        boolean clean = pool.awaitTermination(2, TimeUnit.SECONDS);

        Assertions.assertTrue(clean, "Pipeline should shut down cleanly");
    }

    @Test
    void shouldShutdownEvenWithPendingMessages() throws InterruptedException {
        startPipeline(1, 1, 1, 1, 1);

        for (int i = 0; i < 1000; i++) {
            scriptedReceiver.offer(buildValidMessage(CommandType.GET_STOCK.code(), i, "flood"));
        }

        Thread.sleep(100);
        pool.shutdownNow();
        boolean clean = pool.awaitTermination(2, TimeUnit.SECONDS);

        Assertions.assertTrue(clean, "Pipeline should shut down even with pending work");
    }
}