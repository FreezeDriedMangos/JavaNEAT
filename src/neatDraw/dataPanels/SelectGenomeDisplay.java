package neatDraw.dataPanels;

import java.util.Map;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Button;
import java.awt.TextField;
import java.awt.Label;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import neatCore.Population;
import neatCore.Species;
import neatCore.Genome;
import neatCore.ConnectionGene;
import neatCore.Constants.Genome.NodeGene;

import neatDraw.SelectGenomeFrame;
import neatDraw.DataPanel;
import neatDraw.dataPanels.GenomeDrawer;

public class SelectGenomeDisplay extends DataPanel {
	public static final int BUTTON_AREA_HEIGHT = 35;

	public static final float GENOME_WIDTH = 0.6f;
	public static final float GENOME_HEIGHT = 0.5f;

	public static final float SELECT_GENOME_INFO_HEIGHT = 0.3f;
	public static final int NUM_LINES_OF_TEXT = 1;
	public static final int MIN_TEXT_SIZE = 12;
	public static final float DELTA_AREA_START_PAD = 0.05f;

	SelectGenomeFrame parent;

	Genome g;
	Color color;
	private boolean drawWeights = true;
	
	private boolean lookingForDeltaCompare = false;
	Genome compareTo = null;
	Color compareToColor = null;
	String compareToTitle = null;
	
	ArrayList<Button> buttons = new ArrayList<>();
	Button deltaButton;
	Button clearButton;
	Button printButton;
	Button toggleWeightsButton;
	
	public SelectGenomeDisplay(SelectGenomeFrame parent) {
		this.parent = parent;
		
		deltaButton = new Button("Compare to");
		deltaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.setVisible(false);
				lookingForDeltaCompare = true;
			}
		});
		buttons.add(deltaButton);
		
		clearButton = new Button("Clear compare genome");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				compareTo = null;
				compareToColor = null;
				compareToTitle = null;
				parent.draw();
			}
		});
		buttons.add(clearButton);
		
		printButton = new Button("Print code for this genome");
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				g.printCode();
			}
		});
		buttons.add(printButton);
		
		toggleWeightsButton = new Button("Toggle weights");
		toggleWeightsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawWeights = !drawWeights;
				parent.draw();
			}
		});
		buttons.add(toggleWeightsButton);
	}
	
	public void setGenome(Genome g, Color c, String genomeTitle) {
		if(!lookingForDeltaCompare) {
			this.g = g.copy();
			this.g.setFitness(g.getRawFitness());
			this.color = c;
			parent.setTitle(genomeTitle);
		} else {
			this.compareTo = g.copy();
			this.compareTo.setFitness(g.getRawFitness());
			this.compareToColor = c;
			this.compareToTitle = genomeTitle;
			
			lookingForDeltaCompare = false;
		}
	}
	
	public void draw(
			Graphics gr,
			int x, int y, int width, int height, 
			Population p, Map<Species, Color> speciesColors) {
	
		y += BUTTON_AREA_HEIGHT;
	
		// draw selected genome
		
		float genomeWidth = GENOME_WIDTH*(float)width;
		float genomeHeight = GENOME_HEIGHT*(float)height;
		
		gr.setColor(new Color(200,200,200));
		gr.fillRect(x + (int)(width - genomeWidth)/2, y, (int)genomeWidth, (int)genomeHeight);
		
		GenomeDrawer.draw(g, gr, x + (width - genomeWidth)/2, y, (int)genomeWidth, (int)genomeHeight, color, 5, drawWeights);
				
		// selected genome info		
				
		float fractionAllowed = compareTo == null? 1-GENOME_HEIGHT : SELECT_GENOME_INFO_HEIGHT;
		float textDisplayHeight = (height - genomeHeight) * fractionAllowed;
		int textSize = (int)(textDisplayHeight / NUM_LINES_OF_TEXT);
		
		gr.setColor(Color.BLACK);
		gr.setFont(gr.getFont().deriveFont(textSize));
		
		gr.drawString("Fitness: " + g.getRawFitness(), 0, (int)genomeHeight + textSize);
		
		// delta comparing
		
		if(compareTo != null) {
			int separatorY = (int)(textDisplayHeight+genomeHeight+(DELTA_AREA_START_PAD*height)/2);
			gr.drawLine(0, separatorY, width, separatorY);
		
			float deltaAreaEndY = height;
			float deltaAreaStartY = textDisplayHeight+genomeHeight+DELTA_AREA_START_PAD*height;
			float deltaGenomeHeight = (deltaAreaEndY-deltaAreaStartY) * GENOME_HEIGHT;
			float deltaGenomeWidth = width * GENOME_WIDTH;
		
			int deltaTextSize = 12;
			gr.drawString(compareToTitle+":", 0, (int)(deltaAreaStartY+deltaTextSize));
		
			gr.setColor(new Color(200,200,200));
			gr.fillRect(x + (int)(width - deltaGenomeWidth)/2, (int)(deltaAreaStartY+deltaTextSize), (int)deltaGenomeWidth, (int)deltaGenomeHeight);
			GenomeDrawer.draw(compareTo, gr, x + (width - deltaGenomeWidth)/2, deltaAreaStartY+deltaTextSize, (int)deltaGenomeWidth, (int)deltaGenomeHeight, compareToColor, 1, drawWeights);
		
		
			
		
			//deltaButton.setLocation((width-deltaButton.getWidth())/2, (int)deltaAreaStartY);
			gr.setColor(Color.BLACK);
			gr.drawString("Delta between: " + Genome.deltaEquation(g, compareTo, neatCore.Constants.Species.C1, neatCore.Constants.Species.C2, neatCore.Constants.Species.C3), 0, (int)(deltaAreaStartY+deltaGenomeHeight+deltaTextSize*2));
		}
	}
	
	public void captureData(Population p) {}
	
	public float getDrawComplexity() {
		return 1;
	}
	
	public void handleClick(int x, int y) {}
	
	public ArrayList<Button> getButtons() {
		return buttons;
	}
}
