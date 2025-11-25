import Application.Views.LoginView;
import Domain.Concretes.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            LoginView login = new LoginView();
            login.setVisible(true);
        });
    }
}