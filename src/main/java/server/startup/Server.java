package server.startup;

import server.controller.Controller;
import server.integrations.FileSystemDAO;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

public class Server {
    public static void main(String[] args) {
        try {
            Server server = new Server();
            FileSystemDAO fileSystemDAO = new FileSystemDAO();
            server.startRegistry();
            Naming.rebind("FILE_SERVER", new Controller(fileSystemDAO));
            System.out.println("The server is running");
        } catch (RemoteException | MalformedURLException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startRegistry() throws RemoteException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
    }

}
