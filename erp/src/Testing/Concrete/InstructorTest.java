package Testing.Concrete;

import Domain.Concretes.Instructor;
import Domain.Concretes.Section;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class InstructorTest {

    @BeforeEach
    void setUp() { TestDatabaseUtils.clearAllTables(); }

    @AfterEach
    void tearDown() { TestDatabaseUtils.clearAllTables(); }

    @Test
    void testInstructorLifecycle() throws Exception {
        Instructor instructor = new Instructor("INS001", "Dr. Smith", "smith@univ.edu", "555-0199", "pass123");
        instructor.onPresistenceSave();

        Instructor loaded = new Instructor("INS001");
        assertEquals("Dr. Smith", loaded.getName());

        loaded.onPresistenceDelete();
        Instructor deleted = new Instructor("INS001");
        assertEquals("TempLoad", deleted.getName());
    }

    @Test
    void testTeachingAssignment() throws Exception {
        Instructor ins = new Instructor("INS_A");
        ins.assignToSection("SEC_A");
        ins.onPresistenceSave();

        Instructor loaded = new Instructor("INS_A");
        assertDoesNotThrow(() -> loaded.enterMarks("SEC_A", "STU_1", 0,0,0,0,0,0,0));
    }

    @Test
    void testInstructorStats() throws Exception {
        new Section("SEC_STATS", "Stats", "C1", "INS_S", "Sem1", 10, 0).onPresistenceSave();
        Instructor ins = new Instructor("INS_S");
        ins.assignToSection("SEC_STATS");
        ins.onPresistenceSave();

        ins.enterMarks("SEC_STATS", "S1", 10,10,10,10,10,10,10); // 70
        ins.enterMarks("SEC_STATS", "S2", 20,10,10,10,10,10,10); // 80

        Instructor.CourseStatsModel stats = ins.getSectionStats("SEC_STATS");
        assertEquals(75.0f, stats.getAverage());
        assertEquals(80.0f, stats.getHighest());
        assertEquals(70.0f, stats.getLowest());
    }
}