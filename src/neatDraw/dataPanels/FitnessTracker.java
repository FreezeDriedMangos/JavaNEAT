package neatDraw.dataPanels;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.awt.Color;
import java.awt.Graphics;

import neatDraw.DataPanel;

import neatCore.Population;
import neatCore.Species;

public class FitnessTracker extends DataPanel {
	LineGraph graph;
	
	public FitnessTracker() {
		graph = new LineGraph();
	}

	@Override
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		//Graphics gr = this.getGraphics();
		gr.clearRect(x, y, width, height);
		
		graph.draw(gr, x, y, width, height);
	}
	
	public void captureData(Population p) {
		graph.addData(p.getBestRawFitness());
	}
	
	public float getDrawComplexity() {
		return 2;
	}
}
