/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Shalini
 */

    import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/MovieBooking?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "Shalini@123";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Database Connected Successfully!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Connection Failed: " + e.getMessage());
        }
    }
}


