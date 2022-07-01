package com.rafaelb.vertx.server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class Server {
    public static void main( String[] args ) {
        Server server = new Server();
        server.start();
    }

    public static final Logger logger = LogManager.getLogger(Server.class);
    public static final int PORT = 8585;
    private static final boolean USE_NATIVE_TRANSPORT = Boolean
            .parseBoolean(System.getenv().getOrDefault("USE_NATIVE_TRANSPORT", "true").toLowerCase());

    public void start() {

        // Create Vertx
        VertxOptions options = new VertxOptions().setPreferNativeTransport(true);
        Vertx vertx = Vertx.vertx(options);

        // Create HTTP Server
        HttpServerOptions serverOptions = new HttpServerOptions()
                .setTcpKeepAlive(true)
                .setTcpFastOpen(true);

        logger.info("Starting server with config: {}", serverOptions.toJson());
        HttpServer server = vertx.createHttpServer(serverOptions);
        logger.info("Is native transport: {}", vertx.isNativeTransportEnabled());

        // Create server handler
        server.requestHandler(req -> {
            logger.info("Received request: - {} {}", req.method(), req.absoluteURI());
            req.response().setChunked(true);
            req.response().write("Hi");
            req.response().end();
        });

        // Start server
        server.listen(PORT, res -> {
            if(res.failed()){
                logger.error("Couldn't start server. ", res.cause());
            }else{
                logger.info("Listening on port {}...", PORT);
            }
        });
    }
}
