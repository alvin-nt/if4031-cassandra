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
    public void execute() throws CommandException {
        Session session = client.session();
        String follower = client.username();

        Date currentTimestamp = new Date();
        BatchStatement batchStatement = new BatchStatement();

        // check whether the user already followed him/her
        BoundStatement checkFollowerStatement = new BoundStatement(
                session.prepare("SELECT COUNT(*) FROM followers WHERE username=? AND follower=?")
        );
        Row checkRow = session.execute(checkFollowerStatement.bind(userToFollow, follower)).one();
        if (checkRow != null) {
            if (checkRow.getLong("count") == 1) {
                throw new CommandException("You already followed user " + userToFollow + "!");
            }
        } else {
            throw new CommandException("Cannot check follower existence!");
        }

        // step 1: add to friend list
        BoundStatement friendStatement = new BoundStatement(
                session.prepare("INSERT INTO friends (username, friend, since) VALUES (?,?,?);")
        );
        batchStatement.add(friendStatement.bind(follower, userToFollow, currentTimestamp));

        // step 2: add to follower list
        BoundStatement followersStatement = new BoundStatement(
                session.prepare("INSERT INTO followers (username, follower, since) VALUES (?,?,?);")
        );
        batchStatement.add(followersStatement.bind(userToFollow, follower, currentTimestamp));

        // step 3: copy each of the followed's tweet to own's timeline
        BoundStatement followedTweetsStatement = new BoundStatement(
                session.prepare("SELECT * FROM timeline WHERE username=?")
        );
        ResultSet resultSet = session.execute(followedTweetsStatement.bind(userToFollow));
        for (Row row: resultSet.all()) {
            BoundStatement newTweetFromFollowed = new BoundStatement(
                    session.prepare("INSERT INTO userline (username, time, tweet_id) VALUES (?,?,?)")
            );
            batchStatement.add(newTweetFromFollowed.bind(follower, row.getUUID("time"), row.getUUID("tweet_id")));
        }

        session.execute(batchStatement);
    }
}
