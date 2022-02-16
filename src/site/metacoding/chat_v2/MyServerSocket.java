package site.metacoding.chat_v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class MyServerSocket {

    // 리스너 (연결받기) - 메인 스레드 사용
    ServerSocket serverSocket;
    List<고객전담스레드> 고객리스트;

    // 서버는 메시지 받아서 보내기 (클라이언트 수마다) - 새로운 스레드

    public MyServerSocket() {
        try {
            serverSocket = new ServerSocket(2000);
            고객리스트 = new Vector<>(); // 동기화가 처리된 ArrayList
            // while 돌리기 (소켓이 여러사람 다 있어야 하니까)
            while (true) {
                Socket socket = serverSocket.accept(); // main 스레드
                System.out.println("클라이언트 연결됨");
                고객전담스레드 t = new 고객전담스레드(socket);
                고객리스트.add(t);
                System.out.println("고객리스트 크기 : " + 고객리스트.size());
                new Thread(t).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 내부 클래스
    class 고객전담스레드 implements Runnable {

        // 소켓 보관
        Socket socket;

        BufferedReader reader;
        BufferedWriter writer;

        boolean isLogin = true;

        public 고객전담스레드(Socket socket) {
            this.socket = socket;

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
                    String inputData = reader.readLine();
                    System.out.println("from 클라이언트 : " + inputData);

                    // 메시지 받았으니까 List<고객전담스레드> 고객리스트 <== 여기에 담긴
                    // 모든 클라이언트에게 메시지 전송 (for문 돌려서!!)
                    // for each문
                    for (고객전담스레드 t : 고객리스트) { // 왼쪽 : 컬렉션 타입, 오른쪽 : 컬렉션
                        t.writer.write(inputData + "\n");
                        t.writer.flush();
                    }
                } catch (Exception e) {
                    try {
                        System.out.println("통신 실패 : " + e.getMessage());
                        isLogin = false;
                        고객리스트.remove(this); // 더 이상 힙공간을 가리키는 주소가 없다. -> 가비지 컬렉션 대상

                        // 가비지 컬렉션 조금이라도 더 빨리 비우려고(통신의 부하를 줄이기 위해)
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
