package render;

import com.trifidearth.zulu.brain.Brain;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import org.lwjgl.Version;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import render.ui.LogWindow;
import render.ui.LogbackGuiAppender;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Visualizer3D {

    private long window;
    private Brain brain;
    private CoordinateBounds bounds;

    // Orbit camera
    private float distance = 160f;
    private float yaw = 30f;
    private float pitch = 20f;
    private boolean rotating = false;
    private double lastX, lastY;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + " (Visualizer3D)!");
        init();
        loop();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Optional GUI log window (enable with -Dgui.log=true)
        final boolean guiLog = Boolean.getBoolean("gui.log");
        LogWindow logWin = null;
        if (guiLog) {
            logWin = new LogWindow("Brain Console");
            logWin.show();
            try {
                LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
                LogbackGuiAppender guiAppender = new LogbackGuiAppender(logWin);
                guiAppender.setContext(ctx);
                guiAppender.start();
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.addAppender(guiAppender);
            } catch (Throwable t) {
                // ignore if logback isn't available
            }
        }

        final GLFWErrorCallback printer = GLFWErrorCallback.createPrint(System.err);
        GLFWErrorCallback callback = new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                String msg = GLFWErrorCallback.getDescription(description);
                if (error == GLFW_PLATFORM_ERROR && msg != null) {
                    if (msg.contains("/dev/input")
                        || msg.contains("Failed to watch for joystick connections")
                        || msg.contains("Failed to open joystick device directory")) {
                        return; // ignore joystick messages (e.g., WSL)
                    }
                }
                printer.invoke(error, description);
            }
        };
        glfwSetErrorCallback(callback);
        if (!glfwInit()) {
            if (guiLog && logWin != null) {
                logWin.appendLine("ERROR: Unable to initialize GLFW. Check graphics drivers and that natives are available.");
            } else {
                System.out.println("ERROR: Unable to initialize GLFW. Check graphics drivers and that natives are available.");
            }
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(1024, 768, "Zulu 3D Visualizer", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (w, key, sc, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true);
            if (key == GLFW_KEY_R && action == GLFW_RELEASE) { yaw = 30f; pitch = 20f; distance = 160f; }
        });
        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_RIGHT) rotating = (action == GLFW_PRESS);
            if (rotating) {
                double[] x = new double[1], y = new double[1];
                glfwGetCursorPos(window, x, y);
                lastX = x[0];
                lastY = y[0];
            }
        });
        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (rotating) {
                double dx = xpos - lastX;
                double dy = ypos - lastY;
                lastX = xpos; lastY = ypos;
                yaw += (float) dx * 0.3f;
                pitch += (float) dy * 0.3f;
                if (pitch > 89f) pitch = 89f;
                if (pitch < -89f) pitch = -89f;
            }
        });
        glfwSetScrollCallback(window, (w, xoff, yoff) -> {
            distance *= (float) Math.pow(0.90, yoff); // zoom exponential
            if (distance < 20f) distance = 20f;
            if (distance > 1000f) distance = 1000f;
        });

        try (org.lwjgl.system.MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        bounds = new CoordinateBounds(new Coordinate(0, 0, 0), 60);
        try {
            brain = new Brain(bounds, 6, 24, 6);
            brain.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Brain", e);
        }
        // Mirror brain.out to GUI if enabled; otherwise console
        if (guiLog && logWin != null) {
            final java.io.OutputStream guiOut = logWin.asOutputStream();
            brain.out = new java.io.PrintStream(new java.io.OutputStream() {
                @Override public void write(int b) { try { guiOut.write(b); } catch (java.io.IOException ignored) {} }
                @Override public void write(byte[] b, int off, int len) { try { guiOut.write(b, off, len); } catch (java.io.IOException ignored) {} }
            }, true);
        } else {
            brain.out = System.out;
        }
    }

    private void setPerspective(int width, int height) {
        float aspect = (float) width / Math.max(height, 1);
        float fovDeg = 60f;
        float near = 0.1f, far = 5000f;
        float top = (float) (Math.tan(Math.toRadians(fovDeg * 0.5)) * near);
        float bottom = -top;
        float right = top * aspect;
        float left = -right;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glFrustum(left, right, bottom, top, near, far);
        glMatrixMode(GL_MODELVIEW);
    }

    private void applyCamera() {
        glLoadIdentity();
        glTranslatef(0f, 0f, -distance);
        glRotatef(pitch, 1f, 0f, 0f);
        glRotatef(yaw, 0f, 1f, 0f);
    }

    private void drawAxes() {
        glLineWidth(1.5f);
        glBegin(GL_LINES);
        // X - red
        glColor3f(1f, 0f, 0f); glVertex3f(0f, 0f, 0f); glVertex3f(100f, 0f, 0f);
        // Y - green
        glColor3f(0f, 1f, 0f); glVertex3f(0f, 0f, 0f); glVertex3f(0f, 100f, 0f);
        // Z - blue
        glColor3f(0f, 0f, 1f); glVertex3f(0f, 0f, 0f); glVertex3f(0f, 0f, 100f);
        glEnd();
    }

    private void drawBoundsCube() {
        int r = bounds.getRadius();
        float s = 1.0f; // scale factor
        float x = r * s, y = r * s, z = r * s;
        glColor3f(0.5f, 0.5f, 0.5f);
        glLineWidth(1f);
        glBegin(GL_LINES);
        // 12 edges of a cube centered at origin spanning [-r,r]
        float[] xs = {-x, x}; float[] ys = {-y, y}; float[] zs = {-z, z};
        // bottom square
        glVertex3f(-x, -y, -z); glVertex3f(x, -y, -z);
        glVertex3f(x, -y, -z); glVertex3f(x, -y, z);
        glVertex3f(x, -y, z); glVertex3f(-x, -y, z);
        glVertex3f(-x, -y, z); glVertex3f(-x, -y, -z);
        // top square
        glVertex3f(-x, y, -z); glVertex3f(x, y, -z);
        glVertex3f(x, y, -z); glVertex3f(x, y, z);
        glVertex3f(x, y, z); glVertex3f(-x, y, z);
        glVertex3f(-x, y, z); glVertex3f(-x, y, -z);
        // verticals
        glVertex3f(-x, -y, -z); glVertex3f(-x, y, -z);
        glVertex3f(x, -y, -z); glVertex3f(x, y, -z);
        glVertex3f(x, -y, z); glVertex3f(x, y, z);
        glVertex3f(-x, -y, z); glVertex3f(-x, y, z);
        glEnd();
    }

    private void drawBrainPoints() {
        Map<Coordinate, List<String>> map = brain.getNodeLocationMap();
        glPointSize(5f);
        glBegin(GL_POINTS);
        for (Map.Entry<Coordinate, List<String>> entry : map.entrySet()) {
            Coordinate c = entry.getKey();
            float sx = c.getX();
            float sy = c.getY();
            float sz = c.getZ();
            for (String label : entry.getValue()) {
                if (label.startsWith("T")) {
                    glColor3f(0.2f, 0.6f, 1.0f); // transmitters - blue
                } else if (label.endsWith("_N")) {
                    glColor3f(1.0f, 1.0f, 1.0f); // soma - white
                } else if (label.endsWith("_D")) {
                    glColor3f(0.4f, 1.0f, 0.4f); // dendrite - green
                } else if (label.endsWith("_A")) {
                    glColor3f(1.0f, 0.4f, 0.4f); // axon - red
                } else if (label.endsWith("_S")) {
                    glColor3f(1.0f, 0.4f, 1.0f); // synapse - magenta
                } else {
                    glColor3f(0.8f, 0.8f, 0.8f);
                }
                glVertex3f(sx, sy, sz);
            }
        }
        glEnd();
    }

    private void loop() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.08f, 0.08f, 0.10f, 1f);

        while (!glfwWindowShouldClose(window)) {
            try (org.lwjgl.system.MemoryStack stack = stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                glfwGetFramebufferSize(window, w, h);
                int width = w.get(0);
                int height = h.get(0);
                glViewport(0, 0, width, height);
                setPerspective(width, height);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                applyCamera();

                drawAxes();
                drawBoundsCube();
                drawBrainPoints();
            }
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
