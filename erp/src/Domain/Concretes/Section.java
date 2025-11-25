package Domain.Concretes;

import Domain.Abstracts.ResourceEntity;
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

public class Section extends ResourceEntity {

    protected final GradingPolicyModel gradingModel;
    protected final GradingSlabs gradingSlabs;
    private final SectionMetadata metadata;
    private final TimetableModel timetableModel;

    public Section(String section_id, String section_name, String instructor_id,
                   String semester, int capacity, int contains)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(section_id, section_name);
        this.metadata = new SectionMetadata(instructor_id, semester, capacity, contains);
        this.gradingModel = new GradingPolicyModel(15, 10, 25, 25, 15, 10, 5);
        this.gradingSlabs = new GradingSlabs(100, 90, 80, 70, 60, 50, 40, 30, 0);
        this.timetableModel = new TimetableModel(section_id);
    }

    public Section(String section_id)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException {
        super(section_id, "TempLoad");
        metadata     = new SectionMetadata();
        gradingModel = new GradingPolicyModel();
        gradingSlabs = new GradingSlabs();
        timetableModel = new TimetableModel(section_id);

        metadata.ReadFromDatabase();
        gradingModel.ReadFromDatabase();
        gradingSlabs.ReadFromDatabase();
    }

    public List<TimeSlot> getTimetable() {
        return timetableModel.slots;
    }

    public void updateTimetable(List<TimeSlot> newSlots, UserEntity.Permission perm) throws SQLException {
        if (perm != Domain.Abstracts.UserEntity.Permission.PERMISSION_ADMIN) {
            throw new SecurityException("ACCESS DENIED: Only Administrators can modify Section Timetables.");
        }
        this.timetableModel.slots = newSlots;
        this.timetableModel.WriteToDatabase();
    }

    public static class TimeSlot {
        public String day;
        public String startTime;
        public int durationMins;
        public String room;

        public TimeSlot(String day, String startTime, int durationMins, String room) {
            this.day = day; this.startTime = startTime;
            this.durationMins = durationMins; this.room = room;
        }

        @Override
        public String toString() {
            return String.format("%-10s @ %s (%d mins) in %s", day, startTime, durationMins, room);
        }
    }

    public static class TimetableModel implements IDatabaseModel {
        public List<TimeSlot> slots = new ArrayList<>();
        private final String sectionId;

        private static final String database = "jdbc:sqlite:timetable.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS timetable (" +
                                                    "section_id TEXT, day TEXT, " +
                                                    "start_time TEXT, duration INTEGER, room TEXT, " +
                                                    "PRIMARY KEY(section_id, day, start_time)" +
                                                ")";
        private static final String insertSql = "INSERT INTO timetable(section_id, day, start_time, duration, room) " +
                                                "VALUES(?, ?, ?, ?, ?) " +
                                                "ON CONFLICT(section_id, day, start_time) DO UPDATE SET " +
                                                "duration=excluded.duration, room=excluded.room";
        private static final String deleteSql = "DELETE FROM timetable WHERE section_id = ?";
        private static final String selectSql = "SELECT day, start_time, duration, room FROM timetable WHERE section_id = ?";

        public TimetableModel(String sectionId) throws SQLException {
            this.sectionId = sectionId;
            CreateTable();
            ReadFromDatabase();
        }

        @Override public void CreateTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(tableSql))
            {s.executeUpdate();}
        }

        @Override public void WriteToDatabase() throws SQLException {
            CreateTable();
            try(Connection conn = sqliteConnector.connect(database)) {
                try(PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setString(1, sectionId);
                    del.executeUpdate();
                }
                if (!slots.isEmpty()) {
                    try(PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        for(TimeSlot slot : slots) {
                            ins.setString(1, sectionId); ins.setString(2, slot.day);
                            ins.setString(3, slot.startTime); ins.setInt(4, slot.durationMins);
                            ins.setString(5, slot.room); ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
            }
        }

        @Override public void ReadFromDatabase() throws SQLException {
            slots.clear();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql)){
                s.setString(1, sectionId);
                ResultSet rs = s.executeQuery();
                while(rs.next()) {
                    slots.add(new TimeSlot(
                            rs.getString("day"), rs.getString("start_time"),
                            rs.getInt("duration"), rs.getString("room")
                    ));
                }
            }
        }

        @Override public void DeleteFromTable() throws SQLException {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql)){
                s.setString(1, sectionId); s.executeUpdate();
            }
        }
    }

    public class StudentGradeProxy implements IDatabaseModel {
        private final String studentId;
        private final String sectionId;
        private final UserEntity.Permission perm;

        private float labs, quiz, mid_exams, end_exams, assignments, projects, bonus;
        private static final String database = "jdbc:sqlite:academic_records.db";

        public StudentGradeProxy(String studentId, String sectionId, UserEntity.Permission perm)
                throws SQLException {
            this.studentId = studentId;
            this.sectionId = sectionId;
            this.perm = perm;
            ReadFromDatabase();
        }

        private void checkWritePermission() {
            if (perm != Domain.Abstracts.UserEntity.Permission.PERMISSION_INSTRUCTOR &&
                    perm != Domain.Abstracts.UserEntity.Permission.PERMISSION_ADMIN &&
                    perm != Domain.Abstracts.UserEntity.Permission.PERMISSION_STUDENT_INSTRUCTOR) {
                throw new SecurityException("ACCESS DENIED: User does not have permission to modify grades.");
            }
        }

        public float getLabs()          { return labs; }
        public float getQuiz()          { return quiz; }
        public float getMidExams()      { return mid_exams; }
        public float getEndExams()      { return end_exams; }
        public float getAssignments()   { return assignments; }
        public float getProjects()      { return projects; }
        public float getBonus()         { return bonus; }

        public void setLabs(float v)        { checkWritePermission(); this.labs = v; }
        public void setQuiz(float v)        { checkWritePermission(); this.quiz = v; }
        public void setMidExams(float v)    { checkWritePermission(); this.mid_exams = v; }
        public void setEndExams(float v)    { checkWritePermission(); this.end_exams = v; }
        public void setAssignments(float v) { checkWritePermission(); this.assignments = v; }
        public void setProjects(float v)    { checkWritePermission(); this.projects = v; }
        public void setBonus(float v)       { checkWritePermission(); this.bonus = v; }

        @Override
        public void CreateTable() throws SQLException {
            String sql = "CREATE TABLE IF NOT EXISTS records (" +
                            "student_id TEXT, " +
                            "section_id TEXT, " +
                            "labs FLOAT, " +
                            "quiz FLOAT, " +
                            "mid FLOAT, " +
                            "end FLOAT, " +
                            "assign FLOAT, " +
                            "proj FLOAT, " +
                            "bonus FLOAT, " +
                            "PRIMARY KEY(student_id, section_id)" +
                        ")";
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        }

        @Override
        public void WriteToDatabase() throws SQLException {
            checkWritePermission();

            CreateTable();
            String sql = "INSERT INTO records(student_id, section_id, labs, quiz, mid, end, assign, proj, bonus) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT(student_id, section_id) DO UPDATE SET " +
                            "labs=excluded.labs, quiz=excluded.quiz, mid=excluded.mid, end=excluded.end, " +
                            "assign=excluded.assign, proj=excluded.proj, bonus=excluded.bonus";

            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                stmt.setString(2, sectionId);
                stmt.setFloat(3, labs);
                stmt.setFloat(4, quiz);
                stmt.setFloat(5, mid_exams);
                stmt.setFloat(6, end_exams);
                stmt.setFloat(7, assignments);
                stmt.setFloat(8, projects);
                stmt.setFloat(9, bonus);
                stmt.executeUpdate();
            }
        }

        @Override
        public void ReadFromDatabase() throws SQLException {
            CreateTable();
            String sql = "SELECT * FROM records WHERE student_id = ? AND section_id = ?";
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                stmt.setString(2, sectionId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    labs        = rs.getFloat("labs");
                    quiz        = rs.getFloat("quiz");
                    mid_exams   = rs.getFloat("mid");
                    end_exams   = rs.getFloat("end");
                    assignments = rs.getFloat("assign");
                    projects    = rs.getFloat("proj");
                    bonus       = rs.getFloat("bonus");
                }
            }
        }

        @Override
        public void DeleteFromTable() throws SQLException {
            checkWritePermission();

            String sql = "DELETE FROM records WHERE student_id = ? AND section_id = ?";
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                stmt.setString(2, sectionId);
                stmt.executeUpdate();
            }
        }
    }

    public StudentGradeProxy getStudentGradeRecord(String studentId, UserEntity.Permission requestorPermission)
            throws SQLException {
        return new StudentGradeProxy(studentId, getId(), requestorPermission);
    }

    public void setStudentGradeRecord(StudentGradeProxy studentGradeRecord)
            throws SQLException, SecurityException{
        studentGradeRecord.WriteToDatabase();
    }

    @Override
    public void onPresistenceSave() throws SQLException {
        metadata.WriteToDatabase();
        gradingModel.WriteToDatabase();
        gradingSlabs.WriteToDatabase();
        timetableModel.WriteToDatabase();
    }
    @Override
    public void onPresistenceDelete() throws SQLException {
        metadata.DeleteFromTable();
        gradingModel.DeleteFromTable();
        gradingSlabs.DeleteFromTable();
        timetableModel.DeleteFromTable();
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

        public GradingPolicyModel() {}

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
            this.O =o ; this.A =a ; this.A_=a_;
            this.B =b ; this.B_=b_; this.C =c ;
            this.C_=c_; this.D =d ; this.F =f ;
        }

        public GradingSlabs() throws SQLException { ReadFromDatabase(); }

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
                stmt.setFloat(2, O ); stmt.setFloat(3, A );
                stmt.setFloat(4, A_); stmt.setFloat(5, B );
                stmt.setFloat(6, B_); stmt.setFloat(7, C );
                stmt.setFloat(8, C_); stmt.setFloat(9, D );
                stmt.setFloat(10,F );
                stmt.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException {
            CreateTable();
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    O  = rs.getFloat("O" ); A  = rs.getFloat("A" );
                    A_ = rs.getFloat("A_"); B  = rs.getFloat("B" );
                    B_ = rs.getFloat("B_"); C  = rs.getFloat("C" );
                    C_ = rs.getFloat("C_"); D  = rs.getFloat("D" );
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
        public int capacity;
        public int contains;

        private static final String database = "jdbc:sqlite:sections.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS sections(" +
                                                    "id TEXT PRIMARY KEY, " +
                                                    "name TEXT NOT NULL, " +
                                                    "instructor_id TEXT, " +
                                                    "semester TEXT NOT NULL," +
                                                    "capacity INTEGER NOT NULL," +
                                                    "contains INTEGER NOT NULL" +
                                                ")";
        private static final String insertSql = "INSERT INTO sections(id, name, instructor_id, semester, capacity, contains) " +
                                                "VALUES(?, ?, ?, ?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "name=excluded.name,"+
                                                "instructor_id=excluded.instructor_id, "+
                                                "semester=excluded.semester, " +
                                                "capacity=excluded.capacity, " +
                                                "contains=excluded.contains ";
        private static final String selectSql = "SELECT name, instructor_id, semester, capacity, contains FROM sections WHERE id = ?";
        private static final String deleteSql = "DELETE FROM sections WHERE id = ?";

        public SectionMetadata(String instructor_id, String semester, int capacity, int contains)
        { this.instructor_id = instructor_id; this.semester = semester; this.capacity = capacity; this.contains = contains; }
        public SectionMetadata() throws SQLException { ReadFromDatabase(); }

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
                stmt.setString(2, getName());
                stmt.setString(3, instructor_id);
                stmt.setString(4, semester);
                stmt.setInt(5, capacity);
                stmt.setInt(6, contains);
                stmt.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase() throws SQLException {
            try (Connection conn = sqliteConnector.connect(database);
                 PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()){
                    setName(rs.getString("name"));
                    this.instructor_id = rs.getString("instructor_id");
                    this.semester = rs.getString("semester");
                    this.capacity = rs.getInt("capacity");
                    this.contains = rs.getInt("contains");
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
    public int getCapacity() { return metadata.capacity; }
    public int getContains() { return metadata.contains; }

    public void setInstructorId(String instructor_id) { metadata.instructor_id = instructor_id; }
    public void setSemester(String semester) { metadata.semester = semester; }
    public void setCapacity(int capacity) { metadata.capacity = capacity; }
    public void setContains(int contains) { metadata.contains = contains; }
}