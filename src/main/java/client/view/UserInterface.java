package client.view;

import client.Controller.AccessListener;
import client.Controller.Controller;

import java.rmi.RemoteException;
import java.util.Scanner;

public class UserInterface implements AccessListener, Runnable {

    private final Controller controller;
    Scanner cs;

    public UserInterface(Controller controller) {
        this.controller = controller;
        cs = new Scanner(System.in);
        new Thread(this).start();
    }

    @Override
    public void run() {
        welcomeMessage();
        while (true) {
            String command = cs.nextLine();
            try {
                handleInput(command);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private void print(String s) {
        System.out.println(s);
    }

    private String getInput(String message) {
        System.out.print(message + ": ");
        return cs.nextLine();
    }

    private boolean getBoolInput(String message) {
        System.out.print(message + "(y/n): ");
        String choice = cs.nextLine();
        switch (choice) {
            case "y":
                return true;
            case "n":
                return false;
            default:
                print("Input not recognized, try again.");
                getBoolInput(message);
                break;
        }
        return false;
    }

    public void welcomeMessage() {
        print("Welcome to RMI Catalogue!");
        print("To connect type in: 'connect'");
        print("Type 'help' to see the list of available commands");
    }

    public void handleInput(String input) throws RemoteException {
        switch (input) {
            case "help":
                help();
                break;
            case "connect":
                String host = getInput("Host");
                connect(host);
                break;
            case "disconnect":
                disconnect();
                break;
            case "register":
                print("Register a new user");
                print("-------------------");
                String regUser = getInput("Username");
                String regPw = getInput("Password");
                register(regUser, regPw);
                break;
            case "login":
                print("Login");
                print("-----");
                String logUser = getInput("Username");
                String logPw = getInput("Password");
                login(logUser, logPw);
                break;
            case "logout":
                logout();
                break;
            case "download":
                print("Download a file");
                print("---------------");
                String dFile = getInput("Filename");
                download(dFile);
                break;
            case "upload":
                print("Upload a file");
                print("-------------");
                String uFile = getInput("Filename");
                boolean pub = getBoolInput("Public");
                boolean rdo = getBoolInput("Read Only");
                upload(uFile, pub, rdo);
                break;
            case "delete":
                print("Delete a file");
                print("-------------");
                String delFile = getInput("Filename");
                delete(delFile);
                break;
            case "notify":
                print("Get file access notification");
                print("----------------------------");
                String nFile = getInput("Filename");
                notify(nFile);
                break;
            case "list":
                print("List of files");
                print("-------------");
                list();
                break;
            default:
                System.err.println("Command not understood");
        }
    }

    private void help() {
        print("Following commands are available:");
        print("connect, disconnect, register, login, logout");
        print("download, upload, delete, notify, list");
    }

    private void connect(String host) {
        if (host == null) {
            print("No host input");
        } else {
            if(controller.connect(host)) {
                print("You need to 'login' or 'register'");
            } else {
                print("Error connecting to the server");
            }
        }
    }

    private void disconnect() {
        if (controller.disconnect()) {
            print("Disconnected from the server");
        } else {
            print("Error disconnecting");
        }
    }

    private void register(String username, String password) throws RemoteException {
        //print(controller.register(username, password));
        String res = controller.register(username, password);

        switch (res) {
            case "1":
                print("Account with username " + username + " already exists");
                break;
            case "2":
                print("Account with username " + username + " created");
                break;
            case "3":
                print("Account creation has encountered an error");
                break;
        }
    }

    private void login(String username, String password) throws RemoteException {
        //print(controller.login(username, password));
        String res = controller.login(username, password);

        switch (res) {
            case "1":
                print("You are now logged in as " + username);
                break;
            case "2":
                print("Wrong username or password");
                break;
            case "3":
                print("Could not find this account");
                break;
            case "4":
                print("An error occurred while trying to find the account");
                break;
        }
    }

    private void logout() throws RemoteException {
        //print(controller.logout());
        String res = controller.logout();

        switch (res) {
            case "1":
                print("Logged out.");
                break;
            case "2":
                print("Log out failed");
                break;
        }

    }

    private void upload(String filename, boolean isPublic, boolean isReadonly) throws RemoteException {
        //print(controller.upload(filename, isPublic, isReadonly));
        String res = controller.upload(filename, isPublic, isReadonly);

        switch (res) {
            case "1":
                print("File already existed, overwrote with new file.");
                break;
            case "2":
                print("Error, this file already exists and you lack permissions to alter it.");
                break;
            case "3":
                print("Successfully uploaded file " + filename + " to catalog.");
                break;
            case "4":
                print("Error uploading file.");
                break;
            case "5":
                print("Error, you are not logged in.");
                break;
        }
    }

    private void download(String filename) throws RemoteException {
        //print(controller.download(filename));
        String res = controller.download(filename);

        switch (res) {
            case "1":
                print("Error, you lack permissions to download this file.");
                break;
            case "2":
                print("Error, no such file exists. Failed to download file.");
                break;
            case "3":
                print("Could not get the file");
                break;
            case "4":
                print("Error, you are not logged in.");
                break;
            case "5":
                print("Error, connection to the server refused");
                break;
            default:
                print("Downloaded file: " + res);
        }

    }

    private void delete(String filename) throws RemoteException {
        //print(controller.delete(filename));
        String res = controller.delete(filename);

        switch (res) {
            case "1":
                print("Deleted file " + filename);
                break;
            case "2":
                print("Error, you lack permissions to delete this file.");
                break;
            case "3":
                print("Error, could not delete file. No such file exists.");
                break;
            case "4":
                print("Could not find file");
                break;
            case "5":
                print("Error, you are not logged in.");
        }
    }

    private void notify(String filename) throws RemoteException {
        //print(controller.addAccessListener(filename, this));
        String res = controller.addAccessListener(filename, this);

        switch (res) {
            case "1":
                print("Listening for changes on file " + filename);
                break;
            case "2":
                print("Error, you are not logged in.");
                break;
        }

    }

    private void list() throws RemoteException {
        print(controller.list());
    }

    @Override
    public void notifyAccess(String user, String file) {
        print(user + " accessed file: " + file);
    }


}
