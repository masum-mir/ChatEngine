package Server.chat;

import java.io.*;
import java.net.*;
import java.util.*;
import Client.chat.User;

public class Server {

    private static List<ClientHandler> clients = new ArrayList<>();
    private static Map<String, User> users = new HashMap<>();
    private static int port = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            ClientHandler clientThread = new ClientHandler(clientSocket, clients, users);
            new Thread(clientThread).start();
        }
    }
}

class ClientHandler implements Runnable {

    private Socket clientSocket;
    private List<ClientHandler> clients;
    private Map<String, User> users;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public ClientHandler(Socket socket, List<ClientHandler> clients, Map<String, User> users) throws IOException {
        this.clientSocket = socket;
        this.clients = clients;
        this.users = users;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                out.println("Enter 1 to Register, 2 to Login:");
                String option = in.readLine();

                if ("1".equals(option)) {
                    registerUser();
                } else if ("2".equals(option)) {
                    if (loginUser()) {
                        break;
                    }
                }
            }

            clients.add(this);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.out.println(users.get(userName) + ": " + inputLine);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
                System.out.println("Client disconnected: " + clientSocket);
            }
        }
    }

    private void registerUser() throws IOException {
        out.println("Enter username:");
        String username = in.readLine();
        out.println("Enter email:");
        String email = in.readLine();
        out.println("Enter phone number:");
        String phoneNumber = in.readLine();
        out.println("Enter password:");
        String password = in.readLine();

        if (users.containsKey(username)) {
            System.out.println("Username already exists. Try again.");
        } else {
            User user = new User(System.currentTimeMillis(), username, email, phoneNumber, password, true);
            users.put(username, user);
            System.out.println("Registration successful.");
        }

    }

    private boolean loginUser() throws IOException {
        out.println("Enter username:");
        String username = in.readLine();
        out.println("Enter password:");
        String password = in.readLine();

        User user = users.get(username);
        if (user != null && password.equals(user.getPassword())) {
            out.println("Login successful.");
            userName = username;
            return true;
        } else {
            out.println("Invalid username or password. Try again.");
            return false;
        }

    }

}
