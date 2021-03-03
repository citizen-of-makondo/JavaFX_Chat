package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.scene.control.*;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ChatController {


    @FXML
    public ListView<String> usersList;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextField textField;
    @FXML
    private TextField changeNameField;
    @FXML
    private Label usernameTitle;
    @FXML
    private ChoiceBox<String> userSend;
    @FXML
    private Hyperlink change;

    private Network network;

    private int printLimit = 100;

    private List<String> user = new ArrayList<>();

    public ChatController() throws IOException {
    }


    public void setLabel(String usernameTitle) {
        this.usernameTitle.setText(usernameTitle);
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    public void initialize() throws IOException {
        user.add(0, "Всем");
        user.addAll(Network.userList);
        user.remove(usernameTitle.getText());
        userSend.setItems(FXCollections.observableArrayList(user));
        userSend.setValue(user.get(0));
        usersList.setItems(FXCollections.observableArrayList(Network.userList));
        sendButton.setOnAction(event -> ChatController.this.sendMessage());
        textField.setOnAction(event -> ChatController.this.sendMessage());


    }



    private void sendMessage() {
        String message = textField.getText();

        if (message.isBlank()) {
            return;
        }

        appendMessage("Я: " + message);
        textField.clear();

        try {
            if (userSend.getValue().equals("Всем")) {
                network.sendMessage(message);
            } else {
                network.sendPrivateMessage(message, userSend.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
            NetworkClient.showErrorMessage("Ошибка подключения", "Ошибка при отправке сообщения", e.getMessage());
        }

    }

    public void appendMessage(String message) {
        String timestamp = DateFormat.getInstance().format(new Date());
        try (FileOutputStream writer = new FileOutputStream(String.format("ChatClient/src/main/resources/client/%s.HistoryMessage.txt", network.getLogin()), true)) {
            writer.write(timestamp.getBytes(StandardCharsets.UTF_8));
            writer.write(" \n".getBytes(StandardCharsets.UTF_8));
            writer.write(message.getBytes(StandardCharsets.UTF_8));
            writer.write(" \n".getBytes(StandardCharsets.UTF_8));
            writer.write(" \n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }


        chatHistory.appendText(timestamp);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());

    }

    public void chatHistoryAdd() {

        File file = new File(String.format("ChatClient/src/main/resources/client/%s.HistoryMessage.txt", network.getLogin()));
        System.out.println(network.getLogin());

        if (file.exists()) {

            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int lines = 0;
            while (scanner.hasNextLine()) {
                lines++;
                scanner.nextLine();
            }
            scanner.close();
            try (BufferedReader reader = new BufferedReader(new FileReader(String.format("ChatClient/src/main/resources/client/%s.HistoryMessage.txt", network.getLogin())))) {
                String str;
                int counter = 0;
                int startPrintLine = lines - printLimit * 3;
                while ((str = reader.readLine()) != null) {

                    if (counter >= startPrintLine) {
                        chatHistory.appendText(str + "\n");
                    }
                    counter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    public void newUserList(){
        user.clear();
        user.add(0,"Всем");
        user.addAll(Network.userList);
        usernameTitle.setText(network.getUsername());
        user.remove(usernameTitle.getText());
        userSend.setItems(FXCollections.observableArrayList(user));
        userSend.setValue(user.get(0));
        usersList.setItems(FXCollections.observableArrayList(Network.userList));
    }

    public void changeUsernameField() {
        change.setVisible(false);
        changeNameField.setVisible(true);
        changeNameField.setText(usernameTitle.getText());
    }

    public void changeName() {
        String lastUsername = network.getUsername();
        String username = changeNameField.getText();

        if (username.isBlank()) {
            NetworkClient.showErrorMessage("Ошибка смены имени", "Ошибка ввода", "Поле не должно быть пустое");
            return;
        }
        if (username.equals(lastUsername)) {
            NetworkClient.showErrorMessage("Ошибка смены имени", "Ошибка ввода", "Вы вводите старое имя");
            return;
        }

        network.sendChangeNameCommand(lastUsername, username);
        change.setVisible(true);
        changeNameField.setVisible(false);


    }


}