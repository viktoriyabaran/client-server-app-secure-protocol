package pr4.db;

import pr4.filter.Page;
import pr4.filter.ProductFilter;
import pr4.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqliteProductRepository implements ProductRepository {

    private final Connection connection;

    public SqliteProductRepository(String dbName) {
        this(SqliteConnections.open(dbName));
    }

    public SqliteProductRepository(Connection connection) {
        this.connection = connection;
        init();
    }

    @Override
    public int insert(Product product) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO product(name, category, manufacturer, quantity, price, group_id) values (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setString(3, product.getManufacturer());
            ps.setInt(4, product.getQuantity());
            ps.setDouble(5, product.getPrice());
            ps.setObject(6, product.getGroupId());

            int inserted = ps.executeUpdate();
            if (inserted < 1) {
                throw new RuntimeException("Insert failed");
            }

            ResultSet generatedKeys = ps.getGeneratedKeys();
            return generatedKeys.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert product: " + product, e);
        }
    }

    @Override
    public Optional<Product> getById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("select * from product where id = ?")) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Can't get product by id: " + id, e);
        }
    }

    @Override
    public boolean update(Product product) {
        try (PreparedStatement ps = connection.prepareStatement(
                "update product set name = ?, category = ?, manufacturer = ?, quantity = ?, price = ?, group_id = ? where id = ?")) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setString(3, product.getManufacturer());
            ps.setInt(4, product.getQuantity());
            ps.setDouble(5, product.getPrice());
            ps.setObject(6, product.getGroupId());
            ps.setInt(7, product.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can't update product: " + product, e);
        }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = connection.prepareStatement("delete from product where id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete product by id: " + id, e);
        }
    }

    @Override
    public List<Product> search(ProductFilter filt, Page page) {
        SqlWrapper wrap = whereBuilder(filt);
        String sql = "select product.* from product" + buildJoinClause(filt) + wrap.sql;
        if (page != null) {
            sql += " limit ? offset ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            for (Object param : wrap.params) {
                ps.setObject(i++, param);
            }
            if (page != null) {
                ps.setInt(i++, page.limit);
                ps.setInt(i, page.offset);
            }
            System.out.println(ps);

            List<Product> products = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }

            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Can't search products", e);
        }
    }

    @Override
    public int count(ProductFilter filt) {
        SqlWrapper wrap = whereBuilder(filt);
        try (PreparedStatement ps = connection.prepareStatement("select count(*) from product" + buildJoinClause(filt) + wrap.sql)) {
            for (int i = 0; i < wrap.params.size(); i++) {
                ps.setObject(i + 1, wrap.params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can't count products", e);
        }
    }

    @Override
    public int deleteAll() {
        try (PreparedStatement ps = connection.prepareStatement("delete from product")) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete products", e);
        }
    }

    private SqlWrapper whereBuilder(ProductFilter filt) {
        SqlWrapper wrapper = new SqlWrapper();
        if (filt == null) {
            wrapper.sql = "";
            return wrapper;
        }

        String str = Stream.of(
                        stringLike("product.name", filt.name, wrapper.params),
                        stringEquals("product.category", filt.category, wrapper.params),
                        stringEquals("product.manufacturer", filt.manufacturer, wrapper.params),
                        greaterOrEqual("product.quantity", filt.minQuantity, wrapper.params),
                        lowerOrEqual("product.quantity", filt.maxQuantity, wrapper.params),
                        greaterOrEqual("product.price", filt.minPrice, wrapper.params),
                        lowerOrEqual("product.price", filt.maxPrice, wrapper.params),
                        stringLike("product_group.name", filt.groupName, wrapper.params))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" and "));

        wrapper.sql = str.isEmpty() ? "" : " where " + str;
        return wrapper;
    }

    private String buildJoinClause(ProductFilter filt) {
        if (filt != null && filt.groupName != null) {
            return " join product_group on product.group_id = product_group.id";
        }
        return "";
    }

    private String stringEquals(String columnName, Object value, List<Object> params) {
        if (value == null)
            return null;

        params.add(value);
        return columnName + " = ?";
    }

    private String stringLike(String columnName, String value, List<Object> params) {
        if (value == null)
            return null;

        params.add("%" + value + "%");
        return columnName + " like ?";
    }

    private String greaterOrEqual(String columnName, Object value, List<Object> params) {
        if (value == null)
            return null;

        params.add(value);
        return columnName + " >= ?";
    }

    private String lowerOrEqual(String columnName, Object value, List<Object> params) {
        if (value == null)
            return null;

        params.add(value);
        return columnName + " <= ?";
    }

    private Product map(ResultSet rs) throws SQLException {
        Product product = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("manufacturer"),
                rs.getInt("quantity"),
                rs.getDouble("price"));

        int groupId = rs.getInt("group_id");
        product.setGroupId(rs.wasNull() ? null : groupId);

        return product;
    }

    private void init() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS product (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) not null,
                    category VARCHAR(100) not null,
                    manufacturer VARCHAR(100) not null,
                    quantity INTEGER not null,
                    price REAL not null,
                    group_id INTEGER REFERENCES product_group(id)
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Exception while DB init", e);
        }
    }
}
