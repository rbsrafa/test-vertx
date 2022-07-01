# Vertx TCP_USER_TIMEOUT Playground

## Overview

When using Vertx HttpClient with HTTP2 configured, the client gets stuck in an established TCP 
connection in case the FIN (finish) and RST (reset) packets are not transmitted/lost
from the server. 

This repo reproduces this error and also provides a possible solution for the issue.

## How to reproduce




## Solution