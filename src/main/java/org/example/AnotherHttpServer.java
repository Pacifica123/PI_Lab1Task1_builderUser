package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnotherHttpServer {
    private static User.Builder builder;
    public static User.Builder getBuilder() {
        return builder;
    }
    public static void start() throws Exception{
        builder = new User.Builder();
//        myHandler = handler;
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/example2", new AnotherHttpServer.AnotherHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен на порту 8000");

    }
    static class AnotherHandler implements HttpHandler {
        String ACCESS_TOKEN = "vk1.a.Kw4y1E51D6SN-mX2VxSUGVj5VUf2bARNGpBTMjtN4I0Cm1v2LBtSgmnECfVt780NvsGyDZEfib5LYhqim_qcbTwvgqZdbo1HCjXOjVgJGEY4oJnhczrJ5R1lcTRCoge2M9ArjFRcFxuOqEXqFptBF6D4c7mMVRxTNmdnx9LOi4UZ-Rrc4ytZmzIFChT1PV9YKVDT465BvN2ECB6ADJLNpQ";
        private static boolean isServerStopped = false;
        public static CountDownLatch latch2;
        private static int requestCount = 0;
        private void parseFromData2(String data){

        }
        private void toAccountGetProfile(String query) throws URISyntaxException, IOException {
            String userId = "";
            Pattern pattern = Pattern.compile("id(\\d+)");
            Matcher matcher = pattern.matcher(query);
            // чекинг
            if (matcher.find()) {
                userId = query.substring("vk_link=https://vk.com/".length());
            }
            else {
                userId = query.substring("vk_link=https://vk.com/".length());
            }
            if (userId.equals("")){
                throw new RuntimeException("Пустой айдишник.");
            }
            // запрос
            URIBuilder builder = new URIBuilder("https://api.vk.com/method/users.get");
            builder.setParameter("user_id", userId)
                    .setParameter("access_token", ACCESS_TOKEN)
                    .setParameter("v", "5.131");
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // чекинг запроса
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                // парсим ответ и берем имя и возраст
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                String profile_name = rootNode.get("response").get(0).get("first_name").asText();
                AnotherHttpServer.builder.setName(profile_name);
                // На всякий случай:
                httpClient.close();
            } else {
                assert response != null;
                throw new IOException("Сервер вернул ошибку: " + response.getStatusLine());
            }
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("POST")) {
                requestCount++;

                // 4-debug
                InputStream requestBody = exchange.getRequestBody();
                Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
//                String formData = scanner.hasNext() ? scanner.next() : "";


                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                // 2-debug
//                String formData = URLDecoder.decode(exchange.getRequestURI().getQuery(), "UTF-8");
                // 3-debug
//                StringBuilder formDataBuilder = new StringBuilder();
//                int charCode;
//                while ((charCode = br.read()) != -1) {
//                    formDataBuilder.append((char) charCode);
//                }
//                String formData = formDataBuilder.toString();
                // DeBug
                System.out.println("Получено на example2: " + formData);

                // обработка данных с формы:
                try {
                    toAccountGetProfile(URLDecoder.decode(formData) );
                    // 2-debug
//                    toAccountGetProfile(exchange.getRequestURI().getQuery());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            exchange.sendResponseHeaders(200, "отправлено!".getBytes().length);
            OutputStream responseBody = exchange.getResponseBody();

            try {
                // обработка запроса
                responseBody.write("отправлено!".getBytes());
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
                latch2.countDown();
            }
        }
    }
}
