package pr2.pipeline;

import pr1.Message;
import pr1.Packet;
import pr2.contracts.CommandType;
import pr4.api.GroupRequests;
import pr4.api.ProductRequests;
import pr4.db.SqliteConnections;
import pr4.db.SqliteGroupRepository;
import pr4.db.SqliteProductRepository;
import pr4.filter.Page;
import pr4.filter.ProductFilter;
import pr4.model.Group;
import pr4.model.Product;
import pr4.service.GroupService;
import pr4.service.ProductService;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static pr2.pipeline.ProcessorHelpers.buildError;
import static pr2.pipeline.ProcessorHelpers.buildOk;
import static pr2.pipeline.ProcessorHelpers.parse;

public class Processor implements Runnable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id = NEXT_ID.getAndIncrement();
    private final BlockingQueue<Packet> input;
    private final BlockingQueue<Packet> output;
    private final ProductService productService;
    private final GroupService groupService;

    public Processor(BlockingQueue<Packet> input, BlockingQueue<Packet> output) {
        this(input, output, SqliteConnections.open("warehouse.db"));
    }

    private Processor(BlockingQueue<Packet> input, BlockingQueue<Packet> output, Connection connection) {
        this(input, output,
                new ProductService(new SqliteProductRepository(connection)),
                new GroupService(new SqliteGroupRepository(connection)));
    }

    public Processor(BlockingQueue<Packet> input, BlockingQueue<Packet> output, ProductService productService, GroupService groupService) {
        this.input = input;
        this.output = output;
        this.productService = productService;
        this.groupService = groupService;
    }

    private Packet process(Packet request) {
        Message msg = request.getbMsg();
        CommandType command;
        try {
            System.out.println("[Processor " + id + "] Processing message");
            command = CommandType.fromCode(msg.getcType());
        } catch (IllegalArgumentException e) {
            System.err.println("[Processor " + id + "] Unknown command code: " + msg.getcType());
            return buildError(request, "Unknown command code: " + msg.getcType());
        }

        try {
            return switch (command) {
                case ADD_STOCK -> handleAddStock(request, msg);
                case GET_STOCK -> handleGetStock(request, msg);
                case REMOVE_STOCK -> handleRemoveStock(request, msg);
                case SET_PRICE -> handleSetPrice(request, msg);
                case CREATE_GROUP -> handleCreateGroup(request, msg);
                case ADD_PRODUCT_TO_GROUP -> {
                    System.out.println("[Processor " + id + "] " + command + " is not backed by the product service");
                    yield buildOk(request, null);
                }
            };
        } catch (RuntimeException e) {
            System.err.println("[Processor " + id + "] " + command + " failed: " + e.getMessage());
            return buildError(request, e.getMessage());
        }
    }

    private Packet handleAddStock(Packet request, Message msg) {
        ProductRequests.CreateRecord req = parse(msg.getMessage(), ProductRequests.CreateRecord.class);
        int newId = productService.create(new Product(req.name(), req.category(), req.manufacturer(), req.quantity(), req.price()));
        System.out.println("[Processor " + id + "] ADD_STOCK created product id=" + newId);
        return buildOk(request, Map.of("id", newId));
    }

    private Packet handleGetStock(Packet request, Message msg) {
        ProductFilter filter = parseFilter(msg);
        List<Product> found = productService.search(filter, new Page(50, 0));
        System.out.println("[Processor " + id + "] GET_STOCK matched " + found.size() + " product(s)");
        return buildOk(request, Map.of("count", found.size(), "products", found));
    }

    private Packet handleRemoveStock(Packet request, Message msg) {
        int productId = parse(msg.getMessage(), ProductRequests.IdRecord.class).id();
        boolean removed = productService.delete(productId);
        System.out.println("[Processor " + id + "] REMOVE_STOCK id=" + productId + " removed=" + removed);
        return removed ? buildOk(request, null) : buildError(request, "Product not found: " + productId);
    }

    private Packet handleSetPrice(Packet request, Message msg) {
        ProductRequests.SetPriceRecord req = parse(msg.getMessage(), ProductRequests.SetPriceRecord.class);

        Optional<Product> existing = productService.read(req.id());
        if (existing.isEmpty()) {
            return buildError(request, "Product not found: " + req.id());
        }
        Product product = existing.get();
        product.setPrice(req.price());
        productService.update(product);
        System.out.println("[Processor " + id + "] SET_PRICE id=" + req.id() + " price=" + req.price());
        return buildOk(request, null);
    }

    private Packet handleCreateGroup(Packet request, Message msg) {
        GroupRequests.CreateRecord req = parse(msg.getMessage(), GroupRequests.CreateRecord.class);
        int newId = groupService.create(new Group(req.name()));
        System.out.println("[Processor " + id + "] CREATE_GROUP created group id=" + newId);
        return buildOk(request, Map.of("id", newId));
    }

    private ProductFilter parseFilter(Message msg) {
        byte[] raw = msg.getMessage();
        if (raw == null || raw.length == 0) {
            return new ProductFilter();
        }
        return parse(raw, ProductFilter.class);
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