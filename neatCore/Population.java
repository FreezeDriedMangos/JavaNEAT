package neatCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static neatCore.Constants.Population.*;

/**
 * The Population class runs the NEAT algorithm, it's the pupeteer that pulls
 * all the strings that need to be pulled. It directs all the action.
 */
public abstract class Population {

	protected List<Genome> genomes;
	protected List<Species> species;
	protected Map<Genome, Species> speciesMap;

	private int gen = 0;
	protected float targetFitness;
	
	// ============================================================
	//
	//  Constructors
	//
	// ============================================================

	private Population() {
		genomes = new ArrayList<>();
		species = new ArrayList<>();
		speciesMap = new HashMap<>();
	}

	/**
	 * @param numInputNodes The number of input nodes each member of the population
	 * will have. Do not include the bias node in this number. (E.g. for a network that
	 * evaluates XOR, numInputNodes should be 2. The Genome constructor will automatically
	 * add one for the bias node.)
	 */
	public Population(int size, int numInputNodes, int numOutputNodes) {
		this();
		
		this.targetFitness = getTargetFitness();
	
		InnovationTracker it = new InnovationTracker();
		
		for(int i = 0; i < size; i++) {
			this.addGenome(new Genome(numInputNodes, numOutputNodes, it));
		}
	}

	// ============================================================
	//
	//  Core Functions
	//
	// ============================================================

	/**
	 * This function contains the program's main loop. It runs the algorithm.
	 */
	public void run() {
		setUp();
	
		// initial generation
		handleGeneration();
		
		// loop
		while(getBestRawFitness() < this.targetFitness) {
			if(isPaused()) {
				try { Thread.sleep(100); }
				catch (InterruptedException e) {}
				continue;
			}
		
			createNewGeneration();
			
			handleGeneration();
			gen++;
		}
		
		finished();
	}
	
	private void handleGeneration() {
		evaluateFitnesses();
		captureData();
		drawPopulation();
	}
	
	/**
	 * Natural selection. Handles reproduction of old generation and replaces
	 * the old generation with its children.
	 */
	private void createNewGeneration() {
		List<Genome> newGeneration = new ArrayList<>();
		
		//
		// Step 1: Calculate important fitness values
		//
		Genome bestFitRaw = genomes.get(0);
		float fitnessTotal = 0;
		Map<Species, Genome> bestFitPerSpecies = new HashMap<>();
		Map<Species, Float> fitnessTotalsPerSpecies = new HashMap<>();
		
		for(Genome g : genomes) {
			Species s = speciesMap.get(g);
			fitnessTotal += s.getModifiedFitness(g);
			bestFitRaw = g.getRawFitness() > bestFitRaw.getRawFitness() ? g : bestFitRaw;
			
			float currentTotal = fitnessTotalsPerSpecies.getOrDefault(s, 0f);
			fitnessTotalsPerSpecies.put(s, currentTotal+s.getModifiedFitness(g));
			
			Genome oldBest = bestFitPerSpecies.getOrDefault(s, g);
			if(g.getRawFitness() >= oldBest.getRawFitness()) {
				bestFitPerSpecies.put(s, g);
			}
		}
		
		//
		// Step 2: Carry over immortals
		//
		
		newGeneration.add(bestFitRaw.copy());
		for(Species s : species) {
			if(s.size() >= MIN_SPECIES_POPULATION_FOR_IMMORTALITY) {
				if(bestFitPerSpecies.get(s) != bestFitRaw) {
					newGeneration.add(bestFitPerSpecies.get(s).copy());
				}
			}
		}
		
		
		//
		// Step 3: reproduction
		//
		InnovationTracker it = new InnovationTracker();
		int numGenomesNeeded = this.getSize() - newGeneration.size();
		
		// Step 3a: asexual reproduction
		int numAsexual = (int)Math.round(numGenomesNeeded * PERCENT_ASEXUAL);
		for(int i = 0; i < numAsexual; i++) {
			Genome parent = selectParent(fitnessTotal);
			
			newGeneration.add(mutate(parent.copy(), speciesMap.get(parent), it));
		}
		
		// Step 3b: sexual reproduction
		int numSexual = (int)Math.round(numGenomesNeeded * PERCENT_SEXUAL);
		for(int i = 0; i < numSexual; i++) {
			Genome p1 = selectParent(fitnessTotal);
			
			Genome p2;
			if(Math.random() < INTERSPECIES_BREEDING_RATE) {
				p2 = selectParent(fitnessTotal);
			} else {
				Species s = speciesMap.get(p1);
				if(s == null) { System.out.println("s was NULL"); }
				if(fitnessTotalsPerSpecies == null) {System.out.println("fitnessTotalsPerSpecies was null");}
				p2 = selectParentFromSpecies(s, fitnessTotalsPerSpecies.get(s));
			}
			
			if(p2.getRawFitness() > p1.getRawFitness()) {
				// swap p1 and p2 so that p1 references the parent with greater fitness
				Genome temp = p1;
				p1 = p2;
				p2 = temp;
			}
			
			Genome child = Genome.crossover(p1, p2);
			newGeneration.add(mutate(child, speciesMap.get(p1), it));
		}
		
		//
		// Step 4: replace the old members with the new ones
		//
		// Step 4a: remove the old members
		genomes.clear();
		speciesMap.clear();
		for(Species s : species) {
			s.prepareForNextGeneration();
		}
		
		// Step 4b: add the members of the new generation
		for(Genome g : newGeneration) {
			this.addGenome(g);
		}
		
		// Step 4c: clean up - remove memberless species
		for(int i = 0; i < species.size(); i++) {
			if(species.get(i).size() <= 0f) {
				species.remove(i--);
			}
		}
		
		//
		// Step 5: Complete!
		//
	}
	
