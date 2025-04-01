
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class BookingForm extends JFrame {
    private JComboBox<String> movieDropdown, theaterDropdown, timeDropdown;
    private JTextField ticketField, dateField;
    private JButton submitButton, dateButton;
    private JTextArea seatDisplay;
    private boolean[][] seatSelection = new boolean[5][6]; // 5 rows (A-E), 6 columns (1-6)
    private Color[] seatColors = {
        new Color(255, 102, 102), // Light red
        new Color(102, 255, 102), // Light green
        new Color(102, 178, 255), // Light blue
        new Color(255, 255, 102), // Yellow
        new Color(255, 178, 102)  // Orange
    };
    private Set<String> bookedSeats = new HashSet<>();

    private class ColorIcon implements Icon {
        private final Color color;
        private final int width;
        private final int height;

        public ColorIcon(Color color, int width, int height) {
            this.color = color;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, width, height);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    public BookingForm(String username) {
        setTitle("Movie Ticket Booking");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(240, 248, 255));
        setLayout(new BorderLayout());

        // Title Panel with gradient
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 180), getWidth(), 0, new Color(100, 149, 237));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titlePanel.setPreferredSize(new Dimension(700, 60));
        titlePanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("MOVIE TICKET BOOKING", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel userLabel = new JLabel("User: " + username, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        titlePanel.add(userLabel, BorderLayout.EAST);
        
        add(titlePanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(240, 248, 255));

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Movie selection
        formPanel.add(createFormRow("SELECT MOVIE:", movieDropdown = new JComboBox<>(
            new String[]{"Dear Zindagi", "Sita Ramam", "Your Name", "Interstellar", "Avengers"})));

        // Theater selection
        formPanel.add(createFormRow("SELECT THEATRE:", theaterDropdown = new JComboBox<>(
            new String[]{"Divine Cinemas", "INOX Megaplex", "PVR Gold", "Cinepolis VIP", "IMAX"})));

        // Date selection
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.add(new JLabel("SELECT DATE:"), BorderLayout.WEST);
        dateField = new JTextField();
        dateField.setEditable(false);
        datePanel.add(dateField, BorderLayout.CENTER);
        dateButton = new JButton("📅");
        dateButton.addActionListener(e -> showDatePicker());
        datePanel.add(dateButton, BorderLayout.EAST);
        formPanel.add(datePanel);

        // Time selection
        formPanel.add(createFormRow("SELECT TIME:", timeDropdown = new JComboBox<>(
            new String[]{"10:00 AM", "1:00 PM", "4:00 PM", "7:00 PM", "10:00 PM"})));

        // Ticket count
        formPanel.add(createFormRow("NO. OF TICKETS:", ticketField = new JTextField()));

        // Seat display
        JPanel seatPanel = new JPanel(new BorderLayout(5, 5));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.add(new JLabel("SEAT SELECTION:"), BorderLayout.WEST);
        seatDisplay = new JTextArea();
        seatDisplay.setEditable(false);
        seatDisplay.setLineWrap(true);
        seatDisplay.setWrapStyleWord(true);
        seatDisplay.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        updateSeatDisplay();
        seatPanel.add(new JScrollPane(seatDisplay), BorderLayout.CENTER);
        
        JButton selectSeatsButton = new JButton("🎬 Select Seats");
        selectSeatsButton.addActionListener(e -> showSeatSelectionDialog());
        seatPanel.add(selectSeatsButton, BorderLayout.EAST);
        formPanel.add(seatPanel);

        // Submit button
        submitButton = new JButton("💳 PROCEED TO PAYMENT");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(createSubmitActionListener(username));
        styleButton(submitButton, new Color(70, 130, 180));
        
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(submitButton);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private JPanel createFormRow(String labelText, JComponent component) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(70, 130, 180));
        rowPanel.add(label, BorderLayout.WEST);
        
        component.setFont(new Font("Arial", Font.PLAIN, 12));
        if (component instanceof JTextField) {
            ((JTextField)component).setColumns(15);
        }
        rowPanel.add(component, BorderLayout.CENTER);
        
        return rowPanel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }

    private void showDatePicker() {
        JDialog dateDialog = new JDialog(this, "Select Date", true);
        dateDialog.setSize(350, 350);
        dateDialog.setLayout(new BorderLayout());
        
        JCalendar calendar = new JCalendar();
        calendar.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("calendar".equals(evt.getPropertyName())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
                    dateField.setText(sdf.format(calendar.getDate()));
                    dateDialog.dispose();
                }
            }
        });
        
        dateDialog.add(calendar, BorderLayout.CENTER);
        dateDialog.setLocationRelativeTo(this);
        dateDialog.setVisible(true);
    }

    private void showSeatSelectionDialog() {
        // Get booked seats for current selection
        updateBookedSeats();
        
        JDialog seatDialog = new JDialog(this, "Select Your Seats", true);
        seatDialog.setSize(750, 650);
        seatDialog.setLayout(new BorderLayout());
        seatDialog.getContentPane().setBackground(new Color(240, 240, 240));

        // Screen panel with curved screen effect
        JPanel screenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw curved screen
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 50, 50), getWidth(), 0, new Color(100, 100, 100));
                g2d.setPaint(gp);
                g2d.fillRoundRect(50, 10, getWidth()-100, 30, 50, 50);
                
                // Draw screen text
                g2d.setColor(new Color(200, 200, 255));
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String screenText = "S C R E E N";
                int textWidth = g2d.getFontMetrics().stringWidth(screenText);
                g2d.drawString(screenText, (getWidth()-textWidth)/2, 30);
            }
        };
        screenPanel.setPreferredSize(new Dimension(750, 50));
        seatDialog.add(screenPanel, BorderLayout.NORTH);

        // Main seating area
        JPanel seatGridPanel = new JPanel(new GridBagLayout());
        seatGridPanel.setBackground(new Color(240, 240, 240));
        seatGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.CENTER;

        // Column headers
        gbc.gridy = 0;
        gbc.gridx = 0;
        seatGridPanel.add(new JLabel(""), gbc); // Empty corner
        
        for (int col = 0; col < 6; col++) {
            gbc.gridx = col + 1;
            JLabel colLabel = new JLabel(String.valueOf(col + 1), SwingConstants.CENTER);
            colLabel.setFont(new Font("Arial", Font.BOLD, 12));
            colLabel.setPreferredSize(new Dimension(30, 20));
            seatGridPanel.add(colLabel, gbc);
        }

        // Seat buttons with availability status
        for (int row = 0; row < 5; row++) {
            gbc.gridy = row + 1;
            
            // Row label
            gbc.gridx = 0;
            JLabel rowLabel = new JLabel(Character.toString((char)('A' + row)), SwingConstants.CENTER);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 12));
            rowLabel.setPreferredSize(new Dimension(20, 30));
            seatGridPanel.add(rowLabel, gbc);
            
            for (int col = 0; col < 6; col++) {
                gbc.gridx = col + 1;
                final int r = row;
                final int c = col;
                
                String seatId = String.format("%c%d", (char)('A' + row), col + 1);
                boolean isBooked = bookedSeats.contains(seatId);
                
                JToggleButton seatButton = new JToggleButton();
                seatButton.setPreferredSize(new Dimension(40, 40));
                
                if (isBooked) {
                    // Style for booked seats
                    seatButton.setBackground(Color.LIGHT_GRAY);
                    seatButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                    seatButton.setText("X");
                    seatButton.setEnabled(false);
                } else {
                    // Style for available seats
                    seatButton.setSelected(seatSelection[r][c]);
                    if (seatSelection[r][c]) {
                        seatButton.setBackground(seatColors[row]);
                        seatButton.setBorder(BorderFactory.createLineBorder(seatColors[row].darker(), 2));
                        seatButton.setText("✓");
                    } else {
                        seatButton.setBackground(Color.WHITE);
                        seatButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
                        seatButton.setText("");
                    }
                    
                    seatButton.addActionListener(e -> {
                        seatSelection[r][c] = seatButton.isSelected();
                        if (seatButton.isSelected()) {
                            seatButton.setBackground(seatColors[r]);
                            seatButton.setBorder(BorderFactory.createLineBorder(seatColors[r].darker(), 2));
                            seatButton.setText("✓");
                        } else {
                            seatButton.setBackground(Color.WHITE);
                            seatButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
                            seatButton.setText("");
                        }
                        updateSeatDisplay();
                    });
                }
                
                seatButton.setFocusPainted(false);
                seatButton.setOpaque(true);
                seatButton.setFont(new Font("Arial", Font.BOLD, 12));
                seatGridPanel.add(seatButton, gbc);
            }
        }

        // Add aisle space
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 7;
        seatGridPanel.add(Box.createVerticalStrut(30), gbc);

        // Add legend
        gbc.gridy = 7;
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        legendPanel.setBackground(new Color(240, 240, 240));
        
        JLabel availableLabel = new JLabel("Available");
        availableLabel.setIcon(new ColorIcon(Color.WHITE, 15, 15));
        legendPanel.add(availableLabel);
        
        for (int i = 0; i < seatColors.length; i++) {
            JLabel rowLabel = new JLabel("Row " + (char)('A' + i));
            rowLabel.setIcon(new ColorIcon(seatColors[i], 15, 15));
            legendPanel.add(rowLabel);
        }
        
        JLabel selectedLabel = new JLabel("Selected");
        selectedLabel.setIcon(new ColorIcon(new Color(0, 150, 0), 15, 15));
        legendPanel.add(selectedLabel);
        
        JLabel bookedLabel = new JLabel("Booked");
        bookedLabel.setIcon(new ColorIcon(Color.LIGHT_GRAY, 15, 15));
        legendPanel.add(bookedLabel);
        
        seatGridPanel.add(legendPanel, gbc);

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton confirmButton = new JButton("Confirm Selection");
        styleSeatButton(confirmButton, new Color(76, 175, 80));
        confirmButton.addActionListener(e -> seatDialog.dispose());
        
        JButton clearButton = new JButton("Clear All");
        styleSeatButton(clearButton, new Color(244, 67, 54));
        clearButton.addActionListener(e -> {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 6; col++) {
                    seatSelection[row][col] = false;
                }
            }
            updateSeatDisplay();
            seatDialog.dispose();
            showSeatSelectionDialog(); // Refresh
        });
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(clearButton);
        seatGridPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(seatGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        seatDialog.add(scrollPane, BorderLayout.CENTER);
        
        seatDialog.setLocationRelativeTo(this);
        seatDialog.setVisible(true);
    }

    private void updateBookedSeats() {
        bookedSeats.clear();
        String theater = (String)theaterDropdown.getSelectedItem();
        String date = dateField.getText();
        String time = (String)timeDropdown.getSelectedItem();
        
        if (theater != null && date != null && !date.isEmpty() && time != null) {
            try (Connection conn = Database.getConnection()) {
                String query = "SELECT seats FROM bookings WHERE theater = ? AND date = ? AND time = ?";
                try (PreparedStatement pst = conn.prepareStatement(query)) {
                    pst.setString(1, theater);
                    pst.setString(2, date);
                    pst.setString(3, time);
                    
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            String seats = rs.getString("seats");
                            if (seats != null && !seats.isEmpty()) {
                                Collections.addAll(bookedSeats, seats.split(",\\s*"));
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error checking seat availability", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleSeatButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    private void updateSeatDisplay() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                if (seatSelection[row][col]) {
                    String seatId = String.format("%c%d", (char)('A' + row), col + 1);
                    if (count > 0) sb.append(", ");
                    sb.append(seatId);
                    count++;
                }
            }
        }
        
        if (count == 0) {
            sb.append("No seats selected yet");
        } else {
            sb.append("\n\nTotal ").append(count).append(" seat(s) selected");
        }
        
        seatDisplay.setText(sb.toString());
    }

    private ActionListener createSubmitActionListener(String username) {
        return e -> {
            // Validation and payment form creation
            String tickets = ticketField.getText();
            if (!tickets.matches("\\d+") || Integer.parseInt(tickets) < 1) {
                JOptionPane.showMessageDialog(this, "Please enter valid number of tickets", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int selectedSeats = 0;
            StringBuilder seats = new StringBuilder();
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 6; col++) {
                    if (seatSelection[row][col]) {
                        String seatId = String.format("%c%d", (char)('A' + row), col + 1);
                        if (bookedSeats.contains(seatId)) {
                            JOptionPane.showMessageDialog(this, 
                                "Seat " + seatId + " is already booked! Please select different seats.", 
                                "Seat Unavailable", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if (seats.length() > 0) seats.append(", ");
                        seats.append(seatId);
                        selectedSeats++;
                    }
                }
            }
            
            if (selectedSeats != Integer.parseInt(tickets)) {
                JOptionPane.showMessageDialog(this, 
                    "Number of seats selected doesn't match ticket count", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (dateField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a date", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Proceed to payment
            new PaymentForm(username, 
                (String)movieDropdown.getSelectedItem(),
                (String)theaterDropdown.getSelectedItem(),
                dateField.getText(),
                (String)timeDropdown.getSelectedItem(),
                tickets,
                selectedSeats * 200,
                seats.toString()
            ).setVisible(true);
            dispose();
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingForm("TestUser").setVisible(true));
    }
}

  
class JCalendar extends JPanel {
    private Calendar calendar;
    private JButton[][] dayButtons;
    private Color headerColor = new Color(70, 130, 180);
    private Color dayNameColor = new Color(100, 149, 237);
    private Color todayColor = new Color(220, 240, 255);
    
    public JCalendar() {
        calendar = Calendar.getInstance();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Month/year header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel monthLabel = new JLabel(getMonthYear(), SwingConstants.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
        monthLabel.setForeground(Color.WHITE);
        
        JButton prevButton = createNavButton("<");
        JButton nextButton = createNavButton(">");
        
        prevButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        
        nextButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
        
        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Day names
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        JPanel dayNamePanel = new JPanel(new GridLayout(1, 7));
        dayNamePanel.setBackground(Color.WHITE);
        
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setForeground(dayNameColor);
            dayNamePanel.add(dayLabel);
        }
        add(dayNamePanel, BorderLayout.CENTER);
        
        // Days grid
        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        daysPanel.setBackground(Color.WHITE);
        dayButtons = new JButton[6][7];
        
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                JButton dayButton = new JButton();
                dayButton.setFont(new Font("Arial", Font.PLAIN, 12));
                dayButton.setBackground(Color.WHITE);
                dayButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                dayButton.setOpaque(true);
                dayButton.setFocusPainted(false);
                
                dayButton.addActionListener(e -> {
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayButton.getText()));
                    firePropertyChange("calendar", null, calendar.getTime());
                });
                
                dayButtons[row][col] = dayButton;
                daysPanel.add(dayButton);
            }
        }
        
        add(daysPanel, BorderLayout.SOUTH);
        updateCalendar();
    }
    
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(headerColor);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        return button;
    }
    
    private void updateCalendar() {
        // Update month/year label
        ((JLabel)((JPanel)getComponent(0)).getComponent(1)).setText(getMonthYear());
        
        // Clear all buttons
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                dayButtons[row][col].setText("");
                dayButtons[row][col].setEnabled(false);
                dayButtons[row][col].setBackground(Color.WHITE);
            }
        }
        
        // Set current month days
        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDay = temp.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Highlight today's date
        Calendar today = Calendar.getInstance();
        boolean currentMonth = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && 
                              calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH));
        
        for (int day = 1, row = 0, col = firstDay; day <= daysInMonth; day++, col++) {
            if (col == 7) {
                col = 0;
                row++;
            }
            dayButtons[row][col].setText(String.valueOf(day));
            dayButtons[row][col].setEnabled(true);
            
            if (currentMonth && day == today.get(Calendar.DAY_OF_MONTH)) {
                dayButtons[row][col].setBackground(todayColor);
                dayButtons[row][col].setFont(new Font("Arial", Font.BOLD, 12));
            }
        }
    }
    
    private String getMonthYear() {
        return new SimpleDateFormat("MMMM yyyy").format(calendar.getTime());
    }
    
    public Date getDate() {
        return calendar.getTime();
    }
}