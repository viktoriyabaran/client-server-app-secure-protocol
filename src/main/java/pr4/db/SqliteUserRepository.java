package pr4.db;

import pr4.model.User;

import java.sql.*;
import java.util.Optional;

public class SqliteUserRepository implements UserRepository {

    private final Connection connection;

    public SqliteUserRepository(String dbName) {
        this(SqliteConnections.open(dbName));
    }

    public SqliteUserRepository(Connection connection) {
        this.connection = connection;
        init();
    }

    @Override
    public boolean comparePasswordById(int id, String passwordToCheck) {
        try (PreparedStatement ps = connection.prepareStatement("select password from user where id = ?")) {
            ps.setInt(1, id);

            String password = "";
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    password = rs.getString("password");
                }
            }

            return password.equals(passwordToCheck);
        } catch (SQLException e) {
            throw new RuntimeException("Can't compare passwords by id: " + id, e);
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement("select * from user where username = ?")) {
            ps.setString(1, username);

            String password = "";
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(rs.getInt("id"), rs.getString("username"), rs.getString("email")));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Can't get id by username: " + username, e);
        }
    }

    private void init() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS user (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(100) not null,
                    password VARCHAR(100) not null,
                    email VARCHAR(100) not null
                )
                """);

            statement.execute("""
                INSERT INTO user (username, password, email)
                SELECT 'user', 'password', 'user@example.com'
                WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'user')
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Exception while DB init", e);
        }
    }
}
