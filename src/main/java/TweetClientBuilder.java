import java.io.IOException;

/**
 * Created by alvin on 11/12/15.
 */
public class TweetClientBuilder {
    private String host;
    private int port;
    private boolean init = true;

    private String keyspace = "twitter_like";

    public String host() {
        return host;
    }

    public TweetClientBuilder host(String host) {
        this.host = host;
        return this;
    }

    public int port() {
        return port;
    }

    public TweetClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    public String keyspace() {
        return keyspace;
    }

    public TweetClientBuilder keyspace(String keyspace) {
        this.keyspace = keyspace;
        return this;
    }

    public boolean init() {
        return init;
    }

    public TweetClientBuilder init(boolean init) {
        this.init = init;
        return this;
    }

    public TweetClient build() throws IOException {
        return new TweetClient(host, port, keyspace, init);
    }
}
