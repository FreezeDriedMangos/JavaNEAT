package neatCore;

import java.awt.Point;

import static neatCore.Constants.ConnectionGene.*;

public class ConnectionGene {

	int innovation;
	int in;
	int out;
	float weight;
	boolean enabled = true;
	
	// ============================================================
	//
	//  Constructors
	//
	// ============================================================
	
	private ConnectionGene() {}
	
	public ConnectionGene(int in, int out, InnovationTracker innoTracker) {
		this(new Point(in, out), innoTracker);
	}
	
	public ConnectionGene(Point p, InnovationTracker innoTracker) {
		this.in = p.x;
		this.out = p.y;
		this.innovation = innoTracker.get(p);
		
		this.randomizeWeight();
	}
	
	// ============================================================
	//
	//  Weight Adjustment
	//
	// ============================================================
	
	public void randomizeWeight() {
		this.weight = getRandomWeight();
	}
	
	public void uniformlyPerturbWeight() {
		float perturbation = getRandomWeight();
		
		if(PERTURB_BY_ADDITION) {
			setWeight(this.weight + perturbation);
		} else {
			setWeight(this.weight * perturbation);
		}
	}
	
	/**
	 * Handles the bounding of weights, if enabled
	 */
	private void setWeight(float w) {
		if(BOUND_WEIGHT) {
			this.weight = Math.max(Math.min(w, MAX_WEIGHT), MIN_WEIGHT);
		} else {
			this.weight = w;
		}
	}
	
	/**
	 * @return a number on the interval (-2, 2)
	 */
	private static float getRandomWeight() {
		return (float)(Math.random()*4f - 2f);
	}
	
	// ============================================================
	//
	//  Misc
	//
	// ============================================================
	
	/**
	 * Returns a gene with enabled=true but is otherwise an exact copy of this
	 * ConnectionGene object.
	 */
	public ConnectionGene copy() {
		ConnectionGene gene = new ConnectionGene();
		gene.in         = this.in;
		gene.out        = this.out;
		gene.weight     = this.weight;
		gene.innovation = this.innovation;
		
		return gene;
	}
	
	/**
	 * Returns an exact copy of this ConnectionGene object, including the "enabled" member.
	 */
	public ConnectionGene superCopy() {
		ConnectionGene gene = this.copy();
		gene.enabled    = this.enabled;
		return gene;
	}
	
	public String toString() {
		return  (enabled? "" : "X ") + innovation + ": " + in + "->" + out + " (" + weight + ")"; 
	}
	
	// ============================================================
	//
	//  Getters
	//
	// ============================================================

	public int   getIn()     { return in;     }
	public int   getOut()    { return out;    }
	public float getWeight() { return weight; }
	public boolean isEnabled() { return enabled; }
	
}
