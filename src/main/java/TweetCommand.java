import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;

import java.util.List;
import java.util.UUID;

/**
 * Created by alvin on 11/12/15.
 */
public class TweetCommand implements Command {
    private TweetClient client;

    private String tweet;

    public TweetCommand(TweetClient client, String tweet) {
        assert(client != null);
        assert(tweet != null);

        this.client = client;
        this.tweet = tweet;
    }

    @Override
    public void execute() throws CommandException {
        Session session = client.session();
        String username = client.username();
        BatchStatement statements = new BatchStatement();
        UUID tweetId = UUIDs.random();
        UUID timestampId = UUIDs.timeBased();

        // step 1: check if the user exists
        BoundStatement userStatement = new BoundStatement(
                session.prepare("SELECT username FROM users WHERE username=?")
        );
        ResultSet result = session.execute(userStatement.bind(username));
        if (result.one() == null) {
            throw new CommandException("No user with username " + username + "exists.");
        }

        // step 2: insert the tweet to the tweet table.
        BoundStatement tweetStatement = new BoundStatement(
                session.prepare("INSERT INTO tweets (tweet_id, username, body) VALUES (?,?,?)")
        );
        statements.add(tweetStatement.bind(tweetId, username, tweet));

        // step 3: insert the tweet to the global timeline table.
        BoundStatement timelineStatement = new BoundStatement(
                session.prepare("INSERT INTO timeline (username, time, tweet_id) VALUES (?,?,?)")
        );
        statements.add(timelineStatement.bind(username, timestampId, tweetId));

        // step 4: insert the tweet to own's timeline.
        BoundStatement selfTimelineStatement = new BoundStatement(
                session.prepare("INSERT INTO userline (username, time, tweet_id) VALUES (?,?,?)")
        );
        statements.add(selfTimelineStatement.bind(username, timestampId, tweetId));

        // step 5: get the followers
        BoundStatement fetchFollowerStatement = new BoundStatement(
                session.prepare("SELECT follower FROM followers WHERE username=?")
        );
        ResultSet followersResult = session.execute(fetchFollowerStatement.bind(username));

        // step 6: add the tweet to each of the follower's timeline
        List<Row> followers = followersResult.all();
        for (Row row: followers) {
            String follower = row.getString("follower");
            BoundStatement followerStatement = new BoundStatement(
                    session.prepare("INSERT INTO userline (username, time, tweet_id) VALUES (?,?,?)")
            );
            statements.add(followerStatement.bind(follower, timestampId, tweetId));
        }

        // step 7: execute these statements
        session.execute(statements);
    }
}