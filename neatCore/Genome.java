package neatCore;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.awt.Point;

import static neatCore.Constants.Genome.*;

public class Genome {
	
	// actual genome
	protected List<ConnectionGene> connectionGenes;
	protected List<NodeGene>       nodeGenes;
	
	// supporting members
	protected ArrayList<Integer> linearToposort;
	protected ArrayList<ArrayList<Integer>> toposort;
	protected HashSet<Point>      existingConnections;
	protected int numSensorNodes;
	protected int numOutputNodes;
	
	// fitness members
	boolean fitnessHasBeenSet = false;
	float fitness;
	
	// ============================================================
	//
	//  Constructors
	//
	// ============================================================
	
	/**
	 * Creates a genome purely for testing purposes, not meant to join the population. 
	 * Keep this specimen contained within the lab. Don't let it escape.
	 */
	public static Genome createTestingGenome() {
		return new Genome();
	}
	
	private Genome() {
		connectionGenes = new ArrayList<>();
		nodeGenes       = new ArrayList<>();
		existingConnections = new HashSet<>();
	}
	
	/**
	 * Creates a new genome with the specified number of input and output nodes.
	 * Creates one connection for every sensor/output pair.
	 * Automatically includes an extra input node to account for the bias node.
	 * 
	 * <p>Note: for ease of implementation, these genomes will only have one bias
	 * node, which is treated as an input node. It is always at index 0 of nodeGenes.
	 */
	public Genome(int numInput, int numOutput, InnovationTracker it) throws IllegalArgumentException {
		this();
		
		if(numInput <= 0 || numOutput <= 0) {
			throw new IllegalArgumentException("numInput and numOutput must be strictly greater than 0.");
		}
		
		numInput++;
		for(int i = 0; i < numInput; i++) {
			nodeGenes.add(NodeGene.SENSOR);
		}
		
		for(int i = 0; i < numOutput; i++) {
			nodeGenes.add(NodeGene.OUTPUT);
		}
		
		for(int j = 0; j < numInput; j++) {
			for(int k = 0; k < numOutput; k++) {
				connectionGenes.add(new ConnectionGene(j, numInput+k, it));
			}
		}
		
		numSensorNodes = numInput;
		numOutputNodes = numOutput;
	}
	
	// ============================================================
	//
	//  Evaluation
	//
	// ============================================================
	
	/** 
	 * Takes some input and evaluates this genome as a feed-forward neural network
	 * to provide some output. Note: for ease of implementation, there is only
	 * one bias node, which is treated as an input node. This bias node is always
	 * at index 0.
	 * <br>
	 * Note: you do not have to include a bias value in the input array, it is added 
	 * automatically.
	 */
	public float[] evaluate(float[] input) {
		if(input.length != numSensorNodes-1) {
			System.out.println(this);
			throw new IllegalArgumentException("Must provide an array input[] with length equal to the number of sensor nodes this genome was given on construction. Got size: " + input.length + " Expected size: " + (numSensorNodes-1));
		}
	
		if(toposort == null) {
			toposort();
		}
		
		// the first node is always the bias node
		float[] nodeValues = new float[nodeGenes.size()];
		nodeValues[0] = 1;
		
		// search for other input nodes and set their values according to "input"
		int k = 0;
		for(int i = 1; i < nodeGenes.size(); i++) {
			if(nodeGenes.get(i) == NodeGene.SENSOR) {
				nodeValues[i] = input[k++];
			}
		}
		
		// forward propogation
		for(ArrayList<Integer> layer : toposort) {
			for(Integer node : layer) {
				nodeValues[node] = sigmoid(nodeValues[node]);
			
				for(ConnectionGene g : connectionGenes) {
					if(g.in == node) {
						nodeValues[g.out] += g.weight*nodeValues[g.in];
					}
				}
			}
		}	
		
		// search for output nodes, collect values
		float[] output = new float[numOutputNodes];
		k = 0;
		for(int i = 0; i < nodeGenes.size(); i++) {
			if(nodeGenes.get(i) == NodeGene.OUTPUT) {
				output[k++] = nodeValues[i];
			}
		}
				
		return output;
	} 
	
	private static float sigmoid(float x) {
		return (float)(1f / (1f + Math.pow(Math.E, -4.9*x)));
	}
	
	// ============================================================
	//
	//  Mutation
	//
	// ============================================================
	
