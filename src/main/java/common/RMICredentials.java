package common;

import java.io.Serializable;

public class RMICredentials implements Serializable {

    private String username;
    private String password;

    public RMICredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
