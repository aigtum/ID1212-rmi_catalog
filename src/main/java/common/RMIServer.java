package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServer extends Remote {

    String SERVER_URI = "FILE_SERVER";

    // Register user
    String register(RMIClient client, RMICredentials credentials) throws RemoteException;

    // Login with registered user
    String login(RMIClient client, RMICredentials credentials) throws RemoteException;

    // Logout logged in user
    String logout(RMIClient client) throws RemoteException;

    // Upload a RMIFile to the server
    String upload(RMIClient client, RMIFile RMIFile) throws RemoteException;

    // Download a file from the server
    String download(RMIClient client, String filename) throws RemoteException;

    // Delete a file from the server
    String delete(RMIClient client, String filename) throws RemoteException;

    // List files on the server
    String listFiles(RMIClient client) throws RemoteException;

    // Notify user about accessed files
    String notifyUser(RMIClient client, String filename) throws RemoteException;
}
