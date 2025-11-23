package Database;

import Concretes.UserEntity;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;
import Interfaces.IDatabaseModel;

import javax.naming.InvalidNameException;
import java.sql.*;

public class StudentDataModel extends UserEntity
    implements IDatabaseModel
{
    private static final String database =  "jdbc:sqlite:students.db";

    public StudentDataModel(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException
    { super(entity_id, entity_name); permission = Permission.PERMISSION_STUDENT; }

    public StudentDataModel(String entity_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { this(entity_id, ""); ReadFromDatabase(); }

    @Override
    public void CreateTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS "  +
                "students("                         +
                    "id varchar(255) PRIMARY KEY,"  +
                    "name varchar(255)"             +
                ")";
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.executeUpdate();
        }
    }

    @Override
    public void WriteToDatabase() throws SQLException {
        String sql = "INSERT INTO students(id, name) VALUES (?, ?)" +
                "ON CONFLICT DO UPDATE SET name = excluded.name;";

        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1,getId());
            stmt.setString(2,getName());

            stmt.executeUpdate();
        }
    }

    @Override
    public void ReadFromDatabase() throws SQLException {
        String sql = "SELECT id, name FROM students WHERE id = ?";

        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, getId());

            // ResultSet acts as your "Cursor"
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                setId(rs.getString("id"));
                setName(rs.getString("name"));
            } else {
                System.out.println("User not found.");
            }
        }
    }
}
