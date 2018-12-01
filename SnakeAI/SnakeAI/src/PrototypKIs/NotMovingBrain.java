package PrototypKIs;

import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;
import Util.Node;
import Util.Pathfinding;
import Util.UtilFunctions;
import Logic.SnakeBrain;

/**
 * This SnakeBrain is only for testing. It will move to the right-down corner and then continue moving left then right.
 * Use this SnakeBrain for "simulate" one Snake on a gameField
 * 
 * @author Marco
 */
public class NotMovingBrain implements SnakeBrain {
	private Snake mySnake;
	private Snake enemySnake;
	private Direction moveDirection = null;
	private GameInfo info;
	private boolean inPosition = false;
		
	//MinPathFinder Alg.: A*-Algorithm 
	private Pathfinding minPathFinder;
	
	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {
		//Initialize all classvariables
		info = gameInfo;
		init(snake);
		
		//If we are in our end-Position (right-down corner) set inPosition
		if(snake.headPosition().equals(new Point(27,18)) || snake.headPosition().equals(new Point(28,18)))
			inPosition = true;
		else
			inPosition = false;
		
		//if we are in position depending on the Point we are at, move left or right
		if(inPosition)
			return (snake.headPosition().equals(new Point(27,18))?Direction.RIGHT:Direction.LEFT);
		
		//if we're not in Position. We calculate a path to our position and move to the next position on the path
		if(getNextDirection(new Point(27,18)))
			return moveDirection;
		
		//otherwise we cant move there so make a random-move
		return UtilFunctions.randomMove(gameInfo, snake);
	}
	/**
	 * calculates a shortest Path to target from the head position of our snake
	 * and saves the next direction in moveDirection if the path exsists
	 * @param target - where do we want to go?
	 * @return true if we found a path to the target.
	 */
	private boolean getNextDirection(Point target)
	{
		Node path = minPathFinder.getMinPath(mySnake.headPosition(), target,info.field(),mySnake.segments().get(0));
		
		//Does the path exsist?
		if(path != null)
		{	
			//Get the next direction for our current Position
			while(path.getFrom() != null && !path.getFrom().getActual().equals(mySnake.headPosition()))
				path = path.getFrom();	
			moveDirection = UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());
			if(!UtilFunctions.isMoveValid(moveDirection, mySnake, info))
				return false;
			
			//if moveDirection is null we move through a portal. This leads to a distance > 1 so the
			//getDirection Function will return null. If this happens we move to the direction which leads us
			//directly to our target.
			if(moveDirection == null)
			{
				int x = path.getFrom().getActual().x - path.getActual().x; 
				moveDirection = (x > 0?Direction.LEFT:(x == 0?null:Direction.RIGHT));
				int y = path.getFrom().getActual().y - path.getActual().y;
				if(moveDirection == null)
					moveDirection = (y > 0?Direction.UP:Direction.DOWN);
			}
			return true;
		}
		return false;
	}
	private void init(Snake snake)
	{
		
		//initialisieren
		if(minPathFinder == null)
			minPathFinder = new Pathfinding();
		
		//Initialize mySnake and EnemySnake
		if(mySnake == null || enemySnake == null)
		{
			mySnake = snake;
			for(Snake s : info.snakes())
			{
				if(!s.equals(snake))
				{
					enemySnake = s;
					break;
				}
			}
		}
	}
}
