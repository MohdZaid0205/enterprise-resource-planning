package Domain.Concretes;

import Domain.Abstracts.UserEntity;
import Domain.Database.sqliteConnector;
import Domain.Exceptions.InvalidEntityIdentityException;
import Domain.Exceptions.InvalidEntityNameException;
import Domain.Interfaces.IDatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Instructor extends Domain.Abstracts.UserEntity {

    private final InstructorDataModel dataModel;
    private final TeachingAssignmentModel assignmentModel;

    public Instructor(String entity_id, String entity_name, String email, String phone_number, String password)
        throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException{
        super(entity_id, entity_name,  email, phone_number, password);
        this.permission = UserEntity.Permission.PERMISSION_INSTRUCTOR;
        dataModel = new InstructorDataModel();
        assignmentModel = new TeachingAssignmentModel();
    }

    public Instructor(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(entity_id, entity_name);
        this.permission = UserEntity.Permission.PERMISSION_INSTRUCTOR;
        dataModel = new InstructorDataModel();
        assignmentModel = new TeachingAssignmentModel();
    }

    public Instructor(String entity_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(entity_id, "TempLoad");
        this.permission = UserEntity.Permission.PERMISSION_INSTRUCTOR;
        dataModel = new InstructorDataModel();
        dataModel.ReadFromDatabase();
        assignmentModel = new TeachingAssignmentModel();
        assignmentModel.ReadFromDatabase();
    }

    public void assignToSection(String sectionId) throws SQLException {
        if (!assignmentModel.mySections.contains(sectionId)) {
            assignmentModel.mySections.add(sectionId);
            onPresistenceSave();
        }
    }

    public void enterMarks(String sectionId, String studentId, float l, float q, float m,
                           float e, float a, float p, float b)
            throws SQLException {
        validateOwnership(sectionId);
        Section section = new Domain.Concretes.Section(sectionId);
        Domain.Concretes.Section.StudentGradeProxy proxy = section.getStudentGradeRecord(studentId, this.permission);
        proxy.setLabs(l);
        proxy.setQuiz(q);
        proxy.setMidExams(m);
        proxy.setEndExams(e);
        proxy.setAssignments(a);
        proxy.setProjects(p);
        proxy.setBonus(b);
        proxy.WriteToDatabase();
    }

    public CourseStatsModel getSectionStats(String sectionId) throws SQLException {
        validateOwnership(sectionId);
        CourseStatsModel stats = new CourseStatsModel();

        String sql = "SELECT AVG(labs+quiz+mid+end+assign+proj+bonus) as avg_score, " +
                    "MAX(labs+quiz+mid+end+assign+proj+bonus) as max_score, " +
                    "MIN(labs+quiz+mid+end+assign+proj+bonus) as min_score " +
                    "FROM records WHERE section_id = ?";

        try (Connection conn = sqliteConnector.connect("jdbc:sqlite:academic_records.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sectionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setAverage(rs.getFloat("avg_score"));
                stats.setHighest(rs.getFloat("max_score"));
                stats.setLowest(rs.getFloat("min_score"));
            }
        }
        return stats;
    }

    private void validateOwnership(String sectionId) throws SecurityException {
        if (!assignmentModel.mySections.contains(sectionId)) {
            throw new SecurityException("Access Denied: You are not assigned to Section " + sectionId);
        }
    }

    @Override public void onPresistenceSave() throws SQLException {
        dataModel.WriteToDatabase();
        contactInfo.WriteToDatabase();
        assignmentModel.WriteToDatabase();
        security.WriteToDatabase();
    }
    @Override public void onPresistenceDelete() throws SQLException {
        dataModel.DeleteFromTable();
        contactInfo.DeleteFromTable();
        assignmentModel.DeleteFromTable();
        security.DeleteFromTable();
    }

    public class CourseStatsModel {
        private float average;
        private float highest;
        private float lowest;

        public float getAverage() { return average; }
        public void setAverage(float average) { this.average = average; }

        public float getHighest() { return highest; }
        public void setHighest(float highest) { this.highest = highest; }

        public float getLowest() { return lowest; }
        public void setLowest(float lowest) { this.lowest = lowest; }
    }
    private class InstructorDataModel implements IDatabaseModel {
        private static final String database = "jdbc:sqlite:instructors.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS instructors(id TEXT PRIMARY KEY, name TEXT)";
        private static final String insertSql = "INSERT INTO instructors(id, name) VALUES(?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET name=excluded.name";
        private static final String selectSql = "SELECT name FROM instructors WHERE id = ?";
        private static final String deleteSql = "DELETE FROM instructors WHERE id = ?";

        @Override public void CreateTable()      throws SQLException
        {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(tableSql))
            {s.executeUpdate();}
        }
        @Override public void WriteToDatabase()  throws SQLException
        {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                 PreparedStatement s=c.prepareStatement(insertSql)) {
                s.setString(1, getId());
                s.setString(2, getName());
                s.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException
        {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, getId());
                ResultSet rs =s.executeQuery();
                if(rs.next())
                    setName(rs.getString("name"));
            }
        }
        @Override public void DeleteFromTable()  throws SQLException
        {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql)){
                s.setString(1, getId());
                s.executeUpdate();}
        }
    }
    private class TeachingAssignmentModel implements IDatabaseModel {
        public List<String> mySections = new ArrayList<>();
        private static final String database = "jdbc:sqlite:assignments.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS teaching(" +
                                                    "instructor_id TEXT, " +
                                                    "section_id TEXT, " +
                                                "PRIMARY KEY(instructor_id, section_id)" +
                                                ")";
        private static final String insertSql = "INSERT INTO teaching(instructor_id, section_id) " +
                                                "VALUES(?, ?) ON CONFLICT(instructor_id, section_id) " +
                                                "DO UPDATE SET section_id=excluded.section_id";
        private static final String selectSql = "SELECT section_id FROM teaching WHERE instructor_id = ?";
        private static final String deleteSql = "DELETE FROM teaching WHERE instructor_id = ?";

        @Override public void CreateTable() throws SQLException
        {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(tableSql))
            {s.executeUpdate();}
        }
        @Override public void WriteToDatabase() throws SQLException
        {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(insertSql)){
                for(String sec : mySections) {
                    s.setString(1, getId());
                    s.setString(2, sec);
                    s.addBatch();
                }
                s.executeBatch();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException
        {
            mySections.clear();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, getId());
                ResultSet rs =s.executeQuery();
                while(rs.next())
                    mySections.add(rs.getString("section_id"));
            }
        }
        @Override public void DeleteFromTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql)){
                s.setString(1, getId());
                s.executeUpdate();
            }
        }
    }
}