package pr4.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SqliteConnections {

    private SqliteConnections() {
    }

    public static Connection open(String dbName) {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (SQLException e) {
            throw new RuntimeException("Can't create SQLite DB", e);
        }
    }
}
