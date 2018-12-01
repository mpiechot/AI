package PrototypKIs;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;

import Logic.Field;
import Logic.Field.CellType;
import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;
import Logic.SnakeBrain;
import Util.AlphaBeta;
import Util.HamiltonPath;
import Util.Node;
import Util.PathFinder;
import Util.TempSnake;
import Util.UtilFunctions;

/**
 * BrainMaster uses all of our algorithms to choose the best path.
 * It will calculate alphaBeta-pruning to find bad directions and avoid them.
 * Also it will calculate a longest Path if the snake trapped itself.
 * See the nextDirection function for more information
 * 
 * @author Julia Hofmann, Marco Piechotta
 */
public class BrainMaster implements SnakeBrain{
	
	//Constants
	private static final int CHANGE_DISTANCE = 1;
	private static final int MIN_CUT_LENGTH = 15;
	private static final int DESIRED_SNAKE_LENGTH = 9;

	private Snake mySnake;
	private Snake enemySnake;
	private Direction moveDirection = null;
	private boolean firstRound=true;
	private boolean passedPortal = false;
	
	/**
	 * covers all eatable features on the field.
	 * every Type has an index for using as array-index with eatable
	 * @author Marco
	 *
	 */
	private enum Items {
		APPLE(0), WALLITEM(1), CHANGESNAKE(2), CHANGEHEADTAIL(3), PORTAL(4), SPEEDUP(5), CUTTAIL(6);
		private final int value;		
		private Items(int value) {
			this.value = value;
		}
		public int getIndex()
		{
			return value;
		}
	}
	/**
	 * Eatable Stuff<br>
	 * 0 = apple , 1 = wallItem , 2 = changeSnake , 3 = changeHeadTail , 4 = Portal<br>
	 * Use {@link Items}-enum for array access
	 */
	private Point[] eatable = new Point[7];
	private Point[] altTargets;
	private int currentAltTarget;
	private GameInfo info;
	private Point wallPlacedTarget = null;
	
	//MinPathFinder Alg.: A*-Algorithm 
	private PathFinder minPathFinder;
	
	//MaxPathFinder Alg.: HamiltonPath
	private HamiltonPath maxPathFinder;
	private HashMap<Point, Direction> completeMaxPath;
	private Stack<Direction> maxPath = new Stack<>();
	
