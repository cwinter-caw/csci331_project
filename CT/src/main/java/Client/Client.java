package Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * A client has a socket to connect to the server and a reader and writer to
 * receive and send
 * messages respectively
 */

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    // It initializes the socket, username, bufferedReader, and bufferedWriter.
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * It takes in a socket, a BufferedReader and a BufferedWriter, and then scans
     * the terminal for
     * user input and then sends the message to the server
     */
    public void sendMessage() {
        try {
            // Initially send the username of the client.
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // Create a scanner for user input.
            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(System.in);
            // While there is still a connection with the server, continue to scan the
            // terminal and then send the message.
            while (socket.isConnected()) {

                String messageToSend = scanner.nextLine();
                
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                if (messageToSend.equals("4")) {// leave chatroom, sends to main chatroom
                    
                    break;
                }
            }
        } catch (IOException e) {

            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        
        closeEverything(socket, bufferedReader, bufferedWriter);
    }

    // This is the code that runs on a separate thread to listen for messages from
    // the server.
    public void listenForMessage() {// set to string
        new Thread(new Runnable() {
            public void run() {
                String msgFromGroupChat;
                // While there is still a connection with the server, continue to listen for
                // messages on a separate thread.
                while (socket.isConnected()) {
                    try {
                        // Get the messages sent from other users and print it to the console.
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {

                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    /**
     * Close everything
     * 
     * @param socket         The socket that you want to close.
     * @param bufferedReader The BufferedReader that is used to read the incoming
     *                       data.
     * @param bufferedWriter The writer to which you want to write the message.
     */

    public int closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        try {

            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {

                socket.close();
            }
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;

    }
}
