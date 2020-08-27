/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zeroserverpi4j;

import java.io.PrintWriter;
import java.net.Socket;
import static zeroserverpi4j.ZeroServerPi4j.numClients;
import static zeroserverpi4j.ZeroServerPi4j.byYourCommand;
import static zeroserverpi4j.ZeroServerPi4j.clientSocketSet;
import static zeroserverpi4j.ZeroServerPi4j.clientIDSet;
import static zeroserverpi4j.ZeroServerPi4j.clientOutputSet;


/**
 *
 * @author CMcMichael
 */
public class CommandLanguage {
    
    /**
     *
     * @param newCommand is a String sent by a Client
     */
    public synchronized static void setCommand(String newCommand) {
        byYourCommand = newCommand;
    }
    
    public synchronized static String getCommand() {
        return byYourCommand;
    }
    
    public synchronized static Boolean addClient(Socket newSocket, PrintWriter newOutput, String newID) {
        if (newID == null) {
            System.out.println("null?");
            return false;
        }
        if (newID.equals("")) { //.isBlank() caused NoSuchMethodError
            System.out.println("blank!");
            return false;
        }
        if (!clientIDSet.contains(newID)) {
            clientIDSet.add(newID);
            clientSocketSet.add(newSocket);
            for (PrintWriter oneOutput : clientOutputSet) {
                    oneOutput.println("To_Everyone [" + newID + "] has entered the chat room.");
                }
            newOutput.println("ID_Confirmed Welcome to the chat, [" + newID + "].");
            clientOutputSet.add(newOutput);
            numClients = numClients + 1;
            System.out.println("Client [" + newID + "] added at " + newSocket + ".");
        }
        return true;
    }
    
    public synchronized static void removeClient(Socket oldSocket, PrintWriter oldOutput, String oldID) {
        for (PrintWriter oneOutput : clientOutputSet) {
                    oneOutput.println("To_Everyone [" + oldID + "] has departed.");
                    if (getCommand().equals("Command_Shutdown")) {
                        oneOutput.println("Command_Shutdown");
                    }
                }
        oldOutput.println("Exit_Confirmed Nice know you, [" + oldID + "].");
        clientIDSet.remove(oldID);
        clientSocketSet.remove(oldSocket);
        clientOutputSet.remove(oldOutput);
        numClients = numClients -1;
        System.out.println("Client [" + oldID + "] has retired.");
    }
    
    public static void broadcastMessage(String clientID, PrintWriter clientOutput, String theMessage) {
        for (PrintWriter oneOutput : clientOutputSet) {
            if (oneOutput != clientOutput) {
                oneOutput.println("Normal_Text Message from [" + clientID + "]: " + theMessage);
            } else {
                oneOutput.println("Echo_Text " + theMessage);
            }
        }
        
    }
    
}
