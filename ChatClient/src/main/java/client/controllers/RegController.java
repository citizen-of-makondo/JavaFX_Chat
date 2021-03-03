package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;


public class RegController {


    @FXML
    public TextField loginFieldReg;
    @FXML
    public PasswordField passwordFieldReg;
    @FXML
    public TextField usernameFieldReg;

    private Network network;
    private NetworkClient networkClient;

    @FXML
    public void doReg() throws IOException {
        String loginReg = loginFieldReg.getText();
        String usernameReg = usernameFieldReg.getText();
        String passwordReg = passwordFieldReg.getText();

        if (loginReg.isBlank() || passwordReg.isBlank() || usernameReg.isBlank()) {
            NetworkClient.showErrorMessage("Ошибка регистрации", "Ошибка ввода", "Поля не должны быть пустыми");
            return;
        }

        String regErrorMessage = network.sendRegCommand(loginReg, usernameReg, passwordReg);
        if (regErrorMessage != null) {
            NetworkClient.showErrorMessage("Ошибка регистрации", "Что-то не то", regErrorMessage);
        } else {
            networkClient.regStage.close();
            networkClient.openAuthWindow();

        }

    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }
}