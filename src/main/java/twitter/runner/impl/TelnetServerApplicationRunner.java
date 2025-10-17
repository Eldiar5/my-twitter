package twitter.runner.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Value;
import twitter.factory.CommandFactoryBuilder;
import twitter.runner.ApplicationRunner;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Component
public class TelnetServerApplicationRunner implements ApplicationRunner {

    @Value(key = "server.port")
    private Integer port;

    @Value(key = "max.users.count")
    private Integer maxUsersCount;

    private ExecutorService threadPool;
    private volatile boolean running;

    private final CommandFactoryBuilder factoryBuilder;

//    @Injection
    public TelnetServerApplicationRunner(CommandFactoryBuilder factoryBuilder) {
        this.factoryBuilder = factoryBuilder;
    }

    @Override
    public void run() {
        new Thread(() -> {
            if (maxUsersCount == null || maxUsersCount <= 0) {
                System.err.println("Invalid 'max.users.count' configuration. Using default value of 10.");
                maxUsersCount = 10;
            }
            this.threadPool = Executors.newFixedThreadPool(maxUsersCount);

            this.running = true;
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Telnet server started on port " + port + ".");

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("New client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                        Runnable clientHandling = new SystemApplicationRunner(clientSocket, factoryBuilder);

                        threadPool.execute(clientHandling);

                    } catch (IOException e) {
                        if (!running) {
                            System.out.println("Server stopped.");
                            break;
                        }
                        System.err.println("Error with client connection: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error starting Telnet server: " + e.getMessage());
            } finally {
                if (threadPool != null) {
                    threadPool.shutdown(); // Запрещаем добавление новых задач
                    try {
                        // Даем 60 секунд на завершение текущих задач
                        if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                            threadPool.shutdownNow(); // Принудительно останавливаем задачи, если время вышло
                            // Даем еще 60 секунд на реакцию на принудительную остановку
                            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                                System.err.println("Пул потоков не был корректно остановлен.");
                            }
                        }
                    } catch (InterruptedException ie) {
                        threadPool.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
}