import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created by Alvin Natawiguna on 11/11/2015.
 */
public class TweetClient implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(TweetClient.class.getName());

    public static final int STATE_CLOSED = -1;
    public static final int STATE_NOUSER = 1;
    public static final int STATE_OK = 0;

    private int state;

    private Cluster cluster;
    private Session session;

    private String username;

    TweetClient(String host, int port, String keyspace, boolean init) throws IOException {
        state = STATE_NOUSER;
        cluster = Cluster.builder()
                .addContactPoint(host)
                .withPort(port)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .build();

        if (init) {
            session = initSession(keyspace);
            initTables();
        } else {
            session = cluster.connect(keyspace);
        }
    }

    public Session session() {
        return session;
    }

    public int state() {
        return state;
    }

    public void state(int state) {
        this.state = state;
    }

    public String username() {
        return username;
    }

    public void username(String username) {
        this.username = username;
    }

    private Session initSession(String keyspace) {
        // create keyspace if not exists
        Session session = cluster.connect();
        session.execute(String.format("CREATE KEYSPACE IF NOT EXISTS %s " +
                "WITH REPLICATION = { " +
                "'class' : 'SimpleStrategy'," +
                "'replication_factor' : 1 };", keyspace));
        session.close();

        return cluster.connect(keyspace);
    }

    private void initTables() throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        InputStream resource = loader.getResourceAsStream("table-schema.cql");
        if (resource == null) {
            throw new IOException("Schema file does not exist!");
        }

        Scanner scanner = new Scanner(resource).useDelimiter("\\A");
        String buffer = scanner.hasNext() ? scanner.next() : "";

        scanner.close();
        resource.close();

        String tables[] = buffer.split("[\\n\\r|\\n|\\r]{2}");

        for (String table : tables) {
            // Logger.getLogger(getClass().getName()).log(Level.INFO, "Executing;\n " + table);
            session.execute(table);
        }
    }

    public void processCommand(String query)
            throws IllegalArgumentException, IllegalStateException, IOException
    {
        Command command;
        String trimmedQuery = query.trim();

        if (trimmedQuery.startsWith("/")) {
            // parse the command
            trimmedQuery = trimmedQuery.substring(1);
            String tokens[] = trimmedQuery.split("\\s+");

            String commandQuery = tokens[0].toLowerCase();
            switch(commandQuery) {
                case "tweet":
                    if (state != STATE_OK) {
                        throw new IllegalStateException("You are not logged in!");
                    }

                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("No tweet defined.");
                    }
                    String tweet = StringUtils.join(Arrays.copyOfRange(tokens, 1, tokens.length), ' ');

                    command = new TweetCommand(this, tweet);
                    command.execute();
                    break;
                case "follow":
                    if (state != STATE_OK) {
                        throw new IllegalStateException("You are not logged in!");
                    }

                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("No username to follow.");
                    }
                    String userToFollow = tokens[1];

                    command = new FollowCommand(this, userToFollow);
                    command.execute();
                    break;
                case "show":
                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("Too few arguments for show command (minimum 2).");
                    }
                    String showType = tokens[1];
                    String userToShow;

                    if (tokens.length >= 3) {
                        userToShow = tokens[2];
                    } else {
                        userToShow = username;
                    }

                    command = new ShowCommand(this, showType, userToShow);
                    command.execute();

                    System.out.println(((CommandWithResult)command).getResult());
                    break;
                case "login":
                    if (state == STATE_OK) {
                        throw new IllegalStateException("You already logged in!");
                    }

                    // prompt for username and password
                    {
                        String username = Util.readLine("Username: ");
                        String password = new String(Util.readPassword("Password: "));

                        command = new LoginCommand(this, username, password);
                        command.execute();
                    }
                    break;
                case "register":
                    if (state == STATE_OK) {
                        throw new IllegalStateException("You already logged in!");
                    }

                    // prompt for username and password
                    {
                        String username = Util.readLine("Username: ");
                        String password = new String(Util.readPassword("Password: "));

                        command = new RegisterCommand(this, username, password);
                        command.execute();
                    }

                    break;
                case "logout":
                    if (state != STATE_OK) {
                        throw new IllegalStateException("You are not logged in!");
                    }

                    command = new LogoutCommand(this);
                    command.execute();
                    break;
                case "help":
                    command = new HelpCommand();
                    command.execute();
                    break;
                case "exit":
                    command = new ExitCommand(this);
                    command.execute();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command: " + commandQuery);
            }
        } else {
            // assume that the user wants to send a tweet instead
            if (state != STATE_OK) {
                throw new IllegalStateException("You are not logged in!");
            }

            command = new TweetCommand(this, query);
            command.execute();
        }
    }

    public void close() {
        session.close();
        cluster.close();
    }
}
