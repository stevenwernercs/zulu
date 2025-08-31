package render.ui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LogWindow {
    private final JFrame frame;
    private final JTextArea area;

    public LogWindow(String title) {
        frame = new JFrame(title);
        area = new JTextArea(30, 100);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationByPlatform(true);
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public void appendLine(String line) {
        SwingUtilities.invokeLater(() -> {
            area.append(line);
            if (!line.endsWith("\n")) area.append("\n");
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {
            private final StringBuilder sb = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                sb.append(c);
                if (c == '\n') {
                    flushBuffer();
                }
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String s = new String(b, off, len, StandardCharsets.UTF_8);
                sb.append(s);
                int idx;
                while ((idx = sb.indexOf("\n")) >= 0) {
                    String line = sb.substring(0, idx + 1);
                    sb.delete(0, idx + 1);
                    appendLine(line);
                }
            }
            @Override
            public void flush() throws IOException {
                flushBuffer();
            }
            private void flushBuffer() {
                if (sb.length() > 0) {
                    String line = sb.toString();
                    sb.setLength(0);
                    appendLine(line);
                }
            }
        };
    }
}

