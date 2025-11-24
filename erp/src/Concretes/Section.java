package Concretes;

import Abstracts.ResourceEntity;
import Database.sqliteConnector;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;
import Interfaces.IDatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Section extends ResourceEntity {

    protected final GradingPolicyModel gradingModel;
    protected final GradingSlabs gradingSlabs;
    private final SectionMetadata metadata;

    /**
     * Represents a specific running instance of a Course.
     * Contains [GRADING POLICY] and [SLABS] specific to this section instance.
     */
    public Section(String section_id, String section_name, String instructor_id, String semester)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(section_id, section_name);
        this.metadata = new SectionMetadata(instructor_id, semester);
        this.gradingModel = new GradingPolicyModel(15, 10, 25, 25, 15, 10, 5);
        this.gradingSlabs = new GradingSlabs(100, 90, 80, 70, 60, 50, 40, 30, 0);
    }


    public Section(String section_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(section_id, "TempLoad");
        metadata     = new SectionMetadata();
        gradingModel = new GradingPolicyModel();
        gradingSlabs = new GradingSlabs();

        metadata.ReadFromDatabase();
        gradingModel.ReadFromDatabase();
        gradingSlabs.ReadFromDatabase();
    }

    @Override
    public void onPresistenceSave() throws SQLException {
        metadata.WriteToDatabase();
        gradingModel.WriteToDatabase();
        gradingSlabs.WriteToDatabase();
    }
    @Override
    public void onPresistenceDelete() throws SQLException {
        metadata.DeleteFromTable();
        gradingModel.DeleteFromTable();
        gradingSlabs.DeleteFromTable();
    }

    public class GradingPolicyModel implements IDatabaseModel {
        public float labs;
        public float quiz;
        public float mid_exams;
        public float end_exams;
        public float assignments;
        public float projects;
        public float bonus;

        private static final String database = "jdbc:sqlite:gradings.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS gradings (" +
                                                    "id TEXT PRIMARY KEY NOT NULL, " +
                                                    "labs FLOAT, quiz FLOAT, mid_exams FLOAT, end_exams FLOAT, " +
                                                    "assignments FLOAT, projects FLOAT, bonus FLOAT" +
                                                ")";
        private static final String insertSql = "INSERT INTO gradings(" +
                                                    "id, labs, quiz, mid_exams, end_exams, assignments, projects, bonus" +
                                                ") " +
                                                "VALUES(?, ?, ?, ?, ?, ?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "labs=excluded.labs, quiz=excluded.quiz, " +
                                                "mid_exams=excluded.mid_exams, " +
                                                "end_exams=excluded.end_exams, " +
                                                "assignments=excluded.assignments, " +
                                                "projects=excluded.projects, " +
                                                "bonus=excluded.bonus";

        private static final String deleteSql = "DELETE FROM gradings WHERE id = ?";
        private static final String selectSql = "SELECT * FROM gradings WHERE id = ?";

        public GradingPolicyModel(float l, float q, float m,
                                  float e, float a, float p,
                                  float b) {
            this.labs=l; this.quiz=q; this.mid_exams=m; this.end_exams=e;
            this.assignments=a; this.projects=p; this.bonus=b;
        }

        public GradingPolicyModel() {} // Empty constructor for DB load

        @Override public void CreateTable() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(tableSql))
            { stmt.executeUpdate(); }
        }
        @Override public void WriteToDatabase() throws SQLException {
            CreateTable();
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {
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
        @Override public void ReadFromDatabase() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    labs = rs.getFloat("labs");
                    quiz = rs.getFloat("quiz");
                    mid_exams = rs.getFloat("mid_exams");
                    end_exams = rs.getFloat("end_exams");
                    assignments = rs.getFloat("assignments");
                    projects = rs.getFloat("projects");
                    bonus = rs.getFloat("bonus");
                }
            }
        }
        @Override public void DeleteFromTable() throws SQLException {
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

    public void setLabs(float mark)        { this.gradingModel.labs = mark; }
    public void setQuiz(float mark)        { this.gradingModel.quiz = mark; }
    public void setMidExams(float mark)    { this.gradingModel.mid_exams = mark; }
    public void setEndExams(float mark)    { this.gradingModel.end_exams = mark; }
    public void setAssignments(float mark) { this.gradingModel.assignments = mark; }
    public void setProjects(float mark)    { this.gradingModel.projects = mark; }
    public void setBonus(float mark)       { this.gradingModel.bonus = mark; }

    public class GradingSlabs implements IDatabaseModel {
        public float O, A, A_, B, B_, C, C_, D, F;

        private static final String database = "jdbc:sqlite:slabs.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS slabs (" +
                                                    "id TEXT PRIMARY KEY NOT NULL, " +
                                                    "O FLOAT, A FLOAT, A_ FLOAT, B FLOAT, B_ FLOAT, " +
                                                    "C FLOAT, C_ FLOAT, D FLOAT, F FLOAT" +
                                                ")";

        private static final String insertSql = "INSERT INTO slabs(id, O, A, A_, B, B_, C, C_, D, F) " +
                                                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "O =excluded.O , A=excluded.A, " +
                                                "A_=excluded.A_, B=excluded.B, " +
                                                "B_=excluded.B_, C=excluded.C, " +
                                                "C_=excluded.C_, D=excluded.D, " +
                                                "F=excluded.F";

        private static final String deleteSql = "DELETE FROM slabs WHERE id = ?";
        private static final String selectSql = "SELECT * FROM slabs WHERE id = ?";

        public GradingSlabs(float o, float a, float a_, float b,
                            float b_, float c, float c_, float d, float f)
        {
            this.O =o ;
            this.A =a ;
            this.A_=a_;
            this.B =b ;
            this.B_=b_;
            this.C =c ;
            this.C_=c_;
            this.D =d ;
            this.F =f ;
        }

        public GradingSlabs() {}

        @Override public void CreateTable() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(tableSql))
            { stmt.executeUpdate(); }
        }
        @Override public void WriteToDatabase() throws SQLException {
            CreateTable();
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, getId());
                stmt.setFloat(2, O );
                stmt.setFloat(3, A );
                stmt.setFloat(4, A_);
                stmt.setFloat(5, B );
                stmt.setFloat(6, B_);
                stmt.setFloat(7, C );
                stmt.setFloat(8, C_);
                stmt.setFloat(9, D );
                stmt.setFloat(10,F );
                stmt.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    O  = rs.getFloat("O" );
                    A  = rs.getFloat("A" );
                    A_ = rs.getFloat("A_");
                    B  = rs.getFloat("B" );
                    B_ = rs.getFloat("B_");
                    C  = rs.getFloat("C" );
                    C_ = rs.getFloat("C_");
                    D  = rs.getFloat("D" );
                    F  = rs.getFloat("F" );
                }
            }
        }
        @Override public void DeleteFromTable() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, getId());
                stmt.executeUpdate();
            }
        }
    }

    public float getO()  {return gradingSlabs.O ;}
    public float getA()  {return gradingSlabs.A ;}
    public float getA_() {return gradingSlabs.A_;}
    public float getB()  {return gradingSlabs.B ;}
    public float getB_() {return gradingSlabs.B_;}
    public float getC()  {return gradingSlabs.C ;}
    public float getC_() {return gradingSlabs.C_;}
    public float getD()  {return gradingSlabs.D ;}
    public float getF()  {return gradingSlabs.F ;}

    public void setA (float a) {gradingSlabs.A  = a;}
    public void setA_(float a) {gradingSlabs.A_ = a;}
    public void setB (float b) {gradingSlabs.B  = b;}
    public void setB_(float b) {gradingSlabs.B_ = b;}
    public void setC (float c) {gradingSlabs.C  = c;}
    public void setC_(float c) {gradingSlabs.C_ = c;}
    public void setD (float d) {gradingSlabs.D  = d;}
    public void setF (float f) {gradingSlabs.F  = f;}

    private class SectionMetadata implements IDatabaseModel {
        public String instructor_id;
        public String semester;

        private static final String database = "jdbc:sqlite:sections.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS sections(" +
                                                    "id TEXT PRIMARY KEY, " +
                                                    "instructor_id TEXT" +
                                                    "semester TEXT NOT NULL" +
                                                ")";
        private static final String insertSql = "INSERT INTO sections(id, instructor_id, semester) VALUES(?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "instructor_id=excluded.instructor_id"+
                                                "semester=excluded.semester";
        private static final String selectSql = "SELECT instructor_id, semester FROM sections WHERE id = ?";
        private static final String deleteSql = "DELETE FROM sections WHERE id = ?";

        public SectionMetadata(String instructor_id, String semester)
        { this.instructor_id = instructor_id; this.semester = semester; }
        public SectionMetadata() {}

        @Override public void CreateTable() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(tableSql))
            { stmt.executeUpdate(); }
        }
        @Override public void WriteToDatabase() throws SQLException {
            CreateTable();
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, getId());
                stmt.setString(2, instructor_id);
                stmt.setString(3, semester);
                stmt.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()){
                    this.instructor_id = rs.getString("instructor_id");
                    this.semester = rs.getString("semester");
                }
            }
        }
        @Override public void DeleteFromTable() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, getId());
                stmt.executeUpdate();
            }
        }
    }

    public String getInstructorId() { return metadata.instructor_id; }
    public String getSemester() { return metadata.semester; }

    public void setInstructorId(String instructor_id) { metadata.instructor_id = instructor_id; }
    public void setSemester(String semester) { metadata.semester = semester; }
}