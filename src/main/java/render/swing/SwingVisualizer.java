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
    private javax.swing.Timer repaintTimer;
    private javax.swing.Timer logicTimer;
    private boolean paused = false;
    private long iterations = 0;
    private JLabel iterLabel;
    private JButton pauseButton;

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
        // 2D simulation: keep Z at 0
        com.trifidearth.zulu.coordinate.CoordinatePair.setForce2D(true);
        bounds = new CoordinateBounds(new Coordinate(0,0,0), 60);
        try {
            System.out.println("SwingVisualizer: constructing brain...");
            brain = new Brain(bounds, 2, 3, 2);
            brain.start();
            System.out.println("SwingVisualizer: brain started with bounds="+bounds+".");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        view = new View();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(buildControls(), BorderLayout.NORTH);
        frame.getContentPane().add(view, BorderLayout.CENTER);
        frame.getContentPane().add(new LegendPanel(), BorderLayout.SOUTH);
        frame.setSize(1024, 768);
        frame.setLocationByPlatform(true);
        System.out.println("SwingVisualizer: showing UI...");
        frame.setVisible(true);

        // repaint at ~30 FPS
        repaintTimer = new Timer(33, e -> view.repaint());
        repaintTimer.start();

        // periodically grow neuron processes to animate structure
        logicTimer = new Timer(500, e -> tick());
        logicTimer.start();

        System.out.println("SwingVisualizer: UI visible. Use mouse wheel to zoom, drag to pan, 'R' to reset.");
    }

    private void tick(boolean force) {
        if (paused && !force) return;
        try {
            brain.grow();
            Map<Coordinate, List<String>> map = brain.getNodeLocationMap();
            // Prefer stimulus at dendrite endpoints for interpretability
            java.util.List<Coordinate> dendrites = new java.util.ArrayList<>();
            for (Map.Entry<Coordinate, List<String>> en : map.entrySet()) {
                for (String label : en.getValue()) {
                    if (label.endsWith("_D")) { dendrites.add(en.getKey()); break; }
                }
            }
            java.util.List<Coordinate> targets = dendrites.isEmpty() ? new java.util.ArrayList<>(map.keySet()) : dendrites;
            if (!targets.isEmpty()) {
                int injections = Math.min(4, Math.max(1, targets.size()/25));
                for (int i = 0; i < injections; i++) {
                    Coordinate c = targets.get((int)(Math.random() * targets.size()));
                    brain.depositTransmitters(c, Transmitters.getRandomTransmitters());
                }
            }
            iterations++;
            if (iterLabel != null) iterLabel.setText("Iter: " + iterations);
            frame.setTitle("Zulu Swing 2D Visualizer — Iter " + iterations);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
        }
    }

    private JComponent buildControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pauseButton = new JButton("Pause");
        JButton stepButton = new JButton("Step");
        iterLabel = new JLabel("Iter: 0");
        pauseButton.addActionListener(e -> {
            paused = !paused;
            pauseButton.setText(paused ? "Start" : "Pause");
            stepButton.setEnabled(paused);
        });
        stepButton.addActionListener(e -> {
            if (paused) {
                tick(true);
            } else {
                try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Throwable ignore) {}
            }
        });
        stepButton.setEnabled(paused);
        panel.add(pauseButton);
        panel.add(stepButton);
        panel.add(iterLabel);
        return panel;
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
                for (String label : e.getValue()) {
                    if (label.startsWith("T")) {
                        int size = 5;
                        try { int cnt = Integer.parseInt(label.substring(1)); size = Math.min(5 + cnt, 14); } catch (Exception ignore) {}
                        g2.setColor(new Color(50,160,255));
                        g2.fillOval(p.x - size/2, p.y - size/2, size, size);
                    } else if (label.endsWith("_N")) {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(p.x - 5, p.y - 5, 10, 10);
                        g2.setColor(Color.BLACK);
                        g2.drawOval(p.x - 5, p.y - 5, 10, 10);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.soma = p;
                    } else if (label.endsWith("_D")) {
                        g2.setColor(new Color(80,220,80));
                        g2.fillRect(p.x - 3, p.y - 3, 6, 6);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.dend.add(p);
                    } else if (label.endsWith("_A")) {
                        g2.setColor(new Color(255,90,70));
                        Polygon tri = new Polygon();
                        tri.addPoint(p.x, p.y - 5);
                        tri.addPoint(p.x - 4, p.y + 3);
                        tri.addPoint(p.x + 4, p.y + 3);
                        g2.fillPolygon(tri);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.axon = p;
                    } else if (label.endsWith("_S")) {
                        g2.setColor(new Color(240,80,240));
                        Polygon diamond = new Polygon();
                        diamond.addPoint(p.x, p.y - 4);
                        diamond.addPoint(p.x - 4, p.y);
                        diamond.addPoint(p.x, p.y + 4);
                        diamond.addPoint(p.x + 4, p.y);
                        g2.fillPolygon(diamond);
                        String id = label.substring(0, label.indexOf('_'));
                        Ends ends = endsById.computeIfAbsent(id, k -> new Ends());
                        ends.syn.add(p);
                    }
                }
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

    // Legend panel showing visual encodings
    static class LegendPanel extends JPanel {
        LegendPanel() { setPreferredSize(new Dimension(200, 36)); setBackground(new Color(30,30,34)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 10; int y = getHeight()/2;
            // Soma
            g2.setColor(Color.WHITE); g2.fillOval(x, y-6, 12, 12); g2.setColor(Color.BLACK); g2.drawOval(x, y-6, 12, 12);
            g2.setColor(Color.LIGHT_GRAY); g2.drawString("Soma", x+18, y+4);
            x += 80;
            // Dendrite
            g2.setColor(new Color(80,220,80)); g2.fillRect(x, y-4, 8, 8);
            g2.setColor(Color.LIGHT_GRAY); g2.drawString("Dendrite", x+18, y+4);
            x += 100;
            // Axon
            g2.setColor(new Color(255,90,70)); Polygon tri = new Polygon(); tri.addPoint(x+6, y-6); tri.addPoint(x, y+4); tri.addPoint(x+12, y+4); g2.fillPolygon(tri);
            g2.setColor(Color.LIGHT_GRAY); g2.drawString("Axon", x+20, y+4);
            x += 80;
            // Synapse
            g2.setColor(new Color(240,80,240)); Polygon d = new Polygon(); d.addPoint(x+6,y-6); d.addPoint(x,y); d.addPoint(x+6,y+6); d.addPoint(x+12,y); g2.fillPolygon(d);
            g2.setColor(Color.LIGHT_GRAY); g2.drawString("Synapse", x+20, y+4);
            x += 110;
            // Transmitter
            g2.setColor(new Color(50,160,255)); g2.fillOval(x, y-5, 10, 10);
            g2.setColor(Color.LIGHT_GRAY); g2.drawString("Transmitters Tn", x+18, y+4);
        }
    }
}
