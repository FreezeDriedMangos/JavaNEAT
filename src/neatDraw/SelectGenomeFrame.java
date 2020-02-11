package neatDraw;

import java.util.Map;

import java.awt.Panel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Frame;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

import neatCore.Population;
import neatCore.Species;
import neatCore.Genome;

import neatDraw.dataPanels.SelectGenomeDisplay;

public class SelectGenomeFrame extends Frame {

	Panel drawspace;
	SelectGenomeDisplay selectDisplay;
	
	public SelectGenomeFrame() {
		super();
		
		this.setSize(500, 600);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w) {
				setVisible(false);
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				//redrawSelectDisplay();
				drawProtected();
			}
		});
		
		drawspace = new Panel();
		//drawspace.setLayout(null);
		drawspace.setIgnoreRepaint(true);
		this.add(drawspace);
		
		selectDisplay = new SelectGenomeDisplay(this);
		for(java.awt.Button b : selectDisplay.getButtons()) {
			drawspace.add(b);
		}
	}
	
	// previously redrawSelectDisplay()
	public void draw() {
		super.setVisible(true);
		
		drawProtected();
		(new Thread(){
			public void run() {
				try {
					Thread.sleep(200);
					//redrawSelectDisplay();
					drawProtected();
				} catch(Exception e){}
			}
		}).start();
	}
	
	protected void drawProtected() {
		Graphics gr = drawspace.getGraphics();
		gr.clearRect(0, 0, drawspace.getWidth(), drawspace.getHeight());
		
		try { 
			selectDisplay.draw(gr, 0, 0, drawspace.getWidth(), drawspace.getHeight(), null, null);
		} catch (Exception e) {
			gr.setColor(new Color(200, 150, 150));
			gr.fillRect(0, 0, drawspace.getWidth(), drawspace.getHeight());
			gr.setColor(Color.RED);
			gr.drawRect(0, 0, drawspace.getWidth(), drawspace.getHeight());
			gr.setColor(new Color(100, 20, 20));
			gr.drawString(e.getMessage(), 1, 12);
			
			e.printStackTrace();
		}
	}
	
	public void setGenome(Genome g, Color c, String genomeTitle) {
		selectDisplay.setGenome(g, c, genomeTitle);
		draw();
	}
}
