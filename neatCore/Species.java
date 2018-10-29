package neatCore;

import java.util.ArrayList;
import java.util.List;

import static neatCore.Constants.Species.*;

/**
 * This class is meant to be used for organization, rather than actual data storage.
 * I didn't want this class to have the member "List<Genome> genomes", but I 
 * couldn't think of a better way to map a species to a list of genomes.
 */
public class Species {
	private static int ID_COUNTER = 0;
	

	Genome representative;
	List<Genome> genomes;
	
	private int age = 0;
	
	private float peakRawFitness = Integer.MIN_VALUE;
	private int   gensSincePeakFitness = 0; 
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
	 * sets the value of gensSincePeakFitness accordingly. Clears the current members.
	 *
	 * Directly in line with the paper, it also selects a new representative
	 * randomly.
	 */
	public void prepareForNextGeneration() {
		age++;
		
		boolean updatedRawPeak = false;
		for(Genome g : genomes) {
			// this is intentionally strictly greater than. This is to track 
			// improvement. If it were instead greater than or equal to, it would
			// allow for stagnation
			if(g.getRawFitness() > peakRawFitness) {
				gensSincePeakFitness = 0;
				peakRawFitness = g.getRawFitness();
				updatedRawPeak = true;
			}
		}
		
		if(!updatedRawPeak) {
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
		float sharedFitness = g.getRawFitness() / (float)this.size();
		sharedFitness /= ((float)this.gensSincePeakFitness+1);
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
	 * Returns the number of genomes that are part of this species. In other words,
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
	
	public List<Genome> getGenomes() {
		return genomes;
	}
	
	public float getMutationScalar(float totalPopulation) {
		return 1 + (this.size() / totalPopulation) + (gensSincePeakFitness >= GRACE_PERIOD? 1 : 0);
	}
	
	/**
	 * Returns an integer that uniquely represents this Species object
	 */
	public int getID() {
		return id;
	}
}
