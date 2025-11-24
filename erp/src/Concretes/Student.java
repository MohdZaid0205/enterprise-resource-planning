package Concretes;

import Abstracts.ResourceEntity;
import Abstracts.UserEntity;
import Database.sqliteConnector;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;
import Interfaces.IDatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Student extends UserEntity {

    private StudentDataModel dataModel;

    public ArrayList<ResourceEntity> courses;

    public Student(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { super(entity_id, entity_name); permission = Permission.PERMISSION_STUDENT; }

    @Override
    public void onPresistenceSave() throws SQLException {
        dataModel.WriteToDatabase();
        contactInfo.WriteToDatabase();
    }

    @Override
    public void onPresistenceDelete() throws SQLException {
        dataModel.DeleteFromTable();
        contactInfo.DeleteFromTable();
    }

    public Student(String entity_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { this(entity_id, ""); dataModel = new StudentDataModel();}


    public class StudentDataModel implements IDatabaseModel {
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

        public StudentDataModel()
                throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
        { ReadFromDatabase(); }

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
                 PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, getId());
                ResultSet rs = stmt.executeQuery();

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

}