	// ============================================================
	//
	//  Reproduction
	//
	// ============================================================
	
	/**
	 * Does a weighted random selection over all genomes in the population.
	 */
	private Genome selectParent(float fitnessTotal) {
		float val = (float)(Math.random() * fitnessTotal);
		
		for(Genome g : genomes) {
			Species s = speciesMap.get(g);
			val -= s.getModifiedFitness(g);
			if(val <= 0) {
				return g;
			}
		}
		
		// this should never happen
		return null;	
	}
	
	/**
	 * Does a weighted random selection over all genomes in the species "s".
	 */
	private Genome selectParentFromSpecies(Species s, float fitnessTotal) {
		float val = (float)(Math.random() * fitnessTotal);
		
		for(Genome g : s.getGenomes()) {
			val -= s.getModifiedFitness(g);
			if(val <= 0) {
				return g;
			}
		}
		
		System.out.println("Fitness total for species #" + s.getID() + ": "+fitnessTotal);
		for(Genome g : s.getGenomes()) {
			System.out.println("Fitness is " + s.getModifiedFitness(g) + " for genome " + g);
		}
		System.out.println("Final random value = " + val);
		
		// this should never happen
		return null;
	}
	
	/**
	 * Mutates g using info from s.
	 *
	 * <p>Starts by trying mutations that change existing weights, then
	 * tries a mutation that adds a node, then lastly a mutation that adds a
	 * connection.
	 *
	 * @param g The genome to mutate. This function DIRECTLY MODIFIES g.
	 * @param s The species that g's parent belonged to.
	 * @return g after directly mutating it (does NOT clone g)
	 */
	private Genome mutate(Genome g, Species s, InnovationTracker it) {
		// mutationScalar only scales the CHANCE of mutation, it does not
		// interfere with decisions on WHICH mutation to do
		float mutationScalar = s.getMutationScalar(this.size());
		
		// Step 1: mutate existing connections' weight
		if(Math.random() * mutationScalar < CHANCE_MUTATE_WEIGHTS) {
			for(ConnectionGene gene : g.connectionGenes) {
				// mutationScalar is not involved in this decision
				if(Math.random() < CHANCE_ON_WEIGHT_MUTATION_RANDOMIZE_WEIGHT) {
					gene.randomizeWeight();
				} else {
					gene.uniformlyPerturbWeight(); //what does this mean???
				}
			}
		}
		
		// Step 2: structural mutations
		//     try add node mutation, THEN add connection
		if(Math.random() * mutationScalar < CHANCE_ADD_NODE) {
			g.addNodeMutation(it);
		}
		
		if(Math.random() * mutationScalar < CHANCE_ADD_CONNECTION) {
			g.addConnectionMutation(it);
		}
		
		return g;
	}
	
