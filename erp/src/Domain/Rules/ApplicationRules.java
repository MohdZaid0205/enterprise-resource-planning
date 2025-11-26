package Domain.Rules;

import Domain.Database.sqliteConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplicationRules {

    private static final String database = "jdbc:sqlite:settings.db";

    public static boolean isMaintenanceMode() {
        createTable();
        String sql = "SELECT value FROM settings WHERE key = 'maintenance_mode'";
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Boolean.parseBoolean(rs.getString("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to false
    }

    public static void setMaintenanceMode(boolean enabled) {
        createTable();
        String sql = "INSERT INTO settings(key, value) VALUES('maintenance_mode', ?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(enabled));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS settings (" +
                "key TEXT PRIMARY KEY, " +
                "value TEXT)";
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}