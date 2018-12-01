package Util;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import Logic.Field;
import Logic.Point;
import Logic.Field.CellType;

public class Pathfinding {
	
	private int[][] distanceMap;
	private int[][] shortWayMap;
	
	private PriorityQueue<Node> openList = new PriorityQueue<>(new Comparator<Node>(){

		@Override
		public int compare(Node o1, Node o2) {
			if(o1.getFCost() > o2.getFCost())
				return 1;
			else if(o1.getFCost()< o2.getFCost())
				return -1;
			return 0;
		}
		
	});
	
	private List<Node> closedList = new LinkedList<>();
	
	private Field actualField;
	
	//Heuristic values
	private final int SPACE = 1;
	private final int WALL = 9;
	
	//Constants
	private final boolean OPEN = true;
	private final boolean CLOSED = false;
	private final int MAX_WALKABLE_HEIGHT = 19;
	private final int MAX_WALKABLE_WIDTH = 29;
	private final int MIN_WALKABLE = 1;
	
	/**
	 * default constructor
	 */
	public Pathfinding()
	{
		
	}
	
	/**
	 * constructor of A*
	 * @param field - current game field
	 */
	public Pathfinding(Field field)
	{
		actualField = field;
		distanceMap = new int[field.width()][field.height()];
	}
	
	/**
	 * calculates the shortest path from startPoint to target.
	 * @param startPoint - head position of the snake
	 * @param target - destination point
	 * @param field - current game field
	 * @param snakeTail - the last position of the snake body
	 * @return Linked list(Node class) with target as first Node
	 */
	public Node getMinPath(Point startPoint, Point target, Field field, Point snakeTail) 
	{
		openList.clear();
		closedList.clear();
		actualField = field;
		Node start = new Node(null,startPoint,0,0);
		if(shortWayMap == null)
			calcShortWayMap(target,actualField);
		
		if(UtilFunctions.getDistance(startPoint, snakeTail) > 1)
			shortWayMap[snakeTail.x][snakeTail.y] = SPACE;
		// Calculate A*
		openList.add(start);
		
		//Wenn das Ziel in der ClosedList ist oder die OpenList leer ist, sind wir fertig!
		while (!isInList(CLOSED,target) && !openList.isEmpty()) {
			Node min = openList.remove();
			closedList.add(min);
			Point current = min.getActual();
			for (int i = -1; i <= 1; i += 2)
			{
				if (current.x + i < MAX_WALKABLE_WIDTH && current.x + i >= MIN_WALKABLE)
					if(shortWayMap[current.x+i][current.y] == SPACE)
						updateOpenList(new Point(current.x + i, current.y), min);
				if (current.y + i < MAX_WALKABLE_HEIGHT && current.y + i >= MIN_WALKABLE)			
					if(shortWayMap[current.x][current.y+i] == SPACE)
						updateOpenList(new Point(current.x, current.y + i), min);
			}
		}
		
		closedList.remove(0);
		return UtilFunctions.getMovePair(target,closedList);
	}
	
	/** 
	 * add a new point to open list or replace it, if:
	 * - point is not in closed list
	 * - point is not in open list or costs of the new node are less than costs of another node
	 * @param check - particular neighbor point that is supposed to be added
	 * @param node - current Node
	 */
	private void updateOpenList(Point check, Node node) 
	{
		if(isInList(CLOSED,check))
			return;
		
		int gCosts = node.getGCosts() + shortWayMap[check.x][check.y];
		
		if (isInList(OPEN,check) && gCosts >= getElemFromOpenList(check).getGCosts())
			return;
		
		Node checkNode = new Node(node, check, distanceMap[check.x][check.y], gCosts);
		
		if(isInList(OPEN,check))
			openList.remove(getElemFromOpenList(check));

		openList.add(checkNode);
	}
	
	/**
	 * checks if a point is contained in a specific list
	 * @param open - to specify OPEN or CLOSED list
	 * @param target - current analyzed point
	 * @return boolean true if contained in list, else false
	 */
	private boolean isInList(boolean open, Point target)
	{
		for (Node node : (open? openList:closedList))
		{
			if (node.getActual().equals(target))
				return true;
		}
		return false;
	}

	/**
	 * finds the element of open list that contains a specific point
	 * @param p - specific point 
	 * @return - element of open list that contains p
	 */
	private Node getElemFromOpenList(Point p) 
	{
		for (Node open : openList)
		{
			if (open.getActual().equals(p))
				return open;
		}
		return null;
	}
	
	/**
	 * fills short way map with heuristic values and distance map with distance values from each point to target
	 * @param target - destination point of snake
	 * @param actualField - the current game field
	 * @return short way map - map that shows walkable areas and obstacles of the current game field saved as heuristic values SPACE and WALL
	 */
	public int[][] calcShortWayMap(Point target, Field actualField) 
	{
		int fieldWidth = actualField.width();
		int fieldHeight = actualField.height();
		
		distanceMap = new int[fieldWidth][fieldHeight];
		shortWayMap = new int[fieldWidth][fieldHeight];
		
		for (int i = 0; i < fieldWidth; i++)
			for (int j = 0; j < fieldHeight; j++)
			{
				Point currentPoint = new Point(i, j);
				
				distanceMap[i][j] = UtilFunctions.getDistance(currentPoint, target);
				
				if(actualField.cell(currentPoint).equals(CellType.WALL) || actualField.cell(currentPoint).equals(CellType.SNAKE))
				{
					shortWayMap[i][j] = WALL;
				}
				else
				{
					shortWayMap[i][j] = SPACE;
				}	
			}
		
		return shortWayMap;
	}
	
	/**
	 * getter method for the closed list
	 * @return closed list
	 */
	public List<Node> getClosedList()
	{
		return closedList;
	}
	
}
