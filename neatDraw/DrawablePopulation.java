package neatDraw;

import neatCore.Population;
import neatDraw.dataPanels.PopulationDisplay;
import neatDraw.dataPanels.SpeciesPopulationSizes;
import neatDraw.dataPanels.FitnessTracker;
import neatDraw.dataPanels.BestGenomeOfAllTimeTracker;


/**
 * This class is meant to handle all of the GUI stuff for you. Extend it if you
 * want to run a NEAT simulation with a GUI.
 */
public abstract class DrawablePopulation extends Population {
	
	NeatWindow window;
	
	public DrawablePopulation(int size, int numInputNodes, int numOutputNodes) { 
		super(size, numInputNodes, numOutputNodes);
		
	}
	
	@Override
	protected void setUp() {
		window = new NeatWindow(getTitle());
		
		int headerHeight = 36;
		int populationSizesHeight = 75;
		int standardPanelHeight = 100;
		
		int width1 = 200;
		int width2 = NeatWindow.WINDOW_WIDTH-width1;
		
		PopulationDisplay pDisplay = new PopulationDisplay();
		pDisplay.setSize(width2, window.drawspace.getHeight()-headerHeight-populationSizesHeight);
		pDisplay.setLocation(width1,headerHeight);
		window.addDataPanel(pDisplay);
		
		SpeciesPopulationSizes pSizes = new SpeciesPopulationSizes();
		pSizes.setSize(width2, populationSizesHeight);
		pSizes.setLocation(width1, window.drawspace.getHeight()-populationSizesHeight);
		window.addDataPanel(pSizes);
		
		BestGenomeOfAllTimeTracker bTracker = new BestGenomeOfAllTimeTracker();
		
		bTracker.setSize(width1, standardPanelHeight);
		bTracker.setLocation(0, headerHeight);
		window.addDataPanel(bTracker);
		
		FitnessTracker fTracker = new FitnessTracker();
		fTracker.setSize(width1, standardPanelHeight);
		fTracker.setLocation(0, headerHeight+standardPanelHeight);
		window.addDataPanel(fTracker);
		
	}
	
	@Override
	protected void captureData() {
		window.captureData(this);
	}	
	
	@Override
	protected void drawPopulation() {
		window.draw(this);
	}
	
	@Override
	protected boolean isPaused() {
		if(window.iterate) {
			window.iterate = false;
			return false;
		}
		
		return window.paused;
	}
	
	
	// ============================================================
	//
	//  Non-Overridden Abstract Functions (for reference)
	//
	// ============================================================
	protected abstract void evaluateFitnesses();
	protected abstract float getTargetFitness();
	protected abstract void finished();
	
	// ============================================================
	//
	//  New abstract functions
	//
	// ============================================================
	
	protected abstract String getTitle();
}






