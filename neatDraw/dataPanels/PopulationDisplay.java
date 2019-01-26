package neatDraw.dataPanels;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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

import neatDraw.DataPanel;
import neatDraw.SelectGenomeFrame;

/**
 * This DataPanel draws literally every member of the population on every draw() call. Each genome
 * is color coded to represent what species it's a part of.
 */
public class PopulationDisplay extends DataPanel {
	public static final int BACKGROUND_SATURATION = 50;

	int n = -1;
	int genomeWidth;
	int genomeHeight;
	ArrayList<Genome> genomes = new ArrayList<>();
	HashMap<Genome, Color> colors = new HashMap<>();

	//Frame selectDisplayFrame;
	//Panel drawspace;
	//SelectGenomeDisplay selectDisplay;
	
	SelectGenomeFrame selectGenomeFrame;

	public PopulationDisplay(SelectGenomeFrame f) {
		selectGenomeFrame = f;
		
		/*
		selectDisplayFrame = new Frame();
		selectDisplayFrame.setSize(500, 600);
		selectDisplayFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w) {
				selectDisplayFrame.setVisible(false);
			}
		});
		selectDisplayFrame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				redrawSelectDisplay();
			}
		});
		
		drawspace = new Panel();
		drawspace.setIgnoreRepaint(true);
		selectDisplayFrame.add(drawspace);
		
		selectDisplay = new SelectGenomeDisplay();
		*/
	}

	@Override
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		try {
			genomes.clear();
			colors.clear();
	
			for(Species s : p.getSpecies()) {
				for(Genome g : s.getGenomes()) {
					genomes.add(g);
					colors.put(g, speciesColors.get(s));
				}
			}
		
			java.util.Collections.sort(genomes, new java.util.Comparator<Genome>() {
				public int compare(Genome g1, Genome g2) {
					if(g1.getRawFitness() < g2.getRawFitness()) {
						return 1;
					} else if (g1.getRawFitness() == g2.getRawFitness()) {
						return 0;
					} else {
						return -1;
					}
				} 
			});
		
	
			//Graphics gr = this.getGraphics();
			gr.clearRect(x, y, width, height);
		
			n = (int)Math.ceil(Math.sqrt(p.size()));
		
			genomeWidth = width / n;
			genomeHeight = height / n;
		
			int i = 0;
			for(Genome g : genomes) {
			//for(Species s : p.getSpecies()) {
			//	for(Genome g : s.getGenomes()) {
					int col = i%n;
					int row = i/n; 
			
					int alpha = 255-BACKGROUND_SATURATION;
					System.out.println(alpha);
					Color baseColor = row%2 == col%2 ? new Color(220,220,220, alpha) : new Color(200,200,200, alpha);
					Color speciesColor = speciesColors.get(p.getSpeciesOf(g));
					gr.setColor(speciesColor);
					gr.fillRect(x + col*genomeWidth, y + row*genomeHeight, genomeWidth, genomeHeight);
					gr.setColor(baseColor);
					gr.fillRect(x + col*genomeWidth, y + row*genomeHeight, genomeWidth, genomeHeight);
			
					GenomeDrawer.draw(g, gr, x + col*genomeWidth, y + row*genomeHeight, genomeWidth, genomeHeight, speciesColor);
					i++;
				
					//genomes.add(g);
					//colors.put(g, speciesColors.get(s));
			//	}
			//}
			}
		} catch (java.util.ConcurrentModificationException e) {
			System.out.println("ConcurrentModificationException:neatDraw.dataPanels.PopulationDisplay");
		}
	}
	
	public static Color multiply(Color c1, Color c2) {
		// Broken????
		return new Color(
			c1.getRed() * c2.getRed() / 255f,
			c1.getGreen() * c2.getGreen() / 255f,
			c1.getBlue() * c2.getBlue() / 255f	
			);
	}
	
	public void captureData(Population p) {}
	
	public float getDrawComplexity() {
		return 20;
	}
	
	public void handleClick(int x, int y) {
		setUpSelectDisplay(x, y);
	}
	
	private void setUpSelectDisplay(int x, int y) {
		int row = y / genomeHeight;
		int col = x / genomeWidth;
		
		int index = row*n + col;
		
		//selectDisplayFrame.setTitle("Genome #"+index);
		//selectDisplayFrame.setVisible(true);
		//selectGenomeFrame.setTitle("Genome #"+index);
		//selectGenomeFrame.setVisible(true);
		
		Genome gn = genomes.get(index).copy();
		gn.setFitness(genomes.get(index).getRawFitness());
		//selectDisplay.setGenome(gn, colors.get(genomes.get(index)));
		//selectDisplay.color = colors.get(genomes.get(index));
		selectGenomeFrame.setGenome(gn, colors.get(genomes.get(index)), "Genome #"+index);
		
		//redrawSelectDisplay();
		//selectGenomeFrame.draw();
	}
	
	/*
	private void redrawSelectDisplay() {
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
	}*/
}










