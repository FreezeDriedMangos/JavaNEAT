
import neatDraw.DrawablePopulation;
import neatCore.Genome;

public class Main extends DrawablePopulation {
	public static void main(String args[]) {
		(new Main()).run();
	}

	public Main() {
		super(100, 2, 1);
	}
	
	protected void evaluateFitnesses() {
		for(Genome gn : this.genomes) {
			// the 1 at the start of every input is for the bias
			float answer1 = gn.evaluate(new float[]{1, 1})[0];
			float answer2 = gn.evaluate(new float[]{1, 0})[0];
			float answer3 = gn.evaluate(new float[]{0, 1})[0];
			float answer4 = gn.evaluate(new float[]{0, 0})[0];
		
			float err1 = Math.abs(answer1 - 0);
			float err2 = Math.abs(answer2 - 1);
			float err3 = Math.abs(answer3 - 1);
			float err4 = Math.abs(answer4 - 0);
		
			float raw = 4 - (err1+err2+err3+err4);
			gn.setFitness(Math.signum(raw) * raw*raw);
		}
	}
	
	protected float getTargetFitness() {
		return 15f;
	}
	
	protected String getTitle() { return "NEAT XOR"; }
	
	protected void finished() {}
}
