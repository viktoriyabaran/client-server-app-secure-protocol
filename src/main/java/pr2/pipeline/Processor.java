package pr2.pipeline;

import pr1.Message;
import pr1.Packet;
import pr2.contracts.CommandType;
import pr2.contracts.ResponseCode;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Processor implements Runnable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id = NEXT_ID.getAndIncrement();
    private final BlockingQueue<Packet> input;
    private final BlockingQueue<Packet> output;

    public Processor(BlockingQueue<Packet> input, BlockingQueue<Packet> output) {
        this.input = input;
        this.output = output;
    }

    private Packet process(Packet request) {
        Message msg = request.getbMsg();
        CommandType command;
        try {
            System.out.println("[Processor " + id + "] Processing message");
            command = CommandType.fromCode(msg.getcType());
        } catch (IllegalArgumentException e) {
            System.err.println("[Processor " + id + "] Unknown command code: " + msg.getcType());
            return buildResponse(request, ResponseCode.ERROR,
                    "Unknown command code: " + msg.getcType());
        }

        switch (command) {
            case GET_STOCK -> System.out.println("[Processor " + id + "] Handling GET_STOCK from user " + msg.getbUserId());
            case REMOVE_STOCK -> System.out.println("[Processor " + id + "] Handling REMOVE_STOCK from user " + msg.getbUserId());
            case ADD_STOCK -> System.out.println("[Processor " + id + "] Handling ADD_STOCK from user " + msg.getbUserId());
            case CREATE_GROUP -> System.out.println("[Processor " + id + "] Handling CREATE_GROUP from user " + msg.getbUserId());
            case ADD_PRODUCT_TO_GROUP -> System.out.println("[Processor " + id + "] Handling ADD_PRODUCT_TO_GROUP from user " + msg.getbUserId());
            case SET_PRICE -> System.out.println("[Processor " + id + "] Handling SET_PRICE from user " + msg.getbUserId());
        }

        return buildResponse(request, ResponseCode.OK, null);
    }

    private Packet buildResponse(Packet request, ResponseCode status, String errorMessage) {
        String body = status == ResponseCode.OK
                ? "{\"status\":\"OK\"}"
                : "{\"status\":\"ERROR\",\"message\":\"" + escape(errorMessage) + "\"}";

        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        Message responseMsg = new Message(
                request.getbMsg().getcType(),
                request.getbMsg().getbUserId(),
                payload
        );
        return new Packet(request.getbSrc(), responseMsg);
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Packet request = input.take();
                try {
                    Packet response = process(request);
                    output.put(response);
                } catch (RuntimeException e) {
                    System.err.println("[Processor] Error processing packet: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}