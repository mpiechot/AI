package Util;

import java.util.List;
import java.util.Random;

import Logic.Field;
import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.Field.CellType;
import Logic.Snake.Direction;

public final class UtilFunctions {
	private static Direction last;	
	
	/**
	 * determines the direction to take out of two given points
	 * @param a - first point
	 * @param b - second point
	 * @return Direction from a to b
	 */
	public static Direction getDirection(Point a, Point b) 
	{
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
	
	/**
	 * extracts an element of closed list
	 * @param p - current point 
	 * @param closedList - list of Nodes
	 * @return Node containing p
	 */
	public static Node getMovePair(Point p, List<Node> closedList) 
	{
		for (Node n : closedList)
			if (n.getActual().equals(p))
				return n;

		return null;
	}
	
	/**
	 * computes the Manhattan distance of two points
	 * @param a - first point
	 * @param b - second point
	 * @return Manhattan distance
	 */
	public static int getDistance(Point a, Point b) 
	{
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}
	
	/**
	 * duplicates a given game field
	 * @param f - current game field
	 * @return a copy of f
	 */
	public static Field getFieldCopy(Field f)
	{
		Field copy = Field.defaultField(f.width(), f.height());
		for(int x=0;x<f.width();x++)
			for(int y=0;y<f.height();y++)
			{
				Point p = new Point(x,y);
				copy.setCell(f.cell(p), p);
			}
		return copy;
	}
	
	/**
	 * this Function returns if a given Direction is valid.
	 * a Direction is valid if the resulting CellType isnt Snake or Wall.
	 * @param d move direction we want to check if it is valid
	 * @return true if the direction is valid for our snake.
	 */
	public static boolean isMoveValid(Direction d, Snake snake, GameInfo gameInfo) {
		Point newHead = new Point(snake.headPosition().x, snake.headPosition().y);
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
			newHead.x = gameInfo.field().width()-1;
		}
		if (newHead.x == gameInfo.field().width()) {
			newHead.x = 0;
		}
		if (newHead.y == -1) {
			newHead.y = gameInfo.field().height()-1;
		}
		if (newHead.y == gameInfo.field().height()) {
			newHead.y = 0;
		}
		
		return gameInfo.field().cell(newHead) == CellType.SPACE || gameInfo.field().cell(newHead) == CellType.APPLE;
	}
	/**
	 * check if our snake can make a valid move.
	 * @return true if we can make valid moves
	 */
	public static boolean isValidMovePossible(Snake snake, GameInfo gameInfo) {
		return isMoveValid(Direction.DOWN, snake, gameInfo) || isMoveValid(Direction.UP, snake, gameInfo) || isMoveValid(Direction.LEFT, snake, gameInfo) || isMoveValid(Direction.RIGHT, snake, gameInfo);
	}
	/**
	 * calculate a random valid direction.
	 * @return random generated direction
	 */
	public static Direction randomMove(GameInfo gameInfo, Snake snake) {
		Random rand = new Random();
		Direction d;
		if (rand.nextDouble() < 0.95 && last != null && isMoveValid(last, snake, gameInfo)) {
			d = last;
		} else {
			do {
				d = Direction.values()[rand.nextInt(4)];
			} while(!isMoveValid(d, snake, gameInfo) && isValidMovePossible(snake, gameInfo));
		}
		
		last = d;
		
		return d;
	}
}
