package Domain.Concretes;

import Domain.Database.sqliteConnector;
import Domain.Exceptions.InvalidEntityIdentityException;
import Domain.Exceptions.InvalidEntityNameException;
import Domain.Interfaces.IDatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Course extends Domain.Abstracts.ResourceEntity {

    private final CourseMetadata metadata;

    public Course(String course_code, String course_title, int credits, int capacity)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(course_code, course_title);
        this.metadata = new CourseMetadata(credits, capacity);
    }

    public Course(String course_code)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(course_code, "TempLoad");
        this.metadata = new CourseMetadata();
        this.metadata.ReadFromDatabase();
    }

    public int getCredits() { return metadata.credits; }
    public void setCredits(int credits) { metadata.credits = credits; }

    public int getCapacity() { return metadata.capacity; }
    public void setCapacity(int capacity) { metadata.capacity = capacity; }

    @Override
    public void onPresistenceSave() throws SQLException {
        metadata.WriteToDatabase();
    }

    @Override
    public void onPresistenceDelete() throws SQLException {
        metadata.DeleteFromTable();
    }

    private class CourseMetadata implements IDatabaseModel {
        public int credits;
        public int capacity;

        private static final String database = "jdbc:sqlite:courses.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS courses (" +
                                                    "id TEXT PRIMARY KEY, " +
                                                    "title TEXT, " +
                                                    "credits INTEGER, " +
                                                    "capacity INTEGER" +
                                                ")";
        private static final String insertSql = "INSERT INTO courses(id, title, credits, capacity) " +
                                                "VALUES(?, ?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "title=excluded.title, " +
                                                "credits=excluded.credits, " +
                                                "capacity=excluded.capacity";
        private static final String selectSql = "SELECT title, credits, capacity FROM courses WHERE id = ?";
        private static final String deleteSql = "DELETE FROM courses WHERE id = ?";

        public CourseMetadata(int credits, int capacity) {
            this.credits = credits;
            this.capacity = capacity;
        }
        public CourseMetadata() throws SQLException { ReadFromDatabase(); }

        @Override public void CreateTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(tableSql))
            {s.executeUpdate();}
        }

        @Override public void WriteToDatabase() throws SQLException {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(insertSql)){
                s.setString(1, getId());
                s.setString(2, getName());
                s.setInt(3, credits);
                s.setInt(4, capacity);
                s.executeUpdate();
            }
        }

        @Override public void ReadFromDatabase() throws SQLException {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, getId());
                ResultSet rs = s.executeQuery();
                if(rs.next()){
                    setName(rs.getString("title"));
                    credits = rs.getInt("credits");
                    capacity = rs.getInt("capacity");
                }
            }
        }

        @Override public void DeleteFromTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql))
            {s.setString(1, getId()); s.executeUpdate();}
        }
    }
}