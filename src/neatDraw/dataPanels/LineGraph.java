package neatDraw.dataPanels;

import java.util.ArrayList;

import java.awt.Graphics;
import java.awt.Color;

public class LineGraph {
    public static final float BODER_THICKNESS = 10;
    
    private ArrayList<Float> data;
    float maxVal = 0;
    
    int numGenerationsToStore = 100;
    
    LineGraph() {
        data = new ArrayList<>();
        data.add(0f);   
    }
    
    void addData(float d) {
        data.add(d);
        
        maxVal = Math.max(maxVal, d);
        
        if(data.size() > numGenerationsToStore)
            data.remove(0);
    }
    
     public void draw(Graphics g, float x, float y, float w, float h) {
        g.setColor(new Color(250, 250, 250));
        g.fillRect((int)x, (int)y, (int)w, (int)h);
        
        w -= BODER_THICKNESS*2;
        h -= BODER_THICKNESS*2;
        x += BODER_THICKNESS;
        y += BODER_THICKNESS;
        
        if(maxVal == 0)
            return;
        
        float xscale = w / (data.size()-1);
        float yscale = h / maxVal;
        
        g.setColor(Color.BLACK);
        for(int i = 0; i < data.size()-1; i++) {
            float x1 = x + xscale*i;
            float y1 = y + h - yscale*data.get(i);
            float x2 = x + xscale*(i+1);
            float y2 = y + h - yscale*data.get(i+1);
            
            g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
        }
    }
}
