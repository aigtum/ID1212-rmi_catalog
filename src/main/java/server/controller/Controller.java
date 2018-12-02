package server.controller;

import common.RMIClient;
import common.RMICredentials;
import common.RMIFile;
import common.RMIServer;
import server.integrations.FileDBException;
import server.integrations.FileSystemDAO;
import server.model.File;
import server.model.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Controller extends UnicastRemoteObject implements RMIServer {
    private FileSystemDAO fileDB;

    private Map<RMIClient, User> loggedInUsers;
    private Map<String, Set<RMIClient>> fileAccessListeners;

    public Controller(FileSystemDAO fileDB) throws RemoteException {
        super();
        this.fileDB = fileDB;
        loggedInUsers = new HashMap<>();
        fileAccessListeners = new HashMap<>();
    }

    @Override
    public String register(RMIClient remoteNode, RMICredentials credentials) {
        try {
            String username = credentials.getUsername();
            String password = credentials.getPassword();
            if (fileDB.findAccountByName(username) != null) {
                //return "Account with username " + username + " already exists";
                return "1";
            }
            fileDB.createAccount(new User(username, password));

            //return "Account with username " + username + " created.";
            return "2";

        } catch (FileDBException e) {
            e.printStackTrace();
            //return "Account creation has encountered an error";
            return "3";
        }
    }

    @Override
    public String login(RMIClient client, RMICredentials credentials) {
        try {
            String username = credentials.getUsername();
            String password = credentials.getPassword();

            User user = fileDB.findAccountByName(username);
            if (user != null) {
                if (user.getPassword().equals(password)) {
                    loggedInUsers.put(client, user);
                    //return "You are now logged in as " + username;
                    return "1";
                } else {
                    //return "Wrong username or password";
                    return "2";
                }
            } else {
                //return "Could not find this account";
                return "3";
            }
        } catch (FileDBException e) {
            e.printStackTrace();
            //return "An error occurred while trying to find the account";
            return "4";
        }
    }

    @Override
    public String logout(RMIClient client) {
        if (loggedInUsers.get(client) != null) {
            loggedInUsers.remove(client);
            //return "Logged out.";
            return "1";
        } else {
            //return "Log out failed";
            return "2";
        }
    }

    @Override
    public String upload(RMIClient client, RMIFile uploadFile) {
        if(loggedInUsers.get(client) != null) {
            File file = new File();
            file.setName(uploadFile.getName());
            file.setSize(uploadFile.getSize());
            file.setOwner(loggedInUsers.get(client).getUsername());
            file.setPublic(uploadFile.isPublic());
            file.setReadonly(uploadFile.isReadonly());
            try {
                File tmp = fileDB.findFileByName(file.getName());
                if (tmp != null) {
                    if(tmp.getOwner().equals(loggedInUsers.get(client).getUsername())
                            || (tmp.isPublic() && !tmp.isReadonly())) {
                        fileDB.updateFile(file);

                        notifyFileAccessed(loggedInUsers.get(client).getUsername(), file.getName());
                        //return "File already existed, overwrote with new file.";
                        return "1";
                    } else {
                        //return "Error, this file already exists and you lack permissions to alter it.";
                        return "2";
                    }
                } else {
                try {
                    fileDB.createFile(file, loggedInUsers.get(client));
                } catch (FileDBException e1) {
                    e1.printStackTrace();
                }
                //return "Successfully uploaded file " + file.getName() + " to catalog.";
                return "3";
                }
            } catch (Exception e) {
                //return "Error uploading file.";
                return "4";
            }
        } else {
            //return "Error, you are not logged in.";
            return "5";
        }
    }

    private void notifyFileAccessed(String username, String filename) throws RemoteException {
        Set<RMIClient> clients = fileAccessListeners.get(filename);
        if(clients != null) {
            for (RMIClient client : clients) {
                client.notify(username, filename);
            }
        }
    }


    @Override
    public String download(RMIClient client, String filename) {
        if(loggedInUsers.get(client) != null) {
            try {
                File file = fileDB.findFileByName(filename);
                if (file != null) {
                    if (file.getOwner().equals(loggedInUsers.get(client).getUsername()) || file.isPublic()) {
                        notifyFileAccessed(loggedInUsers.get(client).getUsername(), file.getName());
                        //return "Downloaded file: " + createResponse(file);
                        return createResponse(file);
                    } else {
                        //return "Error, you lack permissions to download this file.";
                        return "1";
                    }
                } else {
                    //return "Error, no such file exists. Failed to download file.";
                    return "2";
                }
            } catch (FileDBException e) {
                e.printStackTrace();
                //return "Cold not get the file";
                return "3";
            } catch (RemoteException e) {
                e.printStackTrace();
                return "5";
            }
        } else {
            //return "Error, you are not logged in.";
            return "4";
        }
    }

    private String createResponse(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t").
                append(file.getName()).append(" | ").
                append(file.getOwner()).append(" | ").
                append(file.getSize()).append(" | ").
                append(file.isPublic()).append(" | ").
                append(file.isReadonly()).append(" | ");
        return sb.toString();
    }

    @Override
    public String delete(RMIClient client, String filename) throws RemoteException {
        if(loggedInUsers.get(client) != null) {
            try {
                File file = fileDB.findFileByName(filename);
                if (file != null) {
                    if(file.getOwner().equals(loggedInUsers.get(client).getUsername()) || (file.isPublic() && !file.isReadonly())) {
                        fileDB.deleteFile(filename);
                        notifyFileAccessed(loggedInUsers.get(client).getUsername(), file.getName());
                        //return "Deleted file " + file.getName();
                        return "1";
                    } else {
                        //return "Error, you lack permissions to delete this file.";
                        return "2";
                    }
                } else {
                    //return "Error, no such file exists. failed to delete file.";
                    return "3";
                }
            }
             catch (FileDBException e) {
                e.printStackTrace();
                //return "Could not find file";
                return "4";
            }
        } else {
            //return "Error, you are not logged in.";
            return "5";
        }
    }

    @Override
    public String listFiles(RMIClient client) {
        if(loggedInUsers.get(client) != null) {
            StringBuilder sb = new StringBuilder();
            List<File> ownedFiles = null;
            try {
                ownedFiles = fileDB.listFiles(loggedInUsers.get(client).getUsername());
            } catch (FileDBException e) {
                e.printStackTrace();
            }
            sb.append("Files you have access to: ");
            for(File file : ownedFiles) {
                sb.append("\n\t").
                        append(file.getName()).append("|").
                        append(file.getOwner()).append("|").
                        append(file.getSize()).append("|").
                        append(file.isPublic()).append("|").
                        append(file.isReadonly()).append("|");
            }
            List<File> publicFiles = null;
            try {
                publicFiles = fileDB.listFiles("*");
            } catch (FileDBException e) {
                e.printStackTrace();
            }
            sb.append("\n").append("All public files: ");
            for(File file : publicFiles) {
                sb.append("\n\t").
                        append(file.getName()).append("|").
                        append(file.getOwner()).append("|").
                        append(file.getSize()).append("|").
                        append(file.isPublic()).append("|").
                        append(file.isReadonly()).append("|");
            }
            return sb.toString();
        }
        else {
            return "Error, you are not logged in.";
        }
    }

    @Override
    public String notifyUser(RMIClient client, String filename) {
        if(loggedInUsers.get(client) != null) {
            Set<RMIClient> clients = fileAccessListeners.get(filename);
            if(clients == null) {
                clients = new HashSet<>();
                fileAccessListeners.put(filename, clients);
            }
            clients.add(client);
            //return "Listening for changes on file " + filename;
            return "1";
        }
        else {
            //return "Error, you are not logged in.";
            return "2";
        }
    }
}
