package pr2.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import pr1.Message;
import pr1.Packet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ProcessorHelpers {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ProcessorHelpers() {
    }

    public static <T> T parse(byte[] body, Class<T> type) {
        try {
            return MAPPER.readValue(body, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON body: " + e.getMessage());
        }
    }

    public static String stringify(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't serialize to JSON", e);
        }
    }

    public static Packet buildOk(Packet request, Map<String, Object> data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "OK");
        if (data != null) {
            body.putAll(data);
        }
        return wrap(request, stringify(body));
    }

    public static Packet buildError(Packet request, String message) {
        return wrap(request, stringify(Map.of("status", "ERROR", "message", message)));
    }

    public static Packet wrap(Packet request, String body) {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        Message responseMsg = new Message(
                request.getbMsg().getcType(),
                request.getbMsg().getbUserId(),
                payload
        );
        return new Packet(request.getbSrc(), request.getbPktId(), responseMsg);
    }
}
