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
    private static final String database = "jdbc:sqlite:students.db";
    private static final String tableSql = "CREATE TABLE IF NOT EXISTS students("+
                                                    "id TEXT PRIMARY KEY NOT NULL,"+
                                                    "name TEXT NOT NULL"+
                                            ")";
    private static final String insertSql = "INSERT INTO students(id, name) VALUES(?, ?)" +
                                            "ON CONFLICT DO UPDATE SET name = excluded.name";
    private static final String deleteSql = "DELETE FROM students WHERE id IN"+
                                            "(SELECT id FROM students WHERE id=?)";
    private static final String selectSql = "SELECT id, name FROM students WHERE id = ?";



    public StudentDataModel(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException
    { super(entity_id, entity_name); permission = Permission.PERMISSION_STUDENT; }

    public StudentDataModel(String entity_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { this(entity_id, ""); ReadFromDatabase(); }

    @Override
    public void CreateTable() throws SQLException {
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(tableSql);) {
            stmt.executeUpdate();
        }
    }

    @Override
    public void WriteToDatabase() throws SQLException {

        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(insertSql);) {
            stmt.setString(1,getId());
            stmt.setString(2,getName());

            stmt.executeUpdate();
        }
    }

    @Override
    public void ReadFromDatabase() throws SQLException {
        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                setId(rs.getString("id"));
                setName(rs.getString("name"));
            } else {
                System.out.println("User not found.");
            }
        }
    }

    @Override
    public void DeleteFromTable() throws SQLException {
        try (Connection conn = sqliteConnector.connect(database);
        PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, getId());
            stmt.executeUpdate();
        }
    }
}
