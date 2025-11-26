package Testing.Concrete;

import Domain.Database.sqliteConnector;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class TestDatabaseUtils {

    private static final String DB_URL = "jdbc:sqlite:erp.db";

    public static void clearAllTables() {
        String[] tables = {
                "admins", "instructors", "students",
                "courses", "sections", "teaching",
                "enrollments", "records", "timetable",
                "gradings", "slabs", "contact_info", "security"
        };

        try (Connection conn = sqliteConnector.connect(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF;");

            for (String table : tables) {
                try {
                    stmt.execute("DELETE FROM " + table);
                } catch (SQLException e) {
                }
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}