package org.example;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class Main {
    public static void main(String[] args) throws Exception {

        List<User> users = new ArrayList<>();

        String url = "jdbc:postgresql://localhost:5432/testdbforprograminjenery_lab1";
        String username = "noir";
        String pass = "Noir_Tea";

        // используем строителя для БД
        Connection conn = DriverManager.getConnection(url, username, pass);
        Statement state = conn.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM Users");
        while (res.next()) {
            String name = res.getString("name");
            String email = res.getString("email");

            // создаем с помощью Строителя обхект пользователя из данных БД:
            User.Builder builder = new User.Builder();
            User user = builder
                    .setName(name)
                    .setEmail(email)
                    .build();
            users.add(user);
        }

        // используем строителя для Формы:
        /**
         * Сервер будет работать до получения первого запроса
         * и только потом продолжится выполнение программы
         */
        SimpleHttpServer.MyHandler.latch = new CountDownLatch(1);
        try {
            SimpleHttpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Дожидаемся остановки сервера...
        SimpleHttpServer.MyHandler.latch.await();
        System.out.println("сервер стопнулся");


        // билдим из того что получили из формы:
        User u2 = SimpleHttpServer.getBuilder().build();
        users.add(u2);

        // смотрим накопленных пользователей из БД и формы:
        for (User user : users) {
            System.out.println("Имя: " + user.getName());
            System.out.println("Почта: " + user.getEmail());
            if (user.getAge() == 0) {
                System.out.println("Кажется этот пользователь был взят из БД и у него не указан возраст!");
            } else {
                System.out.println("Возраст: " + user.getAge());
            }
        }
    }
}