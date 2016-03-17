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
                out.format("ERROR: nick already taken\r\n");
            }
            out.format("NICK OK\r\n");
            outputStreams.add(out);

            while (true) {
                String message = in.readLine();
                for (PrintWriter type : outputStreams){
                    type.format("MESSAGE %s: %s\r\n", userName, message);

                    // TODO kanske behövs ändra.
                    if (message.matches("!help")) {
                        out.format("Commands: \r\n" +
                                "!help - command list\r\n" +
                                "!nickedit - change username\r\n" +
                                "!quit - disconnect from the server\r\n");
                    }

                    // TODO kan behöva förkorta/ändra för att unvika dubbel kod.
                    if (message.matches("!nickedit")) {
                        while (true) {
                            out.format("Type 'NICK namehere' to change username\r\n");
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
                            out.format("ERROR: nick already taken\r\n");
                        }
                        out.format("Username changed to: " + userName + "\r\n");

                    }
                    // TODO fixa hur det hanteras.. så användare tas bort som finally blocket
                    if (message.matches("!quit")) {
                        client.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                outputStreams.remove(out);
            }
            if (userName != null) {
                names.remove(userName);

                for (PrintWriter type : outputStreams) {
                    type.format("QUIT %s\r\n", userName);
                }
            }
            try {
                client.close();
                System.out.printf("Client disconnected: %s\n", clientId);

            } catch (IOException e){
                System.out.println("ERROR: close\r\n");
            }
        }
    }
}