package com.rafaelb.vertx.client;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class Client {
    public static void main( String[] args ) throws InterruptedException {
        Client client = new Client();
        client.start();
    }

    private static final Logger logger = LogManager.getLogger(Client.class);

    public static final int SERVER_PORT = 8585;
    public static final String SERVER_HOST = "172.22.0.20";
    private static final boolean USE_NATIVE_TRANSPORT = Boolean
            .parseBoolean(System.getenv().getOrDefault("USE_NATIVE_TRANSPORT", "true").toLowerCase());


    public void start() throws InterruptedException {
        VertxOptions vertxOptions = new VertxOptions().setPreferNativeTransport(USE_NATIVE_TRANSPORT);
        Vertx vertx = Vertx.vertx(vertxOptions);
        logger.info("Is native transport: {}", vertx.isNativeTransportEnabled());

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setMaxPoolSize(1)
                .setMaxWaitQueueSize(2)
                .setProtocolVersion(HttpVersion.HTTP_2);
//                .setTcpUserTimeout(10000);

        logger.info("Starting client with config: {}", clientOptions.toJson());
        HttpClient client = vertx.createHttpClient(clientOptions);

        RequestOptions requestOptions = new RequestOptions()
                .setTimeout(2000)
                .setPort(SERVER_PORT)
                .setHost(SERVER_HOST);

        while (true) {
            client.request(requestOptions, ar1 -> {
                if (ar1.succeeded()) {
                    ar1.result().send(res -> {
                        if (res.succeeded()) {
                            logger.info("SUCCESS - Status Code: {}, message: {}", res.result().statusCode(), res.result().statusMessage());
                        } else {
                            logger.info("FAILURE - cause: {} - message: {}", res.cause().getCause(), res.cause().getMessage());
                        }
                    });
                } else {
                    logger.info("Something went wrong: {}", ar1.cause().getMessage());
                }
            });
            Thread.sleep(1000);
        }
    }
}