	// should mutations be allowed to fail?
	/**
	 * Adds a new connection from a non-output node to a non-sensor/bias node
	 * @return whether or not the mutation was successful
	 */
	public boolean addConnectionMutation(InnovationTracker it) {
		if(linearToposort == null)
			toposort();
		
		// inIndex will always be the topologically sorted index of either a 
		// sensor node or hidden node, and outIndex will always be the 
		// topologically sorted index of either a sensor node or a hidden node 
		// that doesn't have a directed edge to inIndex's node
		//
		// this ensures that no cycles will be formed, no sensor node will 
		// have an edge lead into it, and no output node will have an edge
		// lead out of it (note: those last two conditions also cause cycles)
		
		int inIndex  = (int)(Math.random()*(linearToposort.size() - numOutputNodes));
		int outIndexMin = Math.max(inIndex+1, numSensorNodes);
		int outIndex = (int)(Math.random()*(linearToposort.size() - outIndexMin) + outIndexMin);
		
		int in = linearToposort.get(inIndex);
		int out = linearToposort.get(outIndex);
		
		Point thisPair = new Point(in, out);
		
		if(existingConnections.contains(thisPair)) {
			return false; //failed mutation
		}
		
		existingConnections.add(thisPair);
		
		ConnectionGene g = new ConnectionGene(thisPair, it);
		connectionGenes.add(g);
		
		toposort();
		
		return true;
	}
	
	/**
	 * Adds a new node by replacing an existing connection
	 * @return whether or not the mutation was successful
	 */
	public boolean addNodeMutation(InnovationTracker it) {
		int index = (int)(Math.random() * connectionGenes.size());
		ConnectionGene toReplace = connectionGenes.get(index);
		
		if(!toReplace.enabled) {
			return false; //failed mutation
		}
		
		toReplace.enabled = false;
		
		NodeGene newGene = NodeGene.HIDDEN;
		
		int newNode = nodeGenes.size();
		nodeGenes.add(newGene);
		
		
		ConnectionGene new1 = new ConnectionGene(toReplace.in, newNode, it);
		new1.weight = 1;
		existingConnections.add(new Point(toReplace.in, newNode));
		
		ConnectionGene new2 = new ConnectionGene(newNode, toReplace.out, it);
		new2.weight = toReplace.weight;
		existingConnections.add(new Point(newNode, toReplace.out));
		
		
		connectionGenes.add(new1);
		connectionGenes.add(new2);
		
		toposort();
		
		return true;
	}
	
	// ============================================================
	//
	//  Utility
	//
	// ============================================================
	
	private void toposort() {
		toposort = new ArrayList<>();
		linearToposort = new ArrayList<>();
		
		ArrayList<ConnectionGene> edges = new ArrayList<>();
		for(ConnectionGene g : connectionGenes)
			if(g.enabled)
				edges.add(g);
		
		int inDegree[] = new int[nodeGenes.size()];
		for(ConnectionGene g : edges) {
			if(!g.enabled) continue;
				
			inDegree[g.out]++;
		}
		
		HashSet<Integer> visited = new HashSet<>();
		int c = 0;
		while(visited.size() != nodeGenes.size() && c < nodeGenes.size()) {
			c++;
		
			ArrayList<Integer> layer = new ArrayList<>();
			
			for(int i = 0; i < inDegree.length; i++) {
				if(!visited.contains(i) && inDegree[i] == 0) {
					// add to current layer
					visited.add(i);
					layer.add(i);
					linearToposort.add(i);
				}
			}
			for(int i = 0; i < edges.size(); i++) {
				ConnectionGene edge = edges.get(i);
				
				if(visited.contains(edge.in)) {
					inDegree[edge.out]--;
					
					edges.remove(i);
					i--;
				}
			}
			
			if(layer.size() > 0)
				toposort.add(layer);
		}
	}
	
	/**
	 * A debug tool that prints the nodes of this genome, layer by layer, in a
	 * segmented toposort-like manner.
	 */
	public void printToposort() {
		System.out.println("===Toposort===");
		System.out.println(connectionGenes);
		for(ArrayList<Integer> layer : toposort) {
			for(Integer node : layer) {
				System.out.print(node + " ");
			}
			System.out.println();
		}
		System.out.println("==============");	
	}
	
