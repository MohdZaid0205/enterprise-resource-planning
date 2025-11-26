package Testing.Concrete;

import Domain.Concretes.Course;
import Domain.Concretes.Section;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CourseAndSectionTest {

    @BeforeEach
    void setUp() { TestDatabaseUtils.clearAllTables(); }

    @AfterEach
    void tearDown() { TestDatabaseUtils.clearAllTables(); }

    @Test
    void testCourseLifecycle() throws Exception {
        Course c = new Course("CS101", "Intro to CS", 4, 100);
        c.onPresistenceSave();

        Course loaded = new Course("CS101");
        assertEquals("Intro to CS", loaded.getName());
        assertEquals(4, loaded.getCredits());

        c.setCredits(3);
        c.onPresistenceSave();

        Course updated = new Course("CS101");
        assertEquals(3, updated.getCredits());
    }

    @Test
    void testSectionAndGrading() throws Exception {
        new Course("MATH200", "Calc II", 3, 50).onPresistenceSave();
        Section s = new Section("SEC_MATH_A", "Calc II - A", "MATH200", "INS_MATH", "Spring2025", 50, 0);
        s.onPresistenceSave();

        assertEquals(15, s.getLabs());

        s.setLabs(20);
        s.setMidExams(20);
        s.onPresistenceSave();

        Section loaded = new Section("SEC_MATH_A");
        assertEquals(20, loaded.getLabs());
        assertEquals("Spring2025", loaded.getSemester());
    }

    @Test
    void testGradingSlabs() throws Exception {
        Section s = new Section("SLAB_TEST", "Slab Test", "C1", "I1", "S1", 10, 0);

        s.setA(85.5f);
        s.onPresistenceSave();

        Section loaded = new Section("SLAB_TEST");
        assertEquals(85.5f, loaded.getA());
        assertEquals(100f, loaded.getO());
    }
}