package twitter.runner.impl;

import twitter.exceptions.ClientDisconnectedException;
import twitter.exceptions.CommandNotFoundException;
import twitter.exceptions.DataAccessException;
import twitter.factory.CommandFactory;
import twitter.factory.CommandFactoryBuilder;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class SystemApplicationRunner implements Runnable {

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
                    try {
                        writer.write("Команда неопознана, проверьте список команд и попробуйте снова.\n");
                    } catch (IOException ex1) {
                        System.out.println(ex.getMessage());
                    }
                } catch (DataAccessException ex) {
                    writer.write(ex.getMessage() + "\n");
                    System.err.println(ex.getMessage());
                } catch (ClientDisconnectedException ex) {
                    System.out.println("Client with IP " + clientId + " disconnected.");
                    return;
                }
            }
        } catch (IOException ex) {
            System.out.println("Что то сломалось, сообщение: " + ex.getMessage());
        } finally {
            try {
                if (Objects.nonNull(reader)) {
                    reader.close();
                }
                if (Objects.nonNull(writer)) {
                    writer.close();
                }
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

}