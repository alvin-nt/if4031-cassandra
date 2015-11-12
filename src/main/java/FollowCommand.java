import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Batch;

import java.util.Date;

/**
 * Created by alvin on 11/12/15.
 */
public class FollowCommand implements Command {
    private TweetClient client;

    private String userToFollow;

    public FollowCommand(TweetClient client, String userToFollow) {
        this.client = client;
        this.userToFollow = userToFollow;
    }

    @Override
    public void execute() {
        Session session = client.session();
        String follower = client.username();

        Date currentTimestamp = new Date();
        BoundStatement friendStatement = new BoundStatement(
                session.prepare("INSERT INTO friends (username, friend, since) VALUES (?,?,?);")
        );
        BoundStatement followersStatement = new BoundStatement(
                session.prepare("INSERT INTO followers (username, follower, since) VALUES (?,?,?);")
        );

        BatchStatement batchStatement = new BatchStatement();
        batchStatement.add(friendStatement.bind(follower, userToFollow, currentTimestamp));
        batchStatement.add(followersStatement.bind(userToFollow, follower, currentTimestamp));

        session.execute(batchStatement);
    }
}