	// ============================================================
	//
	//  Misc Util
	//
	// ============================================================
	
	/**
	 * Returns the current generation. The initial generation is 0.
	 */
	public int getCurrentGeneration() {
		return gen;
	}
	
	/**
	 * Returns the number of genomes in the population.
	 */
	public int size() {
		return genomes.size();
	}
	
	/**
	 * Puts g into the first compatible species and records which species it was
	 * placed in, then adds g to the overall population.
	 */
	private void addGenome(Genome g) {
		Species s = null;
		for(Species test : species) {
			if(test.getCompatibility(g) <= COMPATIBILITY_THRESHOLD) {
				s = test;
				break;
			}
		}
			
		if(s == null) {
			s = new Species(g);
			species.add(s);
		} else {
			s.add(g);
		}
		
		speciesMap.put(g, s);
		genomes.add(g);
	}
	
	/**
	 * Returns the best raw fitness value in the current generation.
	 */
	public float getBestRawFitness() {
		float best = genomes.get(0).getRawFitness();
		for(Genome g : genomes) {
			best = Math.max(g.getRawFitness(), best);
		}
		
		return best;
	}
	
	/**
	 * Returns the number of genomes in the population
	 */
	public int getSize() {
		return genomes.size();
	}
	
	/**
	 * Returns the species g belongs to.
	 */
	public Species getSpeciesOf(Genome g) {
		return speciesMap.get(g);
	}
	
	/**
	 * Returns the number of generations that have completed before the current one
	 */
	public int getGenerationNumber() {
		return gen;
	}
	
	/**
	 * Returns a list of all species objects
	 */
	public List<Species> getSpecies() {
		return species;
	}
	
	/**
	 * Returns a list of all genome objects
	 */
	public List<Genome> getGenomes() {
		return genomes;
	}
	
	
	// ============================================================
	//
	//  Abstract Functions
	//
	// ============================================================
	
	/**
	 * This function is optional. It's called on the first line of run(), and 
	 * intended to set up the display, any data collection things, etc. 
	 */
	protected abstract void setUp();
	
	/**
	 * Sets the fitness values of every genome in the population. This function
	 * includes any simulation that your application of NEAT requires
	 */
	protected abstract void evaluateFitnesses();
	
	/**
	 * Collects data of the current generation after all fitnesses have been 
	 * evaluated. If you have any graphs, this is where you should update their data.
	 */
	protected abstract void captureData();	
	
	/**
	 * This function is optional. If you have a display for this population's 
	 * genomes, draw that display in this function.
	 */
	protected abstract void drawPopulation();
	
	
	/**
	 * This function is optional. It's called once any member of the generation
	 * reaches the target fitness. By the time this function is called, the 
	 * algorithm is over and will no longer process new generations.
	 */
	protected abstract void finished();
	
	/**
	 * Mandatory implementation: return the target fitness for this algorithm.
	 * once this fitness is reached or passed, the algorithm ends.
	 */
	protected abstract float getTargetFitness();
	
	/**
	 * Allows pausing of the simulation.
	 */
	protected abstract boolean isPaused();
}

