package site.metacoding.chat;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyClientSocket {

    Socket socket;
    BufferedWriter writer;

    public MyClientSocket() {
        try {
            socket = new Socket("192.168.0.132", 1077);
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            writer.write("구아현\n"); // buffer에 담기만 했다.
            writer.flush(); // 물 내리기
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}
