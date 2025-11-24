import Database.StudentDataModel;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
         try {
            StudentDataModel student = new StudentDataModel("2024535");
//            student.CreateTable();
//            student.WriteToDatabase();
//            System.out.println("added name into database!");
//            System.out.println("name:"+ student.getName());
//            student.setName("Sholk Gupta");
//            student.WriteToDatabase();
             student.DeleteFromTable();

        } catch (SQLException e) {
            System.out.println("cannot add name into database!");
            e.printStackTrace();
        }
    }
}
