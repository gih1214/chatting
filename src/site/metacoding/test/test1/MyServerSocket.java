package site.metacoding.test.test1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

// v1 - 일대일 통신(서버 - 클라이언트)
public class MyServerSocket {

    // 1. 서버 소켓 만들기
    ServerSocket serverSocket; // 리스너
    Socket socket; // 메시지 통신
    BufferedReader reader; // 클라이언트가 보낸 메시지 읽기
    Scanner sc; // 키보드로부터 입력 받아서
    BufferedWriter writer; // 클레이언트에게 메시지 보내기

    // 생성자에서 실행
    public MyServerSocket() {
        // 모든 통신은 예외발생 -> try catch 사용
        try {
            // 2. 서버소켓 생성 (리스너)
            serverSocket = new ServerSocket(2000);
            socket = serverSocket.accept(); // while 돌면서 대기 (랜덤포트)
            // 3. BR 생성
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            // 4. BW 생성
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            sc = new Scanner(System.in);

            // 5. 메시지를 보내기 위한(BW) 새로운 스레드
            new Thread(() -> {
                while (true) { // 채팅처럼 계속해서 메시지를 주고 받기 위해 while 사용
                    try {
                        String inputData = sc.nextLine(); // 키보드 입력
                        writer.write(inputData + "\n"); // 통신 메시지는 끝에 \n을 붙여준다.
                        writer.flush(); // 버퍼 물내리기
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // 6. 메시지 읽기(BR)(반복-while) - 메인 스레드
            // (메인이 바쁘면 다음 코드 실행을 못 하므로 메인 스레드는 다 실행시키고 제일 밑에 적기)
            while (true) {
                String inputData = reader.readLine();
                System.out.println("받은 메시지 : " + inputData);
            }

        } catch (Exception e) {
            System.out.println("통신 오류 발생 : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new MyServerSocket();
        System.out.println("메인 종료");
    }
}
