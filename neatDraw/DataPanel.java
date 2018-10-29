package neatDraw;

import java.util.Map;

import java.awt.Graphics;
import java.awt.Color;

import neatCore.Population;
import neatCore.Species;


public abstract class DataPanel implements Comparable<DataPanel> {
	Map<Species, Color> speciesColors;
	int x;
	int y;
	int width;
	int height;
	
	/**
	 * Draws everything pertaining to this DataPanel using gr, bounded by this object's (x, y) and
	 * the given width/height
	 */
	public void draw(Graphics gr, Population p, Map<Species, Color> speciesColors) {
		this.draw(gr, x, y, width, height, p, speciesColors);
	}
	
	/**
	 * Sets the upper left corner of the bounding box that this panel draws over.
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Sets the size of the bounding box that this panel draws over.
	 */
	public void setSize(int w, int h) {
		this.width = w;
		this.height = h;
	}
	
	@Override
	public int compareTo(DataPanel rhs) {
		float diff = this.getDrawComplexity() - rhs.getDrawComplexity();
		
		return (int)Math.signum(diff);
		
		//if(tComp == rComp) { return 0;  }
		//if(tComp <  rComp) { return -1; }
		//if(tComp >  rComp) { return 1;  }
		
	}
	
	public int getX() { return x; }
	public int getY() { return y; }
	public int getWidth()  { return width;  }
	public int getHeight() { return height; }
	
	
	// ===========================================
	//
	// Abstract methods
	//
	// ===========================================
	
	/**
	 * Draw everything pertaining to this DataPanel using gr, bounded by (x, y) and
	 * the given width/height.
	 * <br>
	 * When implementing this function, please be careful to only draw within the rectangle bounded
	 * by (x,y) and (x+width, y+height)
	 */
	protected abstract void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors);
	
	/**
	 * Collects data that needs to be perserved over multiple generations, BY VALUE (NOT reference), 
	 * relevant to what this class is tracking. Data that does not need to be perserved can be 
	 * ignored
	 */
	public abstract void captureData(Population p);
	
	/**
	 * Returns a subjective measure of the complexity of this data panel. Used to
	 * estimate which panels should be drawn first to keep flickering to a minimum. The value this
	 * returns should be relative to other DataPanel subclasses
	 */
	public abstract float getDrawComplexity();
	
	/**
	 * Called when this data panel is clicked. The x and y values are not relative to this
	 * panel. They are relative to the top left corner of the drawspace.
	 */
	public abstract void handleClick(int x, int y);
}
