package pr4.filter;

public class Page {
    public final int limit;
    public final int offset;

    public Page(int limit, int offset) {
        if (limit < 0 || offset < 0) {
            throw new IllegalArgumentException("limit and offset must be >= 0");
        }
        this.limit = limit;
        this.offset = offset;
    }

    public static Page of(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be >= 1");
        }
        return new Page(pageSize, (pageNumber - 1) * pageSize);
    }
}
