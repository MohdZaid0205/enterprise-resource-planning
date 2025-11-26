package Domain.Abstracts;

import Domain.Database.sqliteConnector;
import Domain.Exceptions.InvalidEntityIdentityException;
import Domain.Exceptions.InvalidEntityNameException;
import Domain.Interfaces.IDatabaseModel;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class UserEntity extends EntityABC {

    // no permission given to any USER-ENTITY needs overwriting in deriving subclasses.
    public Permission permission = Permission.PERMISSION_NONE;

    public UserEntity(String entity_id, String entity_name)
        throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    { super(entity_id, entity_name); contactInfo = new ContactInformationModel(); security= new SecurityModel(); }

    public UserEntity(String entity_id, String entity_name, String email, String phone_number, String password)
            throws InvalidEntityIdentityException, InvalidEntityNameException, SQLException
    {
        super(entity_id, entity_name);
        contactInfo = new ContactInformationModel(email, phone_number);
        security = new SecurityModel(password);
    }

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

        private static final String tableSql = "CREATE TABLE IF NOT EXISTS contact("+
                                                    "id TEXT PRIMARY KEY NOT NULL, "+
                                                    "email TEXT, "+
                                                    "phone TEXT"+
                                                ")";
        private static final String insertSql = "INSERT INTO contact(id, email, phone) VALUES(?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "email = excluded.email, " +
                                                "phone = excluded.phone";
        private static final String deleteSql = "DELETE FROM contact WHERE id IN"+
                                                "(SELECT id FROM contact WHERE id=?)";
        private static final String selectSql = "SELECT email, phone FROM contact WHERE id = ?";

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
            CreateTable();
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
            CreateTable();
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

    protected final SecurityModel security;
    protected class SecurityModel implements IDatabaseModel {
        public String password = hash("password");
        public String permissionLevel = Permission.PERMISSION_NONE.name();

        private static final String database = "jdbc:sqlite:credentials.db";
        private static final String tableSql = "CREATE TABLE IF NOT EXISTS credentials(" +
                                                    "id TEXT PRIMARY KEY, " +
                                                    "password TEXT, " +
                                                    "permission_level TEXT" +
                                                ")";
        private static final String insertSql = "INSERT INTO credentials(id, password, permission_level) VALUES(?, ?, ?) " +
                                                "ON CONFLICT(id) DO UPDATE SET " +
                                                "password=excluded.password, " +
                                                "permission_level=excluded.permission_level";
        private static final String selectSql = "SELECT password, permission_level FROM credentials WHERE id = ?";
        private static final String deleteSql = "DELETE FROM credentials WHERE id = ?";

        public SecurityModel(String pass) throws SQLException { password = hash(pass); }
        public SecurityModel() throws SQLException { ReadFromDatabase(); }

        public boolean checkCredentials(String inputPass, String expectedPermission) {
            String inputHash = hash(inputPass);
            boolean passMatch = password != null && password.equals(inputHash);
            boolean permMatch = permissionLevel != null && permissionLevel.equals(expectedPermission);

            return passMatch && permMatch;
        }

        public void updatePassword(String rawPassword) throws SQLException {
            this.password = hash(rawPassword);
            WriteToDatabase();
        }

        private String hash(String raw) {
            if (raw == null) return null;
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
                for (int i = 0; i < encodedhash.length; i++) {
                    String hex = Integer.toHexString(0xff & encodedhash[i]);
                    if(hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override public void CreateTable()
                throws SQLException
        {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(tableSql))
            {s.executeUpdate();}
        }
        @Override public void WriteToDatabase()
                throws SQLException
        {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(insertSql))
            {
                s.setString(1,getId());
                s.setString(2,password);
                this.permissionLevel = UserEntity.this.permission.name();
                s.setString(3,this.permissionLevel);
                s.executeUpdate();
            }
        }
        @Override public void ReadFromDatabase()
                throws SQLException
        {
            CreateTable();
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(selectSql))
            {
                s.setString(1,getId());
                ResultSet rs=s.executeQuery();
                if(rs.next()){
                    password=rs.getString("password");
                    permissionLevel = rs.getString("permission_level");
                }
            }
        }
        @Override public void DeleteFromTable()
                throws SQLException
        {
            try(Connection c=sqliteConnector.connect(database);
                PreparedStatement s=c.prepareStatement(deleteSql))
            {
                s.setString(1,getId());
                s.executeUpdate();
            }
        }
    }

    public boolean authenticate(String inputPassword) {
        return security.checkCredentials(inputPassword, this.permission.name());
    }

    public void setPassword(String newPassword) throws SQLException {
        security.updatePassword(newPassword);
    }

    public boolean resetPassword(String email, String phone, String newPassword) throws SQLException {
        if (contactInfo.email.equalsIgnoreCase(email) && contactInfo.phone.equals(phone)) {
            setPassword(newPassword);
            return true;
        }
        return false;
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
