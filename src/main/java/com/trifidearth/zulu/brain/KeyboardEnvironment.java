package com.trifidearth.zulu.brain;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

public class KeyboardEnvironment extends Environment implements Runnable {

    private static final Logger log = Logger.getLogger(KeyboardEnvironment.class);

    public KeyboardEnvironment(Brain brain) {
        super(brain);
    }

    @Override
    public void run() {

        JFrame frame = new JFrame("Key Listener");
        Container contentPane = frame.getContentPane();
        KeyListener listener = new KeyListener() {

            @Override
            public void keyPressed(KeyEvent event) {
                //printEventInfo("Key Pressed", event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
                //printEventInfo("Key Released", event);
            }

            @Override
            public void keyTyped(KeyEvent event) {
                brain.stimulate(""+event.getKeyChar());
                printEventInfo("Key Typed", event);
            }

            private void printEventInfo(String str, KeyEvent e) {
                //log.debug(str);

                if(e.getKeyChar() == '+') {
                    try {
                        String snapshot = "brain_SNAPSHOT_" + brain.info() + "_" + System.currentTimeMillis() + ".json";
                        log.info("Writing brain SNAPSHOT @ " + snapshot);
                        brain.serialize(new File(snapshot));
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }

                //int code = e.getKeyCode();
                //log.debug("   Code: " + KeyEvent.getKeyText(code));
                log.debug("   Char: " + e.getKeyChar());
                //int mods = e.getModifiersEx();
                //log.debug("    Mods: "
                //        + KeyEvent.getModifiersExText(mods));
                //log.debug("    Location: "
                //        + keyboardLocation(e.getKeyLocation()));
                //log.debug("    Action? " + e.isActionKey());
            }

            private String keyboardLocation(int keybrd) {
                switch (keybrd) {
                    case KeyEvent.KEY_LOCATION_RIGHT:
                        return "Right";
                    case KeyEvent.KEY_LOCATION_LEFT:
                        return "Left";
                    case KeyEvent.KEY_LOCATION_NUMPAD:
                        return "NumPad";
                    case KeyEvent.KEY_LOCATION_STANDARD:
                        return "Standard";
                    case KeyEvent.KEY_LOCATION_UNKNOWN:
                    default:
                        return "Unknown";
                }
            }
        };

        JTextField textField = new JTextField();
        textField.addKeyListener(listener);
        contentPane.add(textField, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
    }

}