import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    final int panelWidth = 500, panelHeight = 600;
    Timer timer;
    int paddleX;
    final int paddleY = 585; // Adjusted paddleY to be above the bottom
    final int paddleHeight = 12;
    int paddleWidth;
    int paddleSpeed;

    Point ball;

    final int ballDiameter = 20;
    final int ballWidth = 20;

    double ballXVelocity, ballYVelocity, ballSpeed;

    ArrayList<Rectangle> bricks = new ArrayList<>();
    int brickWidth, brickHeight, noOfBricksInARow, noOfRows;

    boolean gameBegin = false;
    boolean moveLeft = false, moveRight = false; // Track key states

    GamePanel() {
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        this.setVisible(true);
        this.setFocusable(true);
        this.requestFocusInWindow(); // Request focus for key events
        this.addKeyListener(this);
        paddleWidth = 100;
        paddleX = panelWidth / 2 - paddleWidth / 2;
        paddleSpeed = 10; 
        // The ball should always start on top of the paddle but it's x position could
        // be anywhere on the paddle
        Random random = new Random();
        ball = new Point();
        ball.x = random.nextInt((paddleX + paddleWidth) - paddleX + 1) + paddleX;
        ball.y = paddleY - ballDiameter + 1;

        ballXVelocity = 0;
        ballYVelocity = 0;
        ballSpeed = 5;

        generateBricks();

        // Start the game timer
        timer = new Timer(10, this);
        timer.start();
    }

    public void paint(Graphics g) {
        super.paint(g); // paint background
        Graphics2D g2D = (Graphics2D) g;

        // Paint the black background
        g2D.setColor(Color.BLACK);
        g2D.fillRect(0, 0, panelWidth, panelHeight);

        // Paint the paddle in white
        g2D.setColor(Color.WHITE);
        g2D.fillRect(paddleX, paddleY, paddleWidth, paddleHeight); // Use paddleHeight

        g2D.setColor(Color.RED);
        g2D.fillOval(ball.x, ball.y, ballWidth, ballDiameter);

        g2D.setColor(Color.GREEN);

        for (int i = 0; i < bricks.size(); i++)
            g2D.draw(bricks.get(i));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameBegin == false) {
            // Start the game when the UP arrow key is pressed
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                gameBegin = true;
                ballYVelocity = -ballSpeed; // Start ball moving upward
            }
        }

        // Paddle movement
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                moveLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
                moveRight = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                moveLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
                moveRight = false;
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Continuously update paddle position based on key states
        if (gameBegin) {
            if (moveLeft && paddleX > 0) {
                paddleX -= paddleSpeed;
            }
            if (moveRight && paddleX < panelWidth - paddleWidth) {
                paddleX += paddleSpeed;
            }

            moveBall();
        }

        checkCollision();
        repaint();
    }

    void moveBall() {
        Rectangle ballRect = new Rectangle(ball.x, ball.y, ballDiameter, ballDiameter);
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

        if (ballRect.intersects(paddleRect)) {
            double angleComponent = ((double) ball.x - ((double) paddleX + ((double) paddleWidth / 2))) / ((double) paddleWidth / 2);

            double newAngle = Math.toRadians(45) * angleComponent;
            ballXVelocity = ballSpeed * Math.sin(newAngle);
            ballYVelocity = -ballSpeed * Math.cos(newAngle);

            playSound("sounds/ball.wav");
        }

        if (ball.x + ballDiameter + (int) ballXVelocity >= 500 || ball.x <= 0) {
            ballXVelocity = -ballXVelocity;
            playSound("sounds/ball.wav");
        }

        if (ball.y <= 0) {
            ballYVelocity = -ballYVelocity;
            playSound("sounds/ball.wav");
        }

        if (ball.y >= 600) {
            playSound("sounds/game_over.wav");
            JOptionPane.showMessageDialog(null, "Game Over!");
            System.exit(0);
        }

        ball.x = ball.x + (int) ballXVelocity;
        ball.y = ball.y + (int) ballYVelocity;
    }

    void generateBricks() {
        noOfBricksInARow = 8;
        noOfRows = 5;
        brickWidth = (int) (Math.floor((panelWidth - 1) / noOfBricksInARow));
        brickHeight = brickWidth / 2;
        int y = 50;
        int x = 2;
        for (int i = 1; i <= noOfRows; i++) {
            y = y + brickHeight;
            for (int j = 0; j < noOfBricksInARow; j++)
                bricks.add(new Rectangle(x + (brickWidth * j), y, brickWidth, brickHeight));
        }
    }

    void checkCollision() {
        Rectangle ballRect = new Rectangle(new Rectangle(ball.x, ball.y, ballDiameter, ballDiameter));
        for (int i = 0; i < bricks.size(); i++) {
            if (ballRect.intersects(bricks.get(i))) {
                bricks.remove(bricks.get(i));
                playSound("sounds/ball.wav");
                ballYVelocity = -ballYVelocity;
            }
        }

        if (bricks.size() == 0) {
            playSound("sounds/win.wav");
            JOptionPane.showMessageDialog(null, "You win!");
            System.exit(0);
        }
    }

    public void playSound(String soundFileName) {
        try {
            // Load the sound file
            File soundFile = new File(soundFileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            // Get a clip resource
            Clip clip = AudioSystem.getClip();

            // Open the audio stream and start playing it
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
