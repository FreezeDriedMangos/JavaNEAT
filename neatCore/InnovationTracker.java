package neatCore;

import java.util.HashMap;
import java.awt.Point;

public class InnovationTracker {
	private static int GLOBAL_INNOVATION = 0;
	
	// should this be global or per-generation?
	private HashMap<Point, Integer> thisGenerationInnovationMap = new HashMap<>();
	
	public int get(Point connection) {
		if(!thisGenerationInnovationMap.containsKey(connection)) {
			thisGenerationInnovationMap.put(connection, GLOBAL_INNOVATION++);
		}
		
		return thisGenerationInnovationMap.get(connection);
	}
}
