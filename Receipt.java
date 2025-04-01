
import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
public class Receipt extends JFrame {
    public Receipt(String username, String movie, String theater, String date, String time, String tickets, int fare, String seats) {
        setTitle("Movie Ticket Receipt");
        setSize(650, 700);  // Increased size for better layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 245, 245));

        // Header with logo
        try {
            URL logoUrl = new URL("https://cdn-icons-png.flaticon.com/512/1179/1179120.png");
            Image logo = ImageIO.read(logoUrl).getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(logo));
            logoLabel.setBounds(50, 20, 80, 80);
            add(logoLabel);
        } catch (IOException e) {
            // If image fails to load, just continue without it
            System.out.println("Could not load logo image");
        }

        // Title Label
        JLabel titleLabel = new JLabel("MOVIE TICKET RECEIPT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 51, 102));
        titleLabel.setBounds(150, 30, 350, 40);
        add(titleLabel);

        // Company info
        JLabel companyLabel = new JLabel("CINEPLEX ENTERTAINMENT", SwingConstants.CENTER);
        companyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        companyLabel.setForeground(new Color(102, 102, 102));
        companyLabel.setBounds(150, 70, 350, 20);
        add(companyLabel);

        // Main Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setBounds(50, 120, 550, 350);
        detailsPanel.setLayout(new GridLayout(0, 2, 15, 15));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        detailsPanel.setBackground(Color.WHITE);

        // Add details with better formatting
        addStyledDetailRow(detailsPanel, "Booking Reference:", generateBookingRef());
        addStyledDetailRow(detailsPanel, "Customer Name:", username);
        addStyledDetailRow(detailsPanel, "Movie:", movie);
        addStyledDetailRow(detailsPanel, "Theater:", theater);
        addStyledDetailRow(detailsPanel, "Date:", date);
        addStyledDetailRow(detailsPanel, "Show Time:", time);
        addStyledDetailRow(detailsPanel, "Number of Tickets:", tickets);
        addStyledDetailRow(detailsPanel, "Seats:", seats);
        addStyledDetailRow(detailsPanel, "Total Amount:", "₹" + fare);
        addStyledDetailRow(detailsPanel, "Tax (18%):", "₹" + (int)(fare * 0.18));
        addStyledDetailRow(detailsPanel, "Total Payable:", "₹" + (int)(fare * 1.18));

        // Add QR code for ticket
        try {
            URL qrUrl = new URL("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" +
                URLEncoder.encode("Movie:" + movie + "\nTheater:" + theater + "\nDate:" + date + "\nTime:" + time + "\nSeats:" + seats, "UTF-8"));
            Image qrImage = ImageIO.read(qrUrl).getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
            qrLabel.setBounds(250, 480, 150, 150);
            add(qrLabel);
        } catch (IOException e) {
            System.out.println("Could not generate QR code");
        }

        // Footer
        JLabel footerLabel1 = new JLabel("Please present this receipt at the theater for entry", SwingConstants.CENTER);
        footerLabel1.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel1.setForeground(new Color(102, 102, 102));
        footerLabel1.setBounds(50, 640, 550, 20);
        add(footerLabel1);

        JLabel footerLabel2 = new JLabel("© 2023 Cineplex Entertainment. All Rights Reserved", SwingConstants.CENTER);
        footerLabel2.setFont(new Font("Arial", Font.PLAIN, 10));
        footerLabel2.setForeground(new Color(150, 150, 150));
        footerLabel2.setBounds(50, 660, 550, 15);
        add(footerLabel2);

        add(detailsPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addStyledDetailRow(JPanel panel, String label, String value) {
        JLabel labelField = new JLabel(label);
        labelField.setFont(new Font("Arial", Font.BOLD, 14));
        labelField.setForeground(new Color(51, 51, 51));
        panel.add(labelField);
        
        JLabel valueField = new JLabel(value);
        valueField.setFont(new Font("Arial", Font.PLAIN, 14));
        valueField.setForeground(new Color(0, 0, 0));
        panel.add(valueField);
    }

    private String generateBookingRef() {
        return "CPX" + (int)(Math.random() * 9000 + 1000) + 
               (char)(Math.random() * 26 + 'A') + 
               (char)(Math.random() * 26 + 'A');
    }

    public static void main(String[] args) {
        // Sample data for testing
        new Receipt("John Doe", "Avengers: Endgame", 
                   "PVR Phoenix Marketcity, Pune", "15-JUN-2023", 
                   "10:00 AM", "3", 1200, "G5, G6, G7").setVisible(true);
    }
}