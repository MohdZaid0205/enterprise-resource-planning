package Testing.Concrete;

import Domain.Concretes.*;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AcademicFlowTest {

    private final String SEMESTER = "Fall2024";
    private final String COURSE_ID = "CS50";
    private final String SECTION_ID = "CS50-A";
    private final String INSTRUCTOR_ID = "INS_DAVID";
    private final String STUDENT_ID = "STU_JOHN";

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseUtils.clearAllTables();

        new Admin("ADMIN", "System Admin").onPresistenceSave();
        new Course(COURSE_ID, "Intro to CS", 4, 100).onPresistenceSave();

        Instructor instructor = new Instructor(INSTRUCTOR_ID, "David J", "david@cs.edu", "123", "pass");
        instructor.onPresistenceSave();

        Section section = new Section(SECTION_ID, "CS50 Section A", COURSE_ID, INSTRUCTOR_ID, SEMESTER, 2, 0);
        section.onPresistenceSave();

        instructor.assignToSection(SECTION_ID);
        instructor.onPresistenceSave();
    }

    @AfterEach
    void tearDown() {
        TestDatabaseUtils.clearAllTables();
    }

    @Test
    void testFullAcademicFlow() throws Exception {
        Student student = new Student(STUDENT_ID, "John Doe", "2024-01-01");
        student.onPresistenceSave();

        student.enrollInCourse(SECTION_ID);
        Section secCheck = new Section(SECTION_ID);
        assertEquals(1, secCheck.getContains());

        Instructor instructor = new Instructor(INSTRUCTOR_ID);
        instructor.enterMarks(SECTION_ID, STUDENT_ID, 10, 5, 20, 20, 10, 5, 0);

        Student loadedStudent = new Student(STUDENT_ID);
        List<Section.StudentGradeProxy> report = loadedStudent.getSemesterRecord(SEMESTER);

        assertNotNull(report);
        assertEquals(1, report.size());
        assertEquals(10, report.get(0).getLabs());

        Instructor.CourseStatsModel stats = instructor.getSectionStats(SECTION_ID);
        assertEquals(70.0f, stats.getAverage(), 0.01);
    }
}