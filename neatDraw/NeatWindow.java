package neatDraw;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Button;
import java.awt.TextField;
import java.awt.Label;

//import javax.swing.GroupLayout;
//import javax.swing.GroupLayout.SequentialGroup;
//import javax.swing.GroupLayout.Alignment;
import javax.swing.BoxLayout;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import neatCore.Population;
import neatCore.Species;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import java.lang.reflect.Field;

public class NeatWindow extends Frame {
	public static int WINDOW_WIDTH  = 1200;//900;
	public static int WINDOW_HEIGHT = 600;
	
	public static final int HEADER_HEIGHT = 40;

	public boolean simulationCompleted = false;

	/*private*/ Panel drawspace;
	private int mouseX;
	private int mouseY;
	private List<DataPanel> dataPanels;
	private Map<DataPanel, String> panelsWithErrors;
	
	Map<Species, Color> speciesColors;
	Population lastDrawnPopulation;
	
	//Panel pauseButtonPanel;
	TextField iterationDelayBox;
	int delay = 0;
	
	Button parametersButton;
	
	Button pauseButton;
	boolean paused = true;
	
	Button iterateButton;
	boolean iterate = false;
	
	Label genCounter;
	
	
	Frame parametersFrame;
	SelectGenomeFrame selectGenomeFrame;
	
	public NeatWindow(String title, DrawablePopulation parent) {
		super(title);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		//this.setResizable(false);
		
		dataPanels = new ArrayList<>();
		panelsWithErrors = new HashMap<>();
		
		drawspace = new Panel();
		drawspace.setIgnoreRepaint(true);
		drawspace.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent event) {
				paused = true;
				pauseButton.setLabel(" ▶ ");
				
				int x = event.getX();
				int y = event.getY();
				
				for(DataPanel p : dataPanels) {
					if(p.getX() < x && x < p.getX() + p.getWidth()
					&& p.getY() < y && y < p.getY() + p.getHeight()) {
						System.out.println(p.getClass().getName());
						p.handleClick(x-p.getX(), y-p.getY());
						p.draw(drawspace.getGraphics(), lastDrawnPopulation, speciesColors);
					}
				}
			}
			
