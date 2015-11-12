import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alvin on 11/12/15.
 */
public class Main {
    private static Logger LOGGER = Logger.getGlobal();

    public static void main(String args[]) {
        // default host:port
        String host = "localhost";
        int port = 9042;
        if (args.length >= 1) {
            host = args[0];
            if (args.length >= 2) {
                port = Integer.valueOf(args[1]);
            }
        }

        // create the client
        TweetClientBuilder builder = new TweetClientBuilder();
        TweetClient client = null;

        try {
            LOGGER.log(Level.INFO, String.format("Connecting to cassandra@%s:%d...", host, port));
            client = builder.host(host).port(port).build();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        assert(client != null);

        // start the program loop.
        System.out.println("Type '/help' to show available commands.");
        Scanner scanner = new Scanner(System.in);
        while(client.state() != TweetClient.STATE_CLOSED) {
            String displayedUsername = client.username();
            if (displayedUsername == null) {
                displayedUsername = "[not logged in]";
            } else if (displayedUsername.equals("") || displayedUsername.isEmpty()) {
                displayedUsername = "[not logged in]";
            }

            try {
                System.out.printf("%s > ", displayedUsername);
                String query = scanner.nextLine();
                client.processCommand(query);
            } catch (CommandException | IllegalArgumentException | IllegalStateException e) {
                System.err.println(e.toString());
                HelpCommand help = new HelpCommand();
                help.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
