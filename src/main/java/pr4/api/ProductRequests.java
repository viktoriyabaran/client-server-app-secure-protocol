package pr4.api;

public final class ProductRequests {

    private ProductRequests() {
    }

    public record CreateRecord(String name, String category, String manufacturer, int quantity, double price) {
    }

    public record SetPriceRecord(int id, double price) {
    }

    public record IdRecord(int id) {
    }
}
