import Concretes.Student;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
         try {
            Student student = new Student("2024353");
//            student.CreateTable();
//            student.WriteToDatabase();
//            System.out.println("added name into database!");
//            System.out.println("name:"+ student.getName());
//            student.setName("Sholk Gupta");
//            student.WriteToDatabase();
             System.out.println(student.getName());

        } catch (SQLException e) {
            System.out.println("cannot add name into database!");
            e.printStackTrace();
        }
    }
}
