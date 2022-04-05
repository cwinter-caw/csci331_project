/**
 * The ChatroomHandler class is used to handle the client connections. It is used to listen for
 * messages from the client and to broadcast messages to all the clients
 */
package Client;

import java.io.*;
import java.net.Socket;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

/**
 * When a client connects the server spawns a thread to handle the client.
 * This way the server can handle multiple clients at the same time.
 *
 * This keyword should be used in setters, passing the object as an argument,
 * and to call alternate constructors (a constructor with a different set of
 * arguments.
 * 
 * Runnable is implemented on a class whose instances will be executed by a
 * thread.
 */

public class ChatroomHandler implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String currentChatroom = "MAIN";
    private static String mainChat = "MAIN";
    StringBuilder tempName = new StringBuilder();
    String input = "";

    public static ArrayList<ChatroomHandler> clientList = new ArrayList<ChatroomHandler>();
    // Client socket,username
    public static HashMap<String, ArrayList<String>> chatroom = new HashMap<>();
    // Chatroom names, client usernames
    public static HashMap<String, ArrayList<String>> messageHistory = new HashMap<String, ArrayList<String>>();
    // client usernames, messages.
    public static ArrayList<String> chatroomList = new ArrayList<String>();

    /**
     * When a client connects, their username is sent to the server
     * 
     * @param socket The socket that the client is connected to.
     */
    public ChatroomHandler(Socket socket) {
        try {

            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // When a client connects their username is sent.
            this.clientUsername = bufferedReader.readLine();
            // Add the new client handler to the array so they can receive messages from
            // others.
            clientList.add(this);

            if (chatroom.get(currentChatroom) == null) {
                chatroom.put(currentChatroom, new ArrayList<String>());
            }

            chatroom.get(currentChatroom).add(clientUsername);
            broadcastMessage(clientUsername + " has entered the chat!");
            chatRoomWelcome();

        } catch (IOException e) {

            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * This function shows the client the options available to them
     */
    public void options() {

        showClientMessage("Options:");
        showClientMessage("1) Join a chatroom");
        showClientMessage("2) Create a chatroom");
        showClientMessage("3) Leave chatroom");
        showClientMessage("4) Exit");

    }

    /**
     * It clears the console
     */

    public void clearConsole() {
        showClientMessage("\033[H\033[2J");
        showClientMessage("\033[H\033[2J");
    }

    /**
     * Prints a banner to the client
     */
    public void banner() {

        LocalDate date = LocalDate.now();
        showClientMessage("*****************************************************");
        showClientMessage("*             MY COOL CHAT TOOL Ver 1.0             *");
        showClientMessage("*****************************************************");
        showClientMessage("");
        showClientMessage("Username: " + clientUsername);
        showClientMessage("Chatroom: " + currentChatroom);
        showClientMessage("Date: " + date);
    }

    /**
     * This function is called when a client connects to the server.
     * It reads the client's username and sends it to the other clients.
     * It also sends the client's username to the main chatroom
     *
     * Everything in this method is run on a separate thread. We want to listen for
     * messages
     * on a separate thread because listening (bufferedReader.readLine()) is a
     * blocking operation.
     * A blocking operation input the caller waits for the callee to finish its
     * operation.
     */
    @Override
    public void run() {
        String messageFromClient;

        if (!chatroomList.contains("MAIN")) {

            chatroomList.add(currentChatroom);
        }
        // Continue to listen for messages while a connection with the client is still
        // established.
        while (socket.isConnected()) {
            try {
                // Read what the client sent and then send it to every other client.
                clientChatName();
                messageFromClient = bufferedReader.readLine();

                if (messageHistory.get(clientUsername) == null) {
                    messageHistory.put(clientUsername, new ArrayList<String>());
                }
                messageHistory.get(clientUsername).add(messageFromClient);// client messages
                broadcastMessage(messageFromClient);
                // join create a new chatroom
                if (messageFromClient.equals(clientUsername + ": 1")) {
                    joinChatroom();

                }
                // create a new chatroom
                if (messageFromClient.equals(clientUsername + ": 2")) {
                    if (checkTotalChatrooms() == true)
                        createChatroom();
                }

                if (messageFromClient.equals(clientUsername + ": 3")) {// leave chatroom, sends to main chatroom
                    leaveChatroom();
                }

                if (messageFromClient.equals(clientUsername + ": 4")) {// leave chatroom, sends to main chatroom

                    removeClient();
                }

            } catch (IOException e) {
                break;

            }
        }
        closeEverything(socket, bufferedReader, bufferedWriter);

    }

    /**
     * This function is called when the user wants to leave the chatroom.
     * It removes the client from the chatroom and sends a message to the chatroom
     * that the client
     * has left.
     * It then sets the current chatroom to the main chatroom.
     * It then prints a message to the client that they have left the chatroom.
     * It then calls the chatRoomWelcome function to print the welcome message for
     * the main
     * chatroom.
     */
    public void leaveChatroom() {

        removeFromChatroom();
        chatroom.get(mainChat).add(clientUsername);// sends back to main chat if they leave.
        currentChatroom = mainChat;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        showClientMessage("BYE BYE...");
        chatRoomWelcome();
    }

    /**
     * This function is used to join a chatroom
     */
    public void joinChatroom() {

        showClientMessage("");
        showClientMessage("Chatrooms available: ");
        int j = 1;
        int position = 10;
        // Displaying the chatroom list to the user.
        for (int i = 0; i < chatroomList.size(); i++) {
            if (chatroomList.get(i).equals(currentChatroom)) {
                showClientMessage(j + ") " + chatroomList.get(i) + " (Current chatroom)");
            } else {
                showClientMessage(j + ") " + chatroomList.get(i));
            }

            j++;
        }
        clientChatName();
        // This code is checking to see if the user is already in the chatroom. If they
        // are,
        // it will display a message to the user. If they are not, it will add them to
        // the
        // chatroom and display a message to the user.
        try {
            input = bufferedReader.readLine();
            messageHistory.get(clientUsername).add(input);// client messages
            tempName = new StringBuilder(input);

            tempName.delete(0, clientUsername.length() + 2);
            input = tempName.toString();

            if (!Character.isDigit(input.charAt(0)) && !Character.isDigit(input.charAt(1))) {

                showClientMessage("ERROR: INVALID INPUT CHARACTER ENTERED!");
                Thread.sleep(1000);
            } else {
                position = Integer.parseInt(input) - 1;
            }

            if (position >= chatroomList.size()) {
                showClientMessage("ERROR: INVALID INPUT GREATER THAN TOTAL CHATROOMS!");
                Thread.sleep(1000);
            } else {

                // This code is checking to see if the user is already in the chatroom. If they
                // are,
                // it will display a message to the user. If they are not, it will add them to
                // the
                // chatroom and display a message to the user.
                if (!chatroomList.get(position).equals(currentChatroom)) {
                    removeFromChatroom();
                    addUser(chatroomList.get(position));
                    currentChatroom = chatroomList.get(position);
                    chatRoomWelcome();

                    broadcastMessage(clientUsername + " has entered the chat!");

                } else {
                    showClientMessage("Your already in the " + currentChatroom + "!");
                    showClientMessage("");
                    options();
                }

            }

        } catch (IOException | InterruptedException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * If the total number of chatrooms is 10, then return false
     * 
     * @return A boolean value.
     */

    public Boolean checkTotalChatrooms() {
        if (chatroomList.size() == 10) {
            banner();
            showClientMessage("");
            showClientMessage("MAX TOTAL CHATROOM REACHED");

            return false;
        }

        return true;
    }

    /**
     * This function creates a new chatroom and adds the client to it if they type
     * "y"
     */
    public void createChatroom() {

        showClientMessage("");
        showClientMessage("What would you like to name the chatroom?");
        showClientMessage("");
        try {
            input = bufferedReader.readLine();
            messageHistory.get(clientUsername).add(input);// client messages
            tempName = new StringBuilder(input);
            tempName.delete(0, clientUsername.length() + 2);
            input = tempName.toString();

            if (chatroom.get(input) == null) {

                chatroom.put(input, new ArrayList<String>());
                chatroomList.add(input);
            }

            showClientMessage("New Chatroom " + input + " created!");
            showClientMessage("");
            showClientMessage("Would you like to join? (y/n)");
            showClientMessage("");
            bufferedWriter.flush();
            input = bufferedReader.readLine();

            if (!input.equals(clientUsername + ": y") && !input.equals(clientUsername + ": n")) {
                showClientMessage("ERROR: INVALID INPUT!");
                Thread.sleep(1000);
            } else {
                messageHistory.get(clientUsername).add(input);// client messages

                if (input.equals(clientUsername + ": y")) {

                    chatroom.get(chatroomList.get(chatroom.size() - 1)).add(clientUsername);
                    removeFromChatroom();
                    currentChatroom = chatroomList.get(chatroom.size() - 1);
                    chatRoomWelcome();

                }
                if (input.equals(clientUsername + ": n")) {
                    showClientMessage("Thats ok maybe another time...");
                    Thread.sleep(1000);
                    showClientMessage("");
                    options();
                }
            }

        } catch (IOException | InterruptedException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * Adds the user to the chatroom
     * 
     * @param currentChatroom The name of the chatroom that the user is joining.
     */

    public void addUser(String currentChatroom) {
        if (chatroom.get(currentChatroom) == null)
            chatroom.put(currentChatroom, new ArrayList<String>());

        chatroom.get(currentChatroom).add(clientUsername);

    }

    /**
     * Remove the client from the chatroom
     */
    public void removeFromChatroom() {

        chatroom.get(currentChatroom).remove(clientUsername);
        broadcastMessage("");
        broadcastMessage(clientUsername + " left the chatroom!");
    }

    /**
     * Write a message to the client
     * 
     * @param message The message to be sent to the client.
     */
    public void showClientMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * This function is used to display the client's username in the chat
     */
    public void clientChatName() {
        try {
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.write(clientUsername + "                            [[Enter] to send]");
            bufferedWriter.newLine();
            bufferedWriter.write("----------------------------------------------------");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * This function is called when the client first enters a chatroom. It displays
     * a banner,
     * a welcome message, and a list of options
     */
    public void chatRoomWelcome() {

        clearConsole();
        banner();
        showClientMessage("");
        showClientMessage("           Welcome to the " + currentChatroom + " chatroom!");
        showClientMessage("");
        options();
    }

    /**
     * This function is used to broadcast a message to all the users in the chatroom
     * 
     * @param messageToSend The message that you want to send to the client.
     * @param input         The message that the user typed in.
     */
    public void broadcastMessage(String messageToSend) {

        String one = clientUsername + ": 1";
        String two = clientUsername + ": 2";
        String three = clientUsername + ": 3";
        String four = clientUsername + ": 4";
        String yes = clientUsername + ": y";
        String no = clientUsername + ": n";

        for (ChatroomHandler clientHandler : clientList) {
            try {
                // You don't want to broadcast the message to the user who sent it.`
                if (!clientHandler.clientUsername.equals(clientUsername)
                        && !messageToSend.equals(one)
                        && !messageToSend.equals(two)
                        && !messageToSend.equals(three)
                        && !messageToSend.equals(four)
                        && !messageToSend.equals(yes)
                        && !messageToSend.equals(no)
                        && clientHandler.currentChatroom == currentChatroom) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }

            } catch (IOException e) {

                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    /**
     * Remove the client from the chatroom and broadcast a message to the chatroom
     * that the client has
     * left
     */
    public void removeClient() {

        chatroom.get(currentChatroom).remove(clientUsername);
        clientList.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    /**
     * Close the socket, the reader and the writer
     * 
     * @param socket         The socket that is being closed.
     * @param bufferedReader The BufferedReader that is used to read the incoming
     *                       messages from the
     *                       client.
     * @param bufferedWriter The writer to send messages to the client.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        removeClient();
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
