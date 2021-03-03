package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;


public class AuthController {


    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    private Network network;
    private NetworkClient networkClient;
    public static int deadlineReg = 120; //в секундах

    @FXML
    public void openReg() throws IOException {
        networkClient.openRegWindow();
    }

    @FXML
    public void checkAuth() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isBlank() || password.isBlank()) {
            NetworkClient.showErrorMessage("Ошибка авторизации", "Ошибка ввода", "Поля не должны быть пустыми");
            return;
        }

        String authErrorMessage = network.sendAuthCommand(login, password);
        if (authErrorMessage != null) {
            NetworkClient.showErrorMessage("Ошибка авторизации", "Что-то не то", authErrorMessage);
        } else {
            network.setLogin(login);
            networkClient.openMainChatWindow();

        }

    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }
}
