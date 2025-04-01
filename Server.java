
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 5000;
    private static final int MAX_THREADS = 10;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    private static Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                activeClients.put(client.getClientId(), client);
                threadPool.execute(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    public static void broadcast(String message, String senderId) {
        for (ClientHandler client : activeClients.values()) {
            if (!client.getClientId().equals(senderId)) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(String clientId) {
        activeClients.remove(clientId);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientId = UUID.randomUUID().toString();
    }

    public String getClientId() {
        return clientId;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("CONNECTED:Welcome to Movie Booking System");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client " + clientId + ": " + message);
                
                // Process different commands
                if (message.startsWith("LOGIN:")) {
                    handleLogin(message.substring(6));
                } 
                else if (message.startsWith("BOOK:")) {
                    handleBooking(message.substring(5));
                }
                else if (message.equals("LOGOUT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client " + clientId + " disconnected unexpectedly");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Server.removeClient(clientId);
            System.out.println("Client " + clientId + " disconnected");
        }
    }

    private void handleLogin(String credentials) {
        String[] parts = credentials.split(":");
        if (parts.length == 2) {
            String email = parts[0];
            String password = parts[1];
            
            if (DatabaseUtil.authenticateUser(email, password)) {
                this.username = email;
                sendMessage("LOGIN_SUCCESS:Welcome " + email);
            } else {
                sendMessage("LOGIN_FAILED:Invalid credentials");
            }
        } else {
            sendMessage("ERROR:Invalid login format");
        }
    }

    private void handleBooking(String bookingDetails) {
        if (username == null) {
            sendMessage("ERROR:Please login first");
            return;
        }
        
        String[] parts = bookingDetails.split(":");
        if (parts.length == 6) {
            String movie = parts[0];
            String theater = parts[1];
            String date = parts[2];
            String time = parts[3];
            int tickets = Integer.parseInt(parts[4]);
            String seats = parts[5];
            
            String result = DatabaseUtil.processBooking(username, movie, theater, 
                                                      date, time, tickets, seats);
            sendMessage(result);
            
            if (result.startsWith("BOOKING_SUCCESS")) {
                // Notify other clients about the booking
                Server.broadcast("UPDATE:" + theater + ":" + date + ":" + time + ":" + seats, clientId);
            }
        } else {
            sendMessage("ERROR:Invalid booking format");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
