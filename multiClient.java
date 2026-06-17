package group2_rtm_fall2025;
// we used this source to learn how to use class thread
// https://www.geeksforgeeks.org/java/java-threads/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class multiClient extends Thread {
//-------------------------------------------------------------------------------------------------------

    //Socket representing this client's network connection
    private final Socket socket;
    //Object of the server to access needed functions
    private final ChatServer server;
    //Writer used to send messages to the client
    private PrintWriter out;
    //Username readed from the client
    private String username;
    //ipAddress to store Client's IP address
    private String ipAddress;
    //isAdmin variable to indicate where the user admin or not
    private boolean isAdmin = false;

//-------------------------------------------------------------------------------------------------------
    //Constructor to store the client's socket and server reference.
    public multiClient(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

//-------------------------------------------------------------------------------------------------------
    //Method to Returns the client's username.
    public String getUsername() {
        return username;
    }

//-------------------------------------------------------------------------------------------------------
    //Method to Sends message to client.
    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

//-------------------------------------------------------------------------------------------------------
    @Override
    public void run() {
        try {
            // Reading the message from the client.
            InputStream in = socket.getInputStream();
            // Converts byte stream to character stream.
            InputStreamReader inr = new InputStreamReader(in);
            // Reads text lines from the client.
            BufferedReader br = new BufferedReader(inr);
            // Sends messages to client 
            out = new PrintWriter(socket.getOutputStream(), true);
            // Prompts client for username.
            out.println("Enter username: ");
            username = br.readLine();

            if (username.startsWith("admin ")) {
                isAdmin = true;
                username = username.substring(6);
                ipAddress = "/" + socket.getInetAddress().getHostAddress();
                server.log("Client connected: " + ipAddress + " (Admin user: " + username + ")");
                out.println("[SERVER] " + username + " joined the chat as ADMIN.");
            } else {
                //Get client's IP address and Log client connection.
                ipAddress = "/" + socket.getInetAddress().getHostAddress();
                server.log("Client connected: " + ipAddress + " (User: " + username + ")");
                //Notify all users that new client joined.
                server.broadcast("[SERVER] " + username + " joined the chat");
            }

            String msg;
            //loop to keep reading messages from client.
            while ((msg = br.readLine()) != null) {
                msg = msg.trim();

                //Command to Sends list of usernames.
                if (msg.equalsIgnoreCase("/users")) {
                    out.println("Connected users: " + String.join(", ", server.getUsernames()));
                    server.log("Command /users requested by " + ipAddress);

                } //Command to Sends count of logged messages.
                else if (msg.equalsIgnoreCase("/log")) {
                    out.println("Total messages logged: " + server.getLogCount());
                    server.log("Command /log requested by " + ipAddress);

                } //Command to  Extracts alert message.
                else if (msg.startsWith("/alert ")) {
                    if (isAdmin) {
                        String alert = msg.substring(7);
                        server.broadcast("ALERT: " + alert);
                        server.log("Alert broadcasted: '" + alert + "'");
                    } else {
                        out.println("You can't use this command, allow used by the ADMIN only!!");
                    }

                } //Exit client command 
                else if (msg.equalsIgnoreCase("/exit")) {
                    break;

                } //close the server by the admin only 
                else if (msg.equalsIgnoreCase("/StopServer")) {
                    if (isAdmin) {
                        server.stop();
                    } else {
                        out.println("You can't use this command, allow used by the ADMIN only!!");
                    }
                } //Otherwise Broadcast the message.
                else {
                    server.broadcast(username + ": " + msg);
                    server.log("Message received from " + username + ": " + msg);
                }
            }

        } catch (IOException e) {
            if (!server.running) {
                return; // To safety exit without error message
            }
            // Print error message if error detected.
            System.out.println("Handling error: " + e.getMessage());
        } finally {
            //Safely close client connection.
            close();
        }
    }

    // To acsses socket outside this class  
    public Socket getSocket() {
        return socket;
    }

//-------------------------------------------------------------------------------------------------------
    private void close() {
        try {
            //To close the connecation remove client from list.
            server.removeClient(this);
            server.broadcast("[SERVER] " + username + " left the chat");
            server.log("Client disconnected: " + ipAddress + " (User: " + username + ")");
            //Close client socket.
            socket.close();
        } catch (IOException e) {
        }
    }

//-------------------------------------------------------------------------------------------------------
}
