package Concretes;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends UserEntity {

    private final StudentDataModel dataModel;
    private final EnrollmentModel enrollmentModel;

    /**
     * CONSTRUCTOR FOR NEW STUDENT
     * @param enrollmentDate Date string (e.g., "2025-08-01") or Batch ID.
     */
    public Student(String entity_id, String entity_name, String enrollmentDate)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(entity_id, entity_name);
        this.permission = Permission.PERMISSION_STUDENT;

        dataModel = new StudentDataModel(enrollmentDate);
        enrollmentModel = new EnrollmentModel();
    }

    public Student(String entity_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(entity_id, "TempName");
        this.permission = Permission.PERMISSION_STUDENT;

        dataModel = new StudentDataModel();
        enrollmentModel = new EnrollmentModel();

        dataModel.ReadFromDatabase();
        enrollmentModel.ReadFromDatabase();
    }

    public String getEnrollmentDate() { return dataModel.enrollmentDate; }
    public List<Section.StudentGradeProxy> getSemesterRecord(String semesterKey) {
        List<Section.StudentGradeProxy> semesterReport = new ArrayList<>();

        List<String> sectionIds = enrollmentModel.getSectionsForSemester(semesterKey);

        if (sectionIds != null) {
            for (String secId : sectionIds) {
                try {
                    Section section = new Section(secId);
                    Section.StudentGradeProxy proxy = section.getStudentGradeRecord(this.getId(), this.permission);
                    semesterReport.add(proxy);
                } catch (Exception e) {
                    System.err.println("Error loading section " + secId + ": " + e.getMessage());
                }
            }
        }
        return semesterReport;
    }
    public void enrollInCourse(String sectionId) throws SQLException {
        Section section = new Section(sectionId);
        String sectionSemester = section.getSemester();

        if (sectionSemester == null || sectionSemester.isEmpty()) {
            throw new SQLException("Cannot enroll: Section " + sectionId + " has no assigned semester.");
        }

        enrollmentModel.addCourse(sectionSemester, sectionId);
        onPresistenceSave();
    }

    @Override
    public void onPresistenceSave() throws SQLException {
        dataModel.WriteToDatabase();
        contactInfo.WriteToDatabase();
        enrollmentModel.WriteToDatabase();
    }

    @Override
    public void onPresistenceDelete() throws SQLException {
        dataModel.DeleteFromTable();
        contactInfo.DeleteFromTable();
        enrollmentModel.DeleteFromTable();
    }

    private class StudentDataModel implements IDatabaseModel {
        public String enrollmentDate;

        private static final String database = "jdbc:sqlite:students.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS students(" +
                                                    "id TEXT PRIMARY KEY, " +
                                                    "name TEXT, " +
                                                    "enrollment_date TEXT" +
                                                ")";
        private static final String insertSql = "INSERT INTO students(id, name, enrollment_date) VALUES(?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "name=excluded.name, " +
                                                "enrollment_date=excluded.enrollment_date";
        private static final String selectSql = "SELECT name, enrollment_date FROM students WHERE id = ?";
        private static final String deleteSql = "DELETE FROM students WHERE id = ?";

        public StudentDataModel(String date) { this.enrollmentDate = date; }
        public StudentDataModel() {}

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
                s.setString(3, enrollmentDate);
                s.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, getId());
                ResultSet rs = s.executeQuery();
                if(rs.next()) {
                    setName(rs.getString("name"));
                    enrollmentDate = rs.getString("enrollment_date");
                }
            }
        }
        @Override public void DeleteFromTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql))
            {s.setString(1, getId()); s.executeUpdate();}
        }
    }
    private class EnrollmentModel implements IDatabaseModel {
        public Map<String, List<String>> transcript = new HashMap<>();

        public void addCourse(String sem, String secId) {
            transcript.computeIfAbsent(sem, k -> new ArrayList<>()).add(secId);
        }

        public List<String> getSectionsForSemester(String sem) {
            return transcript.get(sem);
        }

        private static final String database = "jdbc:sqlite:enrollments.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS enrollments(" +
                                                    "student_id TEXT, " +
                                                    "section_id TEXT, " +
                                                    "semester TEXT, " +
                                                "PRIMARY KEY(student_id, section_id)" +
                                                ")";
        private static final String insertSql = "INSERT OR IGNORE INTO enrollments(student_id, section_id, semester) " +
                                                "VALUES(?, ?, ?)";
        private static final String selectSql = "SELECT section_id, semester FROM enrollments WHERE student_id = ?";
        private static final String deleteSql = "DELETE FROM enrollments WHERE student_id = ?";

        @Override
        public void CreateTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(tableSql))
            {s.executeUpdate();}
        }

        @Override
        public void WriteToDatabase() throws SQLException {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(insertSql)){
                for(Map.Entry<String, List<String>> entry : transcript.entrySet()) {
                    String sem = entry.getKey();
                    for(String secId : entry.getValue()) {
                        s.setString(1, getId());
                        s.setString(2, secId);
                        s.setString(3, sem);
                        s.addBatch();
                    }
                }
                s.executeBatch();
            }
        }

        @Override
        public void ReadFromDatabase() throws SQLException {
            transcript.clear();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, getId());
                ResultSet rs = s.executeQuery();
                while(rs.next()) {
                    addCourse(rs.getString("semester"),
                            rs.getString("section_id"));
                }
            }
        }

        @Override
        public void DeleteFromTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql))
            {s.setString(1, getId()); s.executeUpdate();}
        }
    }
}