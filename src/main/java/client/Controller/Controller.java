package client.Controller;


import common.RMIClient;
import common.RMICredentials;
import common.RMIServer;
import common.RMIFile;

import java.io.File;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Handles communication between view and server.
 */
public class Controller {

    private RMIServer server;
    private final RMIClient client;

    private boolean connected = false;
    private boolean loggedIn = false;

    private final Map<String, Set<AccessListener>> accessListeners;


    public Controller() throws RemoteException{
        accessListeners = new HashMap<>();
        client = new ClientController();
    }

    public String addAccessListener(String file, AccessListener listener) throws RemoteException {
        Set<AccessListener> listeners = accessListeners.get(file);
        if(listeners != null) {
            listeners.add(listener);
        }
        else {
            listeners = new HashSet<>();
            listeners.add(listener);
            accessListeners.put(file, listeners);
        }
        return server.notifyUser(client, file);
    }

    public boolean connect(String host) {
        try {
            server = (RMIServer)Naming.lookup("//" + host + "/" + RMIServer.SERVER_URI);
            connected = true;
            return true;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean disconnect() {
        connected = false;
        try {
            UnicastRemoteObject.unexportObject(server, false);
            return true;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        finally {
            server = null;
        }
    }

    public String login(String username, String password) throws RemoteException {
        RMICredentials RMICredentials = new RMICredentials(username, password);
        return server.login(client, RMICredentials);
    }

    public String logout() throws RemoteException {
        return server.logout(client);
    }

    public String register(String username, String password) throws RemoteException {
        RMICredentials RMICredentials = new RMICredentials(username, password);
        return server.register(client, RMICredentials);
    }

    public String upload(String filename, boolean isPublic, boolean isReadonly) throws RemoteException {
        File file = new File(filename);
        System.out.println(file.getAbsolutePath());
        if(file.exists()) {
            RMIFile rmiFile;
            if(isPublic) {
                rmiFile = new RMIFile(filename, file.length(), isPublic, isReadonly);
            }
            else {
                rmiFile = new RMIFile(filename, file.length());
            }
            return server.upload(client, rmiFile);
        }
        else {
            return "Error, no such file.";
        }
    }

    public String download(String filename) throws RemoteException {
        return server.download(client, filename);
    }

    public String delete(String filename) throws RemoteException {
        return server.delete(client, filename);
    }

    public String list() throws RemoteException {
        return server.listFiles(client);
    }

    private class ClientController extends UnicastRemoteObject implements RMIClient {
        ClientController() throws RemoteException {
        }

        @Override
        public void notify(String user, String file) throws RemoteException {
            Set<AccessListener> listeners = accessListeners.get(file);
            if(listeners != null) {
                for(AccessListener listener : listeners) {
                    listener.notifyAccess(user, file);
                }
            }
        }


    }
}
