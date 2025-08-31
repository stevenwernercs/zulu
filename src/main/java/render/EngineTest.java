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
        // Default: Swing 2D renderer (most compatible)
        try {
            javax.swing.SwingUtilities.invokeLater(new SwingVisualizer());
            return;
        } catch (Throwable t) {
            // fallback to JavaFX if Swing fails (unlikely)
        }
        // JavaFX renderer
        try {
            FxVisualizerApp.main(args);
            return;
        } catch (Throwable t) {
            // ignore, try LWJGL last
        }
        // LWJGL visualizer remains available for future fixes
        new Visualizer3D().run();
    }
    
    
    
}
