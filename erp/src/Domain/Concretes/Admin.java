package Domain.Concretes;

import Domain.Abstracts.UserEntity;
import Domain.Database.sqliteConnector;
import Domain.Interfaces.IDatabaseModel;
import Domain.Concretes.Section;
import Domain.Exceptions.InvalidEntityIdentityException;
import Domain.Exceptions.InvalidEntityNameException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Admin extends Domain.Abstracts.UserEntity {

    private final AdminDataModel dataModel;

    public Admin(String entity_id, String entity_name, String email, String phone_number, String password)
        throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException{
        super(entity_id, entity_name,  email, phone_number, password);
        this.permission = UserEntity.Permission.PERMISSION_ADMIN;
        dataModel = new AdminDataModel();
    }

    public Admin(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(entity_id, entity_name);
        this.permission = UserEntity.Permission.PERMISSION_ADMIN;
        dataModel = new AdminDataModel();
    }

    public Admin(String entity_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(entity_id, "TempName");
        this.permission = UserEntity.Permission.PERMISSION_ADMIN;
        dataModel = new AdminDataModel();
        dataModel.ReadFromDatabase();
    }

    public void setSectionTimetable(String sectionId, List<Domain.Concretes.Section.TimeSlot> newSlots)
            throws SQLException {
        try {
            Domain.Concretes.Section section = new Domain.Concretes.Section(sectionId);
            section.updateTimetable(newSlots, this.permission);
        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("Failed to update timetable: " + e.getMessage());
        }
    }

    public void overrideStudentGrade(String sectionId, String studentId,
                                     float l, float q, float m, float e, float a, float p, float b)
            throws SQLException {
        Domain.Concretes.Section section = new Domain.Concretes.Section(sectionId);
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


    @Override
    public void onPresistenceSave() throws SQLException {
        dataModel.WriteToDatabase();
        contactInfo.WriteToDatabase();
        security.WriteToDatabase();
    }

    @Override
    public void onPresistenceDelete() throws SQLException {
        dataModel.DeleteFromTable();
        contactInfo.DeleteFromTable();
        security.DeleteFromTable();
    }

    private class AdminDataModel implements IDatabaseModel {
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS admins(" +
                                                    "id TEXT PRIMARY KEY, " +
                                                    "name TEXT" +
                                                ")";
        private static final String insertSql = "INSERT INTO admins(id, name) VALUES(?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "name=excluded.name";
        private static final String selectSql = "SELECT name FROM admins WHERE id = ?";
        private static final String deleteSql = "DELETE FROM admins WHERE id = ?";

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
                s.executeUpdate();
            }
        }

        @Override public void ReadFromDatabase() throws SQLException {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, getId());
                ResultSet rs = s.executeQuery();
                if(rs.next()) {
                    setName(rs.getString("name"));
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