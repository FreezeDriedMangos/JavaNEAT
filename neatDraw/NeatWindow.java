package neatDraw;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Button;
import java.awt.TextField;


import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import neatCore.Population;
import neatCore.Species;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class NeatWindow extends Frame {
	public static int WINDOW_WIDTH  = 900;
	public static int WINDOW_HEIGHT = 600;

	/*private*/ Panel drawspace;
	private List<DataPanel> dataPanels;
	private Map<DataPanel, String> panelsWithErrors;
	
	Map<Species, Color> speciesColors;
	Population lastDrawnPopulation;
	
	//Panel pauseButtonPanel;
	TextField iterationDelayBox;
	int delay = 0;
	
	Button pauseButton;
	boolean paused = false;
	
	Button iterateButton;
	boolean iterate = false;
	
	public NeatWindow(String title) {
		super(title);
		this.setResizable(false);
		
		dataPanels = new ArrayList<>();
		panelsWithErrors = new HashMap<>();
		
		drawspace = new Panel();
		drawspace.setIgnoreRepaint(true);
		this.add(drawspace);
		
		speciesColors = new HashMap<>();
		
		
		pauseButton = new Button("❚❚");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paused = !paused;
				
				if(paused) {
					pauseButton.setLabel("▶");
				} else {
					pauseButton.setLabel("❚❚");
					
					// try to parse the text entry as an int, if it fails, don't update
					// the delay value
					try {
						int parseAttempt = (int)Double.parseDouble(iterationDelayBox.getText());
						delay = parseAttempt;
					} catch (Exception e1) {}
				}
			}
		});
		drawspace.add(pauseButton);
		
		iterationDelayBox = new TextField("0", 20);
		iterationDelayBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// try to parse the text entry as an int, if it fails, don't update
				// the delay value
				try {
					int parseAttempt = (int)Double.parseDouble(iterationDelayBox.getText());
					delay = parseAttempt;
				} catch (Exception e1) {}
			}
		});
		drawspace.add(iterationDelayBox);
		
		iterateButton = new Button("❚▶");
		iterateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iterate = true;
			}
		});
		drawspace.add(iterateButton);
		
		//pauseButtonPanel = new Panel() { public void paint(java.awt.Graphics g) {} public void draw() {super.paint(this.getGraphics());}};
		//pauseButtonPanel.setMaximumSize(new Dimension(10, 10));
		//pauseButtonPanel.add(pauseButton);
		//pauseButtonPanel.setLocation(0,0);
		//drawspace.add(pauseButtonPanel);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w) {
				System.exit(0);
			}
		});
		
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setVisible(true);
		
		
	}
	
	/**
	 * The display function, updates all data panels
	 */
	public void draw(Population p) {
		lastDrawnPopulation = p;
		
		Graphics gr = drawspace.getGraphics();
		gr.clearRect(0, 0, drawspace.getWidth(), drawspace.getHeight());
		
		
		for(DataPanel panel : dataPanels) {
			if(!panelsWithErrors.containsKey(panel)) {
				try {
					panel.draw(gr, p, speciesColors);
				} catch (Exception e) {
					e.printStackTrace();
					panelsWithErrors.put(panel, "DRAW ERROR");
				}
			}
			
			// this isn't an else statement so that we can display that a panel
			// had a drawing error the same draw() call as it had that error
			if(panelsWithErrors.containsKey(panel)) {
				gr.setColor(new Color(200, 150, 150));
				gr.fillRect(panel.x, panel.y, panel.width, panel.height);
				gr.setColor(Color.RED);
				gr.drawRect(panel.x, panel.y, panel.width, panel.height);
				gr.setColor(new Color(100, 20, 20));
				gr.drawString(panelsWithErrors.get(panel), panel.x + 1, panel.y + 12);
			}
		}
		
		// draw the toolbar buttons
		pauseButton.paint(this.getGraphics());
		iterationDelayBox.paint(this.getGraphics());
		iterateButton.paint(this.getGraphics());
		
		if(delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e){}
		}
	}
	
	/**
	 * Assigns any new species a color and calls captureData on all DataPanel objects
	 */
	public void captureData(Population p) {
		for(Species s : p.getSpecies()) {
			if(!speciesColors.containsKey(s)) {
				speciesColors.put(s, getRandomColor());
			}
		}
		
		for(DataPanel panel : dataPanels) {
			if(panelsWithErrors.containsKey(panel)) {
				// don't cause another error, we'd just spam the console with
				// error messages.
				continue;
			}
			
			try {
				panel.captureData(p);
			} catch (Exception e) {
				e.printStackTrace();
				panelsWithErrors.put(panel, "DATA COLLECTION ERROR");
			}
		}
	}
	
	/**
	 * Adds a data panel to track data and be drawn
	 */
	public void addDataPanel(DataPanel p) {
		dataPanels.add(p);
		Collections.sort(dataPanels);
	}
	
	/**
	 * Returns a completely random color. I'm not sure why, but this method of
	 * generating random colors tends to make pretty pastels. You won't hear me
	 * complaining!
	 */
	private static Color getRandomColor() {
		return new Color(
				(float)Math.random(),
				(float)Math.random(),
				(float)Math.random()
				);
	}
	
	@Override
	public void paint(Graphics g) {
		System.out.println("hello!");
		// only draw if paused, otherwise thread collisions will be an issue
		// besides, the next generation will redraw everything anyway.
		if(paused) {
			this.draw(lastDrawnPopulation);
		}
	}
}
