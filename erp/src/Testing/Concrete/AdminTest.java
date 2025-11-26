package Testing.Concrete;

import Domain.Concretes.Admin;
import Domain.Concretes.Section;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminTest {

    @BeforeEach
    void setUp() {
        TestDatabaseUtils.clearAllTables();
    }

    @AfterEach
    void tearDown() {
        TestDatabaseUtils.clearAllTables();
    }

    @Test
    @Order(1)
    void testCreateDefaultAdmin() throws Exception {
        Admin masterAdmin = new Admin("ADMIN", "System Administrator", "admin@erp.edu", "0000000000", "admin");
        masterAdmin.onPresistenceSave();

        Admin loadedAdmin = new Admin("ADMIN");
        assertEquals("System Administrator", loadedAdmin.getName());
        assertEquals("ADMIN", loadedAdmin.getId());
    }

    @Test
    @Order(2)
    void testAdminTimetableOverride() throws Exception {
        Admin admin = new Admin("ADM01", "AdminUser");
        admin.onPresistenceSave();

        Section section = new Section("SEC_TEST_01", "Physics A", "PHY101", "INS01", "Fall2024", 30, 0);
        section.onPresistenceSave();

        List<Section.TimeSlot> newSlots = new ArrayList<>();
        newSlots.add(new Section.TimeSlot("Monday", "10:00", 60, "Room 101"));

        admin.setSectionTimetable("SEC_TEST_01", newSlots);

        Section loadedSection = new Section("SEC_TEST_01");
        List<Section.TimeSlot> slots = loadedSection.getTimetable();

        assertEquals(1, slots.size());
        assertEquals("Monday", slots.get(0).day);
        assertEquals("Room 101", slots.get(0).room);
    }

    @Test
    @Order(3)
    void testAdminDeletion() throws Exception {
        Admin admin = new Admin("DEL_TEST", "To Delete");
        admin.onPresistenceSave();

        admin.onPresistenceDelete();

        Admin deletedAdmin = new Admin("DEL_TEST");
        assertEquals("TempName", deletedAdmin.getName());
    }
}