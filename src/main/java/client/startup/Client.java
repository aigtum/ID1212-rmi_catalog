package client.startup;

import client.Controller.Controller;
import client.view.UserInterface;

import java.rmi.RemoteException;

public class Client {

    public static void main(String[] args) {
        try {
            Controller controller = new Controller();
            new UserInterface(controller);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
