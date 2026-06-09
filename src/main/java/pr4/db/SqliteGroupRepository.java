package pr4.db;

import pr4.model.Group;

import java.sql.*;

public class SqliteGroupRepository implements GroupRepository {

    private final Connection connection;

    public SqliteGroupRepository(String dbName) {
        this(SqliteConnections.open(dbName));
    }

    public SqliteGroupRepository(Connection connection) {
        this.connection = connection;
        init();
    }

    @Override
    public int insert(Group group) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO product_group(name) values (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());

            int inserted = ps.executeUpdate();
            if (inserted < 1) {
                throw new RuntimeException("Insert failed");
            }

            ResultSet generatedKeys = ps.getGeneratedKeys();
            return generatedKeys.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert group: " + group, e);
        }
    }

    private void init() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS product_group (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) not null
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Exception while DB init", e);
        }
    }
}
