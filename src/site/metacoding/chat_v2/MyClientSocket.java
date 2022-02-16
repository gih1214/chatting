package site.metacoding.chat_v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyClientSocket {

    Socket socket;

    // 키보드로 부터 받아서 바로 쓸 스레드
    Scanner sc;
    BufferedWriter writer;

    // 읽는 스레드
    BufferedReader reader;

    public MyClientSocket() {
        try {
            socket = new Socket("localhost", 2000);

            sc = new Scanner(System.in);
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // 새로운 스레드 (읽기 전용)
            new Thread(new 읽기전담스레드()).start();

            // 메인 스레드 (쓰기 전용) -> 메인이 실행 마지막
            while (true) {
                String keyboardInputData = sc.nextLine();
                writer.write(keyboardInputData + "\n"); // 버퍼에 담기
                writer.flush(); // 버퍼에 담긴 것을 stream으로 흘려보내기
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class 읽기전담스레드 implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    String inputData = reader.readLine();
                    System.out.println("받은 메시지 : " + inputData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}
