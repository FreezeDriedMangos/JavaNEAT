package neatDraw.dataPanels;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import java.awt.Color;
import java.awt.Graphics;

import neatDraw.DataPanel;
import neatDraw.SelectGenomeFrame;

import neatCore.Population;
import neatCore.Species;
import neatCore.Genome;

public class SpeciesList extends DataPanel {
	private static int LINE_HEIGHT = 13;
	private static int NUM_LINES = 5;
	private static int ENTRY_HEIGHT = NUM_LINES * LINE_HEIGHT;
	private static int ENTRY_PADDING = 3;
	
	private static int COLORBOX_WIDTH = 10;
	private static int COLORBOX_PADDING = 2;
	
	private static int SCROLL_BAR_WIDTH = COLORBOX_WIDTH;
	private static float SCROLL_SCALAR = 5f;

	SelectGenomeFrame selectFrame;
	ArrayList<Species> species = new ArrayList<>();
	Map<Species, Color> speciesColors;
	
	public float scrollValue = 0;
	private int totalHeight = 0;
	
	public SpeciesList(SelectGenomeFrame f) {
		selectFrame = f;
	}

	@Override
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		this.speciesColors = speciesColors;
	
		gr.clearRect(x, y, width, height);
		
		// scroll bar
		int scrollAmount = (int)(scrollValue*SCROLL_SCALAR);
		int scrollBarX = x+width-SCROLL_BAR_WIDTH;
		gr.setColor(new Color(240,240,240));
		gr.fillRect(scrollBarX, y, SCROLL_BAR_WIDTH, height);
		
		gr.setColor(new Color(200,200,200));
		totalHeight = p.getSpecies().size()*(ENTRY_HEIGHT+ENTRY_PADDING) - ENTRY_PADDING;
		if(scrollAmount > totalHeight) {
			scrollAmount = totalHeight;
		}
		
		if(false&& height >= totalHeight) {
			gr.fillRect(scrollBarX, y, SCROLL_BAR_WIDTH, height);
		} else {
			float fractionDisplayed = (float)height/(float)totalHeight;
			int scrollBarHeight = (int)Math.ceil(fractionDisplayed*height);
			
			float fractionScrolled = (float)scrollAmount / (float)totalHeight;
			System.out.println(fractionScrolled+" ?? " + scrollAmount);
			int scrollBarY = y + (int)(fractionScrolled*height);
			gr.fillRect(scrollBarX, scrollBarY, SCROLL_BAR_WIDTH, scrollBarHeight);
		}
		
		int minY = y;
		y -= scrollAmount;
		
		// actual stuff
		int i = 0;
		for(Species s : p.getSpecies()) {
			int thisY = y + i*(ENTRY_HEIGHT+2*ENTRY_PADDING);
			
			if(thisY < minY) { i++; continue; }
			if(thisY + ENTRY_HEIGHT > minY+height) { i++; continue; }
			
			gr.setColor(speciesColors.get(s));
			gr.fillRect(x, thisY, COLORBOX_WIDTH, ENTRY_HEIGHT+ENTRY_PADDING);
			
			gr.setColor(Color.BLACK);
			gr.drawString(
				String.format(
					"Best fitness: %1.6e (found %d gens ago)", 
					s.getPeakRawFitness(),  
					s.getGensSincePeakRawFitness()), 
				x+(COLORBOX_WIDTH+COLORBOX_PADDING), 
				thisY + 1*LINE_HEIGHT);
			gr.drawString(
				String.format("Population: %d members", (int)s.size()), 
				x+(COLORBOX_WIDTH+COLORBOX_PADDING), 
				thisY + 2*LINE_HEIGHT);
			gr.drawString(
				String.format("Latest gen best: %1.6e ", s.getLatestBestRawFitness()), 
				x+(COLORBOX_WIDTH+COLORBOX_PADDING), 
				thisY + 3*LINE_HEIGHT);
			
			Genome g = Genome.createTestingGenome();
			g.setFitness(1);
			gr.drawString(String.format("Species fitness multiplier: %1.6e ", s.getModifiedFitnessBeforeAdjustingForSize(g)), x+(COLORBOX_WIDTH+COLORBOX_PADDING), thisY + 4*LINE_HEIGHT);
			gr.drawString(String.format("Species mutation multiplier: %1.6e ", s.getMutationScalar(p.size())), x+(COLORBOX_WIDTH+COLORBOX_PADDING), thisY + 5*LINE_HEIGHT);
			
			
			i++;
		}
	}
	
	public void handleClick(int x, int y) {
		y += (int)(scrollValue*SCROLL_SCALAR);
		y /= ENTRY_HEIGHT+ENTRY_PADDING;
	
		Species s = species.get(y);
	
		selectFrame.setGenome(s.getRepresentative(), speciesColors.getOrDefault(s, Color.WHITE), "Representative of Species #" + s.getID());
		//selectFrame.setTitle("Representative of Species #" + s.getID());
		//selectFrame.draw();
	}
	
	public void handleScroll(int x, int y, double scrollAmount) {
		scrollValue += scrollAmount;
		
		
		if(scrollValue > totalHeight/SCROLL_SCALAR) {
			scrollAmount = totalHeight/SCROLL_SCALAR;
		} else if (scrollValue < 0) {
			scrollValue = 0;
		}
	}
	
	public void captureData(Population p) {
		species.clear();
		species.addAll(p.getSpecies());
	}
	
	public float getDrawComplexity() {
		return 3.5f;
	}
}


class Data {
	//Color color;
	float bestFitness;
	int genOfBestFitness;
	
	float latestGenBestFitness;
	
	// based on lack of progress
	float fitnessMultiplier;
	
	float mutationScalar;
}




