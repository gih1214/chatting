package site.metacoding.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyClientSocket {

    Socket socket;
    BufferedWriter writer;
    Scanner sc;

    // 추가 (서버에게 메시지 받기)
    BufferedReader reader;

    public MyClientSocket() {
        try {
            socket = new Socket("localhost", 1077);
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            // 메시지 받기
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // 2. 이 부분 추가 (클라이언트 소켓쪽)
            new Thread(() -> {
                while (true) {
                    try {
                        String inputData = reader.readLine();
                        System.out.println("받은 메시지 : " + inputData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // 키보드 입력 (반복x)
            sc = new Scanner(System.in);

            // 키보드로부터 입력 받는 부분 (반복)
            while (true) {
                String inputData = sc.nextLine();
                writer.write(inputData + "\n"); // buffer에 담기만 했다.
                writer.flush(); // 물 내리기
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}
