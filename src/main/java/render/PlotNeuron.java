package render;

import com.trifidearth.zulu.brain.Brain;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import com.trifidearth.zulu.neuron.Neuron;
import org.knowm.xchart.*;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Creates a real-time chart using SwingWorker
 */
public class PlotNeuron {

    MySwingWorker mySwingWorker;
    SwingWrapper<XYChart> sw;
    List<XYChart> charts;

    public static void main(String[] args) throws Exception {

        PlotNeuron swingWorkerRealTime = new PlotNeuron();
        Coordinate orgin = new Coordinate(0, 0, 0);
        CoordinateBounds bounds = new CoordinateBounds(orgin, 2);
        Brain brain = new Brain(bounds, 2, 4, 1);
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    Brain.runBrain(brain);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        swingWorkerRealTime.go(brain);
    }

    private void go(Brain brain) {

        // Create Charts
        charts = new LinkedList<>();
        int idx = 0;
        for(Neuron neuron : brain.getNeurons()) {
            String name = (char)neuron.name+"";
            XYChart chart = new XYChartBuilder().title("Neurons").xAxisTitle("Time").yAxisTitle((char)neuron.name+"").build();// .width(100).height(33).build();
            XYSeries series = chart.addSeries(name+"", new double[]{0}, new double[]{0});
            series.setMarker(SeriesMarkers.NONE);
            chart.getStyler().setYAxisMin(-90d);
            chart.getStyler().setYAxisMax(20d);
            boolean printFirst = (idx == 0);
            boolean printLast = (idx == brain.getNeurons().size() - 1);
            XYStyler styler = chart.getStyler();
            styler.setChartTitleVisible(printFirst);
            styler.setLegendVisible(false);
            styler.setXAxisTitleVisible(printLast);
            styler.setXAxisTicksVisible(false);
            styler.setYAxisTitleVisible(true);
            styler.setYAxisTicksVisible(true);
            styler.setPlotMargin(-5);
            styler.setPlotGridHorizontalLinesVisible(true);
            charts.add(chart);
            idx++;
        }

        // Show it
        sw = new SwingWrapper<XYChart>(charts, charts.size(), 1);
        sw.displayChartMatrix();

        mySwingWorker = new MySwingWorker(brain);
        mySwingWorker.execute();
    }

    private class Points {
        final LinkedList<Long> xs = new LinkedList<>();
        final LinkedList<Float> ys = new LinkedList<>();
        int max = 1000;

        public Points() {

        }

        public Points(Points ps) {
            xs.addAll(ps.xs);
            ys.addAll(ps.ys);
        }

        public void add(Point p) {
            xs.add(p.x);
            ys.add(p.y);
            if (xs.size() > max) {
                xs.removeFirst();
                ys.removeFirst();
            }
        }

        public Point getLast() {
            return new Point(xs.getLast(), ys.getLast());
        }

        public int size() {
            return xs.size();
        }
    }

    private class Point {
        final long x;
        final float y;

        public Point(float y) {
            this(System.currentTimeMillis(), y);
        }

        public Point(long x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private class MySwingWorker extends SwingWorker<Boolean, Map<String, Points>> {

        Brain brain;
        Map<String,Points> fifo = new LinkedHashMap<>();

        public MySwingWorker(Brain brain) {

            this.brain = brain;
            update();
        }

        private void update() {
            for(Neuron neuron : brain.getNeurons()) {
                double voltage = neuron.getSoma().getPotiential().getPotientialVoltage();
                String key = (char)(neuron.name)+"";
                Points points = fifo.get(key);
                if(points == null) {
                    points = new Points();
                }
                points.add(new Point((float)voltage));
                fifo.put(key,points);
            }
        }

        @Override
        protected Boolean doInBackground() throws Exception {

            while (!isCancelled()) {

                update();
                Map<String,Points> latest = new LinkedHashMap<>();
                for(Map.Entry<String,Points> each : fifo.entrySet()) {
                    latest.put(each.getKey(), new Points(each.getValue()));
                }
                publish(latest);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // eat it. caught when interrupt is called
                    System.out.println("MySwingWorker shut down.");
                }

            }

            return true;
        }

        @Override
        protected void process(List<Map<String,Points>> chunks) {

            System.out.println("number of chunks: " + chunks.size());

            if(chunks.isEmpty()){
                return;
            }

            Map<String,Points> chunk = chunks.get(chunks.size() - 1);
            for(XYChart chart : charts) {
               for(String name : chart.getSeriesMap().keySet()) {
                   Points current = chunk.get(name);
                   List<Long> mostRecentDataX = current.xs;
                   List<Float> mostRecentDataY = current.ys;
                   chart.updateXYSeries(name, mostRecentDataX, mostRecentDataY, null);
               }
            }

            for(int idx = 0; idx < charts.size(); idx++) {
                sw.repaintChart(idx);
            }

            long start = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - start;
            try {
                //Thread.sleep(40 - duration); // 40 ms ==> 25fps
                //Thread.sleep(400 - duration); // 400 ms ==> 2.5fps
                Thread.sleep(1000 - duration); // 1000 ms ==> 1fps
            } catch (InterruptedException e) {
            }

        }
    }
}