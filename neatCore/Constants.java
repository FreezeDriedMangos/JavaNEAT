package neatCore;

public class Constants {
	static class ConnectionGene {
		public static final boolean PERTURB_BY_ADDITION = false;
		public static final boolean BOUND_WEIGHT        = true;
		public static final float   MAX_WEIGHT          = 5;
		public static final float   MIN_WEIGHT          = -5;
	}
	
	static class Genome {
		public static final double CHANCE_TO_PASS_DOWN_GENE_AS_DISABLED = 0.75;
		public static final double CHANCE_TO_INHERIT_DISJOINT_OR_EXCESS = 0.5;

		enum NodeGene { SENSOR, HIDDEN, OUTPUT }
	}
	
	static class Species {
		public static final float C1 = 1f;
		public static final float C2 = 1f;
		public static final float C3 = 0.4f;
	
		public static final int GRACE_PERIOD = 15;
	}
	
	static class Population {
		public static int MIN_SPECIES_POPULATION_FOR_IMMORTALITY = 10;
	
		public static float INTERSPECIES_BREEDING_RATE = 0.001f;
		public static float PERCENT_ASEXUAL = 0.2f;
		public static float PERCENT_SEXUAL  = 1f - PERCENT_ASEXUAL; 
	
		public static float CHANCE_MUTATE_WEIGHTS = 0.8f;
		public static float CHANCE_ON_WEIGHT_MUTATION_RANDOMIZE_WEIGHT = 0.1f;
		public static float CHANCE_ADD_NODE = 0.02f;
		public static float CHANCE_ADD_CONNECTION = 0.03f;
	
		public static float COMPATIBILITY_THRESHOLD = 3f;
	}
}
