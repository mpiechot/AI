package Util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import Logic.Field;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;

public class AlphaBeta {
	//Class variables
	private int MAXDEPTH;					//Max search depth
	private TempSnake mySnake;				//copy of my Snake - Max-Player
	private TempSnake enemySnake;			//copy of enemySnake - Min-Player
	private Point startingPoint;			//Start point of the calculations
	private Point saveApple;				//apple Position save (if another snake ate the apple)
	private Point[] eatable = new Point[7];	//array of eatable stuff
	
	private HashMap<Direction,Integer> directionScores = new HashMap<>();	//Map to get the score for every Direction
	private int bestScore;													//save the bestScore
	private Direction bestMove;												//save the best Direction
	
	//{ WIN LOOSE }
	private int[] evalSituation = {100000,-100000 };
	
	//Eatable Enum
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
	 * evaluate every direction.
	 * sets bestScore, bestMove, directionScores.
	 * @param field actual gamefield
	 * @param mySnake Max-Player
	 * @param enemySnake Min-Player
	 * @param searchDepth maxDepth
	 */
	public void alphaBeta(Field field, Snake mySnake, Snake enemySnake, int searchDepth,Point[] eatable)
	{
		//Init AlphaBeta
		this.MAXDEPTH = searchDepth;
		this.mySnake = new TempSnake(mySnake,"MYSNAKE");
		this.enemySnake = new TempSnake(enemySnake, "ENEMYSNAKE");
		this.eatable = eatable;
		startingPoint = new Point(mySnake.headPosition().x,mySnake.headPosition().y);
		
		//Init editable GameField
		Type[][] gameField = new Type[field.width()][field.height()];
		fillGameField(gameField,field);
		
		//clear last calculations
		directionScores.clear();
		
		//start calculating with Max-Player
		bestScore = max(MAXDEPTH,Integer.MIN_VALUE,Integer.MAX_VALUE,this.mySnake,this.enemySnake,gameField);
	}
	
