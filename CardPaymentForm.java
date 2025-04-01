
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CardPaymentForm extends JFrame {
    private JTextField cardNumberField, cardHolderField;
    private JPasswordField cvvField;
    private JComboBox<String> expMonth, expYear;
    private JButton proceedButton;

    public CardPaymentForm(String username, String movie,String theater,String date,String time, String tickets, int fare,String seats) {
        setTitle("Card Payment");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel bookingLabel = new JLabel("CONFIRM BOOKING");
        bookingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bookingLabel.setForeground(Color.RED);
        bookingLabel.setBounds(180, 10, 200, 30);
        add(bookingLabel);

        JLabel movieLabel = new JLabel("MOVIE: " + movie);
        movieLabel.setBounds(50, 50, 200, 20);
        add(movieLabel);

        JLabel theaterLabel = new JLabel("THEATER: "+theater);
        theaterLabel.setBounds(280, 50, 200, 20);
        add(theaterLabel);

        JLabel dateLabel = new JLabel("DATE: "+date);
        dateLabel.setBounds(280, 80, 200, 20);
        add(dateLabel);

        JLabel timeLabel = new JLabel("TIME: "+time);
        timeLabel.setBounds(280, 110, 200, 20);
        add(timeLabel);

        JLabel ticketLabel = new JLabel("NO OF TICKETS: " + tickets);
        ticketLabel.setBounds(50, 80, 200, 20);
        add(ticketLabel);

        JLabel fareLabel = new JLabel("FARE: " + fare);
        fareLabel.setBounds(50, 110, 200, 20);
        add(fareLabel);
         JLabel seatsLabel = new JLabel("SEATS: " + seats);
        seatsLabel.setBounds(50, 140, 300, 20);
        add(seatsLabel);

        JLabel cardDetailsLabel = new JLabel("CARD DETAILS");
        cardDetailsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cardDetailsLabel.setForeground(Color.BLUE);
        cardDetailsLabel.setBounds(180, 150, 200, 20);
        add(cardDetailsLabel);

        JLabel cardNumberLabel = new JLabel("Card number");
        cardNumberLabel.setBounds(50, 180, 120, 20);
        add(cardNumberLabel);

        cardNumberField = new JTextField();
        cardNumberField.setBounds(180, 180, 200, 20);
        add(cardNumberField);

        JLabel cardHolderLabel = new JLabel("Card holder name");
        cardHolderLabel.setBounds(50, 210, 120, 20);
        add(cardHolderLabel);

        cardHolderField = new JTextField();
        cardHolderField.setBounds(180, 210, 200, 20);
        add(cardHolderField);

        JLabel cvvLabel = new JLabel("CVV");
        cvvLabel.setBounds(50, 240, 40, 20);
        add(cvvLabel);

        cvvField = new JPasswordField();
        cvvField.setBounds(90, 240, 50, 20);
        add(cvvField);

        JLabel expLabel = new JLabel("EXP");
        expLabel.setBounds(150, 240, 40, 20);
        add(expLabel);

        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        expMonth = new JComboBox<>(months);
        expMonth.setBounds(190, 240, 50, 20);
        add(expMonth);

        String[] years = {"2024", "2025", "2026", "2027", "2028", "2029", "2030"};
        expYear = new JComboBox<>(years);
        expYear.setBounds(250, 240, 70, 20);
        add(expYear);
 
        proceedButton = new JButton("Proceed");
        proceedButton.setBounds(180, 270, 100, 30);
        add(proceedButton);

        
proceedButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Validate card details first
        if (cardNumberField.getText().isEmpty() || 
            cardHolderField.getText().isEmpty() ||
            cvvField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(null, 
                "Please fill all card details!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save user's booking to database
        try {
            DatabaseUtil.saveBooking(username, movie, theater, date, time, 
                                   Integer.parseInt(tickets), fare,seats);
            
            JOptionPane.showMessageDialog(null, 
                "Payment Successful!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            new Receipt(username,movie,theater,date,time,tickets,fare,seats).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, 
                "Error processing payment: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
});
        setLocationRelativeTo(null);
        setVisible(true);
    }
public static void main(String[] args) {
    // Provide all required parameters including seats
    new CardPaymentForm("TestUser", "Sample Movie", "PVR", "01-01-2023", 
        "10:00 AM", "2", 400, "A1,A2").setVisible(true);
}
}
