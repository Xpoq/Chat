package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread implements Runnable {

    Socket client;
    String clientId = "";
    private String userName;
    private BufferedReader in;
    private PrintWriter out;
    private static HashSet<String> names = new HashSet<>();
    private static HashSet<PrintWriter> outputStreams = new HashSet<>();
    public ClientThread(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {
        try {
            clientId = client.getRemoteSocketAddress().toString();

            in = new BufferedReader(new InputStreamReader(
                    client.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(
                    client.getOutputStream(), "UTF-8"), true);

            // TODO eventuellt byta hur username hanteras.. för att undvika 'NICK användarnamn'
            while (true) {
                out.format("NICK?\r\n");
                userName = in.readLine();
                if (userName == null) {
                    return;
                }
                if (!userName.startsWith("NICK ")) {
                    continue;
                }
                userName = userName.substring(5);
                synchronized (names) {
                    if (!names.contains(userName)) {
                        names.add(userName);
                        break;
                    }
                }
                // Name taken
                out.format("ERROR nick already taken\r\n");
            }
            while (true) {
                //if (userName.startsWith("NICK ")) {

                String message = in.readLine();
                for (PrintWriter writer : outputStreams){
                    writer.format("MESSAGE %s: %s\r\n", userName, message);
                    System.out.println("Message: " + message);

                    //out.write(userName.substring(5) + ": " + message + "\r\n");
                    out.flush();

                    // TODO kanske behövs ändra.
                    if (message.matches("!help")) {
                        out.write("Commands: \r\n" +
                                "!help - command list\r\n" +
                                "!nickedit - change username\r\n" +
                                "!quit - disconnect from the server\r\n");
                        out.flush();
                    }

                    if (message.matches("!nickedit")) {
                        out.write("Type 'NICK namehere' to change username\r\n");
                        out.flush();
                        userName = in.readLine();

                        if (userName.startsWith("NICK ")) {
                            out.write("Username changed to: " + userName.substring(5) + "\r\n");
                            out.flush();
                        } else {
                            out.write("Error: NICK, try 'NICK namehere'\r\n");
                            out.flush();
                        }
                    }

                    if (message.matches("!quit")) {
                        client.close();
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
                System.out.printf("Client disconnected: %s\n", clientId);

            } catch (IOException e){
                System.out.println("Error: close\r\n");
            }
        }
    }
}