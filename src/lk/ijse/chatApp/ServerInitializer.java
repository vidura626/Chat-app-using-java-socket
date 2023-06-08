package lk.ijse.chatApp;

import lk.ijse.chatApp.server.ServerSocketThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerInitializer {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(3000)) {
            System.out.println(serverSocket.getLocalPort());
            System.out.println("Server started");


            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("Client Accepted");

                System.out.println(socket.getPort() + " : Server Side");

                new ServerSocketThread(socket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
