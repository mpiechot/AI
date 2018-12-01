package Util;


import Logic.Field;
import Logic.Point;
import Logic.Portals;
import Logic.Snake.Direction;

public class HamiltonPath {
	/**
	 * MinPathFinder
	 */
	private PathFinder finder;
	/**
	 * the current game situation
	 */
	private Field actualField;
	/**
	 * this map displays all blocked fields. it contains 1 if we can expand our snake with this Position or 100 if this Point is already
	 * in the path. Points with value of 100 are used as expand-points to check if their neighbors arent already in the path
	 */
	private int[][] visitedMap;
	private static final int SPACE = 1;
	private static final int WALL = 100;
	private static final int VISITED = 100;
	
	/**
	 * calculates a complete Path through every walkable Position in the gamefield.
	 * This function ignores walls from the WallFeature and the snake-positions.
	 * @param f the actual gamefield. for getting the size of the field.
	 * @return Node of the last position of this max-Path.<br>
	 * <br>
	 * Note: see comments in getMaxPath for further informations about the algorithm
	 */
	public Node getCompleteMaxPath(Field f)
	{
		finder = new PathFinder();
		actualField = Field.defaultField(f.width(), f.height());
		Node start = new Node(null,new Point(1,1),0,0);
		Point target = new Point(1,2);
		calcVisitedMap();
		
		Node way = new Node(new Node(null,start.getActual(),0,0),target,0,0);
		Node tempWay = way;
		while(tempWay != null)
		{
			visitedMap[tempWay.getActual().x][tempWay.getActual().y]=100;
			tempWay = tempWay.getFrom();
		}
		tempWay = way;
		while(tempWay != null && tempWay.getFrom() != null)
		{
			Point from = tempWay.getFrom().getActual();
			Point to = tempWay.getActual();
			Direction dir = UtilFunctions.getDirection(from,to);
			boolean changed = false;
			switch(dir)
			{
			case UP:
			case DOWN:
				Point left = new Point(from.x-1,from.y);
				Point right = new Point(from.x+1,from.y);
				if(visitedMap[left.x][left.y] == 1 && !pathContains(left,way))
				{
					Point leftUp = new Point(to.x-1,to.y);
					if(visitedMap[leftUp.x][leftUp.y] == 1 && !pathContains(leftUp,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),left,0,0),leftUp,0,0));						
						changed=true;
					}
				}
				else if(visitedMap[right.x][right.y] == 1 && !pathContains(right,way))
				{
					Point rightDown = new Point(to.x+1,to.y);
					if(visitedMap[rightDown.x][rightDown.y] == 1 && !pathContains(rightDown,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),right,0,0),rightDown,0,0));
						changed=true;						
					}
				}
				break;
			case LEFT:
			case RIGHT:
				Point up = new Point(from.x,from.y-1);
				Point down = new Point(from.x,from.y+1);
				if(visitedMap[up.x][up.y] == 1 && !pathContains(up,way))
				{
					Point upLeft = new Point(to.x,to.y-1);
					if(visitedMap[upLeft.x][upLeft.y] == 1 && !pathContains(upLeft,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),up,0,0),upLeft,0,0));
						changed=true;						
					}
				}
				else if(visitedMap[down.x][down.y] == 1 && !pathContains(down,way))
				{
					Point downRight = new Point(to.x,to.y+1);
					if(visitedMap[downRight.x][downRight.y] == 1 && !pathContains(downRight,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),down,0,0),downRight,0,0));
						changed=true;						
					}
				}
				break;
			}
			if(!changed)
			{
				visitedMap[tempWay.getActual().x][tempWay.getActual().y]=100;
				tempWay = tempWay.getFrom();
			}
		}
		return way;
	}
	/**
	 * This Function calculates a MaxPath on the given GameField for our snake. This calculation minds the position of the enemySnake
	 * or near WallPositions.
	 * @param startPoint - startPosition of the algorithm
	 * @param field - actual gameField
	 * @param snake - reference to our Snake as TempSnake
	 * @param enemySnake - reference to the enemysnake as TempSnake
	 * @param portals - note portals if they are active
	 * @return Node with the next Step to take. This contains the whole path through predecessor
	 */
	public Node getMaxPath(Point startPoint, Field field, TempSnake snake, TempSnake enemySnake, Portals portals) {
		actualField = field;
		Node way = null;
		Field tmpField = UtilFunctions.getFieldCopy(field);
		
		if(snake.segments().size() == 1)
		{
			Point p = enemySnake.headPosition();
			calcVisitedMap();
			for(Point p2 : enemySnake.segments())
			{	
				visitedMap[p2.x][p2.y] = 100;
			}
			for(Point p2 : snake.segments())
			{	
				visitedMap[p2.x][p2.y] = 100;
			}
			if(finder == null)
				finder = new PathFinder();
			finder.ignorePortals = true;
			finder.getMinPathWithTail(snake, p, tmpField, portals, p);
			way = UtilFunctions.getMovePair(p,finder.getClosedList());
		}
		else
		{
			//Try every Position of the snake to calculate the shortest way to it. beginning at the end of the snake.
			//if this way exsists it is the first point where we can get out of our trapped situation
			for(Point p : snake.segments())
			{
				calcVisitedMap();
				for(Point p2 : enemySnake.segments())
				{	
					visitedMap[p2.x][p2.y] = 100;
				}
				for(Point p2 : snake.segments())
				{	
					visitedMap[p2.x][p2.y] = 100;
				}
	
				if(finder == null)
					finder = new PathFinder();
				finder.ignorePortals = true;
				finder.getMinPathWithTail(snake, p, tmpField, portals, p);
				way = UtilFunctions.getMovePair(p,finder.getClosedList());
				if(way != null)
					break;
			}
		}
		Node tempWay = way;
		//Set all Positions of the path to VISITED
		while(tempWay != null)
		{
			visitedMap[tempWay.getActual().x][tempWay.getActual().y]=VISITED;
			tempWay = tempWay.getFrom();
		}
		tempWay = way;
		//Move through the path end expand in every direction if possible
		while(tempWay != null && tempWay.getFrom() != null)
		{
			Point from = tempWay.getFrom().getActual();
			Point to = tempWay.getActual();
			Direction dir = UtilFunctions.getDirection(from,to);
			boolean changed = false;
			switch(dir)
			{
			case UP:
			case DOWN:
				Point left = new Point(from.x-1,from.y);
				Point right = new Point(from.x+1,from.y);
				//If we can move down or up we check if we can make a move left then up/down and right again instead.
				if(visitedMap[left.x][left.y] == 1 && !pathContains(left,way))
				{
					Point leftUp = new Point(to.x-1,to.y);
					if(visitedMap[leftUp.x][leftUp.y] == 1 && !pathContains(leftUp,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),left,0,0),leftUp,0,0));						
						changed=true;
					}
				}
				//If we can move down or up we check if we can make a move right then up/down and left again instead.
				else if(visitedMap[right.x][right.y] == 1 && !pathContains(right,way))
				{
					Point rightDown = new Point(to.x+1,to.y);
					if(visitedMap[rightDown.x][rightDown.y] == 1 && !pathContains(rightDown,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),right,0,0),rightDown,0,0));
						changed=true;						
					}
				}
				break;
			case LEFT:
			case RIGHT:
				Point up = new Point(from.x,from.y-1);
				Point down = new Point(from.x,from.y+1);
				//if we can move right or left we check if we can make a move up then left/right and down again instead
				if(visitedMap[up.x][up.y] == 1 && !pathContains(up,way))
				{
					Point upLeft = new Point(to.x,to.y-1);
					if(visitedMap[upLeft.x][upLeft.y] == 1 && !pathContains(upLeft,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),up,0,0),upLeft,0,0));
						changed=true;						
					}
				}
				//if we can move right or left we check if we can make a move down then left/right and up again instead
				else if(visitedMap[down.x][down.y] == 1 && !pathContains(down,way))
				{
					Point downRight = new Point(to.x,to.y+1);
					if(visitedMap[downRight.x][downRight.y] == 1 && !pathContains(downRight,way))
					{
						tempWay.setFrom(new Node(new Node(tempWay.getFrom(),down,0,0),downRight,0,0));
						changed=true;						
					}
				}
				break;
			}
			//if we havnt change anything we visit the next Position in the path.
			if(!changed)
			{
				visitedMap[tempWay.getActual().x][tempWay.getActual().y]=100;
				tempWay = tempWay.getFrom();
			}
		}	
		return way;
	}
	/**
	 * calculates if the given path contains a specific Point
	 * @param contains point to check if the way contains it
	 * @param way whole already calculated path
	 * @return true if way contains the point
	 */
	private boolean pathContains(Point contains, Node way) {
		while(way != null)
		{
			if(way.getActual().equals(contains))
				return true;
			way = way.getFrom();
		}
		return false;
	}
	/**
	 * initialize the visited Map for Longest-Path calculations
	 */
	public void calcVisitedMap() {
		visitedMap = new int[actualField.width()][actualField.height()];
		for (int i = 0; i < actualField.width(); i++)
			for (int j = 0; j < actualField.height(); j++)
			{
				switch(actualField.cell(new Point(i,j)))
				{
				case PORTAL:
				case APPLE:
				case SPACE:
				case CHANGESNAKE:
				case CHANGEHEADTAIL:
				case SPEEDUP:
				case OPENFIELD:
				case OPENFIELDPICTURE:
				case CUTTAIL:
				case SNAKE:
				case FEATUREWALL: visitedMap[i][j] = SPACE; break;
				case WALL: visitedMap[i][j] = WALL; break;
				}
			}
	}
}
