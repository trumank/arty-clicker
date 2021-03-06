package me.kilen.factorio.artyclicker;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class ArtyClicker implements NativeKeyListener {
    public static void main(String[] args) {

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        try {
            Thread.sleep(3000);
            // 0xff990e0e - map biters
            // 0xffff1919 - map train
            // 0xffeea200 - map train wagon

            // 0xff990f0f - map biter
            // 0xff9e1414 - map biter with arty overlay
            // 0xff153956 - map poles/chests/roboports with overlay
            // 0xff155a8b - map poles/chests/roboports with overlay radar coverage
            new ArtyClicker().findAndClick();
            System.exit(0);
        } catch (AWTException | InterruptedException ex) {
            Logger.getLogger(ArtyClicker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static Rectangle getMaximumScreenBounds() {
        int minx=0, miny=0, maxx=0, maxy=0;
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for(GraphicsDevice device : environment.getScreenDevices()){
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            minx = Math.min(minx, bounds.x);
            miny = Math.min(miny, bounds.y);
            maxx = Math.max(maxx,  bounds.x+bounds.width);
            maxy = Math.max(maxy, bounds.y+bounds.height);
        }
        return new Rectangle(minx, miny, maxx-minx, maxy-miny);
    }

    private boolean halt;
    public ArtyClicker() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(this);
    }
    private void findAndClick() throws AWTException, InterruptedException {
        halt = false;
        Rectangle screenRect = new Rectangle(getMaximumScreenBounds());
        Robot bot = new Robot();
        BufferedImage capture = bot.createScreenCapture(screenRect);
        clickRegionSize(capture, bot, 0xff9e1414, 7, 5); // nests
//        clickRegionSize(capture, bot, 0xff9e1414, 3, 3); // worms
//        clickRegionSize(capture, bot, 0xff9e1414, 2, 2);
//        clickRegionSize(capture, bot, 0xff155a8b, 2, 2);
//        clickRegionSize(capture, bot, 0xff155a8b, 1, 1);
//        clickRegionSize(capture, bot, 0xff153956, 2, 2);
//        clickRegionSize(capture, bot, 0xff153956, 1, 1);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    private void clickRegionSize(BufferedImage capture, Robot bot, int color, int width, int height) throws AWTException {
        for (int x = 0; x < capture.getWidth(); x++) {
            for (int y = 0; y < capture.getHeight(); y++) {
                if (halt) return;
                if (capture.getRGB(x, y) == color) {
                    if (check(bot, capture, x, y, width, height, color)) {
                        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    }
                }
            }
        }
    }
    private boolean check(Robot bot, BufferedImage img, int x, int y, int width, int height, int color) throws AWTException {
        if (x + width >= img.getWidth() || y + height >= img.getHeight()) return false;
        boolean valid = true;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                if (img.getRGB(x + w, y + h) != color) {
                    valid = false;
                    break;
                }
            }
        }
        int margin = 3;
        if (valid) {
            for (int w = Math.max(0, x + width / 2 - margin); w < Math.min(img.getWidth() - 1, x + width / 2 + margin); w++) {
                for (int h = Math.max(0, y + height / 2 - margin); h < Math.min(img.getHeight() - 1, y + height / 2 + margin); h++) {
                    img.setRGB(w, h, 0);
                }
            }
            bot.mouseMove(x + width / 2, y + height / 2);
            try {
                Thread.sleep(1000/50);
            } catch (InterruptedException ex) {
                Logger.getLogger(ArtyClicker.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        return false;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_Q) {
            halt = true;
            System.out.println("Exiting");
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }
}
