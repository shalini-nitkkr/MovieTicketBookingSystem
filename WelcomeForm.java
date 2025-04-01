/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Shalini
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WelcomeForm extends JFrame {
    public WelcomeForm() {
        setTitle("Movie Booking System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(50, 50, 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        JLabel titleLabel = new JLabel("Welcome to Movie Booking System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Movie Icon or Image
        JLabel iconLabel = new JLabel(new ImageIcon("movie_icon.png")); // Replace with your image path
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(iconLabel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton loginButton = new JButton("LOGIN");
        styleButton(loginButton, new Color(76, 175, 80)); // Green
        loginButton.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        JButton registerButton = new JButton("REGISTER");
        styleButton(registerButton, new Color(33, 150, 243)); // Blue
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        gbc.gridy = 1;
        centerPanel.add(buttonPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WelcomeForm().setVisible(true);
        });
    }
}