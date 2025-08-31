package render;

import com.trifidearth.zulu.brain.Brain;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


/**
 *
 * @author swerner
 */
public class Render {

    // The window handle
    private long window;

    // Brain model
    private Brain brain;
    private CoordinateBounds bounds;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "Zulu brain visualizer!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Initialize brain
        bounds = new CoordinateBounds(new Coordinate(0, 0, 0), 50);
        try {
            brain = new Brain(bounds, 2, 8, 2);
            brain.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Brain", e);
        }
    }

    private void setup2D(int width, int height) {
        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glDisable(GL_DEPTH_TEST);
    }

    private float[] toScreen(Coordinate c, int width, int height) {
        // Map [-radius, radius] to [margin, size - margin]
        int r = bounds.getRadius();
        float margin = 20f;
        float sx = (float) (c.getX() - (-r)) / (float) (2 * r) * (width - 2 * margin) + margin;
        float sy = (float) (c.getY() - (-r)) / (float) (2 * r) * (height - 2 * margin) + margin;
        return new float[]{sx, sy};
    }

    private void drawBrain(int width, int height) {
        Map<Coordinate, List<String>> map = brain.getNodeLocationMap();

        // Draw bounds box
        glColor3f(0.2f, 0.2f, 0.2f);
        glLineWidth(1f);
        glBegin(GL_LINE_LOOP);
        glVertex2f(20f, 20f);
        glVertex2f(width - 20f, 20f);
        glVertex2f(width - 20f, height - 20f);
        glVertex2f(20f, height - 20f);
        glEnd();

        // Draw nodes and transmitters
        glPointSize(5f);
        glBegin(GL_POINTS);
        for (Map.Entry<Coordinate, List<String>> entry : map.entrySet()) {
            float[] xy = toScreen(entry.getKey(), width, height);
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
                glVertex2f(xy[0], xy[1]);
            }
        }
        glEnd();
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                glfwGetFramebufferSize(window, w, h);
                int width = w.get(0);
                int height = h.get(0);
                setup2D(width, height);

                glClearColor(0.1f, 0.1f, 0.12f, 1f);
                glClear(GL_COLOR_BUFFER_BIT);

                drawBrain(width, height);
            }

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }
}
