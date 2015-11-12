import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by alvin on 11/12/15.
 */
public class ShowCommand implements CommandWithResult {
    private static final int SHOW_TIMELINE = 1;
    private static final int SHOW_FOLLOWERS = 2;
    private static final int SHOW_TWEETS = 3;

    private TweetClient client;
    private int showType;
    private String userToShow;

    private ResultSet result;

    public ShowCommand(TweetClient client, String showType, String userToShow) {
        assert(client != null);
        assert(showType != null);

        this.client = client;

        switch (showType.toLowerCase()) {
            case "timeline":
                this.showType = SHOW_TIMELINE;
                break;
            case "followers":
                this.showType = SHOW_FOLLOWERS;
                break;
            case "tweets":
                this.showType = SHOW_TWEETS;
                break;
            default:
                throw new IllegalArgumentException("Unknown showType: " + showType);
        }

        this.userToShow = userToShow;
    }

    @Override
    public void execute() throws CommandException {
        Session session = client.session();
        BoundStatement statement = null;

        switch (showType) {
            case SHOW_TIMELINE:
                // by default shows only 50 latest tweets on the timeline
                statement = new BoundStatement(session.prepare("SELECT * FROM userline WHERE username=? ORDER BY time DESC LIMIT 50"));
                break;
            case SHOW_FOLLOWERS:
                statement = new BoundStatement(session.prepare("SELECT * FROM followers WHERE username=?"));
                break;
            case SHOW_TWEETS:
                // by default shows only 50 tweets
                statement = new BoundStatement(session.prepare("SELECT * FROM timeline WHERE username=? ORDER BY time DESC LIMIT 50"));
                break;
        }
        assert(statement != null);

        result = session.execute(statement.bind(userToShow));
    }

    @Override
    public ResultSet getRawResult() {
        return result;
    }

    @Override
    public String getResult() {
        List<Row> results = result.all();
        StringBuilder builder = new StringBuilder();

        // print the header first
        switch(showType) {
            case SHOW_FOLLOWERS:
                builder.append("follower,since").append('\n');
                break;
            case SHOW_TWEETS:
                builder.append("tweet_id,timestamp,body").append('\n');
                break;
            case SHOW_TIMELINE:
                builder.append("tweet_id,username,timestamp,body").append('\n');
                break;
        }

        for (Row row: results) {
            Row tweet;

            switch(showType) {
                case SHOW_FOLLOWERS:
                    builder.append(row.getString("follower")).append(",")
                            .append(row.getDate("since"))
                            .append('\n');
                    break;
                case SHOW_TWEETS:
                    tweet = fetchTweet(row.getUUID("tweet_id"));

                    builder.append(row.getUUID("tweet_id")).append(",")
                            .append(getDateFromUuid(row.getUUID("time"))).append(",")
                            .append(tweet.getString("body"))
                            .append("\n");
                    break;
                case SHOW_TIMELINE:
                    tweet = fetchTweet(row.getUUID("tweet_id"));

                    // print it out
                    builder.append(row.getUUID("tweet_id")).append(',')
                            .append(tweet.getString("username")).append(',')
                            .append(getDateFromUuid(row.getUUID("time"))).append(',')
                            .append(tweet.getString("body") == null ? "null" : tweet.getString("body"))
                            .append("\n");
                    break;
            }
        }

        return builder.toString();
    }

    private Row fetchTweet(UUID tweetId) {
        BoundStatement tweetStatement = new BoundStatement(
                client.session().prepare("SELECT * FROM tweets WHERE tweet_id=?")
        );
        return client.session()
                .execute(tweetStatement.bind(tweetId))
                .one();
    }

    private Date getDateFromUuid(UUID uuid) {
        return new Date(UUIDs.unixTimestamp(uuid));
    }
}
