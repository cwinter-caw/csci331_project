package Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import Client.Client;

/**
 * The main method is the entry point of the program. It is the first method
 * that is executed when
 * the program is run
 */

public class Main {
    /**
     * Prints a welcome message to the user
     */

    static void welcome() {

        System.out.println("*****************************************************");
        System.out.println("*        WELCOME TO MY COOL CHAT TOOL Ver 1.0       *");
        System.out.println("*****************************************************");
        System.out.println();
    }

    /**
     * Clear the console screen
     */
    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Prints a string of dots to the console
     */

    public static void connecting() {
        System.out.print(("Connecting "));
        ArrayList<String> connect = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(300);
                connect.add(".");
                System.out.print(connect.get(i));

            }
            System.out.println("");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // The main method is the entry point of the program. It is the first method
    // that is executed when
    // the program is run.
    public static void main(String[] args) throws IOException, InterruptedException {

        clearConsole();
        welcome();

        String username;
        @SuppressWarnings("resource")
        Scanner s = new Scanner(System.in);
        if (args.length >= 1) {
            username = args[0];
        } else {
            System.out.print("Enter your username for the group chat: ");

            username = s.nextLine();
        }
        System.out.println("");

        connecting();

        // Create a socket to connect to the server.
        Socket socket = new Socket("localhost", 1234);

        // Pass the socket and give the client a username.
        Client client = new Client(socket, username);

        client.listenForMessage();
        client.sendMessage();

    }
}
