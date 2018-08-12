/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import org.lwjgl.LWJGLException;

/**
 *
 * @author swerner
 */
public class EngineTest {
    
    
    public static void main(String [] args) throws LWJGLException, InterruptedException {
     
        Render render = new Render();
        render.createDisplay("I love the baes", 500, 500);
        
        while(!render.isCloseRequested()) {
            Thread.sleep(500L);
            render.updateDisplay();
        }
        
    }
    
    
    
}
