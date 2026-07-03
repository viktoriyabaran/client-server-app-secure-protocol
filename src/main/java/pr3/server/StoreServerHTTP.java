package pr3.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import pr2.contracts.JwtToken;
import pr2.contracts.LoginDto;
import pr4.api.ProductRequests;
import pr4.db.SqliteProductRepository;
import pr4.db.SqliteUserRepository;
import pr4.db.UserRepository;
import pr4.model.Product;
import pr4.model.User;
import pr4.service.ProductService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreServerHTTP {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpServer server;
    private final ProductService prodServ;
    private final UserRepository usRep; // TODO: create user service and use it instead

    public StoreServerHTTP(int port) throws IOException {
        this(port, new ProductService(new SqliteProductRepository("warehouse.db")), new SqliteUserRepository("warehouse.db"));
    }

    public StoreServerHTTP(int port, ProductService prodRep, UserRepository usRep) throws IOException {
        this.prodServ = prodRep;
        this.usRep = usRep;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        createEndpoints();
    }

    public static void main(String[] args) throws IOException {
        StoreServerHTTP server = new StoreServerHTTP(1801);
        server.start();
        System.out.println("[HTTP] Listening on 1801");
    }

    public void start() { server.start(); }
    public void stop()  { server.stop(0); }

    private void createEndpoints() throws IOException {
        server.createContext("/login", exchange -> {
            try {
                LoginDto dto = mapper.readValue(exchange.getRequestBody(), LoginDto.class);
                Optional<User> userOpt = usRep.getUserByUsername(dto.username());
                if (userOpt.isEmpty()) {
                    HTTPHelper.sendEmpty(exchange, 401);
                    return;
                }

                User user = userOpt.get();

                if (!usRep.comparePasswordById(user.getId(), dto.password())) {
                    HTTPHelper.sendEmpty(exchange, 401);
                    return;
                }

                String token = JwtService.create(user);
                HTTPHelper.sendJson(exchange, 200, new JwtToken(token));
            } catch (IllegalArgumentException | IOException e) {
                HTTPHelper.sendEmpty(exchange, 400);
            }
        });

        HttpContext productsCtx = server.createContext("/products", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                Integer id = HTTPHelper.pathId(exchange, "/products");

                switch (method) {
                    case "PUT" -> handleCreate(exchange);
                    case "GET" -> handleGet(exchange, id);
                    case "POST" -> handleUpdate(exchange, id);
                    case "DELETE" -> handleDelete(exchange, id);
                    default -> HTTPHelper.sendEmpty(exchange, 405);
                }
            } catch (IllegalArgumentException | IOException e) {
                HTTPHelper.sendEmpty(exchange, 400);
            }
        });
        productsCtx.setAuthenticator(new BearerAuthenticator());

        server.createContext("/", exchange -> HTTPHelper.sendEmpty(exchange, 404));
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        ProductRequests.CreateRecord prod = mapper.readValue(exchange.getRequestBody(), ProductRequests.CreateRecord.class);

        if (prodServ.existsByName(prod.name())) {
            HTTPHelper.sendJson(exchange, 409, Map.of("message", "Name already exists: " + prod.name()));
            return;
        }

        Product product = new Product(prod.name(), prod.category(), prod.manufacturer(), prod.quantity(), prod.price());
        int id = prodServ.create(product);
        product.setId(id);

        HTTPHelper.sendJson(exchange, 201, product);
    }

    private void handleGet(HttpExchange exchange, Integer id) throws IOException {
        if (id == null) {
            HTTPHelper.sendEmpty(exchange, 400);
            return;
        }
        Optional<Product> product = prodServ.read(id);
        if (product.isEmpty()) {
            HTTPHelper.sendEmpty(exchange, 404);
            return;
        }
        HTTPHelper.sendJson(exchange, 200, product.get());
    }

    private void handleUpdate(HttpExchange exchange, Integer id) throws IOException {
        if (id == null) {
            HTTPHelper.sendEmpty(exchange, 400);
            return;
        }

        Optional<Product> prod = prodServ.read(id);
        if (prod.isEmpty()) {
            HTTPHelper.sendEmpty(exchange, 404);
            return;
        }

        ProductRequests.CreateRecord prodRec = mapper.readValue(exchange.getRequestBody(), ProductRequests.CreateRecord.class);

        Product product = new Product(id, prodRec.name(), prodRec.category(), prodRec.manufacturer(), prodRec.quantity(), prodRec.price());
        boolean success = prodServ.update(product);

        if (!success) {
            HTTPHelper.sendEmpty(exchange, 400);
            return;
        }

        HTTPHelper.sendJson(exchange, 200, product);
    }

    private void handleDelete(HttpExchange exchange, Integer id) throws IOException {
        if (id == null) {
            HTTPHelper.sendEmpty(exchange, 400);
            return;
        }

        boolean success = prodServ.delete(id);

        if (!success) {
            HTTPHelper.sendEmpty(exchange, 404);
            return;
        }

        HTTPHelper.sendEmpty(exchange, 204);
    }

    private static class BearerAuthenticator extends Authenticator {
        @Override
        public Result authenticate(HttpExchange exch) {
            List<String> values = exch.getRequestHeaders().get("Authorization");
            if (values == null || values.isEmpty()) {
                return new Failure(401);
            }

            String[] credentialParts = values.getFirst().split(" ");
            if (credentialParts.length != 2 || !credentialParts[0].equals("Bearer")) {
                return new Failure(401);
            }

            String username;
            try {
                username = JwtService.verify(credentialParts[1]);
                return new Success(new HttpPrincipal(username, "ROLE_ADMIN"));
            } catch (RuntimeException e) {
                return new Failure(401);
            }
        }
    }

}
