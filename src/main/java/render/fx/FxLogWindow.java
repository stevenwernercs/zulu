package render.fx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FxLogWindow {
    private final Stage stage;
    private final TextArea textArea = new TextArea();

    public FxLogWindow() {
        stage = new Stage();
        stage.setTitle("Brain Console");
        textArea.setEditable(false);
        textArea.setWrapText(false);
        BorderPane root = new BorderPane(textArea);
        stage.setScene(new Scene(root, 900, 480));
    }

    public void show() {
        Platform.runLater(() -> stage.show());
    }

    public void append(String s) {
        Platform.runLater(() -> {
            textArea.appendText(s);
            if (!s.endsWith("\n")) textArea.appendText("\n");
            textArea.positionCaret(textArea.getText().length());
        });
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {
            private final StringBuilder sb = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                sb.append(c);
                if (c == '\n') flushBuffer();
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String s = new String(b, off, len, StandardCharsets.UTF_8);
                sb.append(s);
                int idx;
                while ((idx = sb.indexOf("\n")) >= 0) {
                    String line = sb.substring(0, idx + 1);
                    sb.delete(0, idx + 1);
                    append(line);
                }
            }
            @Override
            public void flush() throws IOException { flushBuffer(); }
            private void flushBuffer() {
                if (sb.length() > 0) {
                    String line = sb.toString();
                    sb.setLength(0);
                    append(line);
                }
            }
        };
    }
}

