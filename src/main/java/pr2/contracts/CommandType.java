package pr2.contracts;

public enum CommandType {
    GET_STOCK(1),
    REMOVE_STOCK(2),
    ADD_STOCK(3),
    CREATE_GROUP(4),
    ADD_PRODUCT_TO_GROUP(5),
    SET_PRICE(6);

    private final int code;
    CommandType(int code) {
        this.code = code;
    }
    public int code() {
        return code;
    }

    public static CommandType fromCode(int code) {
        for (CommandType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + code);
    }
}