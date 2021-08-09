package ai.implementation.util;

import java.util.LinkedList;
import java.util.List;

import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;
/*
 * CODING TABLE:
 * Number	LandType	Owner
 * 0		LAND
 * 99		WALL
 * 98		WATER
 * 50		BASE		myAI
 * 51		BASE		enemyAI	
 * 			
 * Number	Unit		Owner
 * 1		COLLECTOR	myAI
 * 2		COLLECTOR	enemyAI
 * 3		WARRIOR		myAI
 * 4		WARRIOR		enemyAI
 * 5		ARCHER		myAI
 * 6		ARCHER		enemyAI
 */
public class GameState {
	private int[][] gameField;
	private List<Position>  myUnits = new LinkedList<>();
	private List<Position>  enemyUnits = new LinkedList<>();
	
	public GameState(int width, int height)
	{
		gameField = new int[width][height];
	}
	public GameState(ClientRoundState roundState)
	{
		Field[][] currentGame = roundState.getMap();
		String myAI = "";
		if(!roundState.getOwnUnits().isEmpty())
			myAI = roundState.getOwnUnits().iterator().next().getOwner();
		gameField = new int[currentGame.length][currentGame[0].length];
		for(int x=0;x<currentGame.length;x++)
		{
			for(int y=0;y<currentGame[x].length;y++){
				switch(currentGame[x][y].getType())
				{
				case WALL: gameField[x][y] = 99; break;
				case WATER: gameField[x][y] = 98; break;
				case BASE:
					if(roundState.getBase().getPosition().equals(Position.get(x, y)))
						gameField[x][y] = 50;
					else
						gameField[x][y] = 51; 
					break;
				case LAND:
					if(currentGame[x][y].getUnitOnField() != null)
					{
						Unit unit = currentGame[x][y].getUnitOnField();
						if(unit.getOwner().equals(myAI))
						{
							myUnits.add(unit.getPosition());
						}
						else
						{
							enemyUnits.add(unit.getPosition());
						}
						switch(unit.getUnitType()){
							case WARRIOR: gameField[x][y] = (unit.getOwner().equals(myAI)?3:4); break;
							case COLLECTOR:gameField[x][y] = (unit.getOwner().equals(myAI)?1:2); break;
							case ARCHER:gameField[x][y] = (unit.getOwner().equals(myAI)?5:6); break;
						}					
					}
					else
						gameField[x][y] = 0; 
					break;
				}
			}
		}
	}
	public void setFieldType(int x, int y, int type)
	{
		gameField[x][y] = type;
	}
//	public List<Position> generatePossibleMoves(boolean myTurn)
//	{
//		List<Position> myMoves = new LinkedList<>();
//		if(myTurn)
//		{
//			for(Position pos : myUnits)
//			{
//				switch(gameField[pos.getX()][pos.getY()])
//				{
//				case 0: myMoves.
//				default:
//				}
//				if(gameField[x][y])
//			}
//		}
//		
//	}
	public GameState clone()
	{
		int width = gameField.length;
		int height = gameField[0].length;
		GameState copy = new GameState(width,height);
		for(int x=0;x<gameField.length;x++)
			for(int y = 0;y<gameField[x].length;y++)
				copy.setFieldType(x,y,gameField[x][y]);
		
		return copy;
	}
}
