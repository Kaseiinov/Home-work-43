package config;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BaseServer {


    public static HttpServer  makeServer() throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);
        System.out.printf("Server started on 'http://localhost:%s' address%n", inetSocketAddress.getPort());


        HttpServer httpServer = HttpServer.create(inetSocketAddress, 50);
        System.out.println("       Success!");
        return httpServer;

    }

}
