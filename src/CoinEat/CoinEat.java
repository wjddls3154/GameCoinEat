package CoinEat;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class CoinEat extends JFrame { // JFrame 상속

    private Image bufferImage; // 버퍼 이미지 객체 생성
    private Graphics screenGraphic; // 화면의 이미지를 얻어올 그래픽 객체 생성

    private Clip clip; // 사운드 저장할 변수

    private Image backgroundImage = new ImageIcon("src/images/main.jpg").getImage(); // images 폴더에, 배경 이미지 받아오기
    private Image playerImage = new ImageIcon("src/images/player.png").getImage(); // images 폴더에, 플레이어 이미지 받아오기
    private Image coinImage = new ImageIcon("src/images/coin.png").getImage(); // images 폴더에, 코인 이미지 받아오기

    // 플레이어 설정
    private int playerX, playerY; // player 의 좌표 X,Y 선언
    // 플레이어와 코인의 충돌 여부를 판단하기 위해 각 이미지의 크기 변수에 담기.
    private int playerWidth = playerImage.getWidth(null); // 가로 길이
    private int playerHeight = playerImage.getHeight(null); // 세로 길이

    // 코인 설정
    private int coinX, coinY; // player 의 좌표 X,Y 선언
    // 플레이어와 코인의 충돌 여부를 판단하기 위해 각 이미지의 크기 변수에 담기.
    private int coinWidth = coinImage.getWidth(null); // 가로 길이
    private int coinHeight = coinImage.getHeight(null); // 세로 길이

    private int score; // 점수를 담는 변수 score 선언

    private boolean up, down, left, right ; // 키보드 방향키의 타입을 boolean 으로 하는 이유는 키 두개의 입력을 받아들이기위해서(오른쪽 대각선 등)

    // 게임의 Gui 창 옵션 설정
    public CoinEat() { // 클래스를 만드는 생성자
        setTitle("동전 먹기 게임"); // 제목
        setVisible(true); // 보이기 여부
        setSize(500,500); // GUI 창 크기
        setLocationRelativeTo(null); // setLocationRelativeTo 의 괄호 안에 null 넣으면 실행하면 창이 화면 가운데에 뜬다.
        setResizable(false); // 창 크기 조절못하게 고정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // x 눌러서 창을 닫을 시, 프로세스도 종료되게 한다.
        addKeyListener(new KeyAdapter() { // 키보드 리스너 추가
            public void keyPressed(KeyEvent e) { // 키를 눌렀을 때 실행할 메소드
                switch (e.getKeyCode()) { // 우린 W,A,S,D 를 방향키처럼 게임에서 사용하기로 했다.
                    case KeyEvent.VK_W: // 키보드 W 버튼을 누르면
                        up = true ;     // boolean 타입의 up 변수를 true 시킨다.
                        break;
                    case KeyEvent.VK_A: // 키보드 A 버튼을 누르면
                        left = true ;
                        break;
                    case KeyEvent.VK_S: // 키보드 S 버튼을 누르면
                        down = true ;
                        break;
                    case KeyEvent.VK_D: // 키보드 D 버튼을 누르면
                        right = true ;
                        break;
                }
            }

            public void keyReleased(KeyEvent e) { // 키를 뗐을 때 실행할 메소드
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: // 키보드 W 버튼을 떼면
                        up = false ;    // boolean 타입의 up 변수를 false 시킨다.
                        break;
                    case KeyEvent.VK_A:
                        left = false ;
                        break;
                    case KeyEvent.VK_S:
                        down = false ;
                        break;
                    case KeyEvent.VK_D:
                        right = false ;
                        break;
                }
            }

        });
        Init(); // 초기화 메소드 실행
        // 만들어 놓은 메소드 계속 반복
        while (true) {
            try { // 대기시간 없이 계속 반복하면 무리가 갈수있으므로, 약간의 대기시간 설정.
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            keyProcess();
            crashCheck();
        }
    }

    // 게임을 시작할때 초기화 해주는 Init 메소드
    public void Init() {
        score = 0; // 점수 0점으로 설정
        // 플레이어 좌표 정중앙에 오도록 설정
        playerX = (500 - playerWidth) / 2 ;
        playerY = (500 - playerHeight) / 2 ;

        // 코인의 위치 설정, 코인의 위치는 랜덤이므로 Math.random() 사용
        coinX = (int)(Math.random()*(501-playerWidth)); // random 은 0에서 1사이 난수 나오기때문에 정수형으로 변환시켜 소숫점 삭제, 창의크기+1 - 이미지길이
        coinY = (int)(Math.random()*(501-playerHeight-30))+30; // y 좌표의 경우 프레임 틀의 길이를 생각해 30을 빼준다.

        playSound("src/audio/back.wav",true); // 배경음악, loop 를 true 해서 무한 반복 실행
    }

    // up,down,left,right 의 boolean 값으로 플레이어를 이동시킬 메소드
    public void keyProcess() {
        // 이때, 플레이어의 가로,세로 길이와 이동거리 고려
        if (up && playerY - 3 > 30) playerY-=3;
        if (down && playerY + playerHeight + 3 < 500) playerY+=3;
        if (left && playerX - 3 > 0) playerX-=3;
        if (right && playerX + playerWidth + 3 < 500) playerX+=3;
    }

    // 플레이어와 코인이 닿았을 때 점수 획득 구현
    public void crashCheck() {
        // 먼저 충돌 범위 설정
        if (playerX+playerWidth > coinX && coinX + coinWidth > playerX && playerY + playerHeight > coinY && coinY + coinHeight > playerY) {
            score += 100; // 플레이어와 코인이 닿았을때 +100점 해주고
            playSound("src/audio/button-14.wav",false); // 효과음, loop 를 false 해서 무한 반복 하지 않음
            // 코인의 위치를 랜덤으로 다른 위치로 또 다시 옮겨줌.
            coinX = (int)(Math.random()*(501-playerWidth));
            coinY = (int)(Math.random()*(501-playerHeight-30))+30;
        }

    }

    // 효과음 재생해줄 메소드, 이 메소드를 통해 재생, 무한 반복 여부 설정함.
    public void playSound(String pathName, boolean isLoop) {
        try {
            clip = AudioSystem.getClip();
            File audioFile = new File(pathName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip.open(audioStream); // audioStream 을 clip 에 받아주고
            clip.start(); // start 를 통해 오디오를 재생해준다.
            if (isLoop)
                clip.loop(clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException e) { // 예외처리
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) { // 예외처리
            e.printStackTrace();
        }catch (IOException e) { // 예외처리
            e.printStackTrace();
        }
    }


    // 더블 버퍼링을 이용하면, 버퍼 이미지를 통해 화면의 깜빡임을 최소화 해 준다.
    public void paint(Graphics g){
        bufferImage = createImage(500,500); // 화면 크기의 버퍼 이미지 생성
        screenGraphic = bufferImage.getGraphics(); // getGraphics() 를 통해 그래픽을 받아온다.
        screenDraw(screenGraphic); // 다시 screenDraw 를 호출하여 이 버퍼 이미지를 화면에 그려주는 작업이 반복
        g.drawImage(bufferImage,0,0,null);
    }

    // 이미지를 출력해주는 screenDraw 메소드
    public void screenDraw(Graphics g) {
        // 배경,코인,플레이어 이미지 출력
        g.drawImage(backgroundImage, 0, 0, null); // g.drawImage(이미지,x좌표,y좌표,null)
        g.drawImage(coinImage, coinX, coinY, null);
        g.drawImage(playerImage, playerX, playerY, null);

        // 점수 화면에 출력
        g.setColor(Color.white);
        g.setFont(new Font("Arial",Font.BOLD, 40));
        g.drawString("SCORE : " + score, 30, 80); // 프레임 틀의 길이를 생각해 y 좌표를 더 크게 함.
        this.repaint();
    }

    public static void main(String[] args) {
        new CoinEat(); // 메인 메소드에 CoinEat 객체를 생성함으로써, 위에서 만든 생성자를 호출.

    }

}
