package Testing.Concrete;

import Domain.Concretes.Student;
import Domain.Concretes.Section;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.List;

public class StudentTest {

    @BeforeEach
    void setUp() {
        TestDatabaseUtils.clearAllTables();
    }

    @AfterEach
    void tearDown() {
        TestDatabaseUtils.clearAllTables();
    }

    @Test
    @DisplayName("Test Student Creation and Persistence")
    void testStudentLifecycle() throws Exception {
        Student s = new Student("STU01", "Alice Smith", "2024-09-01", "alice@erp.edu", "555-0101", "password123");
        s.onPresistenceSave();

        Student loaded = new Student("STU01");

        assertEquals("Alice Smith", loaded.getName());
        assertEquals("2024-09-01", loaded.getEnrollmentDate());
        assertEquals("alice@erp.edu", loaded.getEmail());
    }

    @Test
    @DisplayName("Test Enrollment Increases Section Count")
    void testStudentEnrollment() throws Exception {
        Section sec = new Section("SEC_BIO_101", "Biology I", "BIO100", "INS_B", "Fall2024", 20, 0);
        sec.onPresistenceSave();

        Student s = new Student("STU02", "Bob Jones", "2024-01-01");
        s.onPresistenceSave();
        s.enrollInCourse("SEC_BIO_101");

        Student loadedStudent = new Student("STU02");
        List<Section.StudentGradeProxy> records = loadedStudent.getSemesterRecord("Fall2024");
        assertEquals(1, records.size(), "Student should have 1 course record for Fall2024");

        Section loadedSection = new Section("SEC_BIO_101");
        assertEquals(1, loadedSection.getContains(), "Section student count should increment to 1");
    }

    @Test
    @DisplayName("Test Dropping a Course")
    void testStudentDrop() throws Exception {
        Section sec = new Section("SEC_CHEM_101", "Chemistry", "CHM1", "INS_C", "Fall2024", 20, 0);
        sec.onPresistenceSave();

        Student s = new Student("STU_DROP", "Dave Dropper", "2024-01-01");
        s.onPresistenceSave();

        s.enrollInCourse("SEC_CHEM_101");
        s.dropFromCourse("SEC_CHEM_101");

        Student loadedStudent = new Student("STU_DROP");
        assertTrue(loadedStudent.getSemesterRecord("Fall2024").isEmpty(), "Transcript should be empty after drop");

        Section loadedSec = new Section("SEC_CHEM_101");
        assertEquals(0, loadedSec.getContains(), "Section count should return to 0");
    }

    @Test
    @DisplayName("Test Enrollment Rejection at Full Capacity")
    void testEnrollmentFullCapacity() throws Exception {
        Section sec = new Section("SEC_FULL", "Small Class", "F1", "I1", "Spring2025", 1, 0);
        sec.onPresistenceSave();

        Student s1 = new Student("S1", "Student One", "2024");
        s1.onPresistenceSave();
        s1.enrollInCourse("SEC_FULL");

        Student s2 = new Student("S2", "Student Two", "2024");
        s2.onPresistenceSave();

        SQLException exception = assertThrows(SQLException.class, () -> {
            s2.enrollInCourse("SEC_FULL");
        });

        assertTrue(exception.getMessage().toLowerCase().contains("capacity"),
                "Exception should mention capacity limits");
    }
}