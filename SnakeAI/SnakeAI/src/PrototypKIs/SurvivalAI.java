package PrototypKIs;

import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;
import Util.Node;
import Util.Pathfinding;
import Util.UtilFunctions;
import Logic.SnakeBrain;

//SurvivalAI
//
/**
 * This SnakeBrain follows the other Snake.
 * The Idea is that the other snake will make a mistake and hit a wall or itself before our snake dies
 * 
 * @author Julia Hofmann, Marco Piechotta
 */
public class SurvivalAI implements SnakeBrain {

	private static final int DISTANCE_TO_ENEMYSNAKE = 2;
	private static final int MIN_CUT_LENGTH = 2;
	private static final int DESIRED_SNAKE_LENGTH = 1;
	
	private Snake mySnake;
	private Snake enemySnake;
	private Direction moveDirection = null;
	private GameInfo info;
	private boolean passedPortal = false;
		
	//MinPathFinder Alg.: A*-Algorithm 
	private Pathfinding minPathFinder;

	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {
		//Initialize all classvariables
		info = gameInfo;
		init(snake);
		
		//check if we need to cut our Snake because it is to long
		if(isPortalHelpfulForSnake())
			return moveDirection;
		
		//if our distance is high enough to move directly to the tail of the enemysnake then calculate a path to this point
		if(UtilFunctions.getDistance(mySnake.headPosition(),enemySnake.segments().get(0)) > DISTANCE_TO_ENEMYSNAKE && getNextDirection(enemySnake.segments().get(0)))
			return moveDirection;
		
		//otherwise calculate a random move
		return UtilFunctions.randomMove(gameInfo, snake);	
	}
	/**
	 * calculate a path through a portal to cut our snake to the DESIRED_SNAKE_LENGTH if we can.
	 * @return true if we can cut our snake 
	 */
	private boolean isPortalHelpfulForSnake()
	{
		if(mySnake.segments().size() > MIN_CUT_LENGTH && !passedPortal && info.getPortals().isActive())
		{
			Point[] portals = {info.getPortals().getPortal1(),info.getPortals().getPortal2()};
			for(int i=0;i<portals.length;i++)
			{
				Node path = minPathFinder.getMinPath(mySnake.headPosition(), portals[i],info.field(),mySnake.segments().get(0));
				int dist = (path!= null?path.lengthToDest(mySnake.headPosition()):0);
				double TTL = info.getPortals().getTTL();
				if(path != null &&  TTL == dist+DESIRED_SNAKE_LENGTH)
				{	
					//we have a path. calculate the next position
					while(path.getFrom() != null && !path.getFrom().getActual().equals(mySnake.headPosition()))
						path = path.getFrom();	
					moveDirection = UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());
					return true;
				}			
			}
		}
		return false;
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
	/**
	 * init the minPathfinder and the references for mySnake and enemySnake
	 * @param snake
	 */
	private void init(Snake snake)
	{
		
		//init MinPathfinder
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