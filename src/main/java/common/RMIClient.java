package common;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMIClient extends Remote {
    void notify(String user, String file) throws RemoteException;
}
