package Abstracts;

import Database.sqliteConnector;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;
import Interfaces.IDatabaseModel;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class UserEntity extends EntityABC {

    // no permission given to any USER-ENTITY needs overwriting in deriving subclasses.
    public Permission permission = Permission.PERMISSION_NONE;

    public UserEntity(String entity_id, String entity_name)
        throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { super(entity_id, entity_name); contactInfo = new ContactInformationModel();}

    public enum Permission {
        PERMISSION_NONE,                // by default any user has no permission, deriving.
        PERMISSION_ADMIN,               // for super-user called ADMIN. unrestricted access
        PERMISSION_INSTRUCTOR,          // for instructor/manager of courses or [sections.]
        PERMISSION_STUDENT,             // for accessor/choose of resources and data viewer
        PERMISSION_STUDENT_INSTRUCTOR   // for accessor selected as an instructor [FELLOW.]
    }

    protected final ContactInformationModel contactInfo;
    public class ContactInformationModel implements IDatabaseModel {
        public String email;
        public String phone;

        private static final String database = "jdbc:sqlite:contacts.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS contacts("+
                                                    "id TEXT PRIMARY KEY NOT NULL,"+
                                                    "email TEXT NOT NULL"+
                                                    "phone TEXT NOT NULL"+
                                                ")";
        private static final String insertSql = "INSERT INTO contacts(id, email, phone) VALUES(?, ?, ?)" +
                                                "ON CONFLICT DO UPDATE SET email = excluded.name" +
                                                "phone = excluded.phone";
        private static final String deleteSql = "DELETE FROM contacts WHERE id IN"+
                                                "(SELECT id FROM contacts WHERE id=?)";
        private static final String selectSql = "SELECT email, phone FROM contacts WHERE id = ?";

        public ContactInformationModel(@NotNull String email, @NotNull String phone)
        { this.email = email; this.phone = phone; }

        public ContactInformationModel() throws SQLException
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
                stmt.setString(1,getId());
                stmt.setString(2,email);
                stmt.setString(3,phone);

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
                    email = rs.getString("email");
                    phone = rs.getString("phone");
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

    public String getEmail() { return contactInfo.email; }
    public String getPhone() { return contactInfo.phone; }

    public void setEmail(@NotNull String email) { this.contactInfo.email = email; }
    public void setPhone(@NotNull String phone) { this.contactInfo.phone = phone; }

    // Forces concrete classes (like Section) to handle their own DB logic
    public abstract void onPresistenceSave() throws SQLException;
    public abstract void onPresistenceDelete() throws SQLException;

    // TODO: implementation of a private class that holds all data [DATACLASS] for given
    // TODO: [USER] and expose all functions for getting defined attributes of this class
    // TODO: keep [RESOURCE] entity and [USER] entity separate from each other for import
    // TODO: created inner class for local [RESOURCE] Entity Management.

    // TODO: keep [RESOURCE] entity as an inner class for all user entity and their business
    // TODO: create concrete class for each [USER] entity level which create abstract method
    // TODO: to interact with different functionality. move this class to ABSTRACTS
}
