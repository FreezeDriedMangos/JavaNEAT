package neatDraw.dataPanels;

import java.util.*;
import java.awt.*;

import neatCore.Genome;
import neatCore.ConnectionGene;
import neatCore.Constants.Genome.NodeGene;

public class GenomeDrawer {
	public static final float NODE_RADIUS = 10;
	public static final float NODE_DIAMETER = NODE_RADIUS*2f;
	public static final float NODE_SEPARATION = NODE_RADIUS;
	public static final float NODE_LAYER_SEPARATION = NODE_SEPARATION*2;
	
	public static void draw(Genome genome, Graphics g, float x, float y, float w, float h, Color c) {
		draw(genome, g, x, y, w, h, c, 1, false);
	}
	
	public static void draw(Genome genome, Graphics g, float x, float y, float w, float h, Color c, float lineWeight, boolean drawWeights) {
		ArrayList<ArrayList<Integer>> toposort = genome.getToposort();
		
		// remove unconnected nodes
		boolean[] connected = new boolean[genome.getNodeGenes().size()];
		for(ConnectionGene ge : genome.getConnectionGenes()) {
			connected[ge.getIn()] = true;
			connected[ge.getOut()] = true;
		}
		for(int i = 0; i < connected.length; i++) {
			if(!connected[i]) {
				toposort.get(0).remove(Integer.valueOf(i));
			}
		}
		
		// calculate scale
		
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
		
		int sideLengthSquare = (int)(Math.sqrt(2*Math.pow(scale*NODE_DIAMETER/2f, 2)));
		int offset = (int)(NODE_DIAMETER*scale - sideLengthSquare)/2;
		
		for(int i = 0; i < toposort.size(); i++) {
			int numNodes = toposort.get(i).size();
			float spaceRequired = numNodes * NODE_DIAMETER + (numNodes-1) * NODE_SEPARATION;
			spaceRequired *= scale;
			float startY = midY - spaceRequired/2f;
			
			float thisx = x + scale*i*(NODE_DIAMETER + NODE_LAYER_SEPARATION);
			for(int j = 0; j < toposort.get(i).size(); j++) {
				float thisy = startY + scale * j * (NODE_DIAMETER + NODE_SEPARATION);
				
				NodeGene type = genome.getNodeGenes().get(toposort.get(i).get(j));
				if(type == NodeGene.HIDDEN) {
					g.fillOval(
							(int)thisx,
							(int)thisy,	
							(int)(NODE_DIAMETER*scale), 
							(int)(NODE_DIAMETER*scale));
				} else if (type == NodeGene.SENSOR) {
					g.fillRect(
							(int)thisx+offset,
							(int)thisy+offset,	
							sideLengthSquare,//(int)(NODE_DIAMETER*scale), 
							sideLengthSquare);//(int)(NODE_DIAMETER*scale));
				} else if (type == NodeGene.OUTPUT) {
					// diamond shape
					// verticies clockwise from the top
					
					int minXdiamond = (int)thisx;
					int midXdiamond = (int)thisx + (int)(NODE_DIAMETER*scale/2f);
					int maxXdiamond = (int)thisx + (int)(NODE_DIAMETER*scale);
					int minYdiamond = (int)thisy;
					int midYdiamond = (int)thisy + (int)(NODE_DIAMETER*scale/2f);
					int maxYdiamond = (int)thisy + (int)(NODE_DIAMETER*scale);
					g.fillPolygon(
							new int[]{midXdiamond, maxXdiamond, midXdiamond, minXdiamond},
							new int[]{minYdiamond, midYdiamond, maxYdiamond, midYdiamond},
							4);
				}
						
				//nodeLocations.put(nodePlacements[i][j], new Point((int)thisx, (int)thisy));
				nodeLocations.put(toposort.get(i).get(j), new Point((int)thisx, (int)thisy));
			}
		}
		
		//draw connections
		int lineDrawError = 0;
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(lineWeight));
		for(ConnectionGene ge : genome.getConnectionGenes()) {
			if(!ge.isEnabled()) continue;
			
			if(ge.getWeight() == 0) g2d.setColor(Color.BLACK);
			if(ge.getWeight() < 0)  g2d.setColor(Color.RED);
			if(ge.getWeight() > 0)  g2d.setColor(Color.GREEN);
			
			int x1 = nodeLocations.get(ge.getIn()).x + (int)(NODE_RADIUS*scale);
			int y1 = nodeLocations.get(ge.getIn()).y + (int)(NODE_RADIUS*scale);
			int x2 = nodeLocations.get(ge.getOut()).x + (int)(NODE_RADIUS*scale);
			int y2 = nodeLocations.get(ge.getOut()).y + (int)(NODE_RADIUS*scale);
			g2d.drawLine(
					x1, 
					y1,
					x2, 
					y2
					);
					
			if(drawWeights) {
				int mx = (int)((float)x1/2f + (float)x2/2f);
				int my = (int)((float)y1/2f + (float)y2/2f);
				
				int textW = 12*9;
				int textH = 14;
				g2d.clearRect(mx-textW/2, my-textH/2, textW, textH);
				
				g2d.setColor(Color.BLACK);
				g2d.drawString(String.format("%1.6e", ge.getWeight()), mx-textW/2, my-2+textH/2);
			}
		}
		
		//g.setColor(Color.BLACK);
		//for(int i = 0; i < nodeLocations.size(); i++) {
		//	g.drawString(""+i, nodeLocations.get(i).x, nodeLocations.get(i).y);
		//}
	}
}
