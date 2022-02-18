package site.metacoding.test.test2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

// v2 - 다대다 통신
public class MyServerSocket {

    // 리스너 - 메인스레드(누가 언제 들어올지 모르니 메인 바쁨)
    // 서버는 메시지 받아서 보내기 (클라이언트 수마다) - 새로운 스레드

    ServerSocket serverSocket; // 리스너
    List<고객전담스레드> 고객리스트; // socket 담아두기

    // 생성자에서 실행
    public MyServerSocket() {
        // 모든 통신은 예외발생 -> try catch 사용
        try {
            serverSocket = new ServerSocket(2000);
            고객리스트 = new Vector<>(); // 동기화 처리된 ArrayList
            // 클라이언트 수마다 소켓이 필요 -> while
            while (true) {
                // socket은 와일문 종료되면 날라가는 지역변수 -> 컬렉션에 담아두기
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트 연결됨");
                고객전담스레드 t = new 고객전담스레드(socket);
                고객리스트.add(t);
                System.out.println("고객리스트 크기 : " + 고객리스트.size());
                new Thread(t).start(); // t를 실행
            }

        } catch (Exception e) {
            System.out.println("통신 오류 발생 : " + e.getMessage());
        }
    }

    // 내부 클래스
    class 고객전담스레드 implements Runnable {

        Socket socket; // 소켓 보관
        BufferedReader reader;
        BufferedWriter writer;
        boolean isLogin = true;

        public 고객전담스레드(Socket socket) {
            this.socket = socket;

            // BR, BW 만들기
            try {
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (isLogin) {
                try {
                    // 메시지 읽기
                    String inputData = reader.readLine();
                    System.out.println("from 클라이언트 : " + inputData);

                    // 컬렉션에 담긴 받은 메시지를 모든 클라이언트에게 보내기(BW)
                    for (고객전담스레드 t : 고객리스트) { // 왼 : 컬렉션 타입, 오 : 컬렉션
                        t.writer.write(inputData + "\n");
                        t.writer.flush();
                    }
                } catch (Exception e) {
                    try {
                        System.out.println("통신 실패 : " + e.getMessage());
                        isLogin = false;
                        고객리스트.remove(this); // 더 이상 힙공간 가리키는 주소 X -> 가비지 컬렉션 대상
                        // 가비지 컬렉션을 조금이라도 더 빨리 비우자. (통신의 부하를 줄이기 위해)
                        reader.close();
                        writer.close();
                        socket.close();
                    } catch (Exception e1) {
                        System.out.println("연결해제 프로세스 실패" + e1.getMessage());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new MyServerSocket();
    }
}
