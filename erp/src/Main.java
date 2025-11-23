import Database.StudentDataModel;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        StudentDataModel student = new StudentDataModel("2024535","Shlok Gupta");
         try {
            student.CreateTable();
            student.WriteToDatabase();
            System.out.println("added name into database!");

        } catch (SQLException e) {
            System.out.println("cannot add name into database!");
            e.printStackTrace();
        }
    }
}
