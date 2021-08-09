package ai.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import ai.implementation.util.Node;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

public class AStarFinder implements PathFinder{
	private PriorityQueue<Node> openList = new PriorityQueue<>((Node e1, Node e2) -> (int)(e1.getFCost()-e2.getFCost()));
	private List<Node> closedList = new LinkedList<>();
	private Position target;
	private int[][] costsMap;
	private List<Direction>  dirs = new ArrayList<>();
	private ClientRoundState roundState;
		
	//Constants
	private final boolean OPEN = true;
	private final boolean CLOSED = false;
	private static int WIDTH;
	private static int HEIGHT;
	public AStarFinder()
	{
		for (Direction dir : Direction.values())
			dirs.add(dir);
	}
		
	@Override
	public Direction giveNextDirection(ClientRoundState roundState, Unit unit, Position target) {
		if(unit.getPosition().equals(target))
			return Direction.STAY;
		
		this.roundState = roundState;
		WIDTH = roundState.getWidth();
		HEIGHT = roundState.getHeight();
		
		switch(unit.getUnitType())
		{
			case COLLECTOR: collectorMap(unit); break;
			case WARRIOR: calculateCostMap(unit); break;
			case ARCHER: calculateCostMap(unit); break;
		}

		openList.clear();
		closedList.clear();
		Field[][] gameField = roundState.getMap();
		
		this.target = unit.getPosition();
		openList.add(new Node(null,target,0,0));

		Node path = calculateAStar().getFrom();
		
		if(unit.getUnitType().equals(UnitType.COLLECTOR))
			blockedPositions.add(path.getActual());

		Direction def = null;
		for(Direction dir : Direction.values())
		{
			Position p = unit.getPosition().addDirection(dir, WIDTH, HEIGHT);
			if(p.equals(path.getActual()))
				return dir;
			if(gameField[p.getX()][p.getY()].isCrossable(unit))
				def = dir;
		}
		
		return def;
	}
	private void collectorMap(Unit unit)
	{
		costsMap = new int[roundState.getWidth()][roundState.getHeight()];
		Field[][] map = roundState.getMap();
		for(int x = 0;x < costsMap.length;x++)
		{
			for(int y =0;y<costsMap[x].length;y++)
			{
				int score = 0;
				Position p = Position.get(x, y);
				if(blockedPositions.contains(p))
					score += 999;
				switch(map[x][y].getType())
				{
					case LAND:
					{
						score += 1;
						if(map[x][y].getUnitOnField() != null){
							Unit u = map[x][y].getUnitOnField();
							if(u.getOwner().equals(unit.getOwner())){
								score += 1;
							}
						}
						break;
					}
					case BASE:
					{
						if(!roundState.getBase().getPosition().equals(Position.get(x, y))){
							score += 999;
						}
						else{
							score += 1;
						}
						break;
					}
					case WALL:
					case WATER:
					{
						score = considerWaterType(score);
						break;
					}
				}
				costsMap[x][y] = score;
			}
		}
		avoidPositionsNextToEnemyWarriors();
	}
	private Node calculateAStar()
	{
		Node min = null;
		//Wenn das Ziel in der ClosedList ist oder die OpenList leer ist, sind wir fertig!
		while (!isInList(CLOSED,target) && !openList.isEmpty()) {
			min = openList.remove();
			closedList.add(min);
			Position current = min.getActual();
			Collections.reverse(dirs);
			for (Direction dir : dirs)
			{
				Position next = current.addDirection(dir, WIDTH, HEIGHT);
				updateOpenList(next, min);
			}
		}
		closedList.remove(0);
		return min;
	}
	/** 
	 * add a new point to open list or replace it, if:
	 * - point is not in closed list
	 * - point is not in open list or costs of the new node are less than costs of another node
	 * @param check - particular neighbor point that is supposed to be added
	 * @param node - current Node
	 */
	private void updateOpenList(Position check, Node node) 
	{
		//Ist der Neue Punkt bereits in der ClosedList?
		if(isInList(CLOSED,check))
			return;
		
		//Berechne die GCosts fuer den neuen Punkt
		int costs = node.getGCosts() + costsMap[check.getX()][check.getY()];
		
		//Gibt es bereits einen besseren Weg zu dem neuen Punkt?
		if (isInList(OPEN,check) && costs >= getElemFromOpenList(check).getGCosts())
			return;
		
		//Erstelle den neuen Node
		Node checkNode = new Node(node,check,getDistance(check,target),costs);
		//Falls es einen schlechteren Node gab, loesche diesen
		if(isInList(OPEN,check))
			openList.remove(getElemFromOpenList(check));

		//Fuege den neuen/besseren Node der OpenList hinzu
		openList.add(checkNode);
	}
	
	/**
	 * calculates the cost to move the unit from its position to everywhere else
	 * depending on distance, fieldtype or other units.
	 * @param unit
	 */
	private void calculateCostMap(Unit unit)
	{
		costsMap = new int[roundState.getWidth()][roundState.getHeight()];
		Field[][] map = roundState.getMap();
		
		calculateMapCosts(unit, map);
		
		avoidPositionsNextToEnemyWarriors();
	}

