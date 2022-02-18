package site.metacoding.test.test2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

// v2 - 다대다 통신
public class MyClientSocket {

    Socket socket; // 클라이언트 소켓
    Scanner sc; // 키보드로부터 입력 받아서
    BufferedWriter writer; // 서버로 메시지 보내기
    BufferedReader reader; // 서버에게 받은 메시지 읽기

    // 생성자에서 실행
    public MyClientSocket() {
        // 모든 통신은 예외발생 -> try catch
        try {
            socket = new Socket("localhost", 2000); // ip주소, 포트

            // 버퍼 생성 (보내기)
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            sc = new Scanner(System.in);
            // 버퍼 생성 (읽기)
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // 메시지를 읽기 위한(BR) 새로운 스레드
            new Thread(new 읽기전담스레드()).start();

            // 키보드로 입력 받은 메시지 보내기 - 메인 스레드
            while (true) {
                String keyboardInputData = sc.nextLine();
                writer.write(keyboardInputData + "\n"); // 버퍼에 담기
                writer.flush(); // 버퍼 물 내리기 (스트림으로 흘려 보내기)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 내부 클래스
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
