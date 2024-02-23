import com.github.bgora.rpnlibrary.Calculator;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class MouseMovementProgram {
    public static final GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[2];
    private Robot robot;
    private boolean isRunning = false;
    private Thread execThread;
    private ITesseract instance = new Tesseract();
    private Calculator calc = Calculator.createCalculator();


    private final Coordinates answer1 = new Coordinates(512, 832);
    private final Coordinates answer2 = new Coordinates(512, 964);
    private final Coordinates answer3 = new Coordinates(512, 1090);

    private final Rectangle captureRect =  new Rectangle(0, 605, 1024, 123);


    public MouseMovementProgram() {
        try {
            robot = new Robot(screen);
            instance.setDatapath("tessdata");
            instance.setTessVariable("user_defined_dpi", "72");
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Press 'K' to toggle mouse movement.");

        CustomKeyListener keyListener = new CustomKeyListener();
        keyListener.registerKeyListener(this::handleKeyPress);

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void handleKeyPress (){
        System.out.println("Handling...");
        isRunning = !isRunning;
        if (isRunning) {
            startMouseMovement();
            System.out.println("Started");
        } else {
            stopMouseMovement();
            System.out.println("Stopped");
        }
    }
    private void moveProgram(){
        while (true){
            makeScreenshot();
            int result = calculate();
            switch (result) {
                case 1 -> {
                    move(answer1);
                    clickLMB();
                }
                case 2 -> {
                    move(answer2);
                    clickLMB();
                }
                case 3 -> {
                    move(answer3);
                    clickLMB();
                }
            }
            wait(1300);
        }
    }
    private void makeScreenshot(){
        BufferedImage img = robot.createScreenCapture(screen.getDefaultConfiguration().getBounds()).getSubimage(captureRect.x, captureRect.y, captureRect.width, captureRect.height);
        File outputfile = new File("scr.png");
        try {
            ImageIO.write(prepareScreenshot(img), "png", outputfile);
            System.out.println("Saved");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private BufferedImage prepareScreenshot(BufferedImage image){
        // Scale down the image
        int scaledWidth = image.getWidth() / 2;
        int scaledHeight = image.getHeight() / 2;
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        double contrast = 5;
        for (int x = 0; x < scaledWidth; x++) {
            for (int y = 0; y < scaledHeight; y++) {
                int rgb = scaledImage.getRGB(x, y);
                Color color = new Color(rgb, true);
                //invert
                color = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
                //b&w
                int gray = (int) (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue());
                //contrast
                gray = Math.min(Math.max((int) ((gray - 50) * contrast + 128), 0), 255);
                color = new Color(gray, gray, gray);

                scaledImage.setRGB(x, y, color.getRGB());
            }
        }
        return scaledImage;
    }
    private int calculate(){
        File img = new File("scr.png");
        try {
            String result = instance.doOCR(img);
            System.out.println("Result string: " + result);
            return conv(result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return 1;
    }
    private int conv(String eq){
        String cut = eq.substring(0, eq.length() - 3);
        System.out.println("Cut string: " + cut);
        BigDecimal result = calc.calculate(cut);
        System.out.println("Result: " + result.intValue());
        return result.intValue();
    }

    private void move(Coordinates shift){
        robot.mouseMove(shift.x, shift.y);
    }
    private void clickLMB(){
        robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
    }
    private void wait(int time){
        try {
            if (Thread.interrupted())
                throw new RuntimeException();
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startMouseMovement() {
        execThread = new Thread(() -> {
            moveProgram();
        });
        execThread.start();
    }

    private void stopMouseMovement() {
        execThread.interrupt();
    }
}