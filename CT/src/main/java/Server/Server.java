package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import Client.ChatroomHandler;

/**
 * The server accepts a connection from a client and creates a new thread to
 * handle the connection
 */
public class Server {

    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * The server accepts a connection from a client and creates a new thread to
     * handle the connection
     */
    public void startServer() {
        try {
            // Listen for connections (clients to connect) on port 1234.
            while (!serverSocket.isClosed()) {
                // Will be closed in the Client Handler.
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                ChatroomHandler clientHandler = new ChatroomHandler(socket);
                Thread thread = new Thread(clientHandler);
                // The start method begins the execution of a thread.
                // When you call start() the run method is called.
                // The operating system schedules the threads.
                thread.start();

                Date datePlus10 = new Date(0);

                LocalDate date = LocalDate.now();
                int futureDate = date.getDayOfMonth()+10;
           // This is checking if the current date is equal to the date in 10 days. If it is equal,
           // it will remove the client from the chatroom.
                if(date.getDayOfMonth() == futureDate)
                {
                    clientHandler.removeClient();
                }


            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    // Close the server socket gracefully.
    /**
     * Close the server socket
     */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * It starts the server.
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}
