import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CustomKeyListener {
    private int key = 37;
    private Runnable handler;

    public void registerKeyListener(Runnable handler) {
        this.handler = handler;
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Failed to register native hook. Exiting.");
            System.exit(1);
        }

        // Add the listener
        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
    }

    private void keyPressed() {
        handler.run();
    }

    private class GlobalKeyListener implements NativeKeyListener {
        @Override
        public void nativeKeyTyped(NativeKeyEvent e) {
            // Not used
        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            System.out.println("Something pressed: " + e.getKeyCode());
            if (e.getKeyCode() == key) {
                System.out.println("Internal press");
                keyPressed();
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
            // Not used
        }
    }
}