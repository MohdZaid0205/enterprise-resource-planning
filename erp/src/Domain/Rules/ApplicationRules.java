package Domain.Rules;

import Domain.Database.sqliteConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplicationRules {

    private static final String database = "jdbc:sqlite:erp.db";

    public static boolean isMaintenanceMode() {
        createTable();
        return getBoolean("maintenance_mode");
    }

    public static void setMaintenanceMode(boolean enabled) {
        createTable();
        setValue("maintenance_mode", String.valueOf(enabled));
    }

    public static String getCurrentSemester() {
        createTable();
        String val = getValue("current_semester");
        return val == null ? "FALL_2025" : val;
    }

    public static void setCurrentSemester(String semester) {
        createTable();
        setValue("current_semester", semester);
    }

    public static String getAddDropDeadline() {
        createTable();
        return getValue("add_drop_deadline");
    }

    public static void setAddDropDeadline(String date) {
        createTable();
        setValue("add_drop_deadline", date);
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

    private static String getValue(String key) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean getBoolean(String key) {
        String val = getValue(key);
        return Boolean.parseBoolean(val);
    }

    private static void setValue(String key, String value) {
        String sql = "INSERT INTO settings(key, value) VALUES(?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}