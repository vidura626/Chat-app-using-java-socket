package lk.ijse.chatApp.controller;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatRoomFormController {

    public static String username;
    public static boolean isUTF;

    @FXML
    private VBox vBox;

    @FXML
    private AnchorPane pane;

    @FXML
    private JFXTextField txtReply;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String message;

    public void initialize() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 3000);
                System.out.println(socket.getPort() + " : Client side");

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataOutputStream.writeUTF(username.concat(" is joined"));
                dataOutputStream.flush();

                while (true) {
                    isUTF = dataInputStream.readBoolean();
                    System.out.println("Client listner : " + isUTF);
                    if (isUTF) {
                        message = dataInputStream.readUTF();
                        Platform.runLater(() -> {
                            vBox.getChildren().add(new HBox(new Text(message + "\n")));
                        });
                    } else {
                        byte[] arraySize = new byte[6];
                        dataInputStream.read(arraySize);
                        int size = ByteBuffer.wrap(arraySize).asIntBuffer().get();
                        byte[] img = new byte[size];
                        dataInputStream.read(img);
                        String uName = dataInputStream.readUTF();
                        BufferedImage readImage = ImageIO.read(new ByteArrayInputStream(img));
                        Image image = SwingFXUtils.toFXImage(readImage, null);
                        System.out.println("Catch");
                        Platform.runLater(() -> {
                            VBox container = new VBox();
                            container.setPrefWidth(vBox.getPrefWidth());

                            Text text = new Text(uName.concat(" : "));
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(150);
                            imageView.setPreserveRatio(true);
                            container.getChildren().add(text);
                            container.getChildren().add(imageView);
                            vBox.getChildren().add(container);
                        });
                        System.out.println("Completed");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        try {
            HBox container = new HBox();
            Text text = new Text("You : ".concat(txtReply.getText().concat("\n")));
            text.setWrappingWidth(vBox.getWidth());
            container.getChildren().add(text);
            vBox.getChildren().add(container);
            vBox.setPrefHeight(vBox.getPrefHeight());
            isUTF = true;
            dataOutputStream.writeBoolean(isUTF);
            dataOutputStream.writeUTF(username.concat(" : ").concat(txtReply.getText()).concat("\n").trim());
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void txtReplyOnAction(ActionEvent event) {
        btnSendOnAction(event);
    }

    @FXML
    void btnImgOnAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();

        List<String> imgExtenstions = new ArrayList<>();
        Collections.addAll(imgExtenstions, "*.jpeg", "*.jpg", "*.png");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", imgExtenstions);
        fileChooser.getExtensionFilters().add(imageFilter);

        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile == null) return;
        String extension = "";
        for (String type : imgExtenstions) {
            String replace = type.replace("*.", "");
            System.out.println("Replace : ".concat(replace));
            if (selectedFile.getName().endsWith(replace)) {
                extension = replace;
                break;
            }
        }

        BufferedImage image = ImageIO.read(selectedFile);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, extension, byteArrayOutputStream);
        byte[] size = ByteBuffer.allocate(6).putInt(byteArrayOutputStream.size()).array();

        System.out.println("Height : " + image.getHeight() + "Width : " + image.getWidth());

        isUTF = false;
        dataOutputStream.writeBoolean(isUTF);
        dataOutputStream.write(size);
        dataOutputStream.write(byteArrayOutputStream.toByteArray());
        dataOutputStream.writeUTF(username.trim());
        dataOutputStream.flush();

        System.out.println("Flushed: " + System.currentTimeMillis());

        Platform.runLater(() -> {
            try {
                ImageView imageView = new ImageView(selectedFile.toURI().toURL().toExternalForm());
                imageView.setFitWidth(150.00);
                imageView.setSmooth(true);
                imageView.setPreserveRatio(true);
                VBox vBox = new VBox(new Text("You : "), imageView);
                vBox.setAlignment(Pos.CENTER_LEFT);
                vBox.setPadding(new Insets(10, 0, 0, 0));
                this.vBox.getChildren().add(vBox);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }
}
