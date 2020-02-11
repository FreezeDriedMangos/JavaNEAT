package neatCore;

import java.util.ArrayList;
import java.util.List;

import static neatCore.Constants.Species.*;

/**
 * This class is meant to be used for organization, rather than actual data storage.
 * I didn't want this class to have the member "List&lt;Genome&gt; genomes", but I 
 * couldn't think of a better way to map a species to a list of genomes.
 */
public class Species {
	private static int ID_COUNTER = 0;
	

	Genome representative;
	List<Genome> genomes;
	
	private int age = 0;
	
	private float peakRawFitness = Integer.MIN_VALUE;
	private int   gensSincePeakFitness = 0;
	private float latestBestRawFitness; 
	private int   id;
	
	
	private Species() {
		id = ID_COUNTER++;
		genomes = new ArrayList<>();
	}
	
	/**
	 * Constructs a new species with Genome g as its representative. Also adds
	 * g as a member.
	 */
	public Species(Genome g) {
		this();
		
		this.representative = g;
		genomes.add(g);
	}
	
	/**
	 * Increments the species' age, looks for a new peak fitness (unadjusted), and
	 * sets the value of gensSincePeakFitness to the new peak iff the new peak is at least
	 * MINIMUM_CHANGE_IN_FITNESS_CONSIDERED_SIGNIFICANT_PROGRESS higher than the old. 
	 * Finally, it deletes the current members.
	 * <br>
	 * Directly in line with the paper, it also selects a new representative
	 * randomly.
	 */
	public void prepareForNextGeneration() {
		age++;
		
		latestBestRawFitness = genomes.get(0).getRawFitness();
		
		for(Genome g : genomes) {
			if(g.getRawFitness() > latestBestRawFitness) {
				latestBestRawFitness = g.getRawFitness();
			}
		}
		
		// peakRawFitness should only be updated if latestBestRawFitness is 
		// considered significant progress on the old peakRawFitness
		if(latestBestRawFitness-MINIMUM_CHANGE_IN_FITNESS_CONSIDERED_SIGNIFICANT_PROGRESS >= peakRawFitness) {
			gensSincePeakFitness = 0;
			peakRawFitness = latestBestRawFitness;
		} else {
			gensSincePeakFitness++;
		}
		
		int randomIndex = (int)(Math.random() * this.size());
		representative = genomes.get(randomIndex);
		
		genomes.clear();
	}
	
	/**
	 * Calculates the fitness of g, adjusted for species size and progress.
	 */
	 public float getModifiedFitness(Genome g) {
		float sharedFitness = getModifiedFitnessBeforeAdjustingForSize(g);
		
		sharedFitness /= FITNESS_REDUCTION_FOR_SIZE_SCALAR*g.size();
		
		return sharedFitness;
	}
	
	public float getModifiedFitnessBeforeAdjustingForSize(Genome g) {
		float sharedFitness = g.getRawFitness() / (float)this.size();
		
		// note to self: the simulation works better (at least with the default constants) when species don't get this grace period
		if(this.gensSincePeakFitness >= IN_NUMBER_OF_GENS__TIME_LIMIT_TO_MAKE_PROGRESS) {
			sharedFitness /= (NO_PROGRESS_FITNESS_REDUCTION_SCALAR * (float)(this.gensSincePeakFitness+1));
		}
		
		
		return sharedFitness;
	}
	
	/**
	 * Determines the compatibility of g and this species' representative, and 
	 * returns the result.
	 */
	public float getCompatibility(Genome g) {
		return Genome.delta(representative, g, C1, C2, C3);
	}
	
	/**
	 * Returns the number of genomes that are part of this species as a float. In other words,
	 * the species' population size.
	 */
	public float size() {
		return genomes.size();
	}
	
	/**
	 * Adds a genome to the species.
	 */
	public void add(Genome g) {
		genomes.add(g);
	}
	
	/**
	 * Returns a list of all genomes that are a member of this species.
	 */
	public List<Genome> getGenomes() {
		return genomes;
	}
	
	/**
	 * Returns a scalar that influences how common mutations are among this species according to the
	 * following formula:
	 * <br>
	 * <code>mutation_scalar = 1 + (this.size() / totalPopulation) + (gensSincePeakFitness &gt;= GRACE_PERIOD? 1 : 0);</code>
	 */
	public float getMutationScalar(float totalPopulation) {
		return 1 + (this.size() / totalPopulation) + getMutationFactorDueToLackOfProgress();
	}
	
	/**
	 * Calculates how much higher the chance for mutation should be accounting
	 * for this species' lack of significant progress, if applicable
	 */
	public float getMutationFactorDueToLackOfProgress() {
		int val = IN_NUMBER_OF_GENS__TIME_LIMIT_TO_MAKE_PROGRESS - gensSincePeakFitness;
		
		if(val <= 0) {
			// this species still has time to prove progress
			return 0;
		}
		
		return val * val * NO_PROGRESS_MUTATION_SCALAR;
	}
	
	/**
	 * Returns an integer that uniquely represents this Species object
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Returns the fitness of the most fit member of this species that has
	 * ever lived.
	 */
	public float getPeakRawFitness() {
		return peakRawFitness;
	}
	/**
	 * Returns how many generations have happened since the most fit member
	 * of this species was first seen.
	 */
	public int getGensSincePeakRawFitness() {
		return gensSincePeakFitness;
	}
	/**
	 * Returns the raw fitness of the most fit member of this species of 
	 * the current generation.
	 */
	public float getLatestBestRawFitness() {
		return latestBestRawFitness;
	}
	
	/**
	 * returns a copy of the representative. Meant for classes outside this package.
	 * All classes that are inside this package (and therefore part of the algorithm)
	 * should directly access the field.
	 */
	public Genome getRepresentative() {
		Genome g = representative.copy();
		g.setFitness(g.getRawFitness());
		return g;
	}
}
