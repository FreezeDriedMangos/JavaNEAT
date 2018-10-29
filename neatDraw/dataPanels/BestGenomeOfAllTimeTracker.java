package neatDraw.dataPanels;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.awt.Color;
import java.awt.Graphics;

import neatDraw.DataPanel;

import neatCore.Population;
import neatCore.Species;
import neatCore.Genome;

public class BestGenomeOfAllTimeTracker extends DataPanel {
	Genome theGuy;
	int genAppearedIn;
	float rawFitness;
	Species theGuysSpecies;
	
	float improvement;

	@Override
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		gr.clearRect(x, y, width, height);
		gr.setColor(Color.BLACK);
		int lineHeight = 13;
		gr.drawString(String.format("Best fitness: %1.6e", rawFitness), x, y + lineHeight);
		gr.drawString(String.format("Improved by: %1.2e", improvement), x, y + lineHeight*2);
		gr.drawString("Found " + (int)(p.getGenerationNumber() - genAppearedIn) + " gens ago.", x, y + lineHeight*3);
		GenomeDrawer.draw(theGuy, gr, x, y + lineHeight*3, width, height-(lineHeight*3), speciesColors.get(theGuysSpecies));
		
		//System.out.println(rawFitness + " " + );
	}
	
	public void captureData(Population p) {
		if(theGuy == null) {
			theGuy = p.getGenomes().get(0).copy();
			rawFitness = p.getGenomes().get(0).getRawFitness();
			genAppearedIn = p.getGenerationNumber();
			theGuysSpecies = p.getSpeciesOf(p.getGenomes().get(0));
		}
		
		for(Genome g : p.getGenomes()) {
			if(g.getRawFitness() > rawFitness) {
				improvement = g.getRawFitness() - rawFitness;
				
				theGuy = g.copy();
				rawFitness = g.getRawFitness();
				genAppearedIn = p.getGenerationNumber();
				theGuysSpecies = p.getSpeciesOf(g);
			}
		}
	}
	
	public float getDrawComplexity() {
		return 3;
	}
}
