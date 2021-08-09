package ai.implementation;

import java.util.List;
import java.util.PriorityQueue;

import ai.implementation.util.Grid;
import ai.implementation.util.GridNode;
import ai.implementation.util.TraceUtil;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

public class TraceAlgorithm implements PathFinder {
	private PriorityQueue<GridNode> openList = new PriorityQueue<>((GridNode e1, GridNode e2) -> (int)(e1.getfCosts()-e2.getfCosts()));
	private Grid grid;
	private int[][] costsMap;
	private ClientRoundState roundState;
	@Override
	public Direction giveNextDirection(ClientRoundState roundState, Unit unit, Position target) {
		this.roundState = roundState;
		switch(unit.getUnitType())
		{
			case COLLECTOR: collectorMap(unit); break;
			case WARRIOR: calculateCostMap(unit); break;
			case ARCHER: calculateCostMap(unit); break;
		}
		List<GridNode> path = getPath(roundState, unit, target);
//		MyCodeBattleClient.log.log("From: "+unit.getPosition() + " To: " + target);
//		for(GridNode node : path)
//		{
//			MyCodeBattleClient.log.log("("+node.getPos()+")");
//		}
		Position next = null;
		if(path.size() > 1)
			next = path.get(1).getPos();
		else
			next = target;
		Position now = unit.getPosition();
		int xNow = now.getX();
		int yNow = now.getY();
		int xNext = next.getX();
		int yNext = next.getY();
		Direction dir = Direction.STAY;
		if(xNext != xNow){
			dir = (xNext > xNow? Direction.EAST: Direction.WEST);
		}
		else if(yNext != yNow){
			dir = (yNext > yNow? Direction.SOUTH: Direction.NORTH);			
		}
//		MyCodeBattleClient.log.log("First Pos: " + path.get(0).getPos() + "Direction: " +dir);
		
		return dir;
	}
	private List<GridNode> getPath(ClientRoundState roundState, Unit unit, Position target)
	{
		openList.clear();
		grid = new Grid(roundState.getWidth(),roundState.getHeight(),roundState.getMap());
		GridNode startNode = grid.getNodeAt(unit.getPosition().getX(), unit.getPosition().getY());
		GridNode endNode = grid.getNodeAt(target.getX(), target.getY());
		
		startNode.setfCosts(0);
		startNode.setgCosts(0);
		
		openList.add(startNode);
		startNode.setOpened(true);
		
		while(!openList.isEmpty())
		{
			GridNode node = openList.remove();
			node.setClosed(true);
			
			if(node.equals(endNode))
				return TraceUtil.backtrack(endNode);
			
			List<GridNode> neighbors = grid.getNeighbors(node);
			for(GridNode neighbor : neighbors)
			{
				if(neighbor.isClosed()){
					continue;
				}
				Position neighPos = neighbor.getPos();
				int x = neighPos.getX();
				int y = neighPos.getY();
				
				double gScore = node.getgCosts() + costsMap[x][y];
				
				if(!neighbor.isOpened() || gScore < neighbor.getgCosts()){
					neighbor.setgCosts(gScore);
					neighbor.sethCosts(neighPos.getDistance(target));
//					neighbor.setnCosts(grid.getNeighbors(neighbor).size() / 4);
					neighbor.setnCosts(1);
					neighbor.setfCosts(neighbor.getgCosts() * neighbor.getnCosts() + neighbor.gethCosts());
					neighbor.setParent(node);
					
					if(!neighbor.isClosed()){
						openList.add(neighbor);
						neighbor.setOpened(true);
					}
				}
				
			}
		}
		return null;
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
						score+= 9999;
						break;
					}
				}
				costsMap[x][y] = score;
			}
		}
		for(Unit enemy : UnitMovement.enemyUnitPos.get(UnitType.WARRIOR))
		{
			for (int i = -1; i <= 1; i += 2)
			{
				costsMap[enemy.getPosition().getX()+i][enemy.getPosition().getY()] = 999;
				costsMap[enemy.getPosition().getX()][enemy.getPosition().getY()+i] = 999;
			}
		}
	}
	private void calculateCostMap(Unit unit)
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
					score += 10;
				switch(map[x][y].getType())
				{
				case LAND:
				{
					score += 1;
					if(map[x][y].getUnitOnField() != null)
					{
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
					}
					else
					{
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
					}
					break;
				}
				case BASE:
				{
					if(!roundState.getBase().getPosition().equals(Position.get(x, y)))
						score += 999;
					else
						score += 1;
					break;
				}
				case WALL:
				{
					score += 9999;
					break;
				}
				case WATER:
				{
					score+= 9999;
					break;
				}
				}
				costsMap[x][y] = score;
			}
		}
		for(Unit enemy : UnitMovement.enemyUnitPos.get(UnitType.WARRIOR))
		{
			for (int i = -1; i <= 1; i += 2)
			{
				costsMap[enemy.getPosition().getX()+i][enemy.getPosition().getY()] = 999;
				costsMap[enemy.getPosition().getX()][enemy.getPosition().getY()+i] = 999;
			}
		}
	}
}
