// 
//package Server.chat;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// * @author masum
// */
//public class ClientHandler implements Runnable {
//
//    private Socket clientSocket;
//    private PrintWriter out;
//    private BufferedReader in;
//    private String username;
//    private static List<ClientHandler> clients = new ArrayList<>();
//
//    public ClientHandler(Socket clientSocket) {
//        this.clientSocket = clientSocket;
//        try {
//            out = new PrintWriter(clientSocket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void run() {
//        try {
//            // Get the username from the client
//            out.println("Enter your username:");
//            username = in.readLine();
//            System.out.println("User " + username + " connected.");
//
//            out.println("Welcome to the chat, " + username + "!");
//
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                System.out.println("[" + username + "]: " + inputLine);
//
//                // Broadcast the message to all clients
//                broadcast("[" + username + "]: " + inputLine);
//
//                // For exiting client
//                if (inputLine.equalsIgnoreCase("/exit")) {
//                    break;
//                }
//            }
//
//            // Remove the client handler from the list
//            clients.remove(this);
//
//            // Close resources
//            in.close();
//            out.close();
//            clientSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void broadcast(String message) {
//        synchronized (clients) {
//            for (ClientHandler client : clients) {
//                if (client != this) {
//                    client.sendMessage(message);
//                }
//            }
//        }
//    }
//
//    private void sendMessage(String message) {
//        out.println(message);
//    }
//}