	//AlphaBeta
	private AlphaBeta alphaBeta;
	
	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {
		info = gameInfo;
		init(snake);

		if(firstRound)
		{
			firstRound = false;
			return Direction.RIGHT;
		}
		
		minPathFinder.clearBadPositions();
		altTargets[0] = enemySnake.headPosition();
		
		//shift the alternative target if needed
		if(UtilFunctions.getDistance(snake.headPosition(), altTargets[currentAltTarget]) <= CHANGE_DISTANCE)
			changeAltTarget();
		
		alphaBeta.alphaBeta(gameInfo.field(), snake, enemySnake, 5, eatable);
		if(alphaBeta.getBestScore() > 9000)
			return alphaBeta.getBestMove();

		int countWorstScores = 0;
		for(Entry<Direction,Integer> entry : alphaBeta.getDirectionScores().entrySet())
		{
			Point head = new Point(snake.headPosition().x,snake.headPosition().y);
			switch(entry.getKey())
			{
			case UP: head.y--; break;
			case DOWN:head.y++; break;
			case LEFT:head.x--; break;
			case RIGHT:head.x++; break;
			}
			if(entry.getValue() < -9000)
			{
				countWorstScores++;
				minPathFinder.addBadPosition(head);
			}	
		}
		if(countWorstScores > 2)
			return alphaBeta.getBestMove();
		
		wallNextToAppleDetection();
		
		//Is the snakebody in a portal?
		if(gameInfo.getPortals().isActive())
		{
			if(gameInfo.field().cell(snake.headPosition()).equals(CellType.PORTAL))
				passedPortal = true;
			if(gameInfo.field().cell(snake.segments().get(0)).equals(CellType.PORTAL))
				passedPortal = false;
		}
		else
			passedPortal = false;
		
		if(wallPlacedAtApple())
			return moveDirection;
		
		if(isPortalHelpfulForSnake())
			return moveDirection;
		
		if(!isApplePosDangerous() && isAppleReachable())
			return moveDirection;
				
		//if we havent found a good path we should play save now
		if(isWallItemReachable())
			return moveDirection;
		;
		if(isAlternativeTargetReachable())
			return moveDirection;
		
		//It is possible that our snake is trapped, so handle this situation
		if(isSnakeTrapped())
			return moveDirection;
		
		if((moveDirection = completeMaxPath.get(snake.headPosition())) != null && UtilFunctions.isMoveValid(moveDirection, snake, gameInfo))
			return moveDirection;
		
		return UtilFunctions.randomMove(gameInfo, snake);
	}
	/**
	 * depending on wallDetection() this function places a wall for a trap if the apple is already next to a wall.
	 * After placing the wall the snake moves to a 'close point'.
	 * @return true if the snake placed a wall and made a trap.
	 */
	private boolean placeWallIfPossible()
	{
		//   8         4         2         1
		//[wallUp][wallRight][wallDown][wallLeft]
		if(mySnake.getCanSetWall())
		{
			int walls = wallNextToAppleDetection();
			if(walls > 0 && !isSnakeCloserToTarget(eatable[Items.APPLE.getIndex()]))
			{
				Point apple = eatable[Items.APPLE.getIndex()];
				boolean upDown = false;
				Point closePoint = apple;
				boolean foundClosePoint = false;
				switch(walls)
				{
				case 1:
					//
					//| x  
					//			
					for(int i=-3;i<=3;i++)
					{
						closePoint = new Point(apple.x,apple.y+i);
						if(i >= 0)
							upDown = true;
						if(isSnakeCloserToTarget(closePoint,apple))
						{
							foundClosePoint = true;
							break;
						}
					}
					if(foundClosePoint)
						mySnake.setWall(new Point(closePoint.x+1,(upDown?closePoint.y-2:closePoint.y+2)), Direction.DOWN);
					break;
				case 2:
					//
					// x
					// _
					for(int i=-3;i<=3;i++)
					{
						closePoint = new Point(apple.x+i,apple.y);
						if(i >= 0)
							upDown = true;
						if(isSnakeCloserToTarget(closePoint,apple))
						{
							foundClosePoint = true;
							break;
						}
					}
					if(foundClosePoint)
						mySnake.setWall(new Point((upDown?closePoint.x-2:closePoint.x+2),closePoint.y-1), Direction.LEFT);
					break;
				case 3:
					//
					//| x
					//  _
					break;
				case 4:
					//
					// x |
					// 
					for(int i=-3;i<=3;i++)
					{
						closePoint = new Point(apple.x,apple.y+i);
						if(i >= 0)
							upDown = true;
						if(isSnakeCloserToTarget(closePoint,apple))
						{
							foundClosePoint = true;
							break;
						}
					}
					if(foundClosePoint)
						mySnake.setWall(new Point(closePoint.x-1,(upDown?closePoint.y-2:closePoint.y+2)), Direction.UP);
					break;
				case 5:
					//
					//| x |
					// 
				case 6:
					//
					// x |
					// _
				case 7:
					//
					//| x |
					//  _
					break;
				case 8:
					// _
					// x
					// 
					for(int i=-3;i<=3;i++)
					{
						closePoint = new Point(apple.x+i,apple.y);
						if(i >= 0)
							upDown = true;
						if(isSnakeCloserToTarget(closePoint,apple))
						{
							foundClosePoint = true;
							break;
						}
					}
					if(foundClosePoint)
						mySnake.setWall(new Point((upDown?closePoint.x-2:closePoint.x+2),closePoint.y+1), Direction.LEFT);
					break;
				case 9:
					//  _
					//| x
					// _
				case 10:
					// _
					// x
					// _
				case 11:
					//  _
					//| x 
					//  _
				case 12:
					//
					// x |
					// _
				case 13:
					//  _
					//| x |
					// 
				case 14:
					// _
					// x |
					// _
				case 15:
					//  _
					//| x |
					//  _
				default:
				}
				if(foundClosePoint)
				{
					wallPlacedTarget = closePoint;
					if(getNextDirection(closePoint))
						return true;
				}
			}
			//Place Wall at Random Point
			Random r = new Random();
			Point wall = null;
			do
			{
				wall = new Point(r.nextInt(info.field().width()),r.nextInt(info.field().height()));
				mySnake.setWall(wall, Direction.LEFT);
			}
			while(mySnake.getCanSetWall());
		}
		wallPlacedTarget = null;
		return false;
	}
	/**
	 * if the snake made a trap, then the snake should continue to move to the 'close Point'
	 * @return true if it is possible to move to the 'close Point'
	 */
	private boolean wallPlacedAtApple()
	{	
		if(wallPlacedTarget != null)
		{
			if(getNextDirection(wallPlacedTarget))
				return true;
		}
		else
			return placeWallIfPossible();
		wallPlacedTarget = null;
		return false;
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
				Node path = minPathFinder.getMinPath(new TempSnake(mySnake), portals[i],info.field(),info.getPortal());
				int dist = (path!= null?path.lengthToDest(mySnake.headPosition()):0);
				double TTL = info.getPortals().getTTL();
				if(path != null &&  TTL == dist+DESIRED_SNAKE_LENGTH)
				{	
					//we have a path. calculate the next position
					while(path.getFrom() != null && !path.getFrom().getActual().equals(mySnake.headPosition()))
						path = path.getFrom();	
					maxPath.clear();
					moveDirection = UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());
					return true;
				}			
			}
		}
		return false;
	}
	/**
	 * calculate a Pathto the next apple if we are closer to it than the enemy Snake
	 * @return if we can reach the apple before the enemySnake and got a path to the apple
	 */
	private boolean isAppleReachable()
	{
		if(eatable[Items.APPLE.getIndex()] != null && isSnakeCloserToTarget(eatable[Items.APPLE.getIndex()]))
		{
			//our snake is nearer than the enemy to the apple
			if(getNextDirection(eatable[Items.APPLE.getIndex()]))
				return true;
		}
		else
		{
			//the enemy is in a better position for the next apple
			if(eatable[Items.CHANGESNAKE.getIndex()] != null && eatable[Items.APPLE.getIndex()] != null)
			{
				//if we can change our snakes before the enemy is at the apple, then do this
				if(isSnakeCloserToTarget(eatable[Items.CHANGESNAKE.getIndex()], eatable[Items.APPLE.getIndex()]))
				{
					if(getNextDirection(eatable[Items.CHANGESNAKE.getIndex()]))
						return true;
				}
			}
		}
		return false;
	}
	/**
	 * calculate if there is a WallFeatureItem on the gamefield and if there is one: calculate a path to the Item
	 * @return true if there is a WallFeature on the field and we have a path to it.
	 */
	private boolean isWallItemReachable()
	{
		//If we havent one already, then get a WallFeatureitem!
		if(eatable[Items.WALLITEM.getIndex()] != null && !mySnake.getCanSetWall())
		{
			if(getNextDirection(eatable[Items.WALLITEM.getIndex()]))
				return true;
		}
		return false;
	}
	/**
	 * try to move to the alternative Position if possible.
	 * This is good if we cant eat the apple and have nothing else to do.
	 * @return true if we can move to an alternative Position
	 */
	private boolean isAlternativeTargetReachable()
	{
		//check if we can move to one of our alternative targets
		for(int i=0;i<altTargets.length;i++)
		{
			if(!info.field().cell(altTargets[currentAltTarget]).equals(CellType.SNAKE) && 
					!info.field().cell(altTargets[currentAltTarget]).equals(CellType.WALL))
			{
				Node altWay = minPathFinder.getMinPath(new TempSnake(mySnake), altTargets[currentAltTarget],info.field(),info.getPortal());
				
				//does a path exsist?
				if(altWay != null)
				{	
					//Okay thats good. But to determine if we are trapped check if we could find a path to another alternative target!
					int currentAltTarget2 = ((currentAltTarget+1)%4);
					if(!info.field().cell(altTargets[currentAltTarget2]).equals(CellType.SNAKE) && 
							!info.field().cell(altTargets[currentAltTarget2]).equals(CellType.WALL))
					{
						Node altWay2 = minPathFinder.getMinPath(new TempSnake(mySnake), altTargets[currentAltTarget2],info.field(),info.getPortal());
						if(altWay2 != null)
						{
							//we got a path. So we arent trapped -> move the altWay
							while(altWay.getFrom() != null && !altWay.getFrom().getActual().equals(mySnake.headPosition()))
								altWay = altWay.getFrom();	
							maxPath.clear();
							moveDirection = UtilFunctions.getDirection(altWay.getFrom().getActual(),altWay.getActual());
							if(moveDirection == null)
							{
								int x = altWay.getFrom().getActual().x - altWay.getActual().x; 
								moveDirection = (x > 0?Direction.LEFT:(x == 0?null:Direction.RIGHT));
								int y = altWay.getFrom().getActual().y - altWay.getActual().y;
								if(moveDirection == null)
									moveDirection = (y > 0?Direction.UP:Direction.DOWN);
							}
							return true;
						}
					}
				}
				else
					break;
			}
			changeAltTarget();
		}
		return false;
	}
	/**
	 * calculate if the snake is trapped and if it is, then 
	 * calculate the longest path from snake head to the tail using HamiltonPath
	 * @return true if we are trapped and we have a way to our tail
	 */
	private boolean isSnakeTrapped()
	{
		//check if we can switch the our head with our tail
		if(eatable[Items.CHANGEHEADTAIL.getIndex()] != null)
			if(getNextDirection(eatable[Items.CHANGEHEADTAIL.getIndex()]))
				return true;
		
		//check if we can escape through a portal
		if(info.getPortals().isActive())
		{
			if(getNextDirection(info.getPortals().getPortal1()))
				return true;
			if(getNextDirection(info.getPortals().getPortal2()))
				return true;
		}
		
		//We cant escape, so it is possible that we trapped ourself.
		//Do we know that already and have a maxPath calculated?
		if(maxPath.isEmpty())
		{
			Node way = maxPathFinder.getMaxPath(new TempSnake(mySnake).headPosition(), info.field(), new TempSnake(mySnake), new TempSnake(enemySnake),info.getPortal());
			
			//If we found a maxPath to an exitPoint, add this to a pathStack
			while(way != null && way.getFrom() != null && !way.getActual().equals(mySnake.headPosition()))
			{
				maxPath.add(UtilFunctions.getDirection(way.getFrom().getActual(),way.getActual()));
				way = way.getFrom();
			}
			//set the next Direction if we have a path in the pathStack
			if(!maxPath.isEmpty())
			{
				moveDirection = maxPath.pop();
				return true;
			}
			else
				return false;
		}
		else 
		{
			//set the next Direction if we have a path in the pathStack
			moveDirection = maxPath.pop();
			return true;
		}
	}
	/**
	 * calculates if our snake is closer to the target position
	 * @param target - point where we want to go 
	 * @return true if we are closer to the target
	 */
	private boolean isSnakeCloserToTarget(Point target)
	{
		return UtilFunctions.getDistance(mySnake.headPosition(),target) <= UtilFunctions.getDistance(enemySnake.headPosition(),target);
	}
	/**
	 * calculates if we are closer to our target than the enemySnake to his target
	 * @param myTarget - our target
	 * @param enemyTarget the enemy target
	 * @return true if we are closer to our target
	 */
	private boolean isSnakeCloserToTarget(Point myTarget, Point enemyTarget)
	{
		return UtilFunctions.getDistance(mySnake.headPosition(),myTarget) < UtilFunctions.getDistance(enemySnake.headPosition(),enemyTarget);
	}
	/**
	 * change our current alternative target to the next one
	 */
	private void changeAltTarget()
	{
		currentAltTarget = ((++currentAltTarget)%altTargets.length);
	}
	/**
	 * detect all walls next to the apple.
	 * @return the encoded walls next to the apple as an integer
	 */
	private int wallNextToAppleDetection()
	{
		Point apple = eatable[Items.APPLE.value];
		if(apple== null)
			return 0;
		//   8         4         2         1
		//[wallUp][wallRight][wallDown][wallLeft]
		//Bsp.: 1101 => Wall oben,rechts und links
		int wall = 0;
		for (int i = -1; i <= 1; i += 2)
		{
			if (apple.x + i <= 29 && apple.x + i >= 0)
			{
				Point next = new Point(apple.x +i,apple.y);
				if(info.field().cell(next) == CellType.WALL)
				{
					wall |= (i > 0?4:1);
				}
			}
			if (apple.y + i <= 19 && apple.y + i >= 0)		
			{
				Point next = new Point(apple.x,apple.y+i);
				if(info.field().cell(next) == CellType.WALL)
				{
					wall |= (i > 0?2:8);
				}
			}
		}
		return wall;
	}
	/**
	 * initialize the following in 2 steps.
	 * if firstRound = true initialize:<br>
	 * - minPathFinder<br>
	 * - maxPathFinder<br>
	 * - alphaBeta<br>
	 * - alternative Targets<br>
	 * in the secound round initialize:<br>
	 * - mySnake<br>
	 * - enemySnake<br>
	 * - completeMaxPath<br>
	 * - eatable
	 * @param our snake reference
	 */
	private void init(Snake snake)
	{
		//init in the first round:
		if(minPathFinder == null)
			minPathFinder = new PathFinder();
		if(maxPathFinder == null)
			maxPathFinder = new HamiltonPath();
		if(alphaBeta == null)
			alphaBeta = new AlphaBeta();
		if(altTargets == null)
		{
			altTargets = new Point[4];
			altTargets[0] = new Point(info.field().width()/2,1);
			altTargets[1] = new Point(info.field().width()/2,info.field().height()-2);
			altTargets[2] = new Point(1,info.field().height()/2);
			altTargets[3] = new Point(info.field().width()-2,info.field().height()/2);
			currentAltTarget = 0;
		}					
		if(firstRound)
			return;
		
		//Init in the secound round
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
		
		//calculate the hamilton path on the whole field
		if(completeMaxPath == null)
		{
			Node hamiltonPath = maxPathFinder.getCompleteMaxPath(Field.defaultField(info.field().width(), info.field().height()));
			if(hamiltonPath != null)
			{
				completeMaxPath = new HashMap<>();
				Point first = hamiltonPath.getActual();
				Point last = null;
				while(hamiltonPath != null && hamiltonPath.getFrom() != null)
				{
					completeMaxPath.put(hamiltonPath.getActual(), UtilFunctions.getDirection(hamiltonPath.getFrom().getActual(), hamiltonPath.getActual()));
					hamiltonPath = hamiltonPath.getFrom();
					if(hamiltonPath.getFrom() == null)
						last = hamiltonPath.getActual();
				}
				if(last != null)
				{
					completeMaxPath.put(last, UtilFunctions.getDirection(first, last));
				}
			}
		}
	
		//Find all eatable Objects
		getItems(info.field(),snake.headPosition());
	}
	/**
	 * collects all positions of eatable Items on the field and saves their position as {@link Point} in eatable
	 * @param f the current gameField
	 * @param snakeHead the headPosition of the snake
	 */
	private void getItems(Field f, Point snakeHead) {
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
	/**
	 * calculates a shortest Path to target from the head position of our snake
	 * and saves the next direction in moveDirection if the path exsists
	 * @param target - where do we want to go?
	 * @return true if we found a path to the target.
	 */
	private boolean getNextDirection(Point target)
	{
		Node path = minPathFinder.getMinPath(new TempSnake(mySnake), target,info.field(),info.getPortal());
		
		if(path != null)
		{	
			//We found a Path so we doesnt need to follow the maxPath anymore
			maxPath.clear();
			
			//Get the next direction for our current Position
			while(path.getFrom() != null && !path.getFrom().getActual().equals(mySnake.headPosition()))
				path = path.getFrom();

			moveDirection = UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());
			
			//if moveDirection is null we move through a portal. This leads to a distance > 1 so the
			//getDirection Function will return null. If this happens we move to the direction which leads us
			//directly to our target.
			if(moveDirection == null)
			{
				int minDistance = Integer.MAX_VALUE;
				for (int i = -1; i <= 1; i += 2)
				{
					Point movePosX = new Point(mySnake.headPosition().x+i,mySnake.headPosition().y);
					int distanceX = UtilFunctions.getDistance(movePosX,target);
					if(distanceX < minDistance)
					{
						minDistance = distanceX;
						moveDirection = UtilFunctions.getDirection(mySnake.headPosition(), movePosX);
					}
					Point movePosY = new Point(mySnake.headPosition().x,mySnake.headPosition().y+i);
					int distanceY = UtilFunctions.getDistance(movePosY,target);
					if(distanceY < minDistance)
					{
						minDistance = distanceY;
						moveDirection = UtilFunctions.getDirection(mySnake.headPosition(), movePosY);
					}
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * if the apple is near a wall, this position is dangerous and we should'nt eat this apple!
	 * @return
	 */
	private boolean isApplePosDangerous()
	{
		if(wallNextToAppleDetection() != 0)
			return true;
		else 
			return false;
	}
}
