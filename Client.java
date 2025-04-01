
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    private static String username = null;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            // Read welcome message
            System.out.println(in.readLine());

            // Start a thread to listen for server messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server: " + message);
                        
                        if (message.startsWith("UPDATE:")) {
                            // Handle seat update notifications
                            String[] parts = message.substring(7).split(":");
                            System.out.println("Seats " + parts[3] + " at " + parts[0] + 
                                             " on " + parts[1] + " " + parts[2] + 
                                             " have been booked by another user");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            }).start();

            // Main input loop
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("exit")) {
                    out.println("LOGOUT");
                    break;
                }
                else if (input.startsWith("login ")) {
                    out.println("LOGIN:" + input.substring(6));
                }
                else if (input.startsWith("book ")) {
                    if (username == null) {
                        System.out.println("Please login first");
                        continue;
                    }
                    out.println("BOOK:" + input.substring(5));
                }
                else {
                    out.println(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}