	/**
	 * Creates a new Genome that has no references in common with "this" but has
	 * all the same values, apart from fitnessHasBeenSet (initialized to false)
	 * and fitness (not initialized).
	 * 
	 */
	public Genome copy() {
		Genome g = new Genome();
		g.numSensorNodes = this.numSensorNodes;
		g.numOutputNodes = this.numOutputNodes;
		
		for(NodeGene n : nodeGenes) {
			g.nodeGenes.add(n); // superCopy also copies genes' disabled member
		}
		
		for(ConnectionGene n : connectionGenes) {
			g.connectionGenes.add(n.superCopy());
		}
		
		g.existingConnections.addAll(this.existingConnections);
		g.toposort();
		
		return g;
	}
	
	public String toString() {
		return nodeGenes.toString() + " " + connectionGenes.toString();
	}
	
	// ============================================================
	//
	//  Get and Set
	//
	// ============================================================
	
	/**
	 * Returns this genome's fitness, not adjusting for what species it is part
	 * of.
	 */
	public float getRawFitness() {
		return fitness;
	}
	
	/**
	 * Sets the fitness of this genome. It can only called once per object.
	 */
	public void setFitness(float f) throws IllegalStateException {
		if(fitnessHasBeenSet) {
			throw new IllegalStateException("This genome's fitness has already been set.");
		}
		
		fitnessHasBeenSet = true;
		fitness = f;
	}
	
	/**
	 * Returns an array list of layers of this genome (interpereted as a neural net).
	 * I.e. All nodes with no input edges are in getToposort().get(0). Each node is
	 * represented by its index in the genome.
	 */
	public ArrayList<ArrayList<Integer>> getToposort() {
		if(toposort == null) {
			toposort();
		}
		
		return toposort;
	}
	
	/**
	 * Returns an ArrayList of all enabled connection genes, each represented
	 * by a Point object.
	 */
	public ArrayList<Point> getEnabledConnectionsAsPoints() {
		ArrayList<Point> retval = new ArrayList<>();
		
		for(ConnectionGene g : connectionGenes) {
			if(g.enabled) {
				retval.add(new Point(g.in, g.out));
			}
		}
		
		return retval;
	}
	
	public List<ConnectionGene> getConnectionGenes() {
		return connectionGenes;
	}
	public List<NodeGene> getNodeGenes() {
		return nodeGenes;
	}
	
	/**
	 * returns the number of genes in this genome, ie <br>
	 * <code>connectionGenes.size() + nodeGenes.size()</code>
	 */
	public int size() {
		return connectionGenes.size() + nodeGenes.size();
	}
	
	// ============================================================
	//
	//  Static methods
	//
	// ============================================================
	
	/**
	 * Gateway function to the proteced crossover function. Please see its javadoc
	 *
	 * The parameters of this function do not need any special order. I.e. either
	 * p1 or p2 may be the more fit parent
	 *
	 * @param p1 one parent
	 * @param p2 the other parent
	 */
	public static Genome crossover(Genome p1, Genome p2) {
		if(p1.fitness == p2.fitness) { return crossover(p1, p2, true);  } 
		if(p1.fitness >  p2.fitness) { return crossover(p1, p2, false); } 
		if(p1.fitness <  p2.fitness) { return crossover(p2, p1, false); } 
		
		// this should never happen
		return null;
	}
	