	/**
	 * raises the costs on Fields around Enemy Warriors so they cant attack our units
	 */
	private void avoidPositionsNextToEnemyWarriors() {
		for(Unit enemy : UnitMovement.enemyUnitPos.get(UnitType.WARRIOR))
		{
			for (int i = -1; i <= 1; i += 2)
			{
				costsMap[enemy.getPosition().getX()+i][enemy.getPosition().getY()] = 999;
				costsMap[enemy.getPosition().getX()][enemy.getPosition().getY()+i] = 999;
			}
		}
	}
	/**
	 * calculates the costs depending on field types of each position of the map
	 * @param unit
	 * @param map
	 */
	private void calculateMapCosts(Unit unit, Field[][] map) {
		for(int x = 0;x < costsMap.length;x++)
		{
			for(int y =0;y<costsMap[x].length;y++)
			{
				int score = 0;
				Position p = Position.get(x, y);
				if(blockedPositions.contains(p))
					score += 10;
				switch(map[x][y].getType())
				{
				case LAND: score = considerLandType(unit, map, x, y, score);break;
				case BASE: score = considerBaseType(x, y, score); break;
				case WALL: score = considerWallType(score); break;
				case WATER: score = considerWaterType(score); break;
				}
				costsMap[x][y] = score;
			}
		}
	}
	/**
	 * calculate the score on water-type fields
	 * @param score
	 * @return
	 */
	private int considerWaterType(int score) {
		score += 99999;
		return score;
	}
	/**
	 * calculate the score on wall-type fields
	 * @param score
	 * @return
	 */
	private int considerWallType(int score) {
		score += 9999;
		return score;
	}
	/**
	 * calculate the score on base-type fields
	 * @param score
	 * @return
	 */
	private int considerBaseType(int x, int y, int score) {
		if(!roundState.getBase().getPosition().equals(Position.get(x, y)))
			score += 999;
		else
			score += 1;
		return score;
	}

	/**
	 * calculate the score on land-type fields
	 * @param unit
	 * @param map
	 * @param x
	 * @param y
	 * @param score
	 * @return
	 */
	private int considerLandType(Unit unit, Field[][] map, int x, int y, int score) {
		score += 1;			//a move on Land fields costs 1
		if(map[x][y].getUnitOnField() != null)
		{
			score = considerUnitsOnField(unit, map, x, y, score);
		}
		else
		{
			score = considerUnitsOnSurroundingFields(unit, map, x, y, score);
		}
		return score;
	}
	/**
	 * if there is no Unit on this field we shoul check if there are units on surrounding fields
	 * @param unit
	 * @param map
	 * @param x
	 * @param y
	 * @param score
	 * @return
	 */
	private int considerUnitsOnSurroundingFields(Unit unit, Field[][] map, int x, int y, int score) {
		for (int i = -1; i <= 1; i += 2)
		{
			if (map[x+i][y].getUnitOnField() != null)
			{
				Unit u = map[x+i][y].getUnitOnField();
				if(!u.getOwner().equals(unit.getOwner()) && !u.getUnitType().equals(UnitType.COLLECTOR))
				{
					score += 60;														
				}
			}
			if (map[x][y+i].getUnitOnField() != null)
			{
				Unit u = map[x][y+i].getUnitOnField();
				if(!u.getOwner().equals(unit.getOwner()) && !u.getUnitType().equals(UnitType.COLLECTOR))
				{
					score += 60;													
				}
			}
		}
		return score;
	}

	private int considerUnitsOnField(Unit unit, Field[][] map, int x, int y, int score) {
		Unit u = map[x][y].getUnitOnField();
		if(u.getOwner().equals(unit.getOwner()))
		{
			score += 20;
		}
		else
		{
			if(!unit.getUnitType().equals(UnitType.WARRIOR))
			{
				if(!u.getUnitType().equals(UnitType.COLLECTOR))
					score += 60;
				else
					score += 2;
			}															
		}
		return score;
	}
	/**
	 * determines if the position target is inside the open or closed list
	 * @param open - if true -> search inside the openlist false -> search inside the closedList
	 * @param target - the position to check if it is inside the list
	 * @return true if position is inside the list otherwise false
	 */
	private boolean isInList(boolean open, Position target) 
	{
		for (Node node : (open?openList:closedList))
		{
			if (node.getActual().equals(target))
				return true;
		}
		return false;
	}
	/**
	 * finds the element of open list that contains a specific point
	 * @param p - specific point 
	 * @return - element of open list that contains p
	 */
	private Node getElemFromOpenList(Position p) 
	{
		for (Node open : openList)
		{
			if (open.getActual().equals(p))
				return open;
		}
		return null;
	}
	
	/**
	 * computes the Manhattan distance of two points
	 * @param a - first point
	 * @param b - second point
	 * @return Manhattan distance
	 */
	private int getDistance(Position a, Position b) 
	{
		return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
	}
}
