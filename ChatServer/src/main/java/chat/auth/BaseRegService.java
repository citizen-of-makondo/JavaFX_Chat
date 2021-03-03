
package chat.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class BaseRegService {

    private static final Logger LOGGER = LogManager.getLogger(BaseAuthService.class);


    public static String regInDatabase(String login, String username, String password) throws SQLException, ClassNotFoundException {
        BaseAuthService.connection();
        int result = BaseAuthService.stmt.executeUpdate(String.format("INSERT INTO users (login, username, password)  VALUES ('%s', '%s', '%s')", login, username, password));
        BaseAuthService.disconnection();
        if (result == 1) {
            return username;
        } else {
            return null;
        }
    }
}