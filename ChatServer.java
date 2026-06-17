package group2_rtm_fall2025;
// code source :
// https://github.com/Het-Joshi/Client-Server-Communication-Simulator
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatServer {

//-------------------------------------------------------------------------------------------------------
    //The main server socket that accepts incoming client connections.
    private final ServerSocket serverSocket;
    //A thread-safe list holding all connected clients.
    private final List<multiClient> clients = Collections.synchronizedList(new ArrayList<>());
    //logFile where server logs are stored.
    private final File logFile = new File("log.txt");
    //running variable to idicate the server is running or termenate 
    public boolean running = true;

//-------------------------------------------------------------------------------------------------------
    //Constructor that initializes the server.
    public ChatServer(int port) throws Exception {
        //Create server socket to listen on given port.
        serverSocket = new ServerSocket(port);
        // start writing on a clean log file
        if (logFile.exists()) {
            PrintWriter pw = new PrintWriter(logFile);
            pw.close();
        }
        //Log message that server has started.
        log("Server started and listening on port " + port);
        //Print STARTED message to console.
        System.out.println("SERVER STARTED...");
    }

//-------------------------------------------------------------------------------------------------------
    // start() method that the server starts listening and accepts clients
    public void start() {
        try {
            //Infinite loop to continuously accept new clients.
            while (true) {
                // create a socket for the each client
                Socket socket = serverSocket.accept();
                // create a thread for each socket
                multiClient ch = new multiClient(socket, this);
                //Add the client to the list.
                clients.add(ch);
                //Start client thread.
                ch.start();
            }
        } catch (IOException e) {

        }
    }

//-------------------------------------------------------------------------------------------------------
    // broadcast method to Send a message to all connected clients.
    public void broadcast(String msg) {
        // Send message to each client.
        synchronized (clients) {
            for (multiClient c : clients) {
                c.send(msg);
            }
        }
    }

//-------------------------------------------------------------------------------------------------------    
    //removeClient method to Remove a client when they disconnect.
    public void removeClient(multiClient c) {
        clients.remove(c);
    }

//-------------------------------------------------------------------------------------------------------
    //getUsernames method to Returns list of user names.
    public List<String> getUsernames() {
        List<String> names = new ArrayList<>();
        synchronized (clients) {
            for (multiClient c : clients) {
                //Add client username to the list.
                names.add(c.getUsername());
            }
        }
        return names;
    }

//-------------------------------------------------------------------------------------------------------   
    // getLogCount method to Counts the number of lines inside the log file.
    public int getLogCount() throws FileNotFoundException, IOException {
        int count = 0;
        FileReader read = new FileReader(logFile);
        BufferedReader br = new BufferedReader(read);
        while (br.readLine() != null) {
            count++;
        }
        return count;
    }

//-------------------------------------------------------------------------------------------------------
    //log method to Write a log entry in a thread-safe way.
    public synchronized void log(String entry) {
        // synchronized is added to safely deal with multiple threads when writing on the file 
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            pw.println("[" + new Date() + "] " + entry);
        } catch (Exception e) {
            System.out.println("LOG ERROR: " + e.getMessage());
        }
    }

//-------------------------------------------------------------------------------------------------------
    public void stop() {
        running = false;
        try {
            log("Server is shutting down...");
            System.out.println("SERVER SHUTTING DOWN...");

            // Close all client connections
            synchronized (clients) {
                for (multiClient c : clients) {
                    c.send("Server is shutting down...");
                    c.getSocket().close();
                }
                clients.clear();
            }
            serverSocket.close();  // Stop listening
        } catch (Exception e) {
            System.out.println("Error detected: " + e.getMessage());
        }
    }

//-------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        // Entry point of the program.
        // if we are using two device we use port 5050.
        //if one device only port is 5000.
        ChatServer server = new ChatServer(5000);
        //Start the server.
        server.start();
    }
}
