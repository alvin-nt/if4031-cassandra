# Apache Cassandra Client

This is a simple, synchronous Apache Cassandra client. It is designed to handle
"Twitter"-like data (tweets, users, followers, timeline)

Developed for IF4031 - Pengembangan Aplikasi Terdistribusi (Distributed Application Development) assignment

## Requirements
1. JRE 1.8 (JDK is recommended)
2. Apache Cassandra (tested on 2.1.x)

## Supported Commands
1. `/tweet`       <tweet>          sends a tweet
2. `/follow`      <username>       follows a user
3. `/show`        (tweets|followers|timeline) [username=self] show a user's tweet or timeline
4. `/help`        shows this help message
5. `/login`       login to an existing user
6. `/register`    registers a new user
7. `/logout`      logout the current user
8. `/exit`        quits the program

If no "/" supplied, it is assumed that you want to send a tweet.

## Arguments
The produced jar can accept two arguments for runtime.
The first argument will be interpreted as the hostname of the Cassandra cluster,
and the second one as the hostname's port.

For example, `java -jar if4031-cassandra 192.168.1.33 8001` will make the program connect to the Cassandra cluster resided at
192.168.1.33:8001.