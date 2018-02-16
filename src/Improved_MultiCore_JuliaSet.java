/*
Developer: Hung Pham
Reference to the book: Introduction to Programming Using Java, David J. Eck
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;

public class Improved_MultiCore_JuliaSet extends JPanel implements Julia{

    private double startTime;
    private double endTime;
    private double runTime;

    int[] palette;
    private Workers[] workers;

    private LinkedBlockingQueue<Runnable> tasksWaitingProcess;

    private JComboBox<String> coresSelection;

    private int taskCount;
    private volatile int tasksCompleted;

    private volatile boolean isRunning;

    private JButton startButton;
    private BufferedImage image;

    // initialize a panel for displaying image
    private JPanel display = new JPanel() {
        protected void paintComponent(Graphics g) {
            if (image == null)
                super.paintComponent(g);
            else {
                synchronized (Improved_MultiCore_JuliaSet.this) {
                    g.drawImage(image, 0, 0, null);
                }
            }
        }
    };

    public Improved_MultiCore_JuliaSet() {
        display.setPreferredSize(new Dimension(1280, 720));
        display.setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setLayout(new BorderLayout());
        add(display, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        startButton = new JButton("Start");
        bottom.add(startButton);
        bottom.setBackground(Color.WHITE);
        add(bottom, BorderLayout.SOUTH);
        coresSelection = new JComboBox<String>();
        coresSelection.addItem("Use 4 cores.");
        coresSelection.setSelectedIndex(0);
        bottom.add(coresSelection);
        palette = new int[256];
        for (int i = 0; i < 256; i++)
            palette[i] = Color.getHSBColor(i / 255F, 1, 1).getRGB();
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isRunning)
                    stop();
                else
                    start();
            }
        });
        tasksWaitingProcess = new LinkedBlockingQueue<Runnable>();
    }

    synchronized private void start() {
        startButton.setText("Abort");
        int width = display.getWidth() + 2;
        int height = display.getHeight() + 2;
        startTime = System.currentTimeMillis();
        int processors = 4;
        if (image == null) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            workers = new Workers[processors];
            for (int i = 0; i < processors; i++) {
                workers[i] = new Workers();
            }
        }
        Graphics g = image.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.dispose();
        display.repaint();

        for (int y = 0; y < height; y++) {
            JuliaSetTask task = new JuliaSetTask(y, width, height, MAX_ITERATION, C_REAL, C_IMAGINATION);
            tasksWaitingProcess.add(task);
        }
        tasksCompleted = 0;
        taskCount = height;
        isRunning = true;
    }

    synchronized private void stop() {
        startButton.setText("Start Again");
        tasksWaitingProcess.clear();
        endTime = System.currentTimeMillis();
        runTime = (endTime - startTime) / 1000;
        System.out.println("Application runs in: " + runTime + " seconds.");
        isRunning = false;
    }

    synchronized private void taskFinished(JuliaSetTask task) {
        tasksCompleted++;
        image.setRGB(0, task.rowNumber, task.width, 1, task.rgb, 0, task.width);
        display.repaint(0, task.rowNumber, task.width, 1);
        if (tasksCompleted == taskCount) {
            stop();
        }
    }

    public double getRuntime() {
        while(isRunning);
        return runTime;
    }

    private class JuliaSetTask implements Runnable {
        int rowNumber;
        int[] rgb;
        int width, height;

        int maxIteration;
        double C_Re, C_Im;

        JuliaSetTask(int y, int width, int height, int maxIterations, double C_Re, double C_Im) {
            this.rowNumber = y;
            this.width = width;
            this.height = height;
            maxIteration = maxIterations;
            this.C_Re = C_Re;
            this.C_Im = C_Im;
            this.width = width;
        }

        /**
         * Check if the pixel is in Julia Set and set collor for pixel
         */
        public void run() {
            rgb = new int[width];
            int i = 0;
            double newRe, newIm, oldRe, oldIm;
            for (int x = 0; x < rgb.length; x++) {
                i = 0;
                newRe = 1.5 * (x - width / 2) / (0.5 * ZOOM * width);
                newIm = (rowNumber - height / 2) / (0.5 * ZOOM * height);

                while (i < maxIteration && (newRe * newRe + newIm * newIm) < 4) {
                    i++;
                    oldRe = newRe;
                    oldIm = newIm;
                    newRe = oldRe * oldRe - oldIm * oldIm + C_Re;
                    newIm = 2 * oldRe * oldIm + C_Im;
                }
                rgb[x] = palette[i % palette.length];
            }
            taskFinished(this);
        }
    }

    private class Workers extends Thread {
        Workers() {
            try {
                setPriority(Thread.currentThread().getPriority() - 1);
            } catch (Exception e) {
            }
            try {
                setDaemon(true);
            } catch (Exception e) {
            }
            start();
        }

        public void run() {
            while (true) {
                try {
                    Runnable task = tasksWaitingProcess.take();
                    task.run();
                } catch (InterruptedException e) {
                }
            }
        }
    }


}