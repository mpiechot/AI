package Util;

import java.util.HashMap;

import Logic.Field;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;

/**
 * This Thread is for calculation AlphaBetaPruning on a given field for given snakes and given depth.
 * The result can be reached via getter-methods
 * @author Marco
 *
 */
public class AlphaBetaThread extends Thread{
	private AlphaBeta abSearch;
	private Field field;
	private Snake mySnake;
	private Snake enemySnake;
	private int searchDepth;
	private Point[] eatable;
		
	public AlphaBetaThread(Field field, Snake mySnake, Snake enemySnake, int searchDepth, Point[] eatable) {
		this.field = field;
		this.mySnake = mySnake;
		this.enemySnake = enemySnake;
		this.searchDepth = searchDepth;
		this.eatable = eatable;
		abSearch = new AlphaBeta();
	}
	public void run()
	{
		abSearch.alphaBeta(field, mySnake, enemySnake, searchDepth, eatable);
	}
	public Direction getBestDir()
	{
		return abSearch.getBestMove();
	}
	public HashMap<Direction,Integer> getScores()
	{
		return abSearch.getDirectionScores();
	}
}
