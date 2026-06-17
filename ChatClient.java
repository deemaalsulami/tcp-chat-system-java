package group2_rtm_fall2025;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
//-------------------------------------------------------------------------------------------------------
    
    // Socket representing the client’s connection to the server.
    private final Socket socket;
    // Reads incoming text messages from the server.
    private final BufferedReader br;
    // Writes/send messages to the server.
    private final PrintWriter out;

//-------------------------------------------------------------------------------------------------------
    
    //Constructor.
    public ChatClient(String host, int port) throws Exception {
        //Connects to the server using host and port.
        socket = new Socket(host, port); 
        //Gets the input stream from the server.
        InputStream in = socket.getInputStream();
        //Converts byte stream into character stream.
        InputStreamReader inr = new InputStreamReader(in);
        //BufferedReader to read lines from server.
        br = new BufferedReader(inr);
        //Writer to send messages to server and with auto flush enabled.
        out = new PrintWriter(socket.getOutputStream(), true); 
    }

//-------------------------------------------------------------------------------------------------------
    
    public void start() throws Exception { 
       // create a thread to handle all incoming messages.
        new Thread(() -> { 
            try {
                String serverMsg;
                //Reads incoming messages
                while ((serverMsg = br.readLine()) != null) {
                    //Prints server messages to the console.
                    System.out.println(serverMsg);
                }
            } catch (IOException e) {
                //Shows message when the connection is lost.
                System.out.println("Disconnected from server.");
            }
        }).start();

        Scanner sc = new Scanner(System.in);
        while (true) {
            //Reads user input from console.
            String msg = sc.nextLine();
            // send the message to the server to print it 
            out.println(msg); 
            //Exit condition : Breaks loop if user types /exit.
            if (msg.equalsIgnoreCase("/exit")) {
                break;
            }
        }
        //Closes the socket connection.
        socket.close();
    }
    
//-------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        String host = "localhost"; // use IP address of the device that will be used to run server on.
        int port = 5000; // Port number where server is listening
        //Create the client and connect to the server.
        ChatClient client = new ChatClient(host, port);
        //Start client communication.
        client.start();
    }
    
//-------------------------------------------------------------------------------------------------------

}

//192.168.8.154