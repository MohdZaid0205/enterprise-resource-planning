package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DATABASE CONNECTOR utility providing static access to the persistence layer.
 * this class ensures that the application maintains a valid link [BRIDGE]
 * to the underlying SQLite storage engine.
 */
public class sqliteConnector {
    private static Connection connection = null;

    /**
     * ESTABLISHES the physical connection to the SQLite database file.
     * if the connection is currently null or closed it initializes a new one [LAZY LOAD],
     * otherwise it returns the existing instance.
     *
     * @param URL is the JDBC connection string pointing to [LOCATION] of the database.
     * @return the active [OPEN] Connection object ready for statements.
     */
    public static Connection connect(String URL) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
}