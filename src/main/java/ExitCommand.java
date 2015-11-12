/**
 * Created by alvin on 11/12/15.
 */
public class ExitCommand implements Command {
    private TweetClient client;

    public ExitCommand(TweetClient client) {
        this.client = client;
    }

    @Override
    public void execute() throws CommandException {
        client.close();
        client.state(TweetClient.STATE_CLOSED);
    }
}