	/**
	 * determine the next move for the max player and changes the field for this move.
	 * if we cant move, we evaluate the gamefield.
	 * @param depth	how many moves can we still make (Min and Max together)
	 * @param alpha	
	 * @param beta
	 * @param mySnake current MaxPlayers Snake
	 * @param enemySnake current MinPlayers Snake
	 * @param gameField	current ChangedGameField
	 * @return the score of the evaluated gameField.
	 */
	private int max(int depth, int alpha, int beta, TempSnake mySnake, TempSnake enemySnake,Type[][] gameField)
	{
		List<Direction> possibleMoves = getPossibleMoves(mySnake.headPosition(),gameField);		
		
		/*evaluate the gamefield if:
		*-we have no steps left (depth)
		*-we have no possible Moves
		*-a snake hit something
		*-maxPlayer reached the next Apple
		*/
		if(depth==0 || possibleMoves.isEmpty() || gameEnd(gameField,depth) || 
				(eatable[Items.APPLE.getIndex()] != null?mySnake.headPosition().equals(eatable[Items.APPLE.getIndex()]):false))
			return eval(gameField,mySnake.headPosition(),enemySnake.headPosition(),depth);
		
		int maxValue = alpha;
			
		for(Direction dir : possibleMoves)
		{
			TempSnake saveSnake = new TempSnake(mySnake);
			if(gameField[saveSnake.headPosition().x][saveSnake.headPosition().y] == Type.APPLE)
			{
				if(eatable[Items.APPLE.getIndex()] != null)
					saveApple = eatable[Items.APPLE.getIndex()];
				eatable[Items.APPLE.getIndex()] = null;
			}
			Type undo = makeMove(dir,gameField,saveSnake,true);
			int value = min(depth-1,maxValue,beta,saveSnake,enemySnake,gameField);
			undoMove(gameField,mySnake,saveSnake.headPosition(),true,undo);
			if(saveApple != null)
				eatable[Items.APPLE.getIndex()] = saveApple;
			if(depth == MAXDEPTH)
			{
//				System.out.println(dir + " : " + value);
				directionScores.put(dir, value);
			}
			if(value > maxValue)
			{
				maxValue = value;
				if(depth == MAXDEPTH)
				{
					bestMove = dir;
				}
				if(maxValue >= beta)
					break;
			}
		}
		return maxValue;
	}
	/**
	 * determine the next move for the min player and changes the field for this move.
	 * if we cant move, we evaluate the gamefield.
	 * @param depth	how many moves can we still make (Min and Max together)
	 * @param alpha	
	 * @param beta
	 * @param mySnake current MaxPlayers Snake
	 * @param enemySnake current MinPlayers Snake
	 * @param gameField	current ChangedGameField
	 * @return the score of the evaluated gameField.
	 */
	private int min(int depth, int alpha, int beta, TempSnake mySnake, TempSnake enemySnake,Type[][] gameField)
	{
		List<Direction> possibleMoves = getPossibleMoves(enemySnake.headPosition(),gameField);
		if(depth==0 || possibleMoves.isEmpty() || gameEnd(gameField,depth))
			return eval(gameField,mySnake.headPosition(),enemySnake.headPosition(),depth);
		int minValue = beta;
		for(Direction dir : possibleMoves)
		{
			TempSnake saveSnake = new TempSnake(enemySnake);
			if(gameField[saveSnake.headPosition().x][saveSnake.headPosition().y] == Type.APPLE)
			{
				if(eatable[Items.APPLE.getIndex()] != null)
					saveApple = eatable[Items.APPLE.getIndex()];
				eatable[Items.APPLE.getIndex()] = null;
			}
			Type undo = makeMove(dir,gameField,saveSnake,false);
			int value = max(depth-1,alpha,minValue,mySnake,saveSnake,gameField);
			undoMove(gameField,enemySnake,saveSnake.headPosition(),false,undo);
			if(saveApple != null)
				eatable[0] = saveApple;
			if(value < minValue)
			{
				minValue = value;
				if(minValue <= alpha)
					break;
			}
		}
		return minValue;
	}
	/**
	 * simulates a move for snake using the choosen direction dir
	 * @param dir which direction should snake move?
	 * @param gameField	current gameField
	 * @param snake	moving snake
	 * @param mySnake is it the maxPlayer?
	 * @return for undoing this move the function returns the changed Field Type
	 */
	private Type makeMove(Direction dir, Type[][] gameField, TempSnake snake, boolean mySnake) {
		
		Point oldTail = snake.segments().get(0);
		snake.move(dir);
		Point newHead = snake.headPosition();
		Type returnType = gameField[newHead.x][newHead.y];
		gameField[oldTail.x][oldTail.y] = Type.SPACE;
		if(gameField[newHead.x][newHead.y] == Type.APPLE)
			snake.grow(1);
		if(mySnake)
			switch(returnType)
			{
			case SPACE:
			case WALLFEATURE:
			case APPLE: gameField[newHead.x][newHead.y] = Type.MYSNAKE;break;
			case ENEMYSNAKE: gameField[newHead.x][newHead.y] = Type.MYSNAKEINSNAKE;break;
			case MYSNAKE: gameField[newHead.x][newHead.y] = Type.MYSNAKEINSNAKE;break;
			case WALL: gameField[newHead.x][newHead.y] = Type.MYSNAKEINWALL;break;
			default: 
			}
			
		else
			switch(gameField[newHead.x][newHead.y])
			{
			case SPACE:
			case WALLFEATURE:
			case APPLE: gameField[newHead.x][newHead.y] = Type.ENEMYSNAKE;break;
			case ENEMYSNAKE: gameField[newHead.x][newHead.y] = Type.ENEMYSNAKEINSNAKE;break;
			case MYSNAKE: gameField[newHead.x][newHead.y] = Type.ENEMYSNAKEINSNAKE;break;
			case WALL: gameField[newHead.x][newHead.y] = Type.ENEMYINWALL;break;
			default:
			}
		return returnType;
	}
	/**
	 * undo the last move for snake
	 * @param gameField current gamefield
	 * @param snake	- undo the move for this snake
	 * @param movedTo snake moved to this position
	 * @param mySnake is this the max-player?
	 * @param changed Field type, which was changed from the move-function
	 */
	private void undoMove(Type[][] gameField, TempSnake snake,Point movedTo, boolean mySnake, Type changed) {
		gameField[movedTo.x][movedTo.y] = changed;
		Point newTail = snake.segments().get(0);
		if(mySnake)
			gameField[newTail.x][newTail.y] = Type.MYSNAKE;
		else
			gameField[newTail.x][newTail.y] = Type.ENEMYSNAKE;
	}
	/**
	 * calculates all possible directions for the given Position.
	 * this will discard directions if they lead into the surrounding walls of the gamefield
	 * @param sH position of the snake head. calculate the directions from this position
	 * @param gameField actual gamefield
	 * @return list with all possible directions a snake can make depending on the given Position
	 */
	private List<Direction> getPossibleMoves(Point sH, Type[][] gameField) {
		List<Direction> possibleMoves = new LinkedList<>();
		for (int i = -1; i <= 1; i += 2) {
			if (sH.x + i < 29 && sH.x + i >= 1)
			{
				Point newPos = new Point(sH.x+i,sH.y);
				possibleMoves.add(UtilFunctions.getDirection(sH,newPos ));
			}
			if (sH.y + i < 19 && sH.y + i >= 1)
			{					
				Point newPos =  new Point(sH.x,sH.y+i);
				possibleMoves.add(UtilFunctions.getDirection(sH,newPos));
			}
		}
		return possibleMoves;
	}
	/**
	 * evaluate the current gamefield. How desirable is this situation for the max-player
	 * @param gameField actual gamefield
	 * @param myHead head of the Max-Player snake
	 * @param enemyHead head of the Min-Player snake
	 * @return the score of this game situation for the Max-Player
	 */
	private int eval(Type[][] gameField, Point myHead, Point enemyHead, int depth)
	{
		int score = 0;
		
		//evaluate Snake-length and gameScore of Max and Min Player in this situation
		score += mySnake.segments().size();
		score += (mySnake.getScore() - enemySnake.getScore());
		
		//reward fast points
		score += depth * 10;
		
		//evaluate distance to the apple
		if(eatable[Items.APPLE.getIndex()] != null && myHead.equals(eatable[Items.APPLE.getIndex()]))
			score += 100;
		if(eatable[Items.APPLE.getIndex()] != null)
			score += 200 - 2*UtilFunctions.getDistance(myHead, eatable[Items.APPLE.getIndex()]) 
			- UtilFunctions.getDistance(startingPoint, eatable[Items.APPLE.getIndex()]) ;
		score += 200 - 2*UtilFunctions.getDistance(startingPoint, enemyHead) - UtilFunctions.getDistance(myHead, enemyHead);
		//Add more features here!
		
		//evaluate if the Max-Player wins
		switch(gameField[enemyHead.x][enemyHead.y])
		{
		case ENEMYSNAKEINSNAKE: score+= 1*evalSituation[0];break;
		case ENEMYINWALL: score+= 1*evalSituation[0];break;
		default:
		}
		
		//Is the Min-Player in the surrounding walls of the gamefield?
		if(enemyHead.x == 0 || enemyHead.y == 0)
			score += 1*evalSituation[0];
		if(enemyHead.x == gameField.length-1 || enemyHead.y == gameField[0].length-1)
			score += 1*evalSituation[0];
		
		if(pointInSnake(mySnake,enemySnake.headPosition()))
			score+= 1*evalSituation[0];
		if(pointInSnake(enemySnake,enemySnake.headPosition()))
			score+= 1*evalSituation[0];
		
		
		//evaluate if the Max-Player loose
		switch(gameField[myHead.x][myHead.y])
		{
		case MYSNAKEINSNAKE: score+=1*evalSituation[1];break;
		case MYSNAKEINWALL: score+=1*evalSituation[1];break;
		default:
		}
		
		//Is the Max-Player in the surrounding walls of the gamefield?
		if(myHead.x == 0 || myHead.y == 0)
			score += 1*evalSituation[1];
		if(myHead.x == gameField.length-1 || myHead.y == gameField[0].length-1)
			score += 1*evalSituation[1];
		
		if(pointInSnake(enemySnake,mySnake.headPosition()))
			score+= 1*evalSituation[1];
		if(pointInSnake(mySnake,mySnake.headPosition()))
			score+= 1*evalSituation[1];
		return score;
	}
	/**
	 * determines if the game is over depending on the actual gameField.
	 * this is true if any snake hit a wall or the other snake.
	 * @param gameField actual gameField
	 * @return true if a snake hit something.
	 */
	private boolean gameEnd(Type[][] gameField, int depth)
	{
		for(int x=0;x<gameField.length;x++)
			for(int y=0;y<gameField[x].length;y++)
				switch(gameField[x][y])
				{
				case MYSNAKEINSNAKE: 
				case MYSNAKEINWALL:
				case ENEMYSNAKEINSNAKE: 
				case ENEMYINWALL:
					return true;
				default:
				}
		return false;
	}
	/**
	 * dient zur initialisierung des Spielfelds, mit dem simuliert wird
	 * @param gameField aktuelles Spielfeld(leer)
	 * @param field Spielfeld des tats�chlichen Spiels
	 */
	private void fillGameField(Type[][] gameField,Field field)
	{
		for(int x=0;x<gameField.length;x++)
			for(int y=0;y<gameField[x].length;y++)
			{
				Point point = new Point(x,y);
				switch(field.cell(point))
				{
				case SNAKE:
					if(mySnake.segments().contains(point))
						gameField[x][y] = Type.MYSNAKE;
					else
						gameField[x][y] = Type.ENEMYSNAKE;
					break;
				case WALL: gameField[x][y] = Type.WALL; break;
				case APPLE: gameField[x][y] = Type.APPLE; break;
				case SPACE: gameField[x][y] = Type.SPACE; break;
				case FEATUREWALL: gameField[x][y] = Type.WALLFEATURE; break;
				case CHANGEHEADTAIL:
					gameField[x][y] = Type.CHANGEHEADTAIL;
					break;
				case CHANGESNAKE:
					gameField[x][y] = Type.CHANGESNAKE;
					break;
				case CUTTAIL:
					gameField[x][y] = Type.CUTTAIL;
					break;
				case OPENFIELD:
					gameField[x][y] = Type.OPENFIELD;
					break;
				case OPENFIELDPICTURE:
					gameField[x][y] = Type.OPENFIELDPICTURE;
					break;
				case PORTAL:
					gameField[x][y] = Type.PORTAL;
					break;
				case SPEEDUP:
					gameField[x][y] = Type.SPEEDUP;
					break;
				default:
					break;
				}
			}
	}
	/**
	 * calculates if a snake hit the other snake using the head Position and the segments of the other snake
	 * @param snake check the segments of this snake
	 * @param head	check the head of this snake
	 * @return true if a snake hit the other
	 */
	private boolean pointInSnake(TempSnake snake, Point head)
	{
		for(int i=0;i<snake.segments().size()-1;i++)
			if(head.equals(snake.segments().get(i)))
				return true;
		return false;
	}
	/**
	 * gives the calculated bestScore or 0 if we havent calculated it already
	 * @return the highest score for the Max-Player
	 */
	public int getBestScore()
	{
		return bestScore;
	}
	/**
	 * gives the calculated scores for every direction the snake can move as a Map from Direction to Score(Integer)
	 * if we havent calculated the it the map is empty
	 * @return a map with the scores for every direction
	 */
	public HashMap<Direction, Integer> getDirectionScores() {
		return directionScores;
	}
	/**
	 * gives the bestMove or null if we havent calculated it already
	 * @return the best direction depending on bestScore for the current gameField.
	 */
	public Direction getBestMove() {
		return bestMove;
	}
	
}
