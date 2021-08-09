package ai.implementation.util;

import java.util.LinkedList;
import java.util.List;

import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.FieldType;
import de.itdesign.codebattle.api.model.Position;

public class Grid {
	private int width;
	private int height;
	private Field[][] matrix;
	private GridNode[][] nodes;
	private int[][] costsMap;
	
	public Grid(int width, int height, Field[][] matrix)
	{
		this.width = width;
		this.height = height;
		this.matrix = matrix;
		buildNodes();
	}

	private void buildNodes() {
		nodes = new GridNode[width][height];
		for(int x=0;x<nodes.length;x++)
		{
			for(int y=0;y<nodes[x].length;y++)
			{
				nodes[x][y] = new GridNode(Position.get(x, y),isFieldWalkable(x,y));
			}
		}
	}
	private boolean isFieldWalkable(int x, int y)
	{
		return (matrix[x][y].getType().equals(FieldType.LAND) || matrix[x][y].getType().equals(FieldType.BASE)?true:false);
	}
	public GridNode getNodeAt(int x, int y){
		return nodes[x][y];
	}
	public boolean isInside(int x, int y)
	{
		return (x >= 0 && x < width) && (y >= 0 && y < height);
	}
	public boolean isWalkableAt(int x, int y)
	{
		return isInside(x,y) && nodes[x][y].isWalkable();
	}
	public void setWalkableAt(int x, int y, boolean walkable)
	{
		if(isInside(x,y))
		{
			nodes[x][y].setWalkable(walkable);
		}
	}
	public List<GridNode> getNeighbors(GridNode node)
	{
		List<GridNode> neighbors = new LinkedList<>();
		Position pos = node.getPos();
		int x = pos.getX();
		int y = pos.getY();
		 // ↑
	    if (this.isWalkableAt(x, y - 1)) {
	        neighbors.add(nodes[x][y-1]);
	    }
	    // →
	    if (this.isWalkableAt(x + 1, y)) {
	        neighbors.add(nodes[x+1][y]);
	    }
	    // ↓
	    if (this.isWalkableAt(x, y + 1)) {
	        neighbors.add(nodes[x][y+1]);
	    }
	    // ←
	    if (this.isWalkableAt(x - 1, y)) {
	        neighbors.add(nodes[x-1][y]);
	    }
	    return neighbors;
	}
	public Grid clone()
	{
		Grid newGrid = new Grid(width,height,matrix);
		return newGrid;
	}
	
}
