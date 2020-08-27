/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zeroserverpi4j;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import static zeroserverpi4j.CommandLanguage.getCommand;

/**
 *
 * @author CMcMichael
 */
public class ZeroServerPi4j {
    
    private static final int MAXCLIENTS = 2;         // Accept no more than 3 Clients
    private static final int PORT4SERVER = 4242;     // Randomish port assignment
    private static final int SERVER_TIMEOUT = 500;   // Throw java.io.InterruptedIOException after 500 ms
    private static int prevNumClients = -1;
    public static String byYourCommand = "";
    public static int numClients = 0;
    public static Set<Socket> clientSocketSet = Collections.synchronizedSet(new HashSet<>(MAXCLIENTS));
    public static Set<String> clientIDSet = Collections.synchronizedSet(new HashSet<>(MAXCLIENTS));
    public static Set<PrintWriter> clientOutputSet = Collections.synchronizedSet(new HashSet<>(MAXCLIENTS));

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setSoTimeout(SERVER_TIMEOUT);
            System.out.println("Testing getGoodIpAddress() method -- sometimes this doesn't work on startup! " + getGoodIpAddress());
            InetSocketAddress myInetSocketAddress = new InetSocketAddress(getGoodIpAddress(), PORT4SERVER);
            serverSocket.bind(myInetSocketAddress);
            System.out.println("Server Socket Address: " + serverSocket.getInetAddress());
            // Keep server alive until a Client sends s SHUTDOWN command
            while (!getCommand().equals("Command_Shutdown")) {
                while (numClients < MAXCLIENTS) {
                    // Accept new clients up to MAXCLIENTS
                    try {
                        executorService.execute(new ClientHandler(serverSocket.accept()));
                    } catch (InterruptedIOException ex) {
                        // This block executes every SERVER_TIMEOUT milliseconds
                        if (numClients != prevNumClients) {
                            prevNumClients = numClients;
                            System.out.println("Number of Clients: " + numClients);
                        }
                        break;
                    }
                }
                Thread.sleep(100);  //Slow down loop for numClients == MAXCLIENTS
            }
            serverSocket.close();
            clientSocketSet.forEach(s -> {
                try {
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(ZeroServerPi4j.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ZeroServerPi4j.class.getName()).log(Level.SEVERE, null, ex);
        }
        executorService.shutdown();
        /*
        //Note: shutdown / awaitTermination do not STOP the ClientHanders thread.
        //  shutdown() just prevents me from launching a new exeutorService
        //  await() just says "execut next line either after thread termination or after 1 MINUTE
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(ZeroServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        // Note: ClientHandler thread keeps running until user enters X.
        System.out.println("Now we're on the other side of the awaitTermination step.");
        */
    }
    
    /**
     * Java's InetAddress.getLocalHost method typically returns the loopback
     * network interface address.  This method looks for an address which...
     *   - is NOT loopback
     *   - IS an IPv4 address
     *   - IS a site local address
     * ...and returns the String version of that address.
     * 
     * If a site local address is not available, try to meet the first two criteria
     * 
     * If the first two criteria can't be met, fall back on InetAddress.getLocalHost
     * 
     * If an exception occurs, return null.
     * This method ignores the potential exceptions.
     * 
     * @return String version of InetAddress or null.
     */
    public static String getGoodIpAddress() {
        try {
            InetAddress potentialAddress = null;
            Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface singleNetworkInterface : Collections.list(allNetworkInterfaces)) {
                Enumeration<InetAddress> allInterfaceAddresses = singleNetworkInterface.getInetAddresses();
                for (InetAddress singleInterfaceAddress : Collections.list(allInterfaceAddresses)) {
                    if (!singleInterfaceAddress.isLoopbackAddress() && singleInterfaceAddress instanceof Inet4Address) {
                        if(singleInterfaceAddress.isSiteLocalAddress()) {
                            // Return the first InetAddress which meets all three criteria
                            //   NOT loopback, IS IPv4, IS site local
                            return singleInterfaceAddress.getHostAddress();
                        } else if (potentialAddress == null) {
                            // Save the first InetAddress which meets two criteria
                            //    NOT loopback, IS IPv4
                            potentialAddress = singleInterfaceAddress;
                        }
                    }
                }
            }
            if (potentialAddress != null) {
                // Return a good guess
                return potentialAddress.getHostAddress();
            } else {
                // If no good guess, try Java's normal method
                return InetAddress.getLocalHost().getHostAddress();
            }
        } catch (SocketException | UnknownHostException ex) {
            Logger.getLogger(ZeroServerPi4j.class.getName()).log(Level.SEVERE, null, ex);
        }
        // If all attempts failed or caused an exception, return null
        return null;
    }
    
}