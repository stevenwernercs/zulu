package render.swing;

import com.trifidearth.zulu.brain.Brain;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.message.transmitter.Transmitters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class SwingVisualizer implements Runnable {

    private JFrame frame;
    private View view;
    private Brain brain;
    private CoordinateBounds bounds;

    @Override
    public void run() {
        System.out.println("SwingVisualizer: initializing UI and Brain...");
        try {
            System.out.println("SwingVisualizer: Toolkit=" + java.awt.Toolkit.getDefaultToolkit().getClass().getName());
        } catch (Throwable t) {
            t.printStackTrace(System.out);
        }
        System.out.println("SwingVisualizer: creating frame...");
        frame = new JFrame("Zulu Swing 2D Visualizer");
        System.out.println("SwingVisualizer: frame created.");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        bounds = new CoordinateBounds(new Coordinate(0,0,0), 60);
        try {
            System.out.println("SwingVisualizer: constructing brain...");
            brain = new Brain(bounds, 6, 24, 6);
            brain.start();
            System.out.println("SwingVisualizer: brain started with bounds="+bounds+".");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        view = new View();
        frame.setContentPane(view);
        frame.setSize(1024, 768);
        frame.setLocationByPlatform(true);
        System.out.println("SwingVisualizer: showing UI...");
        frame.setVisible(true);

        // repaint at ~30 FPS
        new Timer(33, e -> view.repaint()).start();

        // periodically grow neuron processes to animate structure
        new Timer(500, e -> {
            try {
                brain.grow();
            } catch (Throwable t) {
                t.printStackTrace(System.out);
            }
        }).start();

        // periodically deposit transmitters near existing nodes to stimulate activity
        new Timer(750, e -> {
            try {
                Map<Coordinate, List<String>> map = brain.getNodeLocationMap();
                java.util.List<Coordinate> keys = new java.util.ArrayList<>(map.keySet());
                if (!keys.isEmpty()) {
                    int injections = Math.min(4, Math.max(1, keys.size()/20));
                    for (int i = 0; i < injections; i++) {
                        Coordinate c = keys.get((int)(Math.random() * keys.size()));
                        brain.depositTransmitters(c, Transmitters.getRandomTransmitters());
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace(System.out);
            }
        }).start();
        System.out.println("SwingVisualizer: UI visible. Use mouse wheel to zoom, drag to pan, 'R' to reset.");
    }

    class View extends JPanel {
        private double zoom = 6.0;
        private double offsetX = 0.0, offsetY = 0.0;
        private Point lastDrag;

        View() {
            setBackground(new Color(20,20,24));
            addMouseWheelListener(e -> {
                double factor = Math.pow(1.1, -e.getWheelRotation());
                zoom *= factor;
                zoom = Math.max(1.0, Math.min(zoom, 100.0));
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { lastDrag = e.getPoint(); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseDragged(MouseEvent e) {
                    Point p = e.getPoint();
                    if (lastDrag != null) {
                        offsetX += (p.x - lastDrag.x) / zoom;
                        offsetY += (p.y - lastDrag.y) / zoom;
                    }
                    lastDrag = p;
                    repaint();
                }
            });
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "reset");
            getActionMap().put("reset", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    zoom = 6.0; offsetX = 0; offsetY = 0; repaint();
                }
            });
        }

        private Point worldToScreen(int x, int y) {
            int w = getWidth();
            int h = getHeight();
            double cx = w/2.0 + (x + offsetX) * zoom;
            double cy = h/2.0 - (y + offsetY) * zoom;
            return new Point((int)cx, (int)cy);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw bounds box 
            g2.setColor(new Color(150,150,150));
            int r = bounds.getRadius();
            Point tl = worldToScreen(-r, r);
            Point br = worldToScreen(r, -r);
            int w = br.x - tl.x;
            int h = br.y - tl.y;
            g2.drawRect(tl.x, tl.y, w, h);

            // Draw nodes/transmitters and gather per-neuron endpoints
            Map<Coordinate, List<String>> map = brain.getNodeLocationMap();

            class Ends { Point soma; Point axon; java.util.List<Point> dend = new java.util.ArrayList<>(); java.util.List<Point> syn = new java.util.ArrayList<>(); }
            java.util.Map<String, Ends> endsById = new java.util.HashMap<>();

            for (Map.Entry<Coordinate, List<String>> e : map.entrySet()) {
                Coordinate c = e.getKey();
                Point p = worldToScreen(c.getX(), c.getY());

                // Determine point color and size; also collect endpoints
                int size = 5;
                Color color = new Color(200,200,200);
                for (String label : e.getValue()) {
                    if (label.startsWith("T")) {
                        // transmitter cluster size boost by count
                        try {
                            int cnt = Integer.parseInt(label.substring(1));
                            size = Math.min(5 + cnt, 14);
                        } catch (Exception ignore) {}
                        color = new Color(50,160,255);
                    } else if (label.endsWith("_N")) {
                        color = Color.WHITE;
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.soma = p;
                    } else if (label.endsWith("_D")) {
                        color = new Color(80,220,80);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.dend.add(p);
                    } else if (label.endsWith("_A")) {
                        color = new Color(255,90,70);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.axon = p;
                    } else if (label.endsWith("_S")) {
                        color = new Color(240,80,240);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.syn.add(p);
                    }
                }
                g2.setColor(color);
                g2.fillOval(p.x - size/2, p.y - size/2, size, size);
            }

            // Draw connectors from soma to endpoints
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{6f,6f}, 0f));
            for (Map.Entry<String, Ends> en : endsById.entrySet()) {
                Ends ends = en.getValue();
                if (ends.soma == null) continue;
                // Soma to axon
                if (ends.axon != null) {
                    g2.setColor(new Color(255,90,70,160));
                    g2.drawLine(ends.soma.x, ends.soma.y, ends.axon.x, ends.axon.y);
                }
                // Soma to dendrites
                g2.setColor(new Color(80,220,80,160));
                for (Point dp : ends.dend) {
                    g2.drawLine(ends.soma.x, ends.soma.y, dp.x, dp.y);
                }
                // Soma to synapses
                g2.setColor(new Color(240,80,240,160));
                for (Point sp : ends.syn) {
                    g2.drawLine(ends.soma.x, ends.soma.y, sp.x, sp.y);
                }
            }
        }
    }
}
