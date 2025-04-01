
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    public LoginForm() {
        setTitle("Login to Movie Booking");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(50, 50, 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        JLabel titleLabel = new JLabel("USER LOGIN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 20, 50));
        formPanel.setBackground(new Color(240, 240, 240));
        addFormField(formPanel, "Email Address:", emailField = new JTextField());
        addFormField(formPanel, "Password:", passwordField = new JPasswordField());
        JButton loginButton = new JButton("LOGIN");
        styleButton(loginButton, new Color(76, 175, 80));
        loginButton.addActionListener(e -> attemptLogin());
        
        JButton registerButton = new JButton("REGISTER");
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });
        formPanel.add(loginButton);
        formPanel.add(registerButton);
        add(formPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    private void addFormField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label);
        panel.add(field);
    }
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }
    private void attemptLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        
        if (authenticateUser(email, password)) {
            new BookingForm(email).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean authenticateUser(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}