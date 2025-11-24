package Abstracts;

import Database.sqliteConnector;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;
import Interfaces.IDatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResourceEntity extends EntityABC {
    public ResourceEntity(String entity_id, String entity_name)
        throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { super(entity_id, entity_name); gradingModel = new GradingPolicyModel(); gradingSlabs = new GradingSlabs();}

    protected final GradingPolicyModel  gradingModel;
    public class GradingPolicyModel implements IDatabaseModel {
        public float labs = 15;
        public float quiz = 10;
        public float mid_exams = 25;
        public float end_exams = 25;
        public float assignments = 15;
        public float projects = 10 ;
        public float bonus = 5;

        private static final String database = "jdbc:sqlite:gradings.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS gradings (" +
                                                    "id TEXT PRIMARY KEY NOT NULL, " +
                                                    "labs FLOAT NOT NULL, " +
                                                    "quiz FLOAT NOT NULL, " +
                                                    "mid_exams FLOAT NOT NULL, " +
                                                    "end_exams FLOAT NOT NULL, " +
                                                    "assignments FLOAT NOT NULL, " +
                                                    "projects FLOAT NOT NULL, " +
                                                    "bonus FLOAT NOT NULL" +
                                                ")";
        private static final String insertSql = "INSERT INTO gradings(" +
                                                "id, labs, quiz, mid_exams, end_exams, assignments, projects, bonus) " +
                                                "VALUES(?, ?, ?, ?, ?, ?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "labs = excluded.labs, " +
                                                "quiz = excluded.quiz, " +
                                                "mid_exams = excluded.mid_exams, " +
                                                "end_exams = excluded.end_exams, " +
                                                "assignments = excluded.assignments, " +
                                                "projects = excluded.projects, " +
                                                "bonus = excluded.bonus";
        private static final String deleteSql = "DELETE FROM gradings WHERE id = ?";
        private static final String selectSql = "SELECT labs, quiz, mid_exams, end_exams, " +
                                                "assignments, projects, bonus FROM gradings WHERE id = ?";

        public GradingPolicyModel(float labs, float quiz, float midExams, float endExams,
                                  float assignments, float projects, float bonus) {
            this.labs = labs;
            this.quiz = quiz;
            this.mid_exams = midExams;
            this.end_exams = endExams;
            this.assignments = assignments;
            this.projects = projects;
            this.bonus = bonus;
        }

        public GradingPolicyModel() throws SQLException
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
                stmt.setString(1, getId());
                stmt.setFloat(2, labs);
                stmt.setFloat(3, quiz);
                stmt.setFloat(4, mid_exams);
                stmt.setFloat(5, end_exams);
                stmt.setFloat(6, assignments);
                stmt.setFloat(7, projects);
                stmt.setFloat(8, bonus);
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
                    labs = rs.getFloat(1);
                    quiz = rs.getFloat(2);
                    mid_exams = rs.getFloat(3);
                    end_exams = rs.getFloat(4);
                    assignments = rs.getFloat(5);
                    projects = rs.getFloat(6);
                    bonus = rs.getFloat(7);
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

    protected final GradingSlabs gradingSlabs;
    public class GradingSlabs implements IDatabaseModel {
        public float O = 100;
        public float A = 90;
        public float A_ = 80;
        public float B = 70;
        public float B_ = 60;
        public float C = 50;
        public float C_ = 40;
        public float D = 30;
        public float F = 0;

        private static final String database = "jdbc:sqlite:slabs.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS slabs (" +
                                                    "id TEXT PRIMARY KEY NOT NULL, " +
                                                    "O FLOAT NOT NULL, " +
                                                    "A FLOAT NOT NULL, " +
                                                    "A_ FLOAT NOT NULL, " +
                                                    "B FLOAT NOT NULL, " +
                                                    "B_ FLOAT NOT NULL, " +
                                                    "C FLOAT NOT NULL, " +
                                                    "C_ FLOAT NOT NULL, " +
                                                    "D FLOAT NOT NULL, " +
                                                    "F FLOAT NOT NULL, " +
                                                ")";
        private static final String insertSql = "INSERT INTO slabs(" +
                                                    "id, O, A, B, B_, C, C_, D, F) " +
                                                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                                    "ON CONFLICT(id) DO UPDATE SET " +
                                                    "O = excluded.O,"+
                                                    "A = excluded.A,"+
                                                    "A_ = excluded.A_,"+
                                                    "B = excluded.B,"+
                                                    "B_ = excluded.B_,"+
                                                    "C = excluded.C,"+
                                                    "C_ = excluded.C_,"+
                                                    "D = excluded.D,"+
                                                    "F = excluded.F;";

        private static final String deleteSql = "DELETE FROM slabs WHERE id = ?";
        private static final String selectSql = "SELECT  O, A, B, B_, C, C_, D, F  FROM slabs WHERE id = ?";

        public GradingSlabs(float o, float a, float a_, float b,
                            float b_, float c, float c_, float d, float f) {
            this.O = o;
            this.A = a;
            this.A_ = a_;
            this.B = b;
            this.B_ = b_;
            this.C = c;
            this.C_ = c_;
            this.D = d;
            this.F = f;
        }

        public GradingSlabs() throws SQLException
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
                stmt.setString(1, getId());
                stmt.setFloat(2, O);
                stmt.setFloat(3, A);
                stmt.setFloat(4, A_);
                stmt.setFloat(5, B);
                stmt.setFloat(6, B_);
                stmt.setFloat(7, C);
                stmt.setFloat(8, C_);
                stmt.setFloat(9, D);
                stmt.setFloat(10, F);
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
                    O  = rs.getFloat(1);
                    A  = rs.getFloat(2);
                    A_ = rs.getFloat(3);
                    B  = rs.getFloat(4);
                    B_ = rs.getFloat(5);
                    C  = rs.getFloat(6);
                    C_ = rs.getFloat(7);
                    D  = rs.getFloat(8);
                    F  = rs.getFloat(9);
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

    public float getLabs()          { return gradingModel.labs; }
    public float getQuiz()          { return gradingModel.quiz; }
    public float getMidExams()      { return gradingModel.mid_exams; }
    public float getEndExams()      { return gradingModel.end_exams; }
    public float getAssignments()   { return gradingModel.assignments; }
    public float getProjects()      { return gradingModel.projects; }
    public float getBonus()         { return gradingModel.bonus; }

    public void setLabs(float labs)                 { this.gradingModel.labs = labs; }
    public void setQuiz(float quiz)                 { this.gradingModel.quiz = quiz; }
    public void setMidExams(float mid_exams)        { this.gradingModel.mid_exams = mid_exams; }
    public void setEndExams(float end_exams)        { this.gradingModel.end_exams = end_exams; }
    public void setAssignments(float assignments)   { this.gradingModel.assignments = assignments; }
    public void setProjects(float projects)         { this.gradingModel.projects = projects; }
    public void setBonus(float bonus)               { this.gradingModel.bonus = bonus; }

    public float getO()  {return gradingSlabs.O;}
    public float getA()  {return gradingSlabs.A;}
    public float getA_() {return gradingSlabs.A_;}
    public float getB()  {return gradingSlabs.B;}
    public float getB_() {return gradingSlabs.B_;}
    public float getC()  {return gradingSlabs.C;}
    public float getC_() {return gradingSlabs.C_;}
    public float getD()  {return gradingSlabs.D;}
    public float getF()  {return gradingSlabs.F;}

    public void setO(float o)   {this.gradingSlabs.O = o;}
    public void setA(float a)   {this.gradingSlabs.A = a;}
    public void setA_(float a)  {this.gradingSlabs.A_ = a;}
    public void setB(float b)   {this.gradingSlabs.B = b;}
    public void setB_(float b)  {this.gradingSlabs.B_ = b;}
    public void setC(float c)   {this.gradingSlabs.C = c;}
    public void setC_(float c)  {this.gradingSlabs.C_ = c;}
    public void setD(float d)   {this.gradingSlabs.D = d;}
    public void setF(float f)   {this.gradingSlabs.F = f;}

    public abstract void onPresistenceSave() throws SQLException;
    public abstract void onPresistenceDelete() throws SQLException;

}
