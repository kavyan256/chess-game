import java.io.*;
import java.net.*;
import java.util.*;

// Handles online multiplayer over LAN
// One person hosts, the other joins
public class NetworkManager extends Thread {
    private boolean isHost;
    private ChessGUI gui;
    private String host;
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = true;
    
    private static final int PORT = 5555;  // port number for connection
    
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
            
            // Keep listening for moves from the other player
            String move;
            while (running && (move = in.readLine()) != null) {
                final String receivedMove = move;
                gui.applyNetworkMove(receivedMove);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Network error: " + e.getMessage());
                showError(e);
            }
        } finally {
            close();
        }
    }
    
    private void startServer() throws IOException {
        // Allow reuse of address to prevent "Address already in use" error
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(PORT));
        
        String myIP = getLocalIP();
        System.out.println("Server started! Your IP: " + myIP);
        System.out.println("Waiting for opponent to join...");
        
        // Show dialog with IP so user knows what to share
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Waiting for opponent...\n\n" +
                "Share this IP with your opponent:\n" +
                myIP + "\n\n" +
                "Example: If IP is 192.168.43.x, they enter that when joining",
                "Hosting Game - Port " + PORT,
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
        
        socket = serverSocket.accept();
        System.out.println("Opponent connected!");
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Opponent connected! Game starting...",
                "Connected!",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
        
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    private void connectToServer() throws IOException {
        System.out.println("Connecting to " + host + ":" + PORT);
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, PORT), 10000); // 10 sec timeout
        System.out.println("Connected to host!");
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Connected successfully!",
                "Connected!",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
        
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    // Show useful error messages
    private void showError(IOException e) {
        String msg = "Connection failed!\n\n";
        if (e instanceof ConnectException || e instanceof SocketTimeoutException) {
            msg += "Can't reach the host. Make sure:\n" +
                   "• Host clicked 'Host Game' first\n" +
                   "• Both on same WiFi network\n" +
                   "• Entered correct IP (like 192.168.1.105)\n" +
                   "• No VPN/Proxy running";
        } else {
            msg += "Error: " + e.getMessage();
        }
        
        final String errorMsg = msg;
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, errorMsg,
                "Connection Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        });
    }
    
    // Send a move to the other player (like "e2e4")
    public void sendMove(String move) {
        if (out != null) {
            out.println(move);
        }
    }
    
    // Get your computer's ACTUAL IP address (not 127.0.0.1)
    // Should return something like 192.168.1.105 or 192.168.43.x for hotspot
    public static String getLocalIP() {
        String bestIP = null;
        
        try {
            // Look through all network interfaces to find the right one
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // Skip loopback (127.0.0.1) and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                
                // Print interface name for debugging
                String ifaceName = iface.getName().toLowerCase();
                System.out.println("Checking interface: " + ifaceName + " - " + iface.getDisplayName());
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // We want IPv4 addresses only, not IPv6
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        System.out.println("  Found IP: " + ip);
                        
                        // Prioritize these IP ranges (common for hotspots and WiFi)
                        // 192.168.43.x is common for mobile hotspots
                        // 192.168.x.x is common for regular WiFi
                        if (ip.startsWith("192.168.")) {
                            System.out.println("  >>> Using this IP!");
                            return ip; // Most common, return immediately
                        } else if (ip.startsWith("10.") || ip.startsWith("172.")) {
                            bestIP = ip; // Good backup option
                        } else if (bestIP == null && !ip.startsWith("169.254.")) {
                            bestIP = ip; // Any non-link-local IP is better than nothing
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding IP: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Return the best IP we found
        if (bestIP != null) {
            System.out.println("Using best IP found: " + bestIP);
            return bestIP;
        }
        
        // Last resort - this might still return 127.0.0.1
        try {
            String fallbackIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Using fallback IP: " + fallbackIP);
            if (!fallbackIP.startsWith("127.")) {
                return fallbackIP;
            }
            return fallbackIP + " (WARNING: This is localhost - won't work for LAN!)";
        } catch (UnknownHostException e) {
            return "IP Not Found - Are you connected to the hotspot?";
        }
    }
    
    // Close all network connections
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
