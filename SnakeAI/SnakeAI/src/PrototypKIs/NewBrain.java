package PrototypKIs;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import Logic.Field;
import Logic.Field.CellType;
import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;
import Logic.SnakeBrain;
import Util.Node;
import Util.TempSnake;

/**
 * This Brain is only for testing algorithms or other stuff.
 * The last tests contained errors so dont use this SnakeBrain!
 * 
 * This Code is also not documented because it is only for testing purposes
 * @author Julia Hofmann
 */
public class NewBrain implements SnakeBrain {

	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {		
		// get position of apple
		Point target = getApple(gameInfo.field());
		
		// compute shortest path from snake head to apple
		Stack<Direction> path1 = computeShortestPath(snake.headPosition(), target, gameInfo.field(), snake.segments().get(0));
		
		if(!path1.empty()){ // shortest path exists
			
			// save first step but do not remove from path1
			Direction step1 = path1.peek();
			
			// create virtual snake that follows path1 on a virtual field
			Field tempField = Field.defaultField(gameInfo.field().width(), gameInfo.field().height());
			TempSnake tempSnake = new TempSnake(snake);
			for(Point p : snake.segments())
				tempField.setCell(CellType.SNAKE, p);
			while(!path1.empty())
			    tempSnake.move(path1.pop(), tempField);
			
			// compute longest path from virtual snake head to virtual snake tail
			Direction path2 = computeLongestPath(tempSnake.headPosition(), tempSnake.segments().get(0), tempField);
			
			if(path2 != null){ // longest path exists
				return step1;
			}
			else{
				// compute longest path from snake head to snake tail
				Direction path3 = computeLongestPath(snake.headPosition(), snake.segments().get(0), gameInfo.field());
				
				if(path3 != null){ // longest path exists
					return path3;
				}
				else{
					System.out.println("No LongestPath");
					// go away from apple
					return farthestAwayFromApple(snake.headPosition(), target, gameInfo.field());
				}
			}
		}
		else{ // shortest path does not exist
			System.out.println("No Way");
				// compute longest path from snake head to snake tail
				Direction path3 = computeLongestPath(snake.headPosition(), snake.segments().get(0), gameInfo.field());
				
				if(path3 != null){ // longest path exists
					return path3;
				}else{
					// go away from apple
					return farthestAwayFromApple(snake.headPosition(), target, gameInfo.field());
				}
		}
	}
	
	// get position of the one apple of the field
	public Point getApple(Field field){
		for(int x=0;x<field.width();x++)
			for(int y=0;y<field.height();y++)
			{
				Point p = new Point(x,y);
				if(field.cell(p).equals(CellType.APPLE))
					return p;
			}
		return null;
	}
	
