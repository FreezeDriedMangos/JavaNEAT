package neatDraw.dataPanels;

import java.util.Map;
import java.util.ArrayList;

import java.awt.Panel;
import java.awt.Color;
import java.awt.Graphics;

import neatCore.Population;
import neatCore.Species;
import neatCore.Genome;

import neatDraw.DataPanel;

/**
 * This DataPanel draws literally every member of the population on every draw() call. Each genome
 * is color coded to represent what species it's a part of.
 */
public class PopulationDisplay extends DataPanel {
	int n = -1;
	ArrayList<Genome> genomes = new ArrayList<>();

	@Override
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		//Graphics gr = this.getGraphics();
		gr.clearRect(x, y, width, height);
		
		n = (int)Math.sqrt(p.size());
		
		int genomeWidth = width / n;
		int genomeHeight = height / n;
		
		int i = 0;
		for(Species s : p.getSpecies()) {
			for(Genome g : s.getGenomes()) {
				int col = i%n;
				int row = i/n; 
			
				gr.setColor(row%2 == col%2 ? new Color(220,220,220) : new Color(200,200,200));
				gr.fillRect(x + col*genomeWidth, y + row*genomeHeight, genomeWidth, genomeHeight);
			
				GenomeDrawer.draw(g, gr, x + col*genomeWidth, y + row*genomeHeight, genomeWidth, genomeHeight, speciesColors.get(p.getSpeciesOf(g)));
				i++;
			}
		}
	}
	
	public void captureData(Population p) {}
	
	public float getDrawComplexity() {
		return 20;
	}
	
	
	public void handleClick(int x, int y) {
		
	}
}






