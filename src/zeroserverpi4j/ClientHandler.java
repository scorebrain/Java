/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zeroserverpi4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import static zeroserverpi4j.CommandLanguage.setCommand;
import static zeroserverpi4j.CommandLanguage.getCommand;
import static zeroserverpi4j.CommandLanguage.addClient;
import static zeroserverpi4j.CommandLanguage.removeClient;
import static zeroserverpi4j.CommandLanguage.broadcastMessage;

public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private String clientID;
    private Scanner inFromSocket;
    private PrintWriter outToSocket;
    
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
        
        System.out.println("Connecting with a new client....");
        try {
            inFromSocket = new Scanner(clientSocket.getInputStream());
            outToSocket = new PrintWriter(clientSocket.getOutputStream(), true);    // That TRUE=flush is important!
            // Send a special tag requesting an ID from the new Client
            // Then look for a response.
            outToSocket.println("ID_Request");
            clientID = inFromSocket.nextLine();
            if (addClient(clientSocket, outToSocket, clientID)) {
                //Accept messages from this client and broadcast them.
                //A SHUTDOWN command from ANY Client shuts down everything
                SerialStuff serialStuff = new SerialStuff();
                ExecutorService executorService = Executors.newCachedThreadPool();
                executorService.execute(serialStuff);
                while (!getCommand().equals("Command_Shutdown")) {
                    try {
                        String input = inFromSocket.nextLine();       //Throws NoSuchElementException if Client disappears
                        System.out.println("Message from Client: " + input);
                        broadcastMessage(clientID, outToSocket, input);
                        if (input.equals("Command_Shutdown") || input.equals("Command_Disconnect")) {
                            setCommand(input);
                            serialStuff.stopIt();
                            return;
                        } else if (input.startsWith("mp")) {
                            int mpNumberInput = Integer.parseInt(input.substring(2));
                            serialStuff.mpNumber(mpNumberInput);
                        }
                    } catch (NoSuchElementException ex) {
                        // Client has been shut down on the other side.
                        System.out.println("Missed the desktop shutdown when I closed the FX window....");
                        serialStuff.stopIt();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ZeroServerPi4j.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            System.out.println("Closing client.... " + getCommand());
            removeClient(clientSocket, outToSocket, clientID);           
            try { clientSocket.close(); } catch (IOException e) {}
        }
    }
}
