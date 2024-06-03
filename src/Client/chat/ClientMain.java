package Client.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class ClientMain 
{
    public static void main(String[] args) 
    {
        Client client = new Client();
        client.connectToServer(Client.IPADDRESS, Client.PORT);
        
    }

}

class Message 
{

    private final Date createDate = new Date();
    protected final String textDiv = "------------------------------"
            + "--------------------------------";

    Message() {
    }

    void display() 
    {
        // Clear the console screen 
        System.out.print("\033[H\033[2J"); 
        System.out.flush(); 
        
        System.out.println("\n\n" + textDiv
                + "\n\tWELCOME TO THE CHAT SERVER!\n\t"
                + createDate
                + "\n"
                + textDiv);
   
    }

    void sendMsg(String address, int port) throws IOException
    {
        //function implemented inside Client class
        
    }

}

class Client extends Message 
{

    static final String IPADDRESS = "localhost";
    static final int PORT = 8080;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter out;
    private BufferedReader in;

    Client() {
    }

    public void connectToServer(String address, int port) 
    {
        try 
        {
            display();
            sendMsg(address, port);
        } 
        catch (IOException e) 
        {
            System.err.println("Error: Could not connect to the server.");
        }
        
    }
    
    @Override
    public void sendMsg(String address, int port) throws IOException
    {
            
        socket = new Socket(address, port);

        input = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(() -> {
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                    System.out.println(serverResponse);
                }
            } 
            catch (IOException e) 
            {
                System.err.println(textDiv 
                        + "\n    Disconnected from the chat server successfully!\n"
                        + textDiv);
            }
        }).start();

        String userInput;
        while (true) 
        {
            //System.out.print("me: ");
            userInput = input.readLine();
            out.println(userInput);
        }

    }

}

