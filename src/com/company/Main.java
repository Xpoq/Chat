package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {

    private static final int PORT = 1337;
    private Set<OutputStream> outputStreams = new HashSet<OutputStream>();

    public static void main(String[] args) {
        Main m = new Main();
        m.runServer();
    }
    public void runServer() {
        try  {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started.");
            for (;;) {
                    Socket client = serverSocket.accept();
                    System.out.println("Client connected: " + client.getRemoteSocketAddress());
                    Thread t = new Thread(new ClientThread(client));
                    t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}