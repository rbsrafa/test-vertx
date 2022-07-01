# Vertx TCP_USER_TIMEOUT Playground

## Overview

When using Vertx HttpClient with HTTP2 configured, the client gets stuck in an established TCP 
connection in case the FIN (finish) and RST (reset) packets are not transmitted/lost
from the server. 

This repo reproduces this error and also provides a possible solution for the issue.


## How to Build and Run

Clone this repo:
```shell
git clone git@github.com:rbsrafa/test-vertx.git
```

Move to the root folder:
```shell
cd test-vertx
```

Build the project:
```shell
mvn clean install
```

Run both client and server with docker-compose:
```shell
docker-compose -f docker-compose/docker-compose.yaml up
```

The client will send a GET request every second to the server.

## How to Reproduce

When the application is up and running, exec into the client container:
```shell
docker exec -it docker-compose_vertx-client_1 bash
```

Inside the container, watch for the TCP connections:
```shell
watch netstat

# result:
Active Internet connections (w/o servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State
tcp        0      0 vertx-server:52160      docker-compose_ver:8585 ESTABLISHED
Active UNIX domain sockets (w/o servers)
Proto RefCnt Flags       Type       State         I-Node   Path
unix  2      [ ]         STREAM     CONNECTED     10887623
```

Note that the connection between the client and server:8585 has been established. If you restart
the server container you should see the connection restarted:
```shell
docker restart docker-compose_vertx-server_1
```

The issue appears when for some reason the TCP packets FIN and RST are not received or lost. The
connection keeps active (ESTABLISHED) but the client will start to log `message: Connection was closed`, 
even though it is still active.

To reproduce it, add the following iptables rules on your localhost:
```shell
sudo iptables -I FORWARD -p tcp --sport 8585 -s 172.22.0.20 --tcp-flags FIN FIN -j DROP
sudo iptables -I FORWARD -p tcp --sport 8585 -s 172.22.0.20 --tcp-flags RST RST -j DROP
```

Confirm the rules have beem applied:
```shell
$ sudo iptables -S | grep 172.22.0.20

# result
-A FORWARD -s 172.22.0.20/32 -p tcp -m tcp --sport 8585 --tcp-flags RST RST -j DROP
-A FORWARD -s 172.22.0.20/32 -p tcp -m tcp --sport 8585 --tcp-flags FIN FIN -j DROP
-A DOCKER -d 172.22.0.20/32 ! -i br-f093dd72662f -o br-f093dd72662f -p tcp -m tcp --dport 8585 -j ACCEPT
```

Now restart the server container again.

You should get `message: Connection was closed` for the next 30 min, when the unix `tcp_retries2`
config will kick in and restart the broken connection. By default it's value is 15, you can verify 
your machine setup with:
```shell
cat /proc/sys/net/ipv4/tcp_retries2

# result
15
```

## Proposed Solution

Vertx supports native transport in Linux machines, the tcp_retries relates to the Netty 
[EpollChannelOption#TCP_USER_TIMEOUT](https://netty.io/4.1/api/io/netty/channel/epoll/EpollChannelOption.html#TCP_USER_TIMEOUT)
config.

Unfortunately, this option wasn't implemented alongside other native transport options, see Vert.x
docs [Native Linux Transport](https://vertx.io/docs/vertx-core/java/#_native_linux_transport).

To add it, a few changes in the Transport and Configs would be required. I've added these changes to
this fork: [vert.x fork](https://github.com/rbsrafa/vert.x).

You can also test it by locally building the above project fork on the main branch.
After building the Vert.x fork, change to this (test-vertx) project's branch `with-native-transport-and-tcp-user-timeout`:
```shell
git checkout with-native-transport-and-tcp-user-timeout
```

Now follow all the above steps to reproduce the issue, but this time the connection will be reset
after ~10 seconds, due to the `TCP_USER_TIMEOUT` option added to HttpClientOptions and running natively
on Linux.