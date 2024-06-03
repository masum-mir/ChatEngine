package Server.chat;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerMain {

    public static void main(String[] args) {
        new ServerHelper().start();
    }
}

interface UserConfig {

    void addUser() throws IOException;

    void removeUser() throws IOException;

    void receiveMessage(String inputLine);

    void displayMenu() throws IOException;

    void showExistingUsers() throws IOException;

    boolean loginUser() throws IOException;
}

class Server implements UserConfig, Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;
    private static final List<Server> clients = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, User> users = Collections.synchronizedMap(new HashMap<>());
    private static final String userDataFile = "userInfo.txt";
    private static final String textDiv = "------------------------------";

    public Server(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            displayMenu();
            clients.add(this);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                receiveMessage(inputLine);
            }
        } catch (IOException e) {
            System.out.println("User Disconnected!");
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
                System.out.println("[Client] " + userName + " has been removed from the chat!");
            }
        }
    }

    @Override
    public void receiveMessage(String inputLine) {
        synchronized (clients) {
            for (Server client : clients) {
                if (client != this) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa");
                    String formattedDate = dateFormat.format(new Date());
                    client.out.println("[" + formattedDate + "] " + userName + ": " + inputLine);
                }
            }
        }
    }

    @Override
    public void displayMenu() throws IOException {
        while (true) {
            out.println("\n\t[1] Register User"
                    + "\n\t[2] Login to existing account"
                    + "\n\t[3] Show existing users"
                    + "\n\t[4] Remove User"
                    + "\n\t[5] Exit Server"
                    + "\n\nChoose your option: ");
            String option = in.readLine();
            if ("1".equals(option)) {
                addUser();
            } else if ("2".equals(option)) {
                if (loginUser()) {
                    break;
                }
            } else if ("3".equals(option)) {
                showExistingUsers();
            } else if ("4".equals(option)) {
                removeUser();
            } else if ("5".equals(option)) {
                stop();
            } else {
                out.println(textDiv + "\nPlease enter a valid option!\n" + textDiv);
            }
        }
    }

    @Override
    public void addUser() throws IOException {
        out.println("\nEnter a username:");
        String username = in.readLine();
        out.println("\nEnter a valid email id:");
        String email = in.readLine();
        out.println("\nEnter a phone number:");
        String phoneNumber = in.readLine();
        out.println("\nEnter a password:");
        String password = in.readLine();

        // Check if user exists in file
        boolean userExists = false;
        File userFile = new File(userDataFile);
        try (Scanner input = new Scanner(userFile)) {
            while (input.hasNext()) {
                if (username.equals(input.next())) {
                    userExists = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Could not check for user in the file");
        }

        if (users.containsKey(username) || userExists) {
            out.println(textDiv + "\nUsername already exists. Please try again with a different username.\n" + textDiv);
        } else {
            User user = new User(System.currentTimeMillis(), username, email, phoneNumber, password, true);
            users.put(username, user);
            out.println(textDiv + "\n\tUser Successfully Registered!\n" + textDiv);

            try (FileWriter fw = new FileWriter(userFile, true); PrintWriter pw = new PrintWriter(fw)) {
                pw.println(username + " " + email + " " + phoneNumber + " " + password);
            } catch (IOException e) {
                out.println("Error: File could not be written!");
            }
        }
        out.println("Press any key to return to the main menu.....");
        in.readLine();
    }

    @Override
    public void removeUser() throws IOException {
        out.println("\nEnter username to remove user:");
        String userName = in.readLine();

        File userFile = new File(userDataFile);
        File tempFile = new File("temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(userFile)); PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (!currentLine.contains(userName)) {
                    writer.println(currentLine);
                }
            }
        }

        userFile.delete();
        tempFile.renameTo(userFile);

        users.remove(userName);

        out.println(textDiv + "\nUSER SUCCESSFULLY REMOVED!!!\n\nPress any key to return to the main menu.......");
        in.readLine();
    }

    @Override
    public boolean loginUser() throws IOException {
        out.println("\nEnter username:");
        String username = in.readLine();
        out.println("\nEnter password:");
        String password = in.readLine();

        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            out.println(textDiv + "\n\tLogin successful.\n" + textDiv + "\n\nWelcome " + username + "!!!\n");
            userName = username;
            return true;
        } else {
            out.println(textDiv + "\nInvalid username or password. Try again.\n" + textDiv);
            return false;
        }
    }

    @Override
    public void showExistingUsers() throws IOException {
        out.println("\n\nList of all registered users: \n" + textDiv);
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] userData = currentLine.split("\\s+");
                out.println("Username: " + userData[0] + "\nEmail: " + userData[1] + "\nPhone: " + userData[2] + "\n");
            }
        } catch (Exception e) {
            out.println("Error: Could not read all user data from file!");
        }
        out.println("Press any key to return to main menu....");
        in.readLine();
    }

    private void stop() {
        System.exit(0);
    }

}

class ServerHelper {

    private static final int port = 8080;
    private static final String textDiv = "------------------------------";

    void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("\n\n" + textDiv + "\n\tSERVER SUCCESSFULLY STARTED!"
                    + "\n\tWAITING FOR CLIENTS......."
                    + "\n" + textDiv
                    + "\n\nSERVER LOG:\n" + textDiv);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Client] Connection successfully established with a client!\n" + clientSocket + "\n");

                Server clientThread = new Server(clientSocket);
                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            System.out.println("Error: Server could not be started.");
        }
    }
}

class User {

    private long id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String password;
    private boolean activeStatus;

    public User(long id, String userName, String email, String phoneNumber, String password, boolean activeStatus) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.activeStatus = activeStatus;
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

}
