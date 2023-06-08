package lk.ijse.chatApp.controller;

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class LogFormController {

    @FXML
    private AnchorPane pane;

    @FXML
    private JFXTextField txtUsername;

    @FXML
    void btnLogOnAction(ActionEvent event) throws IOException {
        if (txtUsername.getText().equals("")) {
            new Alert(Alert.AlertType.WARNING, "Please enter your name for chat").show();
            return;
        }

        ChatRoomFormController.username = txtUsername.getText();

        pane.getChildren().clear();
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.setTitle("Chat Room");
        pane.getChildren().add(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../view/ChatRoomForm.fxml"))));
    }

    @FXML
    void txtUsernameOnAction(ActionEvent event) throws IOException {
        btnLogOnAction(event);
    }
}