			public void mouseEntered(MouseEvent event){}
			public void mouseExited(MouseEvent event){}
			public void mousePressed(MouseEvent event){}
			public void mouseReleased(MouseEvent event){}
			
		});
		drawspace.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent event){
				mouseX = event.getX();
				mouseY = event.getY();
			} 
			
			public void mouseDragged(MouseEvent event){
				mouseX = event.getX();
				mouseY = event.getY();
			} 
		});
		drawspace.addMouseWheelListener(new MouseWheelListener(){
			public void mouseWheelMoved(MouseWheelEvent event) {
				double notches = event.getPreciseWheelRotation();
				int x = mouseX;
				int y = mouseY;
				
				for(DataPanel p : dataPanels) {
					if(p.getX() < x && x < p.getX() + p.getWidth()
					&& p.getY() < y && y < p.getY() + p.getHeight()) {
						paused = true;
						System.out.println(p.getClass().getName());
						p.handleScroll(x-p.getX(), y-p.getY(), notches);
						p.draw(drawspace.getGraphics(), lastDrawnPopulation, speciesColors);
					}
				}
			}
		});
		this.add(drawspace);
		
		speciesColors = new HashMap<>();
		
		
		// ======================
		//
		//  Main Window Menu
		//
		// ======================
		
		// refresh display button
		Button refreshDisplay = new Button("Refresh Display");
		refreshDisplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelsWithErrors.clear();
				draw(lastDrawnPopulation);
			}
		});
		drawspace.add(refreshDisplay);
		
		// open parameters window button
		parametersButton = new Button("Parameters");
		parametersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parametersFrame.setVisible(true);
			}
		});
		drawspace.add(parametersButton);
		
		// separator
		Label pad1 = new Label("      ");
		drawspace.add(pad1);
		
		// pause simulation
		pauseButton = new Button(" ▶ ");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paused = !paused;
				
				if(paused) {
					pauseButton.setLabel(" ▶ ");
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
		
		// box for delay between iterations when on auto play
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
		
		// advance one generation
		iterateButton = new Button("❚▶");
		iterateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iterate = true;
				paused = true;
			}
		});
		drawspace.add(iterateButton);
		
		
		// separator
		drawspace.add(new Label("      "));
		
		// display what the current generation is
		genCounter = new Label("Generation N/A");
		drawspace.add(genCounter);
		
		// ======================
		//
		//  Peripheral Windows
		//
		// ======================
		
		// parameters window
		try {
			setUpParametersFrame(title);
		} catch (Exception e) {
			e.printStackTrace();
			parametersFrame = new Frame("ERROR");
		}
		
		// select display window
		selectGenomeFrame = new SelectGenomeFrame();
		
		// ======================
		//
		//  Main Window
		//
		// ======================
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w) {
				System.exit(0);
			}
		});
		
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				paused = true;
				pauseButton.setLabel(" ▶ ");
				
				parent.updateDisplayLayout();
				draw(lastDrawnPopulation);
			}
		});
		
		this.setVisible(true);
	}
	
	private void setUpParametersFrame(String mainTitle) {
	
		parametersFrame = new Frame(mainTitle + " - Parameters");
		parametersFrame.setSize(600, 700);
		//parametersFrame.setResizable(false);
		
		
		Panel mainPanel = new Panel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		
		for(Class c : neatCore.Constants.class.getDeclaredClasses()) {
			mainPanel.add(new Label("------------- " + c.getName() + " --------------"));
			
			Panel categoryPanel = new Panel();
			categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.LINE_AXIS));
			mainPanel.add(categoryPanel);
			
			Panel labelPanel = new Panel();
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
			categoryPanel.add(labelPanel);
			
			for(Field field : c.getDeclaredFields()) {
				labelPanel.add(new Label(field.getName()));
			}
			
			Panel fieldPanel = new Panel();
			fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.PAGE_AXIS));
			categoryPanel.add(fieldPanel);
			
			for(Field field : c.getDeclaredFields()) {
				TextField box = new TextField();
				resetBox(box, field);
				
				box.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateBoxVal(box, field);	
					}
				});
				box.addFocusListener( new FocusListener() {
					public void focusLost(FocusEvent e) {
						updateBoxVal(box, field);
					}
					
					public void focusGained(FocusEvent e) {}	
				});
				
				fieldPanel.add(box);
			}
		}
		
		parametersFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w) {
				parametersFrame.setVisible(false);
			}
		});
		
		parametersFrame.add(mainPanel);
	}
	
	private static void updateBoxVal(TextField box, Field field) {
		try {
			String t = field.getType().toString();
			boolean changed = false;

			if(t.equals("boolean")) {
				boolean val = Boolean.parseBoolean(box.getText());
				boolean oldVal = field.getBoolean(null);
				changed = oldVal != val;
				
				field.setBoolean(null, val);
				box.setText(val + "");
			} else if(t.equals("int")) {
				int val = (int)Double.parseDouble(box.getText());
				int oldVal = field.getInt(null);
				changed = oldVal != val;
				
				field.setInt(null, val);
				box.setText(val + "");
			} else if(t.equals("float")) {
				float val = (float)Double.parseDouble(box.getText());
				float oldVal = field.getFloat(null);
				changed = oldVal != val;
				
				field.setFloat(null, val);
				box.setText(val + "");
			} else if(t.equals("double")) {
				double val = Double.parseDouble(box.getText());
				double oldVal = field.getDouble(null);
				changed = oldVal != val;
				
				field.setDouble(null, val);
				box.setText(val + "");
			} else if(t.equals("java.lang.String")) {
				String val = box.getText();
				String oldVal = (String)field.get(null);
				changed = !val.equals(oldVal);
				
				field.set(null, box.getText());
			} else {
				box.setText("UNEDITABLE: FIELD IS UNEXPECTED TYPE");
				return;
			}

			if(changed) {
				new Thread() {
					public void run() {
						String old = box.getText();
						box.setText("UPDATED");
						try {
							Thread.sleep(500);
						} catch (Exception e) {}
		
						box.setText(old);
					}
				}.start();
			}
	
		} catch (Exception e1) {
			// e1.printStackTrace();
			new Thread() {
				public void run() {
					String old = box.getText();
					box.setText("UPDATE ERROR");
					try {
						Thread.sleep(500);
					} catch (Exception e) {}
	
					resetBox(box, field);
				}
			}.start();
		}
	}
	
	private static void resetBox(TextField box, Field field) {
		String t = field.getType() + "";
				
		try {
			if(t.equals("boolean")) {
				box.setText(field.getBoolean(null) + "");		
			} else if(t.equals("int")) {
				box.setText(field.getInt(null) + "");
			} else if(t.equals("float")) {
				box.setText(field.getFloat(null) + "");
			} else if(t.equals("double")) {
				box.setText(field.getDouble(null) + "");
			} else if(t.equals("java.lang.String")) {
				box.setText(field.get(null) + "");
			} else {
				box.setText("UNEXPECTED TYPE");
			}
		} catch (Exception e) {
			box.setText("ERROR");
		}
	}
	
	/**
	 * The display function, updates all data panels
	 */
	public void draw(Population p) {
		lastDrawnPopulation = p;
		
		Graphics gr = drawspace.getGraphics();
		gr.clearRect(0, 0, drawspace.getWidth(), drawspace.getHeight());
		
		gr.setColor(simulationCompleted? new Color(200, 10, 10) : new Color(200,200,200, 0));
		gr.fillRect(0, 0, getWidth(), HEADER_HEIGHT);
		
		for(DataPanel panel : dataPanels) {
			if(!panelsWithErrors.containsKey(panel)) {
				try {
					panel.draw(gr, p, speciesColors);
				} catch (Exception e) {
					e.printStackTrace();
					panelsWithErrors.put(panel, "DRAW ERROR: " + e.getClass().getName());
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
		
		genCounter.setText(String.format("Generation %-12d", p.getGenerationNumber()));
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
