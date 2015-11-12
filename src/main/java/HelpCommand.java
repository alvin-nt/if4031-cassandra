/**
 * Created by alvin on 11/12/15.
 */
public class HelpCommand implements Command {


    private String showHelp() {
        // load the help.txt, or just show the hardcoded default help

        String builder = "<tweet>\tsends a tweet\n" +
                "\n" +
                "Available commands:\n" +
                "<tweet>\tsends a tweet\n" +
                "/tweet <tweet>\tsends a tweet\n" +
                "/follow <username>\tfollows a user\n" +
                "/show (tweets|followers|timeline) <username>\tshow a user's tweet or timeline\n" +
                "/help\tshows this help message\n" +
                "/logout\tlogout the current user\n" +
                "/exit\tquits the program\n";

        return builder;
    }

    @Override
    public void execute() {
        System.out.println(showHelp());
    }
}
