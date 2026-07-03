package pr3.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public final class HTTPHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HTTPHelper() {
    }

    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    public static Integer pathId(HttpExchange exchange, String prefix) {
        String tail = exchange.getRequestURI().getPath().substring(prefix.length());
        if (tail.startsWith("/")) {
            tail = tail.substring(1);
        }
        if (tail.isBlank()) {
            return null;
        }
        return Integer.parseInt(tail);
    }
}
