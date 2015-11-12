import java.io.*;
import java.util.Scanner;

/**
 * Created by alvin on 11/12/15.
 */
public class HelpCommand implements Command {
    private static String helpOutput;

    static {
        ClassLoader loader = HelpCommand.class.getClassLoader();
        if (loader != null) {
            InputStream resource = loader.getResourceAsStream("help.txt");

            if (resource != null) {
                Scanner scanner = new Scanner(resource).useDelimiter("\\A");
                helpOutput = scanner.hasNext() ? scanner.next() : "";

                scanner.close();
                try {
                    resource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            helpOutput = null;
        }
    }

    private String showHelp() {
        // load the help.txt, or just show the hardcoded default help
        String output;
        if (helpOutput != null) {
            output = helpOutput;
        } else {
            output = "If no \"/\" supplied, it is assumed that you want to send a tweet.\n" +
                    "\n" +
                    "Available commands:\n" +
                    "/tweet       <tweet>          sends a tweet\n" +
                    "/follow      <username>       follows a user\n" +
                    "/show        (tweets|followers|timeline) [username=self] show a user's tweet or timeline\n" +
                    "/help        shows this help message\n" +
                    "/login       login to an existing user\n" +
                    "/register    registers a new user\n" +
                    "/logout      logout the current user\n" +
                    "/exit        quits the program";
        }

        return output;
    }

    @Override
    public void execute() throws CommandException {
        System.out.println(showHelp());
    }
}
