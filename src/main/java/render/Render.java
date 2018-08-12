/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

/**
 *
 * @author swerner
 */
public class Render {
    
    private static final int FPS_CAP = 20;
    
    public void createDisplay(String title, int x, int y) throws LWJGLException {
        
        ContextAttribs attribs = new ContextAttribs(3,2);
        attribs.withForwardCompatible(true);
        attribs.withProfileCore(true);
        
        Display.setDisplayMode(new DisplayMode(x, y));
        Display.setTitle(title);
        Display.create(new PixelFormat(), attribs);
        
        GL11.glViewport(0, 0, x, y);
    }
    
    public boolean isCloseRequested() {
        return Display.isCloseRequested();
    }
    
    public void updateDisplay() {
        Display.sync(FPS_CAP);
        Display.update();
    }
    
    public void closeDisplay() {
        Display.destroy();
    }
    
}
