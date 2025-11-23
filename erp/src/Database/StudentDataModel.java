package Database;

import Concretes.UserEntity;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;
import Interfaces.IDatabaseModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentDataModel extends UserEntity
    implements IDatabaseModel
{
    private static final String database =  "jdbc:sqlite:students.db";

    public StudentDataModel(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException
    { super(entity_id, entity_name); permission = Permission.PERMISSION_STUDENT; }


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
        String sql = "INSERT INTO students(id, name) VALUES (?, ?)";

        try (Connection conn = sqliteConnector.connect(database);
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1,getId());
            stmt.setString(2,getName());

            stmt.executeUpdate();
        }
    }

    @Override
    public void ReadFromDatabase() {

    }
}
