/*
Developer: Hung Pham
 */
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        createWindowFrames();
    }

    private static void createWindowFrames() {
        JFrame window = new JFrame("Julia Set Generation");
        JuliaSet julia = new JuliaSet();
        window.setContentPane(julia);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation((screenSize.width - window.getWidth()) / 2,
                (screenSize.height - window.getHeight()) / 2);
        window.setVisible(true);

        JFrame window1 = new JFrame("MultiCore - Julia Set Generation");
        MultiCore_JuliaSet firstAttempt = new MultiCore_JuliaSet();
        window1.setContentPane(firstAttempt);
        window1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window1.pack();
        window1.setResizable(false);
        window1.setLocation((screenSize.width - window1.getWidth()) / 2,
                (screenSize.height - window1.getHeight()) / 2);
        window1.setVisible(true);

        JFrame window2 = new JFrame("Improved MultiCore - Julia Set Generation");
        Improved_MultiCore_JuliaSet betterJulia = new Improved_MultiCore_JuliaSet();
        window2.setContentPane(betterJulia);
        window2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window2.pack();
        window2.setResizable(false);
        window2.setLocation((screenSize.width - window2.getWidth()) / 2,
                (screenSize.height - window2.getHeight()) / 2);
        window2.setVisible(true);
    }
}
