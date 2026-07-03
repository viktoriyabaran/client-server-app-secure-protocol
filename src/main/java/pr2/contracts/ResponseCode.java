package pr2.contracts;

public enum ResponseCode {
    OK(100),
    ERROR(199);

    private final int code;
    ResponseCode(int code) { this.code = code; }
    public int code() { return code; }
}
