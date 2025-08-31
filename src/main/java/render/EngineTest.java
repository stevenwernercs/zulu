/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import render.fx.FxVisualizerApp;
import render.swing.SwingVisualizer;

/**
 *
 * @author swerner
 */
public class EngineTest {
    
    
    public static void main(String [] args) {
        System.out.println("EngineTest: starting Swing 2D visualizer...");
        try {
            new SwingVisualizer().run();
            return;
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.out.println("Swing failed; attempting JavaFX 3D...");
        }
        try {
            FxVisualizerApp.main(args);
            return;
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.out.println("JavaFX failed; attempting LWJGL 3D...");
        }
        new Visualizer3D().run();
    }
    
    
    
}
