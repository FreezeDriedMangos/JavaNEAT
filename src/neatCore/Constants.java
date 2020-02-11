package neatCore;

public class Constants {

	public static class ConnectionGene {
		public static boolean PERTURB_BY_ADDITION = false;
		public static boolean BOUND_WEIGHT        = true;
		public static float   MAX_WEIGHT          = 5;
		public static float   MIN_WEIGHT          = -5;
		public static float   TEST_ADDITION       = 42;
	}
	
	public static class Genome {
		public static double CHANCE_TO_PASS_DOWN_GENE_AS_DISABLED = 0.75;
		public static double CHANCE_TO_INHERIT_DISJOINT_OR_EXCESS = 0.5;

		public enum NodeGene { SENSOR, HIDDEN, OUTPUT }
	}
	
	public static class Species {
		/**
		 * Delta calculation: scalar for how important excess genes are considered.
		 */
		public static float C1 = 2f;//1f;
		/**
		 * Delta calculation: scalar for how important disjoint genes are considered.
		 */
		public static float C2 = 1f;
		/**
		 * Delta calculation: scalar for how important differences in weight are considered.
		 */
		public static float C3 = 0.4f;
	
		public static int IN_NUMBER_OF_GENS__TIME_LIMIT_TO_MAKE_PROGRESS = 15;
		public static float MINIMUM_CHANGE_IN_FITNESS_CONSIDERED_SIGNIFICANT_PROGRESS = 0.1f;
	
		/**
		 * The longer it has been since significant progress,
	     * the more mutation chance increases for a given species, according to <br> 
	     * <code>NO_PROGRESS_MUTATION_SCALAR * (timeSinceProgress^2)</code>
		 */
		public static float NO_PROGRESS_MUTATION_SCALAR = 0.1f;
		
		/**
		 * The longer it has been since significant progress,
	     * the more the species' members' fitnesses are penalized <br> 
	     * <code>fitness = (raw_fitness / speciesSize) / (NO_PROGRESS_FITNESS_REDUCTION_SCALAR * (gensSincePeakFitness+1))</code>
		 */
		public static float NO_PROGRESS_FITNESS_REDUCTION_SCALAR = 1f;
		
		/**
		 * Divides this genome's fitness by (FITNESS_REDUCTION_FOR_SIZE_SCALAR * numConnectionGenes)
		 * <br>
		 * the smaller this is, the smaller the genomes will be
		 */
		public static float FITNESS_REDUCTION_FOR_SIZE_SCALAR = 1.01f;
	}
	
	public static class Population {
		public static int MIN_SPECIES_POPULATION_FOR_IMMORTALITY = 10;
	
		public static float INTERSPECIES_BREEDING_RATE = 0.001f;
		public static float PERCENT_ASEXUAL = 0.2f;
		public static float PERCENT_SEXUAL  = 1f - PERCENT_ASEXUAL; 
	
		public static float CHANCE_MUTATE_WEIGHTS = 0.8f;
		public static float CHANCE_ON_WEIGHT_MUTATION_RANDOMIZE_WEIGHT = 0.1f;
		public static float CHANCE_ADD_NODE = 0.02f;
		public static float CHANCE_ADD_CONNECTION = 0.07f;//0.03f;
		
		/**
		 * If the delta of two genomes is greater than this value, they are
		 * considered part of separate species
		 */
		 //1 was good, but not great
		public static float COMPATIBILITY_THRESHOLD = 1f;//2.5f;//3f;
	}
}
