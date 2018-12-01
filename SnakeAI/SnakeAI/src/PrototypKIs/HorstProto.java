package PrototypKIs;

import java.util.HashMap;
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
 * This is an old version of the HorstAI
 * It is used to determine if our new AI made any improvment.
 * @author Marco
 */
public class HorstProto implements SnakeBrain{

	private Snake mySnake;
	private Snake enemySnake;
	private Direction last = null;
	private Field tempField;
	private boolean firstRound=true;
	private boolean passedPortal = false;
	
	//Eatable Stuff
	//0 = apple , 1 = wallItem , 2 = changeSnake , 3 = changeHeadTail , 4 = Portal
	private Point[] eatable = new Point[5];
	private Point[] altTargets;
	private int currentAltTarget;
	
	//A* 
	private PathFinder finder;
	
	//HamiltonPath (PreFix h = hat mit dem HamiltonPath zutun
	private HamiltonPath hFinder;
	private HashMap<Point, Direction> hPath;
	private HashMap<Point, Integer> hPointToIndex;
	private Stack<Direction> hDirectionPath = new Stack<>();
	
	//AlphaBeta
	private AlphaBeta alphaBeta;
	
	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {
		//Initialisiere alle n�tigen Variablen, falls diese noch nicht initialisiert wurden
		init(gameInfo,snake);
		if(UtilFunctions.getDistance(snake.headPosition(), altTargets[currentAltTarget]) < 2)
			currentAltTarget = ((++currentAltTarget)%4);
		if(firstRound)
		{
			firstRound = false;
			return Direction.RIGHT;
		}
		if(gameInfo.getPortals().isActive())
		{
			if(gameInfo.field().cell(snake.headPosition()).equals(CellType.PORTAL))
				passedPortal = true;
			if(gameInfo.field().cell(snake.segments().get(0)).equals(CellType.PORTAL))
				passedPortal = false;
		}
		else
			passedPortal = false;
		//Können wir Wände setzen?
		if(snake.getCanSetWall())
		{
			for (int i = -1; i <= 1; i += 2)
			{
				Point head = enemySnake.headPosition();
				if (head.x + i < 29 && head.x + i >= 1)
				{
					Point next = new Point(head.x +i,head.y);
					if(gameInfo.field().cell(next) == CellType.SPACE)
					{
						snake.setWall(next, Direction.RIGHT);
						break;
					}
				}
				if (head.y + i < 19 && head.y + i >= 1)		
				{
					Point next = new Point(head.x +i,head.y);
					if(gameInfo.field().cell(next) == CellType.SPACE)
					{
						snake.setWall(next, Direction.RIGHT);
						break;
					}
				}
			}
		}
		//Gibt es das Schlangentausch Feature und unsere Schlange ist min. 9 lang?
		if(eatable[2] != null && snake.segments().size() >= 9)
		{
			//Auf zum Sieg! Einrollen um den Schlangentausch
			if(UtilFunctions.getDistance(eatable[2], snake.headPosition()) == 1)
			{
				//Okay einkreisen von hier!
				Direction dir = UtilFunctions.getDirection(snake.headPosition(), eatable[2]);
				switch(dir)
				{
				case UP:
				case DOWN:
					if(isMoveValid(Direction.LEFT, snake, gameInfo))
						return Direction.LEFT;
					if(isMoveValid(Direction.RIGHT, snake, gameInfo))
						return Direction.RIGHT;
					return dir;
				case LEFT:
				case RIGHT:
					if(isMoveValid(Direction.UP, snake, gameInfo))
						return Direction.UP;
					if(isMoveValid(Direction.DOWN, snake, gameInfo))
						return Direction.DOWN;
					return dir;
				}
			}
			if(UtilFunctions.getDistance(eatable[2], snake.headPosition()) == 2)
			{
				//Okay einkreisen von hier!
				Direction dir = null;
				for (int i = -1; i <= 1; i += 2)
				{
					Point toCheck = new Point(snake.headPosition().x+i,snake.headPosition().y);
					if(UtilFunctions.getDistance(toCheck, eatable[2])==1)
					{
						dir = UtilFunctions.getDirection(snake.headPosition(), toCheck);
						if(isMoveValid(dir, snake, gameInfo))
							return dir;
					}
					toCheck = new Point(snake.headPosition().x,snake.headPosition().y+i);
					if(UtilFunctions.getDistance(toCheck, eatable[2])==1)
					{
						dir = UtilFunctions.getDirection(snake.headPosition(), toCheck);
						if(isMoveValid(dir, snake, gameInfo))
							return dir;
					}
				}
				
				switch(dir)
				{
				case UP:
				case DOWN:
					if(isMoveValid(Direction.LEFT, snake, gameInfo))
						return Direction.LEFT;
					if(isMoveValid(Direction.RIGHT, snake, gameInfo))
						return Direction.RIGHT;
					return dir;
				case LEFT:
				case RIGHT:
					if(isMoveValid(Direction.UP, snake, gameInfo))
						return Direction.UP;
					if(isMoveValid(Direction.DOWN, snake, gameInfo))
						return Direction.DOWN;
					return dir;
				}
			}
			//Berechne kuerzesten Weg zum Ziel
			Point targetPoint = null;
			foundTarget:for(int i=-1;i<2;i+=2)
			{
				for(int j=-1;j<2;j+=2)
				{
					targetPoint = new Point(eatable[2].x+i,eatable[2].y+i);
					if(gameInfo.field().cell(targetPoint).equals(CellType.SPACE))
						break foundTarget;
				}
			}
			if(targetPoint == null)
				targetPoint = eatable[2];
			Node path = finder.getMinPath(new TempSnake(snake), targetPoint,gameInfo.field(),gameInfo.getPortal());
			//Gibt es keinen Pfad dorthin?
			if(path != null)
			{	
				//Wir haben einen Pfad
				while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition()))
					path = path.getFrom();	
				hDirectionPath.clear();
				return UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());				
			}
			
		}
		if(snake.segments().size() > 15 && !passedPortal && gameInfo.getPortals().isActive())
		{
			Point[] portals = {gameInfo.getPortals().getPortal1(),gameInfo.getPortals().getPortal2()};
			for(int i=0;i<2;i++)
			{
				Node path = finder.getMinPath(new TempSnake(snake), portals[i],gameInfo.field(),gameInfo.getPortal());
				int dist = (path!= null?path.lengthToDest(snake.headPosition()):0);
				double TTL = gameInfo.getPortals().getTTL();
				if(path != null &&  TTL <= dist+9 && TTL >= dist+1)
				{	
					//Wir haben einen Pfad
					while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition()))
						path = path.getFrom();	
					hDirectionPath.clear();
					return UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());				
				}			
			}
		}
		if(eatable[0] != null && UtilFunctions.getDistance(mySnake.headPosition(),eatable[0]) <= 
				UtilFunctions.getDistance(enemySnake.headPosition(),eatable[0]))
		{
			//Wir sind n�her am Apfel!
			//Berechne kuerzesten Weg zum Ziel
			Node path = finder.getMinPath(new TempSnake(snake), eatable[0],gameInfo.field(),gameInfo.getPortal());
			//Gibt es keinen Pfad dorthin?
			if(path != null)
			{	
				//Wir haben einen Pfad
				while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition()))
					path = path.getFrom();	
				hDirectionPath.clear();
				return UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());				
			}
		}
		else
		{
			//Mist! Der Gegner ist n�her am Apfel. K�nnen wir die Schlangen tauschen? bevor er beim Apfel ist?
			if(eatable[2] != null && eatable[0] != null)
			{
				if(UtilFunctions.getDistance(enemySnake.headPosition(),eatable[0]) > UtilFunctions.getDistance(mySnake.headPosition(),eatable[2]))
				{
					//Jap! Dann lass uns da hin gehen
					//Berechne kuerzesten Weg zum Ziel
					Node path = finder.getMinPath(new TempSnake(snake), eatable[2],gameInfo.field(),gameInfo.getPortal());
					//Gibt es keinen Pfad dorthin?
					if(path != null)
					{	
						//Wir haben einen Pfad
						while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition()))
							path = path.getFrom();	
						hDirectionPath.clear();
						return UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());				
					}
				}
			}
		}
		//Wenn wir bis jetzt noch keinen Weg gefunden haben, sollten wir auf Zeit spielen:
		//K�nnen wir durch ein Portal unsere Schlange verk�rzen?
		if(!passedPortal && gameInfo.getPortals().isActive() )
		{
			Point[] portals = {gameInfo.getPortals().getPortal1(),gameInfo.getPortals().getPortal2()};
			for(int i=0;i<2;i++)
			{
				Node path = finder.getMinPath(new TempSnake(snake), portals[i],gameInfo.field(),gameInfo.getPortal());
				int dist = (path!= null?path.lengthToDest(snake.headPosition()):0);
				double TTL = gameInfo.getPortals().getTTL();
				if(path != null && dist+9 >= TTL && dist+1 <= TTL)
				{	
					//Wir haben einen Pfad
					while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition()))
						path = path.getFrom();	
					hDirectionPath.clear();
					return UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());				
				}			
			}
		}
		//Holen wir uns ein WallItem, falls wir noch keine setzen koennen
		if(eatable[1] != null && !snake.getCanSetWall())
		{
			Node path = finder.getMinPath(new TempSnake(snake), eatable[1],gameInfo.field(),gameInfo.getPortal());
			//Gibt es keinen Pfad dorthin?
			if(path != null)
			{	
				//Wir haben einen Pfad
				while(path.getFrom() != null && !path.getFrom().getActual().equals(snake.headPosition()))
					path = path.getFrom();	
				hDirectionPath.clear();
				return UtilFunctions.getDirection(path.getFrom().getActual(),path.getActual());				
			}
		}
		//K�nnen wir zu unserem AlternativZiel gehen?
		for(int i=0;i<4;i++)
		{
			if(!gameInfo.field().cell(altTargets[currentAltTarget]).equals(CellType.SNAKE) && 
					!gameInfo.field().cell(altTargets[currentAltTarget]).equals(CellType.WALL))
			{
				Node altWay = finder.getMinPath(new TempSnake(snake), altTargets[currentAltTarget],gameInfo.field(),gameInfo.getPortal());
				
				//Gibt es keinen Pfad dorthin?
				if(altWay != null)
				{	
					int currentAltTarget2 = ((currentAltTarget+1)%4);
					if(!gameInfo.field().cell(altTargets[currentAltTarget2]).equals(CellType.SNAKE) && 
							!gameInfo.field().cell(altTargets[currentAltTarget2]).equals(CellType.WALL))
					{
						Node altWay2 = finder.getMinPath(new TempSnake(snake), altTargets[currentAltTarget2],gameInfo.field(),gameInfo.getPortal());
						if(altWay2 != null)
						{
							//Wir haben einen Pfad
							while(altWay.getFrom() != null && !altWay.getFrom().getActual().equals(snake.headPosition()))
								altWay = altWay.getFrom();	
							hDirectionPath.clear();
							return UtilFunctions.getDirection(altWay.getFrom().getActual(),altWay.getActual());
						}
					}
				}
				else
					break;
			}
			currentAltTarget = ((++currentAltTarget)%4);
		}
		
		//Wahrscheinlich haben wir uns eingeschlossen! Berechne den k�rzesten Weg zum Schwanz
		if(hDirectionPath.isEmpty())
		{
			hFinder = new HamiltonPath();
			Node way = hFinder.getMaxPath(snake.headPosition(), gameInfo.field(), new TempSnake(snake), new TempSnake(enemySnake),gameInfo.getPortal());
//			System.out.println("First Direction: " + way.getActual() + " -> " + UtilFunctions.getDirection(way.getFrom().getActual(),way.getActual()));
			while(way != null && way.getFrom() != null && !way.getActual().equals(snake.headPosition()))
			{
				hDirectionPath.add(UtilFunctions.getDirection(way.getFrom().getActual(),way.getActual()));
				way = way.getFrom();
			}
//			System.out.println(Arrays.toString(hDirectionPath.toArray()));
//			System.out.println(way.getActual());
//			System.out.println(hDirectionPath.peek());
			if(!hDirectionPath.isEmpty())
				return hDirectionPath.pop();
		}
		else 
			return hDirectionPath.pop();
		Direction move = hPath.get(snake.headPosition());
		if(move!= null && isMoveValid(move, snake, gameInfo))
			return move;
		return randomMove(gameInfo, snake);
	}
	private void init(GameInfo info, Snake snake)
	{
		tempField = new Field(info.field().width(),info.field().height());
		for(int x=0;x<tempField.width();x++)
		{
			for(int y=0;y<tempField.height();y++)
			{
				Point p = new Point(x,y);
				tempField.setCell(info.field().cell(p), p);
			}
		}
		//A* berechnen
		//snake.move(direction)
		for(Point p : snake.segments())
			tempField.setCell(CellType.SNAKE, p);
		
		//initialisieren
		if(finder == null)
		{
			finder = new PathFinder();
			finder.ignorePortals = true;
		}
		if(hFinder == null)
			hFinder = new HamiltonPath();
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
		
		//Berechne HamiltonPath ueber das gesamte Feld
		if(hPointToIndex == null || hPath == null)
		{
			Node hamiltonPath = hFinder.getCompleteMaxPath(Field.defaultField(tempField.width(), tempField.height()));
			if(hamiltonPath != null)
			{
				hPointToIndex = new HashMap<>();
				hPath = new HashMap<>();
				int index = 0;
				Point first = hamiltonPath.getActual();
				Point last = null;
				while(hamiltonPath != null && hamiltonPath.getFrom() != null)
				{
					hPointToIndex.put(hamiltonPath.getActual(), index);
					hPath.put(hamiltonPath.getActual(), UtilFunctions.getDirection(hamiltonPath.getFrom().getActual(), hamiltonPath.getActual()));
					hamiltonPath = hamiltonPath.getFrom();
					if(hamiltonPath.getFrom() == null)
						last = hamiltonPath.getActual();
					index++;
				}
				if(last != null)
				{
					hPointToIndex.put(last, index);
					hPath.put(last, UtilFunctions.getDirection(first, last));
				}
			}
		}
	
		//Find all eatable Stuff
		getItems(info.field(),snake.headPosition());
	}
	private void getItems(Field f, Point snakeHead) {
		eatable = new Point[5];
		for(int x=0;x<f.width();x++)
			for(int y=0;y<f.height();y++)
			{
				Point p = new Point(x,y);
				if(f.cell(p).equals(CellType.APPLE))
					eatable[0] = p;
				if(f.cell(p).equals(CellType.FEATUREWALL))
					eatable[1] = p;
				if(f.cell(p).equals(CellType.CHANGESNAKE))
					eatable[2] = p;
				if(f.cell(p).equals(CellType.CHANGEHEADTAIL))
					eatable[3] = p;
				if(f.cell(p).equals(CellType.PORTAL))
					eatable[4] = p;
			}

	}

	//Calculate Valid Moves
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
		
		return gameInfo.field().cell(newHead) == CellType.SPACE || gameInfo.field().cell(newHead) == CellType.APPLE || gameInfo.field().cell(newHead) == CellType.PORTAL 
				|| gameInfo.field().cell(newHead) == CellType.CHANGESNAKE || gameInfo.field().cell(newHead) == CellType.CHANGEHEADTAIL || gameInfo.field().cell(newHead) == CellType.FEATUREWALL;
	}
	
	public static boolean isValidMovePossible(Snake snake, GameInfo gameInfo) {
		return isMoveValid(Direction.DOWN, snake, gameInfo) || isMoveValid(Direction.UP, snake, gameInfo) || isMoveValid(Direction.LEFT, snake, gameInfo) || isMoveValid(Direction.RIGHT, snake, gameInfo);
	}
	public Direction randomMove(GameInfo gameInfo, Snake snake) {
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
