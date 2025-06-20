import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import config.BaseServer;

import java.io.*;
import java.net.HttpRetryException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        try{
            HttpServer server = BaseServer.makeServer();
            initRoutes(server);
            server.start();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    private static void initRoutes(HttpServer server) {
        server.createContext("/index.html", Main::handleStaticFile);
        server.createContext("/apps/", handler -> handleRequestApps(handler));
        server.createContext("/apps/profile", handler -> handleRequestProfile(handler));
    }


    private static void handleRequest(HttpExchange exchange) {
        try{
            exchange.getResponseHeaders().add("Content-Type", "text/plain: charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try(PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String path = exchange.getHttpContext().getPath();

                write(writer, "HTTP Method", method);
                write(writer, "Request", uri.toString());
                write(writer, "Handled", path);

                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void handleRequestApps(HttpExchange exchange){
        try{
            exchange.getResponseHeaders().add("Content-Type", "text/plain: charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try(PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                write(writer, "That is apps page", method);
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void handleRequestProfile(HttpExchange exchange){
        try{
            exchange.getResponseHeaders().add("Content-Type", "text/plain: charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            try(PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                write(writer, "That is apps/profile page", method);
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void handleStaticFile(HttpExchange exchange) throws IOException {
        String uriPath = exchange.getRequestURI().getPath(); // /index.html или /styles.css
        File file = new File("public", uriPath); // ищем файл в папке public

        if (!file.exists()) {
            String errorMsg = "404 Not Found: " + uriPath;
            exchange.sendResponseHeaders(404, errorMsg.length());
            exchange.getResponseBody().write(errorMsg.getBytes());
            exchange.close();
            return;
        }

        // Определяем тип контента
        String contentType = "text/plain";
        if (uriPath.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (uriPath.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        }

        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileBytes.length); // точная длина
        exchange.getResponseBody().write(fileBytes);
        exchange.close();
    }



    private static String getMimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        return "application/octet-stream";
    }


    private static void writeHeaders(Writer writer, String type, Headers headers){
        write(writer, type, "");
        headers.forEach((key, value) -> write(writer, "\t" + key, value.toString()));
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();
        Charset character = StandardCharsets.UTF_8;
        return new PrintWriter(outputStream, true, character);
    }

    private static void write(Writer writer, String msg, String methode){
        String data = String.format("%s: %s%n%n", msg, methode);
        try{
            writer.write(data);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private static BufferedReader getReader(HttpExchange exchange){
        InputStream input = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(input, charset);
        return new BufferedReader(isr);
    }

    private static void writeData(Writer writer, HttpExchange exchange){
        try(BufferedReader reader = getReader(exchange)){
            if(!reader.ready()) return;
            write(writer, "Data", "");
            reader.lines().forEach(line -> write(writer, "\t", line));
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}