package Util;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import Logic.Field;
import Logic.Point;
import Logic.Portals;

public class PathFinder 
{
	private int[][] distanceMap;
	private int[][] blockingMap;
	/**
	 * openList contains every Node, which need to be considered for the shortest path. This list is ordered by the FCost of the nodes
	 */
	private PriorityQueue<Node> openList = new PriorityQueue<>((Node e1, Node e2) -> (int)(e1.getFCost()-e2.getFCost()));
	/**
	 * the closedList contains all Nodes, which are already checked. 
	 */
	private List<Node> closedList = new LinkedList<>();
	/**
	 * contains Points which are considered bad by the alphaBeta Algorithm. All Points in the list will be handled as walls
	 */
	private List<Point> badPositions = new LinkedList<>();
	private Field actualField;
	private Point portal1;
	private Point portal2;
	
	//Heuristic values
	public static final int SPACE = 1;
	public static final int WALL = 100;
	public boolean ignorePortals = false;
	
	//Constants
    private final boolean OPEN = true;
	private final boolean CLOSED = false;
	private final int MAX_WALKABLE_HEIGHT = 19;
	private final int MAX_WALKABLE_WIDTH = 29;
	private final int MIN_WALKABLE = 1;
		
	
	/**
	 * calculates shortest path to target point
	 * @param snake - copy of current snake that is located in a certain position
	 * @param target - destination point
	 * @param field - current game field
	 * @param portals - occurring feature consisting of 2 portals, an entry and exit
	 * @return Linked list(Node class) with target as first Node
	 */
	public Node getMinPath(TempSnake snake, Point target, Field field, Portals portals) 
	{
		Point snakeTail = snake.segments().get(0);
		return getMinPathWithTail(snake, target ,field, portals, snakeTail);
	}
	
	/**
	 * calculates shortest path to target point
	 * @param snake - copy of current snake that is located in a certain position
	 * @param target - destination point
	 * @param field - current game field
	 * @param portals - occurring feature consisting of 2 portals, an entry and exit
	 * @param tail - last position of the snake body
	 * @return Linked list(Node class) with target as first Node
	 */
	public Node getMinPathWithTail(TempSnake snake, Point target,Field field, Portals portals, Point tail)
	{
		Point startPoint = snake.headPosition();
		Point snakeTail  = tail;
		
		portal1 = portals.getPortal1();
		portal2 = portals.getPortal2();
		
		openList.clear();
		closedList.clear();
		
		actualField = field;
		Node start = new Node(null,startPoint,0,0);
		calculateMaps(target,actualField);
		if(UtilFunctions.getDistance(startPoint, snakeTail) > 1)
			blockingMap[snakeTail.x][snakeTail.y] = SPACE;
		
		// Calculate A*
		openList.add(start);
		
		//Wenn das Ziel in der ClosedList ist oder die OpenList leer ist, sind wir fertig!
		while (!isInList(CLOSED,target) && !openList.isEmpty()) {
			Node min = openList.remove();
			closedList.add(min);
			Point current = min.getActual();
			if(!ignorePortals)
			{
				if(current.equals(portal1) && portals.getTTL() >=  min.lengthToDest(startPoint))
				{
					updateOpenList(portal2, min);
				}
				if(current.equals(portal2) && portals.getTTL() >=  min.lengthToDest(startPoint))
				{
					updateOpenList(portal1, min);
				}
			}
			for (int i = -1; i <= 1; i += 2)
			{
				if (current.x + i < MAX_WALKABLE_WIDTH && current.x + i >= MIN_WALKABLE)
				{
					Point next = new Point(current.x + i, current.y);
					if(blockingMap[current.x+i][current.y] == SPACE)
						updateOpenList(next, min);
				}
				if (current.y + i < MAX_WALKABLE_HEIGHT && current.y + i >= MIN_WALKABLE)
				{
					Point next = new Point(current.x, current.y + i);
					if(blockingMap[current.x][current.y+i] == SPACE)
						updateOpenList(next, min);
				}
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
		//Ist der Neue Punkt bereits in der ClosedList?
		if(isInList(CLOSED,check))
			return;
		
		//Berechne die GCosts fuer den neuen Punkt
		int costs = node.getGCosts() + blockingMap[check.x][check.y];
		
		//Gibt es bereits einen besseren Weg zu dem neuen Punkt?
		if (isInList(OPEN,check) && costs >= getElemFromOpenList(check).getGCosts())
			return;
		
		//Erstelle den neuen Node
		Node checkNode = new Node(node,check,distanceMap[check.x][check.y],costs);
		//Falls es einen schlechteren Node gab, loesche diesen
		if(isInList(OPEN,check))
			openList.remove(getElemFromOpenList(check));

		//Fuege den neuen/besseren Node der OpenList hinzu
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
		for (Node node : (open?openList:closedList))
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
	 * fills blocking map with heuristic values and distance map with distance values from each point to target
	 * @param target - destination point of snake
	 * @param actualField - the current game field
	 */
	public void calculateMaps(Point target, Field actualField) 
	{
		distanceMap = new int[actualField.width()][actualField.height()];
		blockingMap = new int[actualField.width()][actualField.height()];
		for(Point bad : badPositions)
			blockingMap[bad.x][bad.y] = WALL;
		for (int i = 0; i < actualField.width(); i++)
			for (int j = 0; j < actualField.height(); j++)
			{
				distanceMap[i][j] = UtilFunctions.getDistance(new Point(i,j),target);
				switch(actualField.cell(new Point(i,j)))
				{
				case PORTAL:
				case APPLE:
				case SPACE:
				case CHANGESNAKE:
				case CHANGEHEADTAIL:
				case SPEEDUP:
				case CUTTAIL:
				case OPENFIELD:
				case FEATUREWALL: blockingMap[i][j] = SPACE; break;
				case SNAKE:
				case OPENFIELDPICTURE:
				case WALL: blockingMap[i][j] = WALL; break;
				}
			}
	}
	
	/**
	 * getter method for the closed list
	 * @return closed list
	 */
	public List<Node> getClosedList()
	{
		return closedList;
	}
	
	/**
	 * saves another bad position. This Position will treated as non-walkable
	 * @param p - point that is supposed to be avoided
	 */
	public void addBadPosition(Point p)
	{
		badPositions.add(p);
	}
	
	/**
	 * removes all saved bad, treated as non-walkable ,positions
	 */
	public void clearBadPositions()
	{
		badPositions.clear();
	}
}
