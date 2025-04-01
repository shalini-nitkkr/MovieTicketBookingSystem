 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class RegisterForm extends JFrame {
    private JTextField nameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton signUpButton, cancelButton;
    private JCheckBox termsCheckBox;
    private JLabel strengthLabel;
    private JProgressBar strengthBar;

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/MovieBooking";
    private static final String DB_USER = "movie_user";
    private static final String DB_PASSWORD = "moviepass123";

    public RegisterForm() {
        setTitle("Create Your Movie Booking Account");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(50, 50, 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        JLabel titleLabel = new JLabel("SIGN UP", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Form Panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        formPanel.setBackground(new Color(240, 240, 240));

        // Name Field
        addFormField(formPanel, "Full Name:", nameField = new JTextField());
        nameField.setToolTipText("Enter your full name as per ID proof");

        // Email Field
        addFormField(formPanel, "Email Address:", emailField = new JTextField());
        emailField.setToolTipText("We'll send confirmation to this email");

        // Phone Field
        addFormField(formPanel, "Phone Number:", phoneField = new JTextField());
        phoneField.setToolTipText("For booking notifications");

        // Password Field
        addFormField(formPanel, "Password:", passwordField = new JPasswordField());
        passwordField.getDocument().addDocumentListener(new PasswordStrengthListener());

        // Password Strength Indicator
        formPanel.add(new JLabel("Password Strength:"));
        JPanel strengthPanel = new JPanel(new BorderLayout());
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(false);
        strengthBar.setForeground(Color.RED);
        strengthLabel = new JLabel("Weak", JLabel.RIGHT);
        strengthLabel.setForeground(Color.RED);
        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        strengthPanel.add(strengthLabel, BorderLayout.EAST);
        formPanel.add(strengthPanel);

        // Confirm Password Field
        addFormField(formPanel, "Confirm Password:", confirmPasswordField = new JPasswordField());

        // Terms and Conditions
        formPanel.add(new JLabel(""));
        termsCheckBox = new JCheckBox("I agree to Terms & Conditions and Privacy Policy");
        termsCheckBox.setBackground(new Color(240, 240, 240));
        formPanel.add(termsCheckBox);

        // Buttons Panel
        formPanel.add(new JLabel(""));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        signUpButton = new JButton("SIGN UP");
        styleButton(signUpButton, new Color(76, 175, 80)); // Green
        cancelButton = new JButton("CANCEL");
        styleButton(cancelButton, new Color(244, 67, 54)); // Red
        buttonPanel.add(signUpButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        JLabel loginLabel = new JLabel("Already have an account? Login here");
        loginLabel.setForeground(Color.BLUE.darker());
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });
        footerPanel.add(loginLabel);
        add(footerPanel, BorderLayout.SOUTH);

        // Button Actions
        signUpButton.addActionListener(e -> registerUser());
        cancelButton.addActionListener(e -> {
            dispose();
            new WelcomeForm().setVisible(true);
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addFormField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label);
        panel.add(field);
    }

    private void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("All fields are required!");
            return;
        }

        if (!termsCheckBox.isSelected()) {
            showError("You must agree to Terms & Conditions!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address!");
            return;
        }

        if (!isValidPhone(phone)) {
            showError("Please enter a valid 10-digit phone number!");
            return;
        }

        if (!isStrongPassword(password)) {
            showError("Password must be at least 8 characters with uppercase, lowercase, number and special character!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if email already exists
            if (userExists(conn, email)) {
                showError("Email already registered!");
                return;
            }

            // Check if phone already exists
            if (phoneExists(conn, phone)) {
                showError("Phone number already registered!");
                return;
            }

            // Hash the password
            String hashedPassword = hashPassword(password);

            // Insert new user with verification token
            String verificationToken = generateVerificationToken();
            String query = "INSERT INTO users (username, full_name, email, phone, password) " +
                          "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, email); // Using email as username
                pst.setString(2, name);
                pst.setString(3, email);
                pst.setString(4, phone);
                pst.setString(5, hashedPassword);
               // pst.setString(6, verificationToken);
                //pst.setBoolean(7, false); // Not verified yet
                
                pst.executeUpdate();
                
                // In a real app, you would send verification email here
                // sendVerificationEmail(email, verificationToken);
                
                showSuccess("Registration Successful! Please check your email to verify your account.");
                dispose();
                new LoginForm().setVisible(true);
            }
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Helper Methods
    private boolean userExists(Connection conn, String email) throws SQLException {
        String query = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean phoneExists(Connection conn, String phone) throws SQLException {
        String query = "SELECT id FROM users WHERE phone = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, phone);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[32];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return Pattern.compile("^\\d{10}$").matcher(phone).matches();
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 && 
               Pattern.compile("[A-Z]").matcher(password).find() && // At least one uppercase
               Pattern.compile("[a-z]").matcher(password).find() && // At least one lowercase
               Pattern.compile("[0-9]").matcher(password).find() && // At least one digit
               Pattern.compile("[^A-Za-z0-9]").matcher(password).find(); // At least one special char
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Password Strength Listener
    private class PasswordStrengthListener implements javax.swing.event.DocumentListener {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }

        private void update() {
            String password = new String(passwordField.getPassword());
            int strength = calculatePasswordStrength(password);
            
            strengthBar.setValue(strength);
            if (strength < 30) {
                strengthBar.setForeground(Color.RED);
                strengthLabel.setText("Weak");
                strengthLabel.setForeground(Color.RED);
            } else if (strength < 70) {
                strengthBar.setForeground(Color.ORANGE);
                strengthLabel.setText("Medium");
                strengthLabel.setForeground(Color.ORANGE);
            } else {
                strengthBar.setForeground(Color.GREEN);
                strengthLabel.setText("Strong");
                strengthLabel.setForeground(Color.GREEN);
            }
        }

        private int calculatePasswordStrength(String password) {
            int strength = 0;
            
            // Length contributes up to 40 points
            strength += Math.min(40, password.length() * 4);
            
            // Character diversity
            if (Pattern.compile("[A-Z]").matcher(password).find()) strength += 10;
            if (Pattern.compile("[a-z]").matcher(password).find()) strength += 10;
            if (Pattern.compile("[0-9]").matcher(password).find()) strength += 10;
            if (Pattern.compile("[^A-Za-z0-9]").matcher(password).find()) strength += 10;
            
            // Deduct points for common patterns
            if (password.matches(".*(123|abc|qwerty).*")) strength -= 20;
            
            return Math.min(100, Math.max(0, strength));
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            SwingUtilities.invokeLater(() -> {
                new RegisterForm().setVisible(true);
            });
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "MySQL JDBC Driver not found!",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}