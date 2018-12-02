package server.integrations;

import server.model.File;
import server.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileSystemDAO {

    private PreparedStatement createAccountStmt;
    private PreparedStatement findAccountByNameStmt;
    private PreparedStatement createFileStmt;
    private PreparedStatement findFileByNameStmt;
    private PreparedStatement listAllFilesStmt;
    private PreparedStatement deleteFileStmt;
    private PreparedStatement updateFileStmt;

    public FileSystemDAO() throws SQLException, ClassNotFoundException {
        Connection connection = connectToDB();
        preparedStatements(connection);
    }

    private Connection connectToDB() throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        try {
            return DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/rmi", "root", "root1234");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void preparedStatements(Connection connection) throws SQLException {
        createAccountStmt = connection.prepareStatement("INSERT INTO  rmi.users (USERNAME, PASSWORD) VALUES (?,?)");
        createFileStmt = connection.prepareStatement("INSERT INTO rmi.files (name, size, owner, is_public, is_readonly) VALUES (?,?,?,?,?)");
        findFileByNameStmt = connection.prepareStatement("SELECT * FROM rmi.files WHERE name = ? ");
        findAccountByNameStmt = connection.prepareStatement("SELECT * FROM rmi.users WHERE username = ? ");
        listAllFilesStmt = connection.prepareStatement("SELECT * FROM rmi.files WHERE owner = ? OR is_public = ?");
        deleteFileStmt = connection.prepareStatement("DELETE FROM rmi.files WHERE name=?");
        updateFileStmt = connection.prepareStatement("UPDATE rmi.files SET size=?, is_public=?,is_readonly=? WHERE name=?");
    }

    public void createAccount(User account) throws FileDBException {
        String failureMsg = "Could not create the account: " + account;
        try {
            createAccountStmt.setString(1, account.getUsername());
            createAccountStmt.setString(2, account.getPassword());
            int rows = createAccountStmt.executeUpdate();
            if (rows != 1) {
                throw new FileDBException(failureMsg);
            }
        } catch (SQLException sqle) {
            throw new FileDBException(failureMsg, sqle);
        }
    }

    public File findFileByName(String name) throws FileDBException {
        ResultSet result = null;
        try {
            findFileByNameStmt.setString(1, name);
            result = findFileByNameStmt.executeQuery();
            if (result.next()) {
                return new File(
                        result.getString("name"),
                        result.getInt("size"),
                        result.getBoolean("is_public"),
                        result.getBoolean("is_readonly"),
                        result.getString("owner"));
            }
        } catch (SQLException e) {
            throw new FileDBException("Can not find file with name: " + name, e);
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (SQLException e) {
                throw new FileDBException("Can close result", e);
            }
        }
        return null;
    }

    public int createFile(File file, User account) throws FileDBException {
        try {
            createFileStmt.setString(1, file.getName());
            createFileStmt.setLong(2, file.getSize());
            createFileStmt.setString(3, account.getUsername());
            createFileStmt.setBoolean(4, file.isPublic());
            createFileStmt.setBoolean(5, file.isReadonly());
            return createFileStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FileDBException("Can not create file. ", e);
        }

    }

    public User findAccountByName(String userName) throws FileDBException {
        ResultSet result = null;
        try {
            findAccountByNameStmt.setString(1, userName);
            result = findAccountByNameStmt.executeQuery();
            if (result.next()) {
                return new User(result.getString("username"), result.getString("password"), result.getInt("id"));
            }
        } catch (SQLException e) {
            throw new FileDBException("Can't find account by username: " + userName, e);
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (SQLException e) {
                throw new FileDBException("Can't close result ", e);
            }
        }
        return null;
    }

    public List<File> listFiles(String owner) throws FileDBException {
        List<File> listOfFiles = new ArrayList();
        ResultSet result = null;
        try {

            listAllFilesStmt.setString(1, owner);
            listAllFilesStmt.setBoolean(2, true);
            result = listAllFilesStmt.executeQuery();
            while (result.next()) {
                listOfFiles.add(new File(
                        result.getString("name"),
                        result.getLong("size"),
                        result.getBoolean("is_public"),
                        result.getBoolean("is_readonly"),
                        result.getString("owner")));
            }
        } catch (SQLException e) {
            throw new FileDBException("Cant list files", e);
        }
        return listOfFiles;
    }

    public int updateFile(File file) throws FileDBException {

        try {
            updateFileStmt.setLong(1, file.getSize());
            updateFileStmt.setBoolean(2, file.isPublic());
            updateFileStmt.setBoolean(3, file.isReadonly());
            updateFileStmt.setString(4, file.getName());
            return updateFileStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FileDBException("Can not update file. ", e);
        }
    }

    public int deleteFile(String fileName) throws FileDBException {
        try {
            deleteFileStmt.setString(1, fileName);
            return deleteFileStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FileDBException("Can not delete file with name " + fileName, e);
        }
    }



}
