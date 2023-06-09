

// Map1, 2, 3의 부모클래스로, 게임 화면을 나타내는 곳입니다.


package game;

import entity.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GamePanel extends JPanel implements Runnable {
    protected int mapNumber;
    protected int characterType;
    int x, y, width, height;
    protected GameFrame gameFrame;
    protected Thread gameThread;
    protected final int originalTileSize = 48;
    protected final int scale = 2;
    public final int tileSize = originalTileSize * scale;
    protected final int maxScreenCol = 12;
    protected final int maxScreenRow = 9;
    protected final int screenWidth = tileSize * maxScreenCol;
    protected final int screenHeight = tileSize * maxScreenRow;
    protected int FPS = 60;
    protected KeyHandler keyH = new KeyHandler();
    protected volatile boolean running = false;
    protected PlayerU playerU;
    protected PlayerY playerY;
    protected PlayerM playerM;
    protected EnemyMuzan muzan;
    protected EnemyAkaza akaza;
    protected EnemyKoku koku;
    protected JLabel homeLabel;

    public GamePanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setLayout(null);

        int panelWidth = 1152;
        int panelHeight = 864;

        ImageIcon homeIcon = new ImageIcon(getClass().getResource("/res/home.png")); // 홈 아이콘 이미지 불러오기
        ImageIcon homeIconEntered = new ImageIcon(getClass().getResource("/res/home.png")); // 마우스를 올렸을 때의 아이콘도 설정해줍니다. 필요에 따라 다른 아이콘으로 변경 가능합니다.

        homeLabel = new JLabel(homeIcon);
        homeLabel.setBounds(1080, 10, homeIcon.getIconWidth(), homeIcon.getIconHeight()); // 홈 아이콘의 위치와 크기 설정
        add(homeLabel);

        // 마우스 이벤트 처리를 위한 클래스를 생성하고 이를 MouseListener로 추가
        ButtonClickedEvent buttonClickedEvent = new ButtonClickedEvent(gameFrame, GameFrame.BEGINNING_PANEL, homeIconEntered, homeIcon);
        homeLabel.addMouseListener(buttonClickedEvent);
    }


    public void setMapNumber(int mapNumber) {
        this.mapNumber = mapNumber;
    }

    public void setCharacterType(int characterType) {
        this.characterType = characterType;
    }

    public void startGameThread() {
        if (gameThread == null || !running) {
            gameThread = new Thread(this);
            running = true;
            gameThread.start();
        }
    }
    public void stopGameThread() {
        if (gameThread != null && running) {
            running = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void dispose() {
        stopGameThread();
        playerU = null;
        playerY = null;
        playerM = null;
        muzan = null;
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        running = true;

        while (gameThread != null && running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }
            if (timer >= 1000000000) {
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        if (isVisible()) {
            switch (characterType) {
                case 0 -> {
                    if (playerU != null)  playerU.update();
                    break;
                }
                case 1 -> {
                    if (playerY != null) playerY.update();
                    break;
                }
                case 2 -> {
                    if (playerM != null) playerM.update();
                    break;
                }
            }
            if(muzan != null) muzan.update();

            if ((playerU != null && playerU.getHp() <= 0) ||
                    (playerY != null && playerY.getHp() <= 0) ||
                    (playerM != null && playerM.getHp() <= 0) ) {
                // 체력이 0이 된 경우의 추가적인 처리를 실행
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "게임이 종료되었습니다.");
                    returnToBeginningPanel();
                });
                stopGameThread();
            }
            if (muzan != null && muzan.getHp() <= 0) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "다음 단계로 넘어감");
                    returnToCreditPanel();
                });
                stopGameThread();
            }
//            if (muzan != null && muzan.getHp() <= 0) {
//                SwingUtilities.invokeLater(() -> {
//                    JOptionPane.showMessageDialog(null, "다음 단계로 넘어감");
//                    goingToTheSecondPhase();
//                });
//                stopGameThread();
//            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawBackground(g2);
        switch (characterType) {
            case 0 -> playerU.draw(g2);
            case 1 -> playerY.draw(g2);
            case 2 -> playerM.draw(g2);
        }
        muzan.draw(g2);
        drawEnemyHp(g2); // 적의 체력을 그리는 코드 추가
        drawPlayerHp(g2);
//        g2.dispose();
    }

    private void drawBackground(Graphics2D g2) {
        ImageIcon backgroundIcon;
        switch (mapNumber) {
            case 1 -> backgroundIcon = new ImageIcon(getClass().getResource("/res/백그라운드1.png"));
            case 2 -> backgroundIcon = new ImageIcon(getClass().getResource("/res/backgroundG2.png"));
            case 3 -> backgroundIcon = new ImageIcon(getClass().getResource("/res/backgroundG3.png"));
            default -> backgroundIcon = new ImageIcon(getClass().getResource("/res/background1.png"));
        }
        Image backgroundImage = backgroundIcon.getImage();
        g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    public void returnToBeginningPanel() {
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                dispose();
                try {
                    gameFrame.swapPanel(GameFrame.BEGINNING_PANEL);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public void returnToCreditPanel() {
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                dispose();
                try {
                    gameFrame.swapPanel(GameFrame.CREDIT_PANEL);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
//    public void goingToTheSecondPhase() {
//        SwingUtilities.invokeLater(() -> {
//            if (gameFrame != null) {
//                dispose();
//                gameFrame.swapPanel(GameFrame.CREDIT_PANEL);
//            }
//        });
//    }


    private void drawPlayerHp(Graphics2D g2) {
        String hpText = "";
        switch (characterType) {
            case 0 -> hpText = "Player HP: " + playerU.getHp();
            case 1 -> hpText = "Player HP: " + playerY.getHp();
            case 2 -> hpText = "Player HP: " + playerM.getHp();
        }
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        int x = 20;
        int y = fm.getHeight()+50;
        g2.drawString(hpText, x, y);
    }



    private void drawEnemyHp(Graphics2D g2) {
        String hpText = "Enemy HP: " + muzan.getHp();
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        int x = getWidth() - fm.stringWidth(hpText) - 50; // 오른쪽 상단에 위치하도록 x 좌표 계산
        int y = fm.getHeight()+50; // 상단에 위치하도록 y 좌표 계산
        g2.drawString(hpText, x, y);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startGameThread();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        stopGameThread();
    }

}
