package ai.implementation.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TraceUtil {
	public static List<GridNode> backtrack(GridNode node)
	{
		List<GridNode> path = new LinkedList<>();
		while(node.getParent() != null)
		{
			node = node.getParent();
			path.add(node);
		}
		Collections.reverse(path);
		return path;
	}
}
