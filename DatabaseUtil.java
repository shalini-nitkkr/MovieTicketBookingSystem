/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Shalini
 */

// DatabaseUtil.java
//import java.sql.*;
//
//public class DatabaseUtil {
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/MovieBooking";
//    private static final String DB_USER = "movie_user";
//    private static final String DB_PASSWORD = "moviepass123";
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//    }
//
//    public static void saveBooking(String username, String movie, String theater, String date, String time, int tickets, int fare,String seats) {
//        String query = "INSERT INTO bookings (username, movie, theater, date, time, tickets, fare,seats) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
//        
//        try (Connection conn = getConnection();
//             PreparedStatement pst = conn.prepareStatement(query)) {
//            
//            pst.setString(1, username);
//            pst.setString(2, movie);
//            pst.setString(3, theater);
//            pst.setString(4, date);
//            pst.setString(5, time);
//            pst.setInt(6, tickets);
//            pst.setInt(7, fare);
//             pst.setString(8, seats);
//            pst.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static ResultSet getBookingDetails(String username) throws SQLException {
//        String query = "SELECT * FROM bookings WHERE username = ? ORDER BY booking_date DESC LIMIT 1";
//        Connection conn = getConnection();
//        PreparedStatement pst = conn.prepareStatement(query);
//        pst.setString(1, username);
//        return pst.executeQuery();
//    }
//} 
// DatabaseUtil.java





//import java.sql.*;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class DatabaseUtil {
//    private static final ReentrantLock bookingLock = new ReentrantLock();
//    
//    public static boolean authenticateUser(String username, String password) {
//        // Implementation of authentication
//        return true;
//    }
//    
//    public static boolean registerUser(String username, String password, String email) {
//        // Implementation of registration
//        return true;
//    }
//    
//    public static synchronized String processBooking(String username, String movie, 
//            String theater, String date, String time, int tickets, String seats) {
//        bookingLock.lock();
//        try (Connection conn = Database.getConnection()) {
//            // 1. Check seat availability first
//            if (!areSeatsAvailable(conn, theater, date, time, seats)) {
//                return "SEATS_UNAVAILABLE:Some seats are already booked";
//            }
//            
//            // 2. Calculate fare
//            int fare = tickets * 200; // Assuming 200 per ticket
//            
//            // 3. Save booking to database
//            String query = "INSERT INTO bookings (username, movie, theater, date, time, tickets, fare, seats) " +
//                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//            try (PreparedStatement pst = conn.prepareStatement(query)) {
//                pst.setString(1, username);
//                pst.setString(2, movie);
//                pst.setString(3, theater);
//                pst.setString(4, date);
//                pst.setString(5, time);
//                pst.setInt(6, tickets);
//                pst.setInt(7, fare);
//                pst.setString(8, seats);
//                
//                pst.executeUpdate();
//                return "BOOKING_SUCCESS:Booking confirmed. Fare: " + fare;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return "BOOKING_FAILED:Database error occurred";
//        } finally {
//            bookingLock.unlock();
//        }
//    }
//    
//    private static boolean areSeatsAvailable(Connection conn, String theater, 
//            String date, String time, String seats) throws SQLException {
//        // Check if any of the requested seats are already booked
//        String query = "SELECT seats FROM bookings WHERE theater = ? AND date = ? AND time = ?";
//        try (PreparedStatement pst = conn.prepareStatement(query)) {
//            pst.setString(1, theater);
//            pst.setString(2, date);
//            pst.setString(3, time);
//            
//            ResultSet rs = pst.executeQuery();
//            while (rs.next()) {
//                String bookedSeats = rs.getString("seats");
//                for (String seat : seats.split(",")) {
//                    if (bookedSeats.contains(seat.trim())) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//    
//    public static synchronized boolean saveBooking(String username, String movie, 
//            String theater, String date, String time, int tickets, int fare, String seats) {
//        // Similar to processBooking but without seat availability check
//        // Used by the direct GUI booking flow
//        bookingLock.lock();
//        try (Connection conn = Database.getConnection()) {
//            String query = "INSERT INTO bookings (username, movie, theater, date, time, tickets, fare, seats) " +
//                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//            try (PreparedStatement pst = conn.prepareStatement(query)) {
//                pst.setString(1, username);
//                pst.setString(2, movie);
//                pst.setString(3, theater);
//                pst.setString(4, date);
//                pst.setString(5, time);
//                pst.setInt(6, tickets);
//                pst.setInt(7, fare);
//                pst.setString(8, seats);
//                
//                return pst.executeUpdate() > 0;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            bookingLock.unlock();
//        }
//    }
//} 


// DatabaseUtil.java
import java.sql.*;
import java.util.concurrent.locks.ReentrantLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class DatabaseUtil {
    private static final ReentrantLock bookingLock = new ReentrantLock();
    
    public static boolean authenticateUser(String email, String password) {
        String query = "SELECT password FROM users WHERE email = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setString(1, email);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String inputHash = hashPassword(password);
                    return storedHash.equals(inputHash);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
    
    public static synchronized String processBooking(String username, String movie, 
            String theater, String date, String time, int tickets, String seats) {
        bookingLock.lock();
        try (Connection conn = Database.getConnection()) {
            // 1. Check seat availability first
            if (!areSeatsAvailable(conn, theater, date, time, seats)) {
                return "SEATS_UNAVAILABLE:Some seats are already booked";
            }
            
            // 2. Calculate fare
            int fare = tickets * 200; // Assuming 200 per ticket
            
            // 3. Save booking to database
            String query = "INSERT INTO bookings (username, movie, theater, date, time, tickets, fare, seats) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, username);
                pst.setString(2, movie);
                pst.setString(3, theater);
                pst.setString(4, date);
                pst.setString(5, time);
                pst.setInt(6, tickets);
                pst.setInt(7, fare);
                pst.setString(8, seats);
                
                pst.executeUpdate();
                return "BOOKING_SUCCESS:Booking confirmed. Fare: " + fare;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "BOOKING_FAILED:Database error occurred";
        } finally {
            bookingLock.unlock();
        }
    }
    
    private static boolean areSeatsAvailable(Connection conn, String theater, 
            String date, String time, String seats) throws SQLException {
        // Check if any of the requested seats are already booked
        String query = "SELECT seats FROM bookings WHERE theater = ? AND date = ? AND time = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, theater);
            pst.setString(2, date);
            pst.setString(3, time);
            
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String bookedSeats = rs.getString("seats");
                for (String seat : seats.split(",")) {
                    if (bookedSeats.contains(seat.trim())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public static String getBookedSeats(String theater, String date, String time) {
    String query = "SELECT GROUP_CONCAT(seats) AS all_seats FROM bookings WHERE theater = ? AND date = ? AND time = ?";
    
    try (Connection conn = Database.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {
        
        pst.setString(1, theater);
        pst.setString(2, date);
        pst.setString(3, time);
        
        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getString("all_seats");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "";
}
    public static synchronized boolean saveBooking(String username, String movie, 
            String theater, String date, String time, int tickets, int fare, String seats) {
        bookingLock.lock();
        try (Connection conn = Database.getConnection()) {
            String query = "INSERT INTO bookings (username, movie, theater, date, time, tickets, fare, seats) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, username);
                pst.setString(2, movie);
                pst.setString(3, theater);
                pst.setString(4, date);
                pst.setString(5, time);
                pst.setInt(6, tickets);
                pst.setInt(7, fare);
                pst.setString(8, seats);
                
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            bookingLock.unlock();
        }
    }
}