package site.metacoding.test.test3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

// v3 - 다대다 통신 + 프로토콜 추가

// JWP = 채팅 프로토콜
// 1. 최초 메시지는 username으로 체킹
// 2. 구분자 :
// 3. ALL:메시지
// 4. CHAT:아이디:메시지
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
                new Thread(t).start(); // t를 실행(통신은 새로운 스레드)
            }

        } catch (Exception e) {
            System.out.println("통신 오류 발생 : " + e.getMessage());
        }
    }

    // 내부 클래스 (BR, BW)
    class 고객전담스레드 implements Runnable {

        String username; // 최초 메시지만 컬렉션에 담기
        Socket socket; // 소켓 보관
        BufferedReader reader;
        BufferedWriter writer;
        boolean isLogin;

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

        // 프로토콜 메서드 생성
        // 전체전송 -> ALL:메시지
        public void chatPublic(String msg) {
            try {
                // 전체 보내기
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

        // 귓속말 -> CHAT:아이디:메시지
        public void chatPrivate(String receiver, String msg) {
            try {
                // 특정 클라이언트에게 메시지 전송(귓속말)
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

        // 프로토콜 검사기
        // ALL:안녕
        // CHAT:익명:안녕
        public void jwp(String inputData) {
            // 배열로 프로토콜 분리
            String[] token = inputData.split(":"); // :를 기준으로 파싱
            String protocol = token[0]; // 0번지의 프로토콜
            if (protocol.equals("ALL")) {
                String msg = token[1];
                chatPublic(msg); // 전체 메시지 보내기
            } else if (protocol.equals("CHAT")) {
                String receiver = token[1];
                String msg = token[2];
                chatPrivate(receiver, msg); // 귓속말 보내기
            } else { // 프로토콜 통과 못함
                System.out.println("프로토콜 없음");
            }
        }

        @Override
        public void run() {
            try {
                // 최초 메시지는 username (while X)
                username = reader.readLine();
                isLogin = true;
            } catch (Exception e) {
                isLogin = false;
                System.out.println("username을 받지 못했습니다.");
            }

            while (isLogin) {
                try {
                    // 두번째 메시지부터 inputData
                    String inputData = reader.readLine();
                    jwp(inputData); // 받은 메시지 프로토콜 검사 필요
                } catch (Exception e) {
                    try {
                        System.out.println("통신 실패 : " + e.getMessage());
                        isLogin = false;
                        고객리스트.remove(this);
                        reader.close();
                        writer.close();
                        socket.close();
                    } catch (Exception e1) {
                        System.out.println("연결해제 프로세스 실패 : " + e1.getMessage());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new MyServerSocket();
    }
}
