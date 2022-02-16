package site.metacoding.chat_v3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

// JWP = 채팅 프로토콜
// 1. 최초 메시지는 username으로 체킹
// 2. 구분자 :
// 3. ALL:메시지
// 4. CHAT:아이디:메시지

public class MyServerSocket {

    // 리스너 (연결받기) - 메인 스레드 사용
    ServerSocket serverSocket;
    List<고객전담스레드> 고객리스트; // 컬렉션에 소켓 담기

    // 서버는 메시지 받아서 보내기 (클라이언트 수마다) - 새로운 스레드(고객전담스레드)

    public MyServerSocket() {
        try {
            serverSocket = new ServerSocket(2000);
            고객리스트 = new Vector<>(); // Vector = 동기화가 처리된 ArrayList
            // while 돌리기 (접속하는 클라이언트 수 만큼 소켓을 생성해야됨)
            while (true) {
                Socket socket = serverSocket.accept(); // 리스너 - main 스레드
                System.out.println("클라이언트 연결됨");
                고객전담스레드 t = new 고객전담스레드(socket);
                고객리스트.add(t);
                System.out.println("고객리스트 크기 : " + 고객리스트.size());
                new Thread(t).start(); // 통신은 새로운 스레드
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 내부 클래스 (BR, BW)
    class 고객전담스레드 implements Runnable {

        String username; // 최초 메시지만 담기
        Socket socket; // 소켓 보관

        BufferedReader reader;
        BufferedWriter writer;

        boolean isLogin;

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

        // ALL:메시지
        public void chatPublic(String msg) {
            try {
                // 일반 for문과는 다르게 정해진 수만큼 for문 돌아감(컬렉션 사이즈만큼)
                // 프로토콜 검사가 후 받은 메시지를 List<고객전담스레드> 고객리스트 <== 여기에 담긴
                // 모든 클라이언트에게 메시지 전송 (for문 돌려서!!)
                for (고객전담스레드 t : 고객리스트) { // 왼쪽 : 컬렉션 타입, 오른쪽 : 컬렉션
                    if (t != this) {
                        t.writer.write(username + " : " + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // CHAT:아이디:메시지
        public void chatPrivate(String receiver, String msg) {
            try {
                // 프로토콜 검사가 후 받은 메시지를 List<고객전담스레드> 고객리스트 <== 여기에 담긴
                // 특정 클라이언트에게 메시지 전송(귓속말) (for문 돌려서!!)
                for (고객전담스레드 t : 고객리스트) { // 왼쪽 : 컬렉션 타입, 오른쪽 : 컬렉션
                    if (t.username.equals(receiver)) {
                        t.writer.write("[귓속말] " + username + " : " + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 채팅 프로토콜 검사기
        // ALL:안녕
        // CHAT:익명:안녕
        public void jwp(String inputData) {
            // 1. 프로토콜 분리
            String[] token = inputData.split(":");
            String protocol = token[0];
            if (protocol.equals("ALL")) {
                String msg = token[1];
                chatPublic(msg); // 전체 메시지 보내기
            } else if (protocol.equals("CHAT")) {
                String receiver = token[1];
                String msg = token[2];
                chatPrivate(receiver, msg); // 귓속말 보내기
            } else { // 프로토콜 통과 못함.
                System.out.println("프로토콜 없음");
            }
        }

        @Override
        public void run() {
            try {
                // 최초 메시지는 username이다. (while X)
                username = reader.readLine();
                isLogin = true;
            } catch (Exception e) {
                isLogin = false;
                System.out.println("username을 받지 못했습니다.");
            }

            while (isLogin) {
                try {
                    String inputData = reader.readLine();

                    // 받은 메시지 프로토콜 검사기 메서드로 보내기
                    jwp(inputData);
                } catch (Exception e) {
                    try {
                        System.out.println("통신 실패 : " + e.getMessage());
                        isLogin = false;
                        고객리스트.remove(this); // 더 이상 힙공간을 가리키는 주소가 없다. -> 가비지 컬렉션 대상

                        // 가비지 컬렉션 조금이라도 더 빨리 비우기(통신의 부하를 줄이기 위해)
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
