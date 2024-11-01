import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {

        // Create the JFrame
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Brick breaker");

        // Add the GamePanel
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null); // Center the window on the screen
        window.setVisible(true); // Make the window visible
    }
}