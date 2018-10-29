package neatDraw.dataPanels;

import java.util.*;
import java.awt.*;

import neatCore.Genome;
import neatCore.ConnectionGene;

public class GenomeDrawer {
	public static final float NODE_RADIUS = 10;
	public static final float NODE_DIAMETER = NODE_RADIUS*2f;
	public static final float NODE_SEPARATION = NODE_RADIUS;
	public static final float NODE_LAYER_SEPARATION = NODE_SEPARATION*2;
	
	public static void draw(Genome genome, Graphics g, float x, float y, float w, float h, Color c) {
		ArrayList<ArrayList<Integer>> toposort = genome.getToposort();
		
		int maxLayer = 1;
		for(ArrayList layer : toposort)
			if(layer.size() > maxLayer)
				maxLayer = layer.size();
		
		float unscaledYSpace = maxLayer * NODE_DIAMETER + (maxLayer-1) * NODE_SEPARATION;
		float unscaledXSpace = toposort.size() * NODE_DIAMETER + (toposort.size()-1) * NODE_LAYER_SEPARATION;
		
		float scale = Math.min(h/unscaledYSpace, w/unscaledXSpace);
		
		// draw nodes
		g.setColor(c == null ? Color.BLACK : c);
		HashMap<Integer, Point> nodeLocations = new HashMap<>();
		
		float midY = y + h/2;
		for(int i = 0; i < toposort.size(); i++) {
			int numNodes = toposort.get(i).size();
			float spaceRequired = numNodes * NODE_DIAMETER + (numNodes-1) * NODE_SEPARATION;
			spaceRequired *= scale;
			float startY = midY - spaceRequired/2f;
			
			float thisx = x + scale*i*(NODE_DIAMETER + NODE_LAYER_SEPARATION);
			for(int j = 0; j < toposort.get(i).size(); j++) {
				float thisy = startY + scale * j * (NODE_DIAMETER + NODE_SEPARATION);
				
				g.fillOval(
						(int)thisx,
						(int)thisy,	
						(int)(NODE_DIAMETER*scale), 
						(int)(NODE_DIAMETER*scale));
						
				//nodeLocations.put(nodePlacements[i][j], new Point((int)thisx, (int)thisy));
				nodeLocations.put(toposort.get(i).get(j), new Point((int)thisx, (int)thisy));
			}
		}
		
		//draw connections
		int lineDrawError = 0;
		
		for(ConnectionGene ge : genome.getConnectionGenes()) {
			if(!ge.isEnabled()) continue;
			
			if(ge.getWeight() == 0) g.setColor(Color.BLACK);
			if(ge.getWeight() < 0)  g.setColor(Color.RED);
			if(ge.getWeight() > 0)  g.setColor(Color.GREEN);
			
			g.drawLine(
					nodeLocations.get(ge.getIn()).x + (int)(NODE_RADIUS*scale), 
					nodeLocations.get(ge.getIn()).y + (int)(NODE_RADIUS*scale),
					nodeLocations.get(ge.getOut()).x + (int)(NODE_RADIUS*scale), 
					nodeLocations.get(ge.getOut()).y + (int)(NODE_RADIUS*scale)
					);
		}
		
		//g.setColor(Color.BLACK);
		//for(int i = 0; i < nodeLocations.size(); i++) {
		//	g.drawString(""+i, nodeLocations.get(i).x, nodeLocations.get(i).y);
		//}
	}
}
