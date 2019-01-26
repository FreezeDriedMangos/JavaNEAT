package neatDraw;

import neatCore.Population;
import neatDraw.dataPanels.PopulationDisplay;
import neatDraw.dataPanels.SpeciesPopulationSizes;
import neatDraw.dataPanels.FitnessTracker;
import neatDraw.dataPanels.BestGenomeOfAllTimeTracker;
import neatDraw.dataPanels.SpeciesList;


/**
 * This class is meant to handle all of the GUI stuff for you. Extend it if you
 * want to run a NEAT simulation with a GUI.
 */
public abstract class DrawablePopulation extends Population {
	
	NeatWindow window;
	
	PopulationDisplay pDisplay;
	SpeciesPopulationSizes pSizes;
	BestGenomeOfAllTimeTracker bTracker;
	FitnessTracker fTracker;
	SpeciesList sList;
	
	boolean simulationCompleted = false;
	
	public DrawablePopulation(int size, int numInputNodes, int numOutputNodes) { 
		super(size, numInputNodes, numOutputNodes);
		
	}
	
	@Override
	protected void setUp() {
		window = new NeatWindow(getTitle(), this);
		
		pDisplay = new PopulationDisplay(window.selectGenomeFrame);
		window.addDataPanel(pDisplay);
		
		pSizes = new SpeciesPopulationSizes();
		window.addDataPanel(pSizes);
		
		bTracker = new BestGenomeOfAllTimeTracker(window.selectGenomeFrame);
		window.addDataPanel(bTracker);
		
		fTracker = new FitnessTracker();
		window.addDataPanel(fTracker);
		
		sList = new SpeciesList(window.selectGenomeFrame);
		window.addDataPanel(sList);
		
		updateDisplayLayout();
		
		System.out.println(getWidth() + " x " + getHeight());
	}
	
	public void updateDisplayLayout() {
		// note, at the time of coding this, the default drawspace dimensions are 1190 x 570 
		int headerHeight          = NeatWindow.HEADER_HEIGHT;//(int)(getHeight() * 36f/570f);
		int populationSizesHeight = (int)(getHeight() * 75f/570f);
		int standardPanelHeight   = (int)(getHeight() * 100f/570f);
		
		int width1 = (int)(getWidth() * 320f/1190f);
		int width2 = (int)(getWidth())-width1;
		
		
		pDisplay.setSize(width2, window.drawspace.getHeight()-headerHeight-populationSizesHeight);
		pDisplay.setLocation(width1,headerHeight);
		
		pSizes.setSize(width2, populationSizesHeight);
		pSizes.setLocation(width1, window.drawspace.getHeight()-populationSizesHeight);
		
		bTracker.setSize(width1, standardPanelHeight);
		bTracker.setLocation(0, headerHeight);
		
		fTracker.setSize(width1, standardPanelHeight);
		fTracker.setLocation(0, headerHeight+standardPanelHeight);
		
		int sListY = fTracker.getY() + fTracker.getHeight();
		sList.setSize(width1, window.drawspace.getHeight() - sListY);
		sList.setLocation(0, sListY);
	}
	
	protected int getWidth() {
		return window.drawspace.getWidth();
	}
	protected int getHeight() {
		return window.drawspace.getHeight();
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
	
	@Override
	protected boolean continueCondition() {
		boolean superCondition = super.continueCondition();
		
		if(!this.simulationCompleted && !superCondition) {
			window.paused = true;
			this.simulationCompleted = true;
			window.simulationCompleted = true;
			System.out.println("I paused!");
		}
		
		return true;
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






