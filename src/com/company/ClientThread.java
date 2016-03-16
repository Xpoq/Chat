package com.company;

/**
 * Created by Jasmin on 2016-02-16.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread implements Runnable {

    // hantering av användarnamn
    Vector<String> users = new Vector<String>();
    Socket client;
    String clientId = "";
    private InputStream in;
    private OutputStream out;
    private Set<OutputStream> outputStreams = new HashSet<OutputStream>();
    public ClientThread(Socket socket) {
        this.client = socket;
    }
    /*
    void sendAll(String message) {
        synchronized (outputStreams) {
            for (Enumeration e = getOutputStreams(); e.hasMoreElements();){
            }
        }
    }
    */
    @Override
    public void run() {
        try {
            clientId = client.getRemoteSocketAddress().toString();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
            //outputStreams.add(out);

            // TODO eventuellt byta hur username hanteras.. för att undvika 'NICK användarnamn'
            String userName;
            while (true) {
                out.write("NICK?\r\n");
                out.flush();
                userName = in.readLine();
                users.add(userName);
                if (userName.startsWith("NICK ")) {
                    out.write("Welcome " + userName.substring(5) + "!\r\n");
                    out.flush();
                    break;

                } else {
                    out.write("Error: NICK, try 'NICK namehere'\r\n");
                    out.flush();
                }
            }

            while (true) {
                if (userName.startsWith("NICK ")) {
                    /*int c;
                    while ((c = in.read()) > 0) {
                        for (OutputStream o : outputStreams){
                            o.write(c);
                        }
                    }*/
                    String message = in.readLine();
                    out.write(userName.substring(5) + ": " + message + "\r\n");
                    out.flush();

                    if (message.matches("!help")) {
                        out.write("Commands: \r\n" +
                                "!help - command list\r\n" +
                                "!nickedit - change username\r\n" +
                                "!quit - disconnect from the server\r\n");
                        out.flush();
                    }

                    if (message.matches("!nickedit")) {
                        users.remove(userName);
                        out.write("Type 'NICK namehere' to change username\r\n");
                        out.flush();
                        userName = in.readLine();
                        users.add(userName);

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