package lk.ijse.chatApp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ServerSocketThread extends Thread {
    public static ArrayList<Socket> sockets = new ArrayList<>();

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private boolean isUTF;

    public ServerSocketThread(Socket socket) {
        this.socket = socket;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String clientFirstMessage = dataInputStream.readUTF();
//            Username is joined
            System.out.println(clientFirstMessage);

//            Send all clients without this client
            for (Socket acceptedSocket : sockets) {
                DataOutputStream dos = new DataOutputStream(acceptedSocket.getOutputStream());
                isUTF = true;
                dos.writeBoolean(isUTF);
                dos.writeUTF(clientFirstMessage);
                dos.flush();
            }
            isUTF = true;
            dataOutputStream.writeBoolean(isUTF);
            dataOutputStream.writeUTF("You are joined to the chat-room");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        add this client to arraylist
        sockets.add(socket);
    }

    @Override
    public void run() {
        try {

            while (true) {
                isUTF = dataInputStream.readBoolean();

//                If UTF
                if (isUTF) {
                    String message = dataInputStream.readUTF();

                    sockets.forEach(acceptedSocket -> {
                        try {
                            if (acceptedSocket.getPort() != socket.getPort()) {
                                DataOutputStream dos = new DataOutputStream(acceptedSocket.getOutputStream());
                                isUTF = true;
                                dos.writeBoolean(isUTF);
                                dos.writeUTF(message);
                                dos.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

//                    If byte (if img)
                } else {
                    byte[] sizeArray = new byte[6];
                    dataInputStream.read(sizeArray);
                    int size = ByteBuffer.wrap(sizeArray).asIntBuffer().get();
                    byte[] imgArray = new byte[size];
                    dataInputStream.read(imgArray);
                    String username = dataInputStream.readUTF();
                    System.out.println("ServerSocket received");
                    sockets.forEach(acceptedSocket -> {
                        if (acceptedSocket.getPort() != socket.getPort()) {
                            try {
                                DataOutputStream dos = new DataOutputStream(acceptedSocket.getOutputStream());
                                isUTF = false;
                                dos.writeBoolean(false);
                                dos.write(sizeArray);
                                dos.write(imgArray);
                                dos.writeUTF(username.trim());
                                dos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    System.out.println("All Sended");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
