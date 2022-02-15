package site.metacoding.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MyServerSocket {

    ServerSocket serverSocket; // 리스너 (연결 = 세션)
    Socket socket; // 메시지 통신
    BufferedReader reader;

    // 추가 (클라이언트에게 메시지 보내기)
    BufferedWriter writer;
    Scanner sc;

    public MyServerSocket() {
        try {
            // 1. 서버소켓 생성 (리스너)
            // well known port : 0~1023
            serverSocket = new ServerSocket(1077); // 내부적으로 while이 돈다.
            System.out.println("서버 소켓 생성됨");
            socket = serverSocket.accept(); // while을 돌면서 대기 (랜덤포트)
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // 메시지 보내기
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            sc = new Scanner(System.in);

            // 1. 이 부분 추가 (서버 소켓쪽)
            new Thread(() -> {
                while (true) {
                    try {
                        String inputData = sc.nextLine(); // 키보드
                        writer.write(inputData + "\n");
                        writer.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // 메시지 반복해서 받는 서버 소켓 - 메인 스레드
            while (true) {
                String inputData = reader.readLine();
                System.out.println("받은 메시지 : " + inputData);
                // System.out.println("클라이언트 연결됨");
            }

        } catch (Exception e) {
            System.out.println("통신 오류 발생 : " + e.getMessage());
            // e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyServerSocket();
        System.out.println("메인 종료");
    }
}
