import com.datastax.driver.core.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * Created by alvin on 11/12/15.
 */
public class LoginCommand implements Command {
    private static final String USER_TABLE = "users";

    private TweetClient client;
    private String username;
    private String password;

    public LoginCommand(TweetClient client, String username, String password) {
        this.client = client;
        this.username = username;
        this.password = password;
    }

    @Override
    public void execute() {
        Session session = client.session();
        PreparedStatement statement = session.prepare(
                String.format("SELECT FROM %s WHERE username=?", USER_TABLE)
        );
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet resultSet = session.execute(boundStatement.bind(username));

        Row record = resultSet.one();
        if (record == null) {
            throw new CommandException(String.format("No user with username %s found.", username));
        }

        // the user exists
        String password = record.getString("password");
        if (!BCrypt.checkpw(this.password, password)) {
            throw new CommandException(String.format("Invalid credentials for user %s.", username));
        }

        client.username(username);
    }
}
