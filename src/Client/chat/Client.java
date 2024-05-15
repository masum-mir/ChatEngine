package Client.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {

    static final String ipAddress = "localhost";
    static final int port = 8080;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter out;
    private BufferedReader in;
    
    Map<String, User> users = new HashMap<>();

    public static void main(String[] args) {

        Client client = new Client(ipAddress, port); 
    }

    public Client(String address, int port) {
        try {
           
            socket = new Socket(address, port);
            System.out.println("Connected to the chat server");

            input = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    System.err.println("Server connection closed unexpectedly.");
                }
            }).start();

            String userInput;
            while (true) {
                userInput = input.readLine();

                out.println(userInput);
            }

        } catch (IOException e) {
            System.err.println("Error: Could not connect to the server.");
        }
    }

}
