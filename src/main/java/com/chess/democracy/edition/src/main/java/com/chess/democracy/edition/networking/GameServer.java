package com.chess.democracy.edition.networking;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    // Map of gameID to list of client handlers
    private static Map<Integer, Set<ClientHandler>> gameClients = new ConcurrentHashMap<>();

    private static GameServer instance = null;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private boolean isRunning = false;

    // Private constructor to prevent instantiation
    private GameServer(String ipAddress, int port) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(ipAddress, port));
        System.out.println("Game server started on " + ipAddress + ":" + port);
        executor = Executors.newCachedThreadPool();
        acceptClients();
        isRunning = true;
    }

    /**
     * Returns the single instance of GameServer. If it doesn't exist, creates one.
     *
     * @param ipAddress the IP address to bind the server.
     * @param port      the port number to bind the server.
     * @return the single instance of GameServer.
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public static synchronized GameServer getInstance(String ipAddress, int port) throws IOException {
        if (instance == null) {
            instance = new GameServer(ipAddress, port);
        } else {
            System.out.println("Game server is already running on " + instance.getIPAddress() + ":" + instance.getPort());
        }
        return instance;
    }

    /**
     * Retrieves the current instance of GameServer.
     *
     * @return the current instance of GameServer, or null if not initialized.
     */
    public static synchronized GameServer getInstance() {
        return instance;
    }

    private void acceptClients() {
        executor.execute(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Stops the server gracefully.
     */
    public synchronized void stopServer() {
        if (isRunning) {
            try {
                serverSocket.close();
                executor.shutdownNow();
                System.out.println("Game server stopped.");
                instance = null; // Allow future restarts
                isRunning = false;
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        } else {
            System.out.println("Game server is not running.");
        }
    }

    /**
     * Retrieves the IP address the server is bound to.
     *
     * @return the IP address as a String.
     */
    public String getIPAddress() {
        return serverSocket.getInetAddress().getHostAddress();
    }

    /**
     * Retrieves the port number the server is bound to.
     *
     * @return the port number as an int.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private int gameID;
        private String userName;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Setup I/O streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // First, read gameID and userName from the client
                String initInfo = in.readLine();
                String[] parts = initInfo.split(":", 2);
                if (parts.length < 2) {
                    socket.close();
                    return;
                }
                gameID = Integer.parseInt(parts[0]);
                userName = parts[1];

                // Add this client to the gameClients map
                gameClients.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(this);

                // Notify others that a new user has joined
                broadcastMessage("System", userName + " has joined the chat.");

                // Read messages from the client and broadcast them
                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(userName, message);
                }
            } catch (IOException e) {
                System.err.println("Connection error with client: " + e.getMessage());
            } finally {
                // Remove client from the gameClients map
                Set<ClientHandler> clients = gameClients.get(gameID);
                if (clients != null) {
                    clients.remove(this);
                    if (clients.isEmpty()) {
                        gameClients.remove(gameID);
                    }
                }
                broadcastMessage("System", userName + " has left the chat.");
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        private void broadcastMessage(String sender, String message) {
            String fullMessage = sender + ": " + message;
            Set<ClientHandler> clients = gameClients.get(gameID);
            if (clients != null) {
                for (ClientHandler client : clients) {
                    client.sendMessage(fullMessage);
                }
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }
    }
}
