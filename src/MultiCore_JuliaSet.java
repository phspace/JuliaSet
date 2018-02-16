/*
Developer: Hung Pham
Reference to the book: Introduction to Programming Using Java, David J. Eck
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class MultiCore_JuliaSet extends JPanel implements Julia {

    private double startTime;
    private double endTime;
    private double runTime;

    private TaskProcessing[] taskRunnings;
    private volatile boolean isRunning;
    private volatile int coreStopped;
    private JButton startButton;
    private JComboBox<String> coresSelection;
    private BufferedImage image;

    // initialize a panel for displaying image
    private JPanel display = new JPanel() {
        protected void paintComponent(Graphics g) {
            if (image == null)
                super.paintComponent(g);  // fill with background color, gray
            else {
                synchronized (image) {
                    g.drawImage(image, 0, 0, null);
                }
            }
        }
    };

    /**
     * Constructor creates a panel to hold the display, with a "Start" button and options for Core selection below it.
     */
    public MultiCore_JuliaSet() {
        display.setPreferredSize(new Dimension(1280, 720));
        display.setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setLayout(new BorderLayout());
        add(display, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        startButton = new JButton("Start");
        bottom.add(startButton);
        // add Core selections options
        coresSelection = new JComboBox<String>();
        coresSelection.addItem("Use 1 core.");
        coresSelection.addItem("Use 2 cores.");
        coresSelection.addItem("Use 3 cores.");
        coresSelection.addItem("Use 4 cores.");
        // default number of cores are 2
        coresSelection.setSelectedIndex(1);
        bottom.add(coresSelection);
        bottom.setBackground(Color.WHITE);
        add(bottom, BorderLayout.SOUTH);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isRunning)
                    stop();
                else
                    start();
            }
        });
    }

    private void start() {
        startButton.setText("Abort");
        coresSelection.setEnabled(false);
        int width = display.getWidth() + 2;
        int height = display.getHeight() + 2;
        if (image == null)
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.dispose();
        display.repaint();
        int coreNumber = coresSelection.getSelectedIndex() + 1;
        taskRunnings = new TaskProcessing[coreNumber];
        int rowsPerThread;
        rowsPerThread = height / coreNumber;
        isRunning = true;
        coreStopped = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < coreNumber; i++) {
            int startRow;
            int endRow;
            startRow = rowsPerThread * i;
            if (i == coreNumber - 1)
                endRow = height - 1;
            else
                endRow = rowsPerThread * (i + 1) - 1;
            taskRunnings[i] = new TaskProcessing(startRow, endRow);
            try {
                taskRunnings[i].setPriority(Thread.currentThread().getPriority() - 1);
            } catch (Exception e) {
            }
            taskRunnings[i].start();
        }
    }

    private void stop() {
        startButton.setEnabled(false);
        isRunning = false;
    }

    synchronized private void threadFinished() {
        coreStopped++;
        if (coreStopped == taskRunnings.length) {
            startButton.setText("Start Again");
            startButton.setEnabled(true);
            taskRunnings = null;
            coresSelection.setEnabled(true);
            endTime = System.currentTimeMillis();
            runTime = (endTime - startTime) / 1000;
            System.out.println("Application runs in: " + runTime + " seconds.");
            isRunning = false;
        }

    }

    public double getRuntime() {
        while (isRunning) ;
        return runTime;

    }

    private class TaskProcessing extends Thread {
        int[] rgb;
        int[] palette;
        int width, height;

        int maxIteration;
        double C_Re, C_Im;
        int startRow, endRow;

        TaskProcessing(int startRow, int endRow) {
            this.startRow = startRow;
            this.endRow = endRow;
            width = image.getWidth();
            height = image.getHeight();

            maxIteration = MAX_ITERATION;
            C_Re = C_REAL;
            C_Im = C_IMAGINATION;
            rgb = new int[width];
            palette = new int[256];
            for (int i = 0; i < 256; i++)
                palette[i] = Color.getHSBColor(i / 255F, 1, 1).getRGB();
        }

        /**
         * Check if the pixel is in Julia Set and set collor for pixel
         */
        public void run() {
            try {
                double newRe, newIm, oldRe, oldIm;
                int i = 0;
                for (int y = startRow; y <= endRow; y++) {  // Compute one row of pixels.
                    for (int x = 0; x < width; x++) {
                        i = 0;
                        newRe = 1.5 * (x - width / 2) / (0.5 * ZOOM * width);
                        newIm = (y - height / 2) / (0.5 * ZOOM * height);

                        while (i < maxIteration && (newRe * newRe + newIm * newIm) < 4) {
                            i++;
                            oldRe = newRe;
                            oldIm = newIm;
                            newRe = oldRe * oldRe - oldIm * oldIm + C_Re;
                            newIm = 2 * oldRe * oldIm + C_Im;
                        }
                        rgb[x] = palette[i % palette.length];
                    }
                    if (!isRunning) {
                        return;
                    }
                    synchronized (image) {
                        image.setRGB(0, y, width, 1, rgb, 0, width);
                    }
                    display.repaint(0, y, width, 1);
                }
            } finally {
                threadFinished();
            }
        }
    }

}