	// compute the shortest path using a* algorithm
	public Stack<Direction> computeShortestPath(Point start, Point target, Field field, Point snakeTail){
		
		// open list sorted by FCosts, Node of lowest FCost gets out at first 
		PriorityQueue<Node> openList = new PriorityQueue<>(new Comparator<Node>(){

			@Override
			public int compare(Node o1, Node o2) {
				if(o1.getFCost() > o2.getFCost())
					return 1;
				else if(o1.getFCost()< o2.getFCost())
					return -1;
				return 0;
			}
			
		});
		
		// closed list
		List<Node> closedList = new LinkedList<>();
		boolean CLOSED = false;
		
		// heuristic values
		int SPACE = 1;
		int APPLE = 1;
		int SNAKE = 100;
		int WALL = 100;
		
		// map similar to field, points contain distance to target
		int[][] distanceMap = new int[field.width()][field.height()];
		
		// map similar to field, points contain heuristic values
		int[][] shortWayMap =  new int[field.width()][field.height()];
		
		// initialize distanceMap and shortWayMap
		for (int i = 0; i < field.width(); i++)
			for (int j = 0; j < field.height(); j++)
			{
				distanceMap[i][j] = computeDistance(new Point(i,j),target);
				if(field.cell(new Point(i,j)).equals(CellType.APPLE))
				{
					shortWayMap[i][j] = APPLE;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.SPACE))
				{
					shortWayMap[i][j] = SPACE;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.WALL))
				{
					shortWayMap[i][j] = WALL;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.SNAKE))
				{
					shortWayMap[i][j] = SNAKE;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.FEATUREWALL))
				{
					shortWayMap[i][j] = SPACE;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.PORTAL))
				{
					shortWayMap[i][j] = SPACE;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.CHANGESNAKE))
				{
					shortWayMap[i][j] = SPACE;
				}
				else if(field.cell(new Point(i,j)).equals(CellType.CHANGEHEADTAIL))
				{
					shortWayMap[i][j] = SPACE;
				}
			}
		
		// tail of snake is no obstacle
		shortWayMap[snakeTail.x][snakeTail.y] = 1;
		
		// start node
		Node startNode = new Node(null,start,0,0);
		
		// add starting Node to open list
		openList.add(startNode);
		
		// calculation of A*
				while (!isInList(CLOSED, target, openList, closedList) && !openList.isEmpty()) { // stop if target is in closed list or open list is empty
					Node min = openList.remove(); // get Node of lowest FCosts from open list
					closedList.add(min); // add it to closed list
					Point current = min.getActual(); // current analyzed point of field
					for (int i = -1; i <= 1; i += 2) { // looking for neighbors of current analyzed
						if (current.x + i < field.width() - 1 && current.x + i > 0) // left and right neighbor
						{
							if(shortWayMap[current.x+i][current.y] == 1)
								updateOpenList(new Point(current.x + i, current.y), min, distanceMap, openList, closedList); // if neighbor point is no obstacle (snake, wall) add it to open list
						}
						if (current.y + i < field.height() - 1 && current.y + i > 0) // top and bottom neighbor
						{					
							if(shortWayMap[current.x][current.y+i] == 1)
								updateOpenList(new Point(current.x, current.y + i), min, distanceMap, openList, closedList); // if neighbor point is no obstacle (snake, wall) add it to open list
						}
					}
				}
		
		// ???
		closedList.remove(0);
		
		// get node from closed list that contains target
	    Node path = getNodeFromList(CLOSED, target, openList, closedList);
	    
	    // save each direction on a stack, first direction will be on top, last direction will be on bottom of stack
	    Stack<Direction> way = new Stack<Direction>();
	    
	    // shortest path exists
	    if(path != null)
		{
			while(path.getFrom() != null && !path.getFrom().getActual().equals(start)){ // reconstruction of way	
				way.push(getDirection(path.getFrom().getActual(),path.getActual()));
				path = path.getFrom();
			}
		}
	    
	     return way;

	}
	
	// compute longest path
	public Direction computeLongestPath(Point start, Point target, Field field){
		
		// a map similar to field, saves if a point can be entered (1 if space or apple) or not (0 if snake or wall)
		int[][] map = new int[field.width()][field.height()];
		
		// a map similar to field, saves which points have already been visited (true) and not been visited (false)
		boolean[][] visited = new boolean[field.width()][field.height()];
		
		// initializing both maps
		for(int x = 0; x < field.width(); x++)
			for(int y = 0; y < field.height(); y++){
//				visited[x][y] = false; // at beginning no point has been visited
				if(field.cell(new Point(x,y)).equals(CellType.APPLE) || field.cell(new Point(x,y)).equals(CellType.SPACE)  || field.cell(new Point(x,y)).equals(CellType.CHANGEHEADTAIL)
						 || field.cell(new Point(x,y)).equals(CellType.FEATUREWALL)  || field.cell(new Point(x,y)).equals(CellType.PORTAL)  || field.cell(new Point(x,y)).equals(CellType.CHANGESNAKE))
					map[x][y] = 1; // point is of type apple or space, can be entered
				else
					map[x][y] = 0; // point is of type snake or wall, so cannot be entered
			}
		if(start.equals(target))
			target.x+=1;
		// compute the longest path of points from start to target
		System.out.println("start: "+start);
		System.out.println("target: "+target);
		List<Point> path = computeLongestPath(start, target, map, visited);
		System.out.println("Size: "+map.length * map[0].length);
		System.out.println(path.size());
		if(!path.isEmpty() && path.size() > 1) // longest path exists
			return getDirection(path.get(0), path.get(1)); // get following point of start point from path to calculate the first direction
		
		return null;
		
	}
	
	// compute the longest path using recursion
	public List<Point> computeLongestPath(Point start, Point target, int[][] map, boolean[][] visited){
		
		List<Point> path = new ArrayList<Point>();
		
		// start point is also target point, path has already been found
		if(start.equals(target)){
			System.out.println("return Target");
			path.add(start);
			return path;
		}
		
		// start point is not valid (out of field, an obstacle, already visited)
		if(start.x < 0 || start.x >= 30 || start.y < 0 || start.y >= 20 || map[start.x][start.y] == 0 || visited[start.x][start.y]){
			System.out.println("return Null");
			return null;
		}
		
		// start point is visited 
		visited[start.x][start.y] = true;
		
		// maximum path length from start to target point
		int maxLength = Integer.MIN_VALUE;
		
		// analyze left, right, top, bottom point of start point
		Point[] neighbors = new Point[4];
		neighbors[0] = new Point(start.x - 1, start.y);
		neighbors[1] = new Point(start.x + 1, start.y);
		neighbors[2] = new Point(start.x, start.y - 1);
		neighbors[3] = new Point(start.x, start.y + 1);
		
		for(Point p : neighbors){
			path = computeLongestPath(p, target, map, visited);
			if(path != null && path.size() > maxLength){
				maxLength = path.size();
				path.add(0, start); // add start point at beginning to rest of the path
			}
		}
		
		// backtracking
		visited[start.x][start.y] = false;
		
		System.out.println("return done?");
		return path;
	}
	
	// get direction to move away from apple, but avoid crossing a wall or snake!
	public Direction farthestAwayFromApple(Point head, Point apple, Field field){
		Point newHead = null;
		int dist = 0;
		for(int i = -1; i <= 1; i = i+2)
		{
			  Point currHead = new Point(head.x + i, head.y);
			  if(currHead.x > 0 && currHead.x < 29 && !field.cell(new Point(currHead.x, currHead.y)).equals(CellType.WALL)
					  && !field.cell(new Point(currHead.x, currHead.y)).equals(CellType.SNAKE))
			  {
				  int currDist = computeDistance(currHead, apple);
				  if(currDist >= dist)
				  {
					 newHead = new Point(currHead.x, currHead.y);
					 dist = currDist;
				  }
			  }
			  
		}
		for(int i = -1; i <= 1; i = i+2)
		{
			  Point currHead = new Point(head.x, head.y + i);
			  if(currHead.y > 0 && currHead.y < 19 && !field.cell(new Point(currHead.x, currHead.y)).equals(CellType.WALL)
					  && !field.cell(new Point(currHead.x, currHead.y)).equals(CellType.SNAKE))
			  {
				  int currDist = computeDistance(currHead, apple);
				  if(currDist >= dist)
				  {
					 newHead = new Point(currHead.x, currHead.y);
					 dist = currDist;
				  }
			  }
			  
		}
		return getDirection(head, newHead);
	}
	
	// compute Manhattan distance
	public int computeDistance(Point a, Point b){
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}
	
	// compute direction to get from a to b
	public Direction getDirection(Point a, Point b){
		if (a.x + 1 == b.x && a.y == b.y)
			return Direction.RIGHT;
		if (a.x - 1 == b.x && a.y == b.y)
			return Direction.LEFT;
		if (a.x == b.x && a.y + 1 == b.y)
			return Direction.DOWN;
		if (a.x == b.x && a.y - 1 == b.y)
			return Direction.UP;
		return null;
	}
	
	// add a new node containing the neighbor point to the open list if possible
	public void updateOpenList(Point neighbor, Node node, int[][] distanceMap, PriorityQueue<Node> openList, List<Node> closedList) {
		// to check if a point is in open or closed list
		 boolean OPEN = true;
		 boolean CLOSED = false;
		 
		// cancel if neighbor is already in closed list
		if(isInList(CLOSED, neighbor, openList, closedList))
			return;
		
		// otherwise calculate GCosts for neighbor point
		int gCosts = node.getGCosts() + 1;
		
		// cancel if there is already a way of lower GCosts to get to the neighbor
		if (isInList(OPEN, neighbor, openList, closedList) && gCosts >= getNodeFromList(OPEN, neighbor, openList, closedList).getGCosts())
			return;
		
		// otherwise create a new node out of neighbor
		Node neighborNode = new Node(node, neighbor, distanceMap[neighbor.x][neighbor.y], gCosts);
		
		// delete similar node of higher GCosts from open list
		if(isInList(OPEN, neighbor, openList, closedList))
			openList.remove(getNodeFromList(OPEN, neighbor, openList, closedList));

		// add new better node to open list
		openList.add(neighborNode);
	}
	
	// check if p is already contained in a node of a specific list (open or closed list)
	public boolean isInList(boolean OPEN, Point p, PriorityQueue<Node> openList, List<Node> closedList) {
		for (Node node : (OPEN? openList:closedList))
		{
			if (node.getActual().equals(p))
				return true;
		}
		return false;
	}
	
	// get a node containing p of a specific list (open or closed list)
	public Node getNodeFromList(boolean OPEN, Point p, PriorityQueue<Node> openList, List<Node> closedList){
		for (Node node : (OPEN? openList:closedList))
		{
			if (node.getActual().equals(p))
				return node;
		}
		return null;
	}
	
}
