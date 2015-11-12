import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by alvin on 11/12/15.
 */
public class RegisterCommand implements Command {
    private static final String USER_TABLE = "users";

    private TweetClient client;
    private String username;
    private String password;

    public RegisterCommand(TweetClient client, String username, String password) {
        this.client = client;
        this.username = username;

        // using [undefined] for password encryption
        this.password = encrypt(password);
    }

    /**
     * Encrypts the password using bcrypt
     * @param plaintext
     * @return encrypted password
     */
    private String encrypt(String plaintext) {
        // limit for the bcrypt algorithm
        if (plaintext.length() > 30) {
            throw new IllegalArgumentException("Password must be no longer than 30 characters!");
        }
        return BCrypt.hashpw(plaintext, BCrypt.gensalt());
    }

    @Override
    public void execute() throws CommandException {
        Session session = client.session();
        BoundStatement checkUserStatement = new BoundStatement(
                session.prepare("SELECT COUNT(*) FROM users WHERE username=?")
        );
        Row checkResult = session.execute(checkUserStatement.bind(username)).one();

        if (checkResult != null) {
            if (checkResult.getLong("count") == 1) {
                throw new CommandException(String.format("User with name %s already exists!", username));
            }
        } else {
            throw new CommandException("Cannot check user existence!");
        }

        PreparedStatement statement = session.prepare(
                String.format("INSERT INTO %s (username, password) VALUES (?, ?)", USER_TABLE)
        );

        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(username, password));

        // automatically logs in the user
        client.username(username);
        client.state(TweetClient.STATE_OK);
    }
}
