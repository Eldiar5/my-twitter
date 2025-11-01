package twitter.runner.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter.exceptions.ClientDisconnectedException;
import twitter.exceptions.CommandNotFoundException;
import twitter.exceptions.DataAccessException;
import twitter.factory.CommandFactory;
import twitter.factory.CommandFactoryBuilder;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class SystemApplicationRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SystemApplicationRunner.class);

    private final Socket clientSocket;
    private final CommandFactoryBuilder commandFactory;

    public SystemApplicationRunner(Socket clientSocket, CommandFactoryBuilder commandFactory) {
        this.clientSocket = clientSocket;
        this.commandFactory = commandFactory;
    }

    @Override
    public void run() {
        String command = "";
        String clientId = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        BufferedReader reader = null;
        BufferedWriter writer = null;

        logger.info("Новое подключение от клиента: {}", clientId);

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            CommandFactory commandFactory = this.commandFactory.buildCommandFactoryForUser(clientId, reader, writer);
            while (true) {
                try {
                    writer.append("Для получения помощи по командам, используйте команду help.").append("\n");
                    writer.append("Введите команду: ");
                    writer.flush();
                    command = reader.readLine();
                    commandFactory.getHandler(command).handle();
                } catch (CommandNotFoundException ex) {
                    writer.write("Команда неопознана, проверьте список команд и попробуйте снова.\n");
                } catch (DataAccessException ex) {
                    // 1. Для разработчика: логируем ПОЛНУЮ ошибку со стектрейсом
                    logger.error("Произошла ошибка доступа к данным при обработке запроса от клиента {}", clientId, ex);

                    // 2. Для пользователя: отправляем общее, безопасное сообщение
                    writer.write("Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.\n");
                } catch (ClientDisconnectedException ex) {
                    logger.info("Клиент {} отключился.", clientId);
                    return;
                } catch (Exception ex) {
                    // Ловим все остальные непредвиденные ошибки
                    logger.error("Непредвиденная ошибка при обработке запроса от клиента {}", clientId, ex);
                    writer.write("Произошла критическая непредвиденная ошибка.\n");
                }
            }
        } catch (IOException ex) {
            logger.error("Ошибка ввода-вывода для клиента {}: {}", clientId, ex.getMessage());
        } finally {
            try {
                if (Objects.nonNull(reader)) {
                    reader.close();
                }
                if (Objects.nonNull(writer)) {
                    writer.close();
                }
                clientSocket.close();
                logger.info("Соединение с клиентом {} закрыто.", clientId);
            } catch (IOException ex) {
                logger.error("Ошибка при закрытии ресурсов для клиента {}.", clientId, ex);
            }
        }

    }

}