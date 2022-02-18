package site.metacoding.test.test1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

// v1 - 일대일 통신(서버 - 클라이언트)
public class MyClientSocket {

    // 1. 클라이언트 소켓 만들기
    Socket socket;
    Scanner sc; // 키보드로부터 입력 받아서
    BufferedWriter writer; // 서버로 메시지 보내기
    BufferedReader reader; // 서버에게 메시지 받기

    // 생성자에서 실행
    public MyClientSocket() {
        // 모든 통신은 예외발생 -> try catch
        try {
            // 2. 클라이언트 소켓 생성
            socket = new Socket("localhost", 2000); // ip주소, 포트
            // 3. 버퍼 생성 (보내기)
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            sc = new Scanner(System.in);
            // 4. 버퍼 생성 (읽기)
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // 5. 메시지를 읽기 위한(BR) 새로운 스레드
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

            // 6. 키보드로 입력 받은 메시지 보내기 - 메인 스레드
            while (true) {
                String inputData = sc.nextLine();
                writer.write(inputData + "\n"); // 버퍼에 담기
                writer.flush(); // 버퍼 물 내리기
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}
