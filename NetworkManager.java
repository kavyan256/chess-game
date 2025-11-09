import java.io.*;
import java.net.*;

/**
 * NetworkManager - Handles LAN multiplayer functionality
 * Supports both hosting (server) and joining (client) modes
 */
public class NetworkManager extends Thread {
    private boolean isHost;
    private ChessGUI gui;
    private String host;
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = true;
    
    private static final int PORT = 5555;
    
    public NetworkManager(boolean isHost, ChessGUI gui) {
        this.isHost = isHost;
        this.gui = gui;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    @Override
    public void run() {
        try {
            if (isHost) {
                startServer();
            } else {
                connectToServer();
            }
            
            // Listen for incoming moves
            String move;
            while (running && (move = in.readLine()) != null) {
                final String receivedMove = move;
                gui.applyNetworkMove(receivedMove);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Network error: " + e.getMessage());
            }
        } finally {
            close();
        }
    }
    
    private void startServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Waiting for opponent to join...");
        socket = serverSocket.accept();
        System.out.println("Opponent connected!");
        
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    private void connectToServer() throws IOException {
        System.out.println("Connecting to " + host + ":" + PORT);
        socket = new Socket(host, PORT);
        System.out.println("Connected to host!");
        
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    /**
     * Send a move to the opponent
     * @param move Move in format "e2e4"
     */
    public void sendMove(String move) {
        if (out != null) {
            out.println(move);
        }
    }
    
    /**
     * Get the local IP address
     */
    public static String getLocalIP() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }
    
    /**
     * Close all network connections
     */
    public void close() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }
}
