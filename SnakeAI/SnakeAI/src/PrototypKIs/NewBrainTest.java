package PrototypKIs;

import java.util.Stack;

import Logic.Field;
import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;
import Util.HamiltonPath;
import Util.Node;
import Util.PathFinder;
import Util.TempSnake;
import Util.UtilFunctions;
import Logic.SnakeBrain;
import Logic.Field.CellType;

/**
 * This Brain is only for testing algorithms or other stuff.
 * The last tests contained errors so dont use this SnakeBrain!
 * 
 * This Code is also not documented because it is only for testing purposes
 * @author Julia Hofmann, Marco Piechotta
 */
public class NewBrainTest implements SnakeBrain {
	//Eatable Stuff
		//0 = apple , 1 = wallItem , 2 = changeSnake , 3 = changeHeadTail , 4 = Portal
		private enum Items {
			APPLE(0), WALLITEM(1), CHANGESNAKE(2), CHANGEHEADTAIL(3), PORTAL(4);
			private final int value;		
			private Items(int value) {
				this.value = value;
			}
			public int getIndex()
			{
				return value;
			}
		}
		private Point[] eatable = new Point[5];
		private Snake mySnake;
		private Snake enemySnake;
		private GameInfo info;
		
		//MinPathFinder Alg.: A*-Algorithm 
		private PathFinder minPathFinder;
		//MaxPathFinder Alg.: HamiltonPath
		private HamiltonPath maxPathFinder;
		
	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {
		info = gameInfo;
		getItems(gameInfo.field());
		minPathFinder = new PathFinder();
		maxPathFinder = new HamiltonPath();
		if(mySnake == null || enemySnake == null)
		{
			mySnake = snake;
			for(Snake s : gameInfo.snakes())
			{
				if(!s.equals(snake))
				{
					enemySnake = s;
					break;
				}
			}
		}
		// get position of apple
		Point target = eatable[Items.APPLE.getIndex()];
		
		// compute shortest path from snake head to apple
		Node path = minPathFinder.getMinPath(new TempSnake(snake), target, gameInfo.field(), gameInfo.getPortals());
		Stack<Direction> path1 = new Stack<>();
		if(path != null)
		{
			while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition())){ // reconstruction of way	
				path1.push(UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual()));
				path = path.getFrom();
			}
		}		
		if(!path1.empty()){ // shortest path exists
			
			// save first step but do not remove from path1
			Direction step1 = path1.peek();
			
			// create virtual snake that follows path1 on a virtual field
			Field tempField = Field.defaultField(gameInfo.field().width(), gameInfo.field().height());
			for(Point p : enemySnake.segments())
				tempField.setCell(CellType.SNAKE, p);
			TempSnake tempSnake = new TempSnake(snake);
			while(!path1.empty())
			    tempSnake.move(path1.pop(), tempField);
			for(Point p : tempSnake.segments())
				tempField.setCell(CellType.SNAKE, p);
			
			// compute longest path from virtual snake head to virtual snake tail
			Direction path2 = computeLongestPath(tempSnake, tempField, gameInfo);
			System.out.println("Path2: "+path2);
			if(path2 != null){ // longest path exists
				return step1;
			}
			else{
				// compute longest path from snake head to snake tail
				Direction path3 = computeLongestPath(new TempSnake(snake), gameInfo.field(), gameInfo);
				
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
				Direction path3 = computeLongestPath(new TempSnake(snake), gameInfo.field(), gameInfo);
				
				if(path3 != null){ // longest path exists
					return path3;
				}else{
					// go away from apple
					return farthestAwayFromApple(snake.headPosition(), target, gameInfo.field());
				}
		}
	}
	private Direction farthestAwayFromApple(Point headPosition, Point target, Field field) {
		int maxDist = Integer.MIN_VALUE;
		Direction maxDir = null;
		for(int i = -1; i <= 1; i = i+2)
		{
			Point headX = new Point(headPosition.x+i,headPosition.y);
			Direction dir = UtilFunctions.getDirection(headPosition, headX);
			int dist = UtilFunctions.getDistance(headPosition, headX);
			if(isMoveValid(dir) && maxDist < dist)
			{
				maxDir = dir;
				maxDist = dist;
			}
			Point headY = new Point(headPosition.x, headPosition.y+i);
			dir = UtilFunctions.getDirection(headPosition, headY);
			dist = UtilFunctions.getDistance(headPosition, headY);
			if(isMoveValid(dir) && maxDist < dist)
			{
				maxDir = dir;
				maxDist = dist;
			}
		}
		return maxDir;
	}
	private Direction computeLongestPath(TempSnake tempSnake, Field tempField, GameInfo gameInfo) {
//		tempField.draw();
		Node longPath = maxPathFinder.getMaxPath(tempSnake.headPosition(), tempField, tempSnake, new TempSnake(enemySnake), gameInfo.getPortals());
		System.out.println("LongPath: "+longPath);
		if(longPath != null)
		{
			while(longPath.getFrom() != null && !longPath.getFrom().getActual().equals(tempSnake.headPosition())){ // reconstruction of way	
				longPath = longPath.getFrom();
			}
			return UtilFunctions.getDirection(longPath.getFrom().getActual(),longPath.getActual());
		}
		else return null;
	}
	private void getItems(Field f) {
		eatable = new Point[5];
		for(int x=0;x<f.width();x++)
			for(int y=0;y<f.height();y++)
			{
				Point p = new Point(x,y);
				if(f.cell(p).equals(CellType.APPLE))
					eatable[Items.APPLE.getIndex()] = p;
				if(f.cell(p).equals(CellType.FEATUREWALL))
					eatable[Items.WALLITEM.getIndex()] = p;
				if(f.cell(p).equals(CellType.CHANGESNAKE))
					eatable[Items.CHANGESNAKE.getIndex()] = p;
				if(f.cell(p).equals(CellType.CHANGEHEADTAIL))
					eatable[Items.CHANGEHEADTAIL.getIndex()] = p;
				if(f.cell(p).equals(CellType.PORTAL))
					eatable[Items.PORTAL.getIndex()] = p;
			}

	}
	private boolean isMoveValid(Direction d) {
		Point newHead = new Point(mySnake.headPosition().x, mySnake.headPosition().y);
		switch(d) {
		case DOWN:
			newHead.y++;
			break;
		case LEFT:
			newHead.x--;
			break;
		case RIGHT:
			newHead.x++;
			break;
		case UP:
			newHead.y--;
			break;
		default:
			break;
		}
		if (newHead.x == -1) {
			newHead.x = info.field().width()-1;
		}
		if (newHead.x == info.field().width()) {
			newHead.x = 0;
		}
		if (newHead.y == -1) {
			newHead.y = info.field().height()-1;
		}
		if (newHead.y == info.field().height()) {
			newHead.y = 0;
		}
		
		return info.field().cell(newHead) != CellType.SNAKE && info.field().cell(newHead) != CellType.WALL;
	}
}
