package neatDraw.dataPanels;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class AreaGraph {
	public static final float BODER_THICKNESS = 10;
	
	private HashMap<Integer, AreaGraphCategory> categories = new HashMap<>();
	private int numEntries = 0;
	private float maxEntrySum = 1;
	
	int numGenerationsToStore = 100;
	int deletionCount = 0;
	
	//AreaGraph(int numCategories, boolean initializeWithZeroes) {
	 //   categories = new ArrayList<>();
	  //  
	   // for(int i = 0; i < numCategories; i++) {
		//	categories.add(new AreaGraphCategory());
		//}
	   // 
	   // if(initializeWithZeroes)
	   //	 addData(new float[numCategories]);
	//}
	
	void createNewCategory(Integer s) {
		AreaGraphCategory c = new AreaGraphCategory();
		c.id = s;
		
		for(int i = 0; i < numEntries; i++) {
			c.data.add(0f);   
		}
		
		categories.put(s, c);
	}
	
	void draw(Graphics g, float x, float y, float w, float h, HashMap<Integer, Color> setColors) {
		g.setColor(new Color(150, 150, 150));
		g.fillRect((int)x, (int)y, (int)w, (int)h);
		
		w -= BODER_THICKNESS*2;
		h -= BODER_THICKNESS*2;
		x += BODER_THICKNESS;
		y += BODER_THICKNESS;
		
		float xscale = w / (numEntries-1);
		float yscale = h / maxEntrySum;
		
		for(int i = 0; i < numEntries-1; i++) {
			float lastLeftY = y;
			float lastRightY = y;
			
			
			float minX = x + i*xscale;
			float maxX = x + (i+1)*xscale;
				
			// draw a line signifiying this the start of is a new entry
			if(i != numEntries-2) {
		  		g.setColor(Color.BLACK);
		   		g.drawLine((int)maxX, (int)(y+h+BODER_THICKNESS), (int)maxX, (int)(y-BODER_THICKNESS));
			}
			
			// draw all the data for this entry
			for(AreaGraphCategory c : categories.values()) {
				float leftDataPoint = i >= c.data.size() ? 0 : yscale * c.data.get(i);  
				float rightDataPoint = i+1 >= c.data.size() ? 0 :  yscale * c.data.get(i+1);
				
				if(leftDataPoint == 0 && rightDataPoint == 0)
					continue; // optomization
				
				float leftY = lastLeftY + leftDataPoint; //h - (leftDataPoint + lastLeftY);
				float rightY = lastRightY + rightDataPoint; //h - (rightDataPoint + lastRightY);
				
				
				if(deletionCount == 0 && i == 0) {
					leftY = y + h/2;
					lastLeftY = y + h/2;
				}
				
				g.setColor(setColors.get(c.id));
				Polygon poly = new Polygon(
						new int[]{(int)minX,	  (int)maxX,	   (int)maxX,   (int)minX},
						new int[]{(int)lastLeftY, (int)lastRightY, (int)rightY, (int)leftY}, 
						4);
				//quad(
				//	minX, lastLeftY, 
				//	maxX, lastRightY, 
				//	maxX, rightY, 
				//	minX, leftY);
					
				g.fillPolygon(poly);
				
				lastLeftY = leftY;
				lastRightY = rightY;
				
			}
		}
	}
	
	void addData(HashMap<Integer, Float> set) {
		for(Integer s : set.keySet()) {
			if(!categories.containsKey(s))
				createNewCategory(s);
		}
		
		numEntries++;
		
		float sum = 0;
		for(Entry<Integer, Float> e : set.entrySet()) {
			categories.
			get(
			e.getKey()).
			data.
			add(
			e.getValue());
			sum += e.getValue();
		}
		
		if(sum > maxEntrySum)
			maxEntrySum = sum;
			
		if(numEntries > numGenerationsToStore) {
			
			ArrayList<Integer> categoriesToRemove = new ArrayList<>();
			for(Entry<Integer, AreaGraphCategory> l : categories.entrySet()) {
				l.getValue().data.remove(0);
				
				if(l.getValue().data.size() <= 0)
					categoriesToRemove.add(l.getKey());
			}
			for(Integer s : categoriesToRemove)
				categories.remove(s);
			
			numEntries = numGenerationsToStore;
			deletionCount++;
		}
	}
}

class AreaGraphCategory {
	ArrayList<Float> data = new ArrayList<>();
	int id;
}
