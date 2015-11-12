-- daftar user
CREATE TABLE IF NOT EXISTS users (
    username text PRIMARY KEY,
    password text
);

-- daftar friend
CREATE TABLE IF NOT EXISTS friends (
    username text,
    friend text,
    since timestamp,
    PRIMARY KEY (username, friend)
);

-- daftar follower
CREATE TABLE IF NOT EXISTS followers (
    username text,
    follower text,
    since timestamp,
    PRIMARY KEY (username, follower)
);

CREATE TABLE IF NOT EXISTS tweets (
    tweet_id uuid PRIMARY KEY,
    username text,
    body text
);

CREATE TABLE IF NOT EXISTS userline (
    username text,
    time timeuuid,
    tweet_id uuid,
    PRIMARY KEY (username, time)
) WITH CLUSTERING ORDER BY (time DESC);

CREATE TABLE IF NOT EXISTS timeline (
    username text,
    time timeuuid,
    tweet_id uuid,
    PRIMARY KEY (username, time)
) WITH CLUSTERING ORDER BY (time DESC);