	// In this case, equal fitnesses are assumed, so the disjoint and excess genes 
	//are also inherited randomly.
	// ^ does this mean that every disjoint/excess gene has an independant 50% 
	//   chance of being inherited?
	/**
	 * Creates and returns an entirely new genome by choosing between the genes of
	 * the two parents. All genes chosen from parents are copied before being added
	 * to the child, so the child is totally separate from p1 and p2. That is,
	 * child, p1, and p2 will NOT share any references.
	 *
	 * O(n+m+q) where n is p1.connectionGenes.size(), m is 
	 * p2.connectionGenes.size(), and q is the number of node genes that child
	 * inherits.
	 *
	 *
	 *
	 * @param p1 The more fit parent
	 * @param p2 The less fit parent
	 * @param equal true if both parents are equally fit
	 */
	protected static Genome crossover(Genome p1, Genome p2, boolean equal) {
		Genome child = new Genome();
		
		int p1Size = p1.connectionGenes.size();
		int p2Size = p2.connectionGenes.size();
		int i;
		int j;
		for(i = j = 0; i < p1Size && j < p2Size; ) {
			ConnectionGene g1 = p1.connectionGenes.get(i); 
			ConnectionGene g2 = p2.connectionGenes.get(j); 
			ConnectionGene connectionToAdd = null;
			NodeGene       nodeToAdd       = null;
			boolean        choseP1 = true; // this default value doesn't matter, it WILL get overwritten
			
			if(g1.innovation == g2.innovation) {
				choseP1 = Math.random() < 0.5;
				connectionToAdd = choseP1? g1.copy() : g2.copy();
				
				// by default, copy does NOT copy the disabled member. All genes
				// created with the copy function are enabled
				if(!g1.enabled || !g2.enabled) {
					connectionToAdd.enabled = !(Math.random() < CHANCE_TO_PASS_DOWN_GENE_AS_DISABLED);
				}
				
				i++;
				j++;	
			} else if (g1.innovation < g2.innovation) {
				// This is the case where g1 is disjoint/excess, so we'll ignore
				// g2 and only consider g1 here.
				choseP1 = true;
			
				if(!equal || Math.random() < CHANCE_TO_INHERIT_DISJOINT_OR_EXCESS) {
					connectionToAdd = g1.copy();
				
					if(!g1.enabled) {
						connectionToAdd.enabled = !(Math.random() < CHANCE_TO_PASS_DOWN_GENE_AS_DISABLED);
					}
				}
				
				i++;
			} else if (g1.innovation > g2.innovation) { 
				// This is the case where g2 is disjoint/excess, so we'll ignore
				// g1 and only consider g2 here.
			
				// if p1 and p2's fitnesses are not equal, disjoint and excess 
				// genes from the lesser parent are ignored
				
				choseP1 = false;
				
				if(equal && Math.random() < CHANCE_TO_INHERIT_DISJOINT_OR_EXCESS) {
					connectionToAdd = g2.copy();
				
					if(!g2.enabled) {
						connectionToAdd.enabled = !(Math.random() < CHANCE_TO_PASS_DOWN_GENE_AS_DISABLED);
					}
				}
				
				j++;
			} else {} // Not possible
			
			if(connectionToAdd != null) {
				child.connectionGenes.add(connectionToAdd);
				
				//NodeGene ng1 = choseP1 ? p1.nodeGenes.get(connectionToAdd.in) :
				//                         p2.nodeGenes.get(connectionToAdd.in);
				//NodeGene ng2 = choseP1 ? p1.nodeGenes.get(connectionToAdd.out) :
				//                         p2.nodeGenes.get(connectionToAdd.out);
				
				//int index = Math.min(connectionToAdd.in, child.nodeGenes.size());
				//int outdex = Math.min(connectionToAdd.out, child.nodeGenes.size()+1);
				//child.nodeGenes.add(index, ng1);
				//child.nodeGenes.add(outdex, ng2);
			}
		}
		
		
		if(equal) {
			// I'm not sure what to do here
			// System.err.println("Equal fitness crossover not quite supported yet");
		
			//for(NodeGene g : child.nodeGenes) {
			//	if(g == NodeGene.SENSOR) { child.numSensorNodes++; }
			//	if(g == NodeGene.OUTPUT) { child.numOutputNodes++; }
			//}
			
			// just pretend they're not equal for now
			if(p1.nodeGenes.size() < p2.nodeGenes.size()) {
				p1 = p2;
			}
			
			child.nodeGenes.addAll(p1.nodeGenes);
			child.numSensorNodes = p1.numSensorNodes;
			child.numOutputNodes = p1.numOutputNodes;
		} else {
			child.nodeGenes.addAll(p1.nodeGenes);
			child.numSensorNodes = p1.numSensorNodes;
			child.numOutputNodes = p1.numOutputNodes;
		}
		
		return child;
	}
	
	// does delta ignore node genes?
	/**
	 * returns the "compatibility" of genome1 and genome2, only considering
	 * connection genes
	 */
	public static float delta(Genome genome1, Genome genome2, float c1, float c2, float c3) {
		DeltaInfo i = getDeltaInfo(genome1, genome2);
		
		double delta = 
			c1*(double)i.numExcess/i.n + 
			c2*(double)i.numDisjoint/i.n + 
			c3*i.totalWeightDifference/i.n;
			
		return (float)delta;
	}
	
	/**
	 * In the form of a string giving detail on the equation, 
	 * returns the "compatibility" of genome1 and genome2, only considering
	 * connection genes.
	 */
	public static String deltaEquation(Genome genome1, Genome genome2, float c1, float c2, float c3) {
		DeltaInfo i = getDeltaInfo(genome1, genome2);
		
		double delta = 
			c1*(double)i.numExcess/i.n + 
			c2*(double)i.numDisjoint/i.n + 
			c3*i.totalWeightDifference/i.n;
			
		return "C1 * " + (int)i.numExcess + "/" + i.n + " + C2 * " + (int)i.numDisjoint + "/" +i. n + " + C3 * " + i.totalWeightDifference + "/" + i.n + " = " + delta;
	}
	
	private static DeltaInfo getDeltaInfo(Genome genome1, Genome genome2) {
		try {
			int numExcess = 0;
			int numDisjoint = 0;
			int numCommon = 0;
			double totalWeightDifference = 0;
		
			int g1Size = genome1.connectionGenes.size();
			int g2Size = genome2.connectionGenes.size();
			int i;
			int j;
			for(i = j = 0; i < g1Size || j < g2Size; ) {
				ConnectionGene g1 = genome1.connectionGenes.get(Math.min(i, g1Size-1)); 
				ConnectionGene g2 = genome2.connectionGenes.get(Math.min(j, g2Size-1)); 
			
				if(g1.innovation == g2.innovation) {
					numCommon++;
					totalWeightDifference += Math.abs(g1.weight - g2.weight);
				
					i++;
					j++;	
				} else if (j >= g2Size) { // case 1a
					numExcess++;
					i++;
				} else if (i >= g1Size) { // case 2a
					numExcess++;
					j++;
				} else if (g1.innovation > g2.innovation) { // case 1b
					// This is the case where g1 is disjoint/excess
			
					if(j < g2Size) {
						// g1 is disjoint
						numDisjoint++;				
					} else {
						// g1 is excess
						numExcess++;
					}
				
					i++;
				} else if (g1.innovation < g2.innovation) { // case 2b
					// This is the case where g2 is disjoint/excess
			
					if(i < g1Size) {
						// g2 is disjoint
						numDisjoint++;				
					} else {
						// g2 is excess
						numExcess++;
					}
				
					j++;
				} else {} // Not possible
			
				//System.out.printf("\t(i, j) = (%d, %d)\n", i, j);
			}
		
			//System.out.printf("numDisjoint: %d, numExcess: %d\n", numDisjoint, numExcess);
			//System.out.printf("g1Size: %d, g2Size: %d\n", g1Size, g2Size);
		
			double n = Math.max(g1Size, g2Size);
		
			return new DeltaInfo(numExcess, numDisjoint, totalWeightDifference, n);
		} catch (Exception e) {
			e.printStackTrace();
			genome1.printCode();
			genome2.printCode();
			
			return null;
		}
	}
	
	/**
	 * Builds a genome object out of the genes provided
	 */
	public static Genome buildGenome(ArrayList<ConnectionGene> con, ArrayList<NodeGene> nod) {
		Genome g = new Genome();
		g.numSensorNodes = 0;
		g.numOutputNodes = 0;
		
		for(NodeGene n : nod) {
			g.nodeGenes.add(n); // superCopy also copies genes' disabled member
			
			if(n == NodeGene.SENSOR) { g.numSensorNodes++; }
			if(n == NodeGene.OUTPUT) { g.numOutputNodes++; }
		}
		
		for(ConnectionGene n : con) {
			g.connectionGenes.add(n.superCopy());
			g.existingConnections.add(new Point(n.in, n.out));
		}
		
		g.toposort();
		
		return g;
	}
	
	/**
	 * Debug function. Prints code that, when pasted into some function,
	 * will create an exact copy of this genome object.
	 */
	public void printCode() {
		System.out.println("\n{");
		System.out.println("\tArrayList<NodeGene> nodeGenes = new ArrayList<>();");
		for(NodeGene n : this.getNodeGenes()) {
			System.out.println("\tnodeGenes.add(NodeGene." + n + ");");
		}
		System.out.println();
	
		System.out.println("\tArrayList<ConnectionGene> connectionGenes = new ArrayList<>();");
		for(ConnectionGene n : this.getConnectionGenes()) {
			System.out.println(String.format("\tconnectionGenes.add(ConnectionGene.buildManually(%d, %d, %ff, %s, %d));", n.getIn(), n.getOut(), n.getWeight(), (n.isEnabled()? "true" : "false"), n.getInnovation()));
		}
	
		System.out.println("\tGenome g = Genome.buildGenome(connectionGenes, nodeGenes);");
		System.out.println("}\n");
	}
}

class DeltaInfo {
	int numExcess = 0;
	int numDisjoint = 0;
	double totalWeightDifference = 0;
	double n = 0;
	
	public DeltaInfo(int a, int b, double c, double d) {
		numExcess = a;
		numDisjoint = b;
		totalWeightDifference = c;
		n = d;
	}
}

















