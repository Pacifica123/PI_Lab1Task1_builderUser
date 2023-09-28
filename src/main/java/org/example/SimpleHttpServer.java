package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SimpleHttpServer {
    private static User.Builder builder;

    public static User.Builder getBuilder() {
        return builder;
    }
//    public static MyHandler myHandler;
//    public CountDownLatch latch;

    public static void start() throws Exception{
//        myHandler = handler;
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/example", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен на порту 8000");
    }
    static class MyHandler implements HttpHandler {
        public static CountDownLatch latch;
        private static int requestCount = 0;
        public MyHandler() {}
        private static boolean isServerStopped = false;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("POST")) {
                requestCount++;
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                // DeBug
                System.out.println("Получено на example: " + formData);
                // обработка данных с формы:
                parseFormData(formData);
            }
            exchange.sendResponseHeaders(200, "Форма отправлена!".getBytes().length);
            OutputStream responseBody = exchange.getResponseBody();

            try {
                // обработка запроса
                responseBody.write("Форма отправлена!".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            responseBody.flush();
            responseBody.close();

            // Остановка сервера после обработки первого запроса
            if (requestCount == 1)
            {
                isServerStopped = true;
                try {
                    exchange.getHttpContext().getServer().stop(0);
                    System.out.println("Сервер веб-формы завершил работу.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        }

        public static boolean isServerStopped() {
            return isServerStopped;
        }

        // Обработка запроса от веб-формы:
        private void parseFormData(String data){
            builder = new User.Builder();
            if (!data.contains("name") || !data.contains("age") || !data.contains("email")){
                System.out.println("Это какая-то неправильная форма и она делает неправильный данные!");
            }
            else {
               String[] parts_data; //name=%D1%8F&age=10&email=hz%40gmail.com
                parts_data = data.split("&"); //name=%D1%8F | age=10 | email=hz%40gmail.com
               for (String part: parts_data) {
                   String[] micro_part = part.split("="); //| name %D1%8F | age 10 | email hz%40gmail.com |
                   switch (micro_part[0]) {
                       case "name" -> {
                           SimpleHttpServer.builder.setName(
                                   URLDecoder.decode(micro_part[1])
                           );
                       }
                       case "age" -> {
                           SimpleHttpServer.builder.setAge(
                                   Integer.decode(micro_part[1])
                           );
                       }
                       case "email" -> {
                           SimpleHttpServer.builder.setEmail(
                                   URLDecoder.decode(micro_part[1])
                           );
                       }
                       default -> {
                       }
                   }
               }
            }
        }
    }

    /**
     * КОСТЫЛЬ
     */

}
