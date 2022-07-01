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
    private int requestNumber = 1;
    public static final boolean USE_NATIVE_TRANSPORT = false;
    public static final int SERVER_PORT = 8585;

    public void start() throws InterruptedException {
        VertxOptions vertxOptions = new VertxOptions().setPreferNativeTransport(USE_NATIVE_TRANSPORT);
        Vertx vertx = Vertx.vertx(vertxOptions);
        logger.info("Is native transport: {}", vertx.isNativeTransportEnabled());

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setMaxPoolSize(1)
                .setMaxWaitQueueSize(2)
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setHttp2ClearTextUpgrade(false)
//                .setTcpKeepAlive(true)
                .setUseAlpn(true)
                .setTcpFastOpen(true);

        logger.info("Starting client with config: {}", clientOptions.toJson());
        HttpClient client = vertx.createHttpClient(clientOptions);


        while (true) {
            client.request(HttpMethod.GET, new RequestOptions().setPort(SERVER_PORT), ar -> {
                logger.info("Status Code: {}, message: {}", ar.statusCode(), ar.statusMessage());
            }).setTimeout(2000).end();
            Thread.sleep(1000);
        }

    }
}
