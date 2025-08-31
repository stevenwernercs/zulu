/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import render.fx.FxVisualizerApp;

/**
 *
 * @author swerner
 */
public class EngineTest {
    
    
    public static void main(String [] args) {
        // Default: JavaFX renderer for broader OS compatibility
        FxVisualizerApp.main(args);
        // LWJGL visualizer remains available for future fixes:
        // new Visualizer3D().run();
    }
    
    
    
}
