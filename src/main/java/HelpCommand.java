import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by alvin on 11/12/15.
 */
public class HelpCommand implements Command {
    private static final File helpFile;

    static {
        ClassLoader loader = HelpCommand.class.getClassLoader();
        if (loader != null) {
            URL url = loader.getResource("help.txt");

            if (url != null) {
                helpFile = new File(url.getFile());
            } else {
                helpFile = null;
            }
        } else {
            helpFile = null;
        }
    }

    private String showHelp() {
        // load the help.txt, or just show the hardcoded default help
        String output;
        if (helpFile != null) {
            try (FileInputStream fis = new FileInputStream(helpFile)) {
                byte[] data = new byte[(int) helpFile.length()];
                fis.read(data);

                fis.close();
                output = new String(data, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                output = "[HELP NOT AVAILABLE]";
            }
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
