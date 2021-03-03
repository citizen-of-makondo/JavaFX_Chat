package chat.handler;

import chat.MyServer;
import chat.auth.*;
import clientserver.*;
import clientserver.CommandType;
import clientserver.commands.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;


public class ClientHandler {

    private final MyServer myServer;
    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);


    public ClientHandler(MyServer myServer, Socket clientSocket) {
        this.myServer = myServer;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }).start();

    }

    private void authentication() throws IOException {

        while (true) {

            Command command = readCommand();
            if (command == null) {
                continue;
            }
            if (command.getType() == CommandType.AUTH) {

                boolean isSuccessAuth = processAuthCommand(command);

                if (isSuccessAuth) {
                    break;
                }

            } else if (command.getType() == CommandType.REG) {

                processRegCommand(command);


            } else {
                sendMessage(Command.authErrorCommand("Ошибка действия"));

            }
        }

    }

    private boolean processAuthCommand(Command command) throws IOException {
        AuthCommandData cmdData = (AuthCommandData) command.getData();
        String login = cmdData.getLogin();
        String password = cmdData.getPassword();

        AuthService authService = myServer.getAuthService();
        try {
            this.username = authService.getUsernameByLoginAndPassword(login, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                sendMessage(Command.authErrorCommand("Логин уже используется"));
                return false;
            }

            UpdateUsersListCommandData.users.clear();
            for (ClientHandler client : myServer.getClients()) {
                UpdateUsersListCommandData.users.add(client.getUsername());
            }


            sendMessage(Command.authOkCommand(username));
            String message = String.format(">>> %s присоединился к чату", username);
            myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
            myServer.subscribe(this);
            return true;
        } else {
            sendMessage(Command.authErrorCommand("Логин или пароль не соответствуют действительности"));
            LOGGER.info("Клиент ввёл неправильные данные аудентификации.");
            return false;
        }
    }

    private void processRegCommand(Command command) throws IOException {
        RegCommandData cmdData = (RegCommandData) command.getData();
        String login = cmdData.getLogin();
        String username = cmdData.getUsername();
        String password = cmdData.getPassword();
        try {

            if (BaseRegService.regInDatabase(login, username, password) != null) {
                sendMessage(Command.regOkCommand());
                LOGGER.info("Клиент " + username + " успешно зарегистрировался.");
            } else {
                sendMessage(Command.regErrorCommand("Логин или имя уже используется!"));
                LOGGER.info("Клиент при регистрации попытался ввести занятые данные.");
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            LOGGER.error(throwables.getMessage(), throwables);
        }


    }

    private Command readCommand() throws IOException {
        try {
            return (Command) in.readObject();
        } catch (ClassNotFoundException e) {
            String errorMessage = "Получен неизвестный объект";
            LOGGER.error(errorMessage, e);
            return null;
        }
    }

    private void readMessage() throws IOException {

        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END:
                    String messageExit = String.format(">>> %s покинул чат", username);
                    myServer.broadcastMessage(this, Command.messageInfoCommand(messageExit, null));
                    myServer.unSubscribe(this);
                    LOGGER.info("Клиент " + username + " покинул чат.");
                    return;
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    String message = data.getMessage();
                    String sender = data.getSender();
                    myServer.broadcastMessage(this, Command.messageInfoCommand(message, sender));
                    break;
                }
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String message = data.getMessage();
                    myServer.sendPrivateMessage(recipient, Command.messageInfoCommand(message, username));
                    break;
                }
                case CHANGE_NAME: {
                    ChangeNameCommandData data = (ChangeNameCommandData) command.getData();
                    String lastUsername = data.getLastUsername();
                    String username = data.getUsername();

                    try {
                        BaseAuthService.connection();
                    } catch (ClassNotFoundException | SQLException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    try {
                        int result = BaseAuthService.stmt.executeUpdate(String.format("UPDATE users SET username = '%s' WHERE username = '%s';", username, lastUsername));
                        try {
                            BaseAuthService.disconnection();
                        } catch (SQLException throwables) {
                            LOGGER.error(throwables.getMessage(), throwables);
                        }
                        if (result == 1) {


                            myServer.sendPrivateMessage(lastUsername, Command.changeNameOkCommand(username));
                            this.username = username;
                            UpdateUsersListCommandData.users.clear();
                            for (ClientHandler client : myServer.getClients()) {
                                UpdateUsersListCommandData.users.add(client.getUsername());
                            }

                            myServer.broadcastMessage(null, Command.updateUsersListCommand(myServer.getAllUsernames()));
                            String messageChangeName = String.format(">>> %s сменил имя на %s", lastUsername, username);
                            myServer.broadcastMessage(this, Command.messageInfoCommand(messageChangeName, null));
                            LOGGER.info("Клиент " + lastUsername + " сменил имя на " + username);

                        } else {
                            sendMessage(Command.changeNameErrorCommand("Логин уже используется"));
                        }

                    } catch (SQLException throwables) {
                        LOGGER.error(throwables.getMessage(), throwables);
                    }
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(Command command) throws IOException {
        out.writeObject(command);
    }
}