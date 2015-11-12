/**
 * Created by alvin on 11/12/15.
 */
public class LogoutCommand implements Command {
    private TweetClient client;

    public LogoutCommand(TweetClient client) {
        this.client = client;
    }

    @Override
    public void execute() {
        client.state(TweetClient.STATE_NOUSER);
        client.username("");
    }
}
