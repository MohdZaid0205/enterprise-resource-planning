package Application.Views;

import Application.Components.*;
import Domain.Abstracts.*;
import Domain.Concretes.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginView extends JFrame {

    private StyledComboBox<String> userTypeCombo;
    private StyledField idField;
    private StyledPasswordField passField;
    private StyledButton loginButton;
    private JLabel messageLabel;

    // Security State
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_TIME_MS = 30000; // 30 Seconds
    private Timer lockoutTimer;

    public LoginView() {
        setTitle("University Portal - Login");
        setSize(400, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(StyleConstants.TERTIARY_COLOR);
        setLayout(new GridBagLayout()); // GridBag centers the card component

        add(createLoginCard());
    }

    private JPanel createLoginCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleConstants.WHITE);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        panel.setPreferredSize(new Dimension(350, 450));

//        JLabel title = new JLabel("ERP");
//        title.setFont(new Font("Segoe UI", Font.BOLD, 35));
//        title.setForeground(StyleConstants.TERTIARY_COLOR);
//        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Sign in to your account");
        heading.setFont(StyleConstants.HEADER_FONT);
        heading.setForeground(StyleConstants.TERTIARY_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] types = {"Student", "Instructor", "Admin"};
        userTypeCombo = new StyledComboBox<>(types);
        userTypeCombo.setMaximumSize(new Dimension(300, 45));

        idField = new StyledField("User ID");
        idField.setMaximumSize(new Dimension(300, 45));
        idField.setPreferredSize(new Dimension(300, 45));
        idField.setToolTipText("Enter an ID");

        passField = new StyledPasswordField("Password");
        passField.setMaximumSize(new Dimension(300, 45));
        passField.setPreferredSize(new Dimension(300, 45));
        passField.setToolTipText("Enter your password");

        loginButton = new StyledButton("Login", StyleConstants.PRIMARY_COLOR);
        loginButton.setMaximumSize(new Dimension(300, 45));
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(this::handleLogin);
        loginButton.setEnabled(false);

        DocumentListener validationListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateInputs(); }
            @Override public void removeUpdate(DocumentEvent e) { validateInputs(); }
            @Override public void changedUpdate(DocumentEvent e) { validateInputs(); }
        };
        idField.getDocument().addDocumentListener(validationListener);
        passField.getDocument().addDocumentListener(validationListener);

        messageLabel = new JLabel("Enter Your Credentials");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        messageLabel.setForeground(StyleConstants.GRAY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

//        panel.add(title);
//        panel.add(Box.createVerticalStrut(10));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(30));
        panel.add(userTypeCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(idField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginButton);

        return panel;
    }

    private void validateInputs() {
        if (lockoutTimer != null && lockoutTimer.isRunning()) {
            return;
        }
        String id = idField.getText().trim();
        String pass = new String(passField.getPassword());
        loginButton.setEnabled(!id.isEmpty() && !pass.isEmpty());
    }

    private void handleLogin(ActionEvent e) {
        String type = (String) userTypeCombo.getSelectedItem();
        String id = idField.getText().trim();
        String pass = new String(passField.getPassword());

        loginButton.setEnabled(false);
        loginButton.setText("Verifying...");

        SwingWorker<UserEntity, Void> worker = new SwingWorker<>() {
            @Override
            protected UserEntity doInBackground() {
                try {
                    UserEntity user = null;
                    switch (type) {
                        case "Student": user = new Student(id); break;
                        case "Instructor": user = new Instructor(id); break;
                        case "Admin": user = new Admin(id); break;
                    }

                    if (user != null && user.getName() != null &&
                            !user.getName().equals("TempName") && !user.getName().equals("TempLoad")) {

                        if (user.authenticate(pass)) {
                            return user;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    UserEntity user = get();
                    if (user != null) {
                        dispose();
                        System.out.println("User has logged in: " + user.getName() + " (" + type + ")");
                    } else {
                        handleFailedAttempt();
                    }
                } catch (Exception ex) {
                    showMessage("System Error", true);
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        worker.execute();
    }

    private void handleFailedAttempt() {
        failedAttempts++;
        int remaining = MAX_ATTEMPTS - failedAttempts;

        if (remaining <= 0) {
            initiateLockout();
        } else {
            showMessage("Invalid Credentials. Attempts left: " + remaining, true);
            loginButton.setText("Login");
            validateInputs();
        }
    }

    private void initiateLockout() {
        showMessage("Timeout (Take A Break)", true);
        toggleInputs(false);

        lockoutTimer = new Timer(LOCKOUT_TIME_MS, e -> {
            unlockUI();
            lockoutTimer.stop();
        });
        lockoutTimer.setRepeats(false);
        lockoutTimer.start();

        final int[] secondsLeft = {LOCKOUT_TIME_MS / 1000};
        loginButton.setText("Locked (" + secondsLeft[0] + "s)");

        Timer visualTimer = new Timer(1000, null);
        visualTimer.addActionListener(e -> {
            if (loginButton.isEnabled()) {
                visualTimer.stop();
            } else {
                secondsLeft[0]--;
                if (secondsLeft[0] > 0) {
                    loginButton.setText("Locked (" + secondsLeft[0] + "s)");
                } else {
                    visualTimer.stop();
                }
            }
        });
        visualTimer.start();
    }

    private void unlockUI() {
        failedAttempts = 0;
        toggleInputs(true);
        loginButton.setText("Login");
        showMessage("Login Unlocked", false);
        validateInputs();
    }

    private void toggleInputs(boolean enabled) {
        idField.setEnabled(enabled);
        passField.setEnabled(enabled);
        userTypeCombo.setEnabled(enabled);
        loginButton.setEnabled(enabled);
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setForeground(isError ? StyleConstants.RED : StyleConstants.GREEN);
        messageLabel.setText(msg);
    }
}