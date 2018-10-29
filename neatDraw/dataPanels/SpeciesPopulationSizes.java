package neatDraw.dataPanels;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.awt.Color;
import java.awt.Graphics;

import neatDraw.DataPanel;

import neatCore.Species;
import neatCore.Population;

/**
 * This DataPanel displays the population of each species over time by representing a species'
 * population with a polygon color coded to that species' color. The height of each polygon
 * represents that species' population. (Time measured in generations is on the X axis.)
 */
public class SpeciesPopulationSizes extends DataPanel {
	AreaGraph graph;
	
	public SpeciesPopulationSizes() {
		graph = new AreaGraph();
	}

	@Override
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		//Graphics gr = this.getGraphics();
		gr.clearRect(x, y, width, height);
		
		HashMap<Integer, Color> colors = new HashMap<>();
		for(Entry<Species, Color> e : speciesColors.entrySet()) {
			colors.put(e.getKey().getID(), e.getValue());
		}
		
		graph.draw(gr, x, y, width, height, colors);
	}
	
	public void captureData(Population p) {
		HashMap<Integer, Float> data = new HashMap<>();
		
		for(Species s : p.getSpecies()) {
			data.put(s.getID(), s.size());
		}
		
		graph.addData(data);
	}
	
	public float getDrawComplexity() {
		return 5;
	}
	
	public void handleClick(int x, int y) {
		
	}
}
