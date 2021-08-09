package ai.implementation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import ai.implementation.util.Grid;
import ai.implementation.util.GridNode;
import de.itdesign.codebattle.api.model.Base;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.FieldType;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

public class UnitMovement {
	private HashMap<UnitType,Integer> unitMaxResources = new HashMap<>();
	private PathFinder pathfinder;
	private List<Position> takenResourceTargets = new LinkedList<>();
	private HashMap<Unit,Position> unitTargetMap = new HashMap<>();
	public static HashMap<UnitType,List<Unit>>  enemyUnitPos = new HashMap<>();
	public static PriorityQueue<Unit> unitList = new PriorityQueue<>(new Comparator<Unit>(){

		@Override
		public int compare(Unit o1, Unit o2) {
			if(o1.getUnitType().equals(UnitType.COLLECTOR) && o2.getUnitType().equals(UnitType.COLLECTOR))
			{
				if(o1.getResourceCount() >= o2.getResourceCount())
					return -1;
				else
					return 1;
			}
			if(o1.getUnitType().equals(UnitType.COLLECTOR))
				return -1;
			if(o2.getUnitType().equals(UnitType.COLLECTOR))
				return 1;
			
			if(o1.getUnitType().equals(UnitType.WARRIOR) && o2.getUnitType().equals(UnitType.WARRIOR))
				return 0;
			if(o1.getUnitType().equals(UnitType.WARRIOR))
				return -1;
			if(o2.getUnitType().equals(UnitType.WARRIOR))
				return 1;				
			
			return 0;
		}
		
	});
	private ClientRoundState roundState;
	private int archerMaxDistance;
	public static int stayCounter = 0;
	private Position enemyBase;
	private int warriorMaxDistance;
	private Field[][] field;
	
	public UnitMovement(int collectorMaxResources, int warriorMaxResources, int archerMaxResources,int archerMaxDistance, int warriorMaxDistance) {
		unitMaxResources.put(UnitType.COLLECTOR, collectorMaxResources);
		unitMaxResources.put(UnitType.WARRIOR, warriorMaxResources);
		unitMaxResources.put(UnitType.ARCHER, archerMaxResources);
		this.archerMaxDistance = archerMaxDistance;
		this.warriorMaxDistance = warriorMaxDistance;
	}
	
	public void setUnitActions(ClientRoundState roundState)
	{	
		if(pathfinder == null)
			pathfinder = (roundState.getWidth()>30?new TraceAlgorithm():new AStarFinder());
		//reset staycounter if needed
		this.roundState = roundState;
		takenResourceTargets.clear();
		unitList.clear();
		unitList.addAll(roundState.getOwnUnits());
		unitTargetMap.clear();
		if(roundState.getRoundNumber()%2 == 0)
			stayCounter = 0;
		
		//set the map
		field = roundState.getMap();
		
		getEnemyPositions(roundState);
		PathFinder.blockedPositions.clear();
		
		UnitFactory.updateMyUnits(roundState);
		
//		MyCodeBattleClient.log.log("START MOVING");
		while(!unitList.isEmpty()) {
			Unit unit = unitList.remove();
			switch(unit.getUnitType())
			{
			case COLLECTOR: collectorMovement(unit); break;
			case WARRIOR: warriorMovement(unit); break;
			case ARCHER: archerMovement(unit); break;
			}
        }
	}
	private void collectorMovement(Unit collector)
	{
		//Get a target for this Unit
		Position target = (isUnitFull(collector)?roundState.getBase().getPosition():getResourcePositionSearch(collector.getPosition()));
		MyCodeBattleClient.log.log("Target: " + target);
		Direction nextStep = pathfinder.giveNextDirection(roundState, collector, target);
		Position pos = collector.getPosition().addDirection(nextStep, roundState.getWidth(),roundState.getHeight());

		//if there is congestion go back to base
		if(field[pos.getX()][pos.getY()].getUnitOnField() != null){
			Unit u = field[pos.getX()][pos.getY()].getUnitOnField();
			if(unitTargetMap.containsKey(u))
			{
				if(collector.getPosition().equals(unitTargetMap.get(u)))
				{
					int x = collector.getPosition().getX();
					int y = collector.getPosition().getY();
					if(nextStep == Direction.EAST || nextStep == Direction.WEST)
					{
						
						if(field[x+1][y].isCrossable(collector))
							nextStep = Direction.SOUTH; 
						if(field[x-1][y].isCrossable(collector))
							nextStep = Direction.NORTH; 
					}
					else{
						if(field[x][y+1].isCrossable(collector))
							nextStep = Direction.EAST; 
						if(field[x][y-1].isCrossable(collector))
							nextStep = Direction.WEST; 
						
					}
				}
			}
//			if(u.getOwner().equals(collector.getOwner())){
				MyCodeBattleClient.log.log("To Base cause congestion");
				nextStep = pathfinder.giveNextDirection(roundState, collector, roundState.getBase().getPosition());
//			}
		}
		if(collector.getPosition().equals(roundState.getBase().getPosition()))
		{
			Position base = roundState.getBase().getPosition();
			int x = base.getX();
			int y = base.getY();
			int blocked = 0;
			for(int i=-1;i<=1;i+=2)
			{
				if(field[x+i][y].getUnitOnField() != null)
					blocked++;
				if(field[x][y+i].getUnitOnField() != null)
					blocked++;
			}
			MyCodeBattleClient.log.log("To Base cause no space?: "+blocked);
			if(blocked > 1)
				nextStep = Direction.STAY;
		}
		unitTargetMap.put(collector, pos);
        // try to move, and if enemies block the way, fight them
        collector.moveAggressively(nextStep);
	}
	private void warriorMovement(Unit warrior)
	{
		Position target = getWarriorTarget(warrior);
		
		//change target if needed
		if(field[target.getX()][target.getY()].getUnitOnField() != null)
		{
			int distance = getDistance(warrior.getPosition(),target);
			if(distance > 5){
				target = enemyBase;
			}
			if(distance <= warriorMaxDistance)
			{
				warrior.attack(target);
				return;
			}
		}
		
		Direction nextStep = Direction.STAY;
		if(!target.equals(warrior.getPosition()))
			nextStep = pathfinder.giveNextDirection(roundState, warrior, target);
		Position pos = warrior.getPosition().addDirection(nextStep, roundState.getWidth(),roundState.getHeight());
		boolean stay = false;
		for (int i = -1; i <= 1; i += 2)
		{
			if (field[pos.getX()+i][pos.getY()].getUnitOnField() != null)
			{
				Unit u = field[pos.getX()+i][pos.getY()].getUnitOnField();
				if(!u.getOwner().equals(warrior.getOwner()) && u.getUnitType().equals(UnitType.WARRIOR))
				{
					stay = true;
					stayCounter++;
					break;
				}
			}
			if (field[pos.getX()][pos.getY()+i].getUnitOnField() != null)
			{
				Unit u = field[pos.getX()][pos.getY()+i].getUnitOnField();
				if(!u.getOwner().equals(warrior.getOwner()) && u.getUnitType().equals(UnitType.WARRIOR))
				{
					stay = true;
					stayCounter++;
					break;
				}
			}
		}
		if(stay)
			nextStep = Direction.STAY;
		if(field[pos.getX()][pos.getY()].getUnitOnField() != null)
		{
			Unit u = field[pos.getX()][pos.getY()].getUnitOnField();
			if(u.getOwner().equals(warrior.getOwner()))
			{
				nextStep = pathfinder.giveNextDirection(roundState, warrior, roundState.getBase().getPosition());
			}
		}
        // try to move, and if enemies block the way, fight them
        warrior.moveAggressively(nextStep);
	}
	private void archerMovement(Unit archer)
	{
		Position target = getArcherTarget(archer);
		if(field[target.getX()][target.getY()].getUnitOnField() != null && roundState.hasLineOfSight(archer.getPosition(), target))
		{
			int distance = getDistance(archer.getPosition(),target);
			if(distance < 2){
				target = roundState.getBase().getPosition();
			}
			else if(distance > 1 && distance <= archerMaxDistance)
			{
				archer.attack(target);
				return;
			}
		}
		Direction nextStep = Direction.STAY;
		if(!target.equals(archer.getPosition()))
			nextStep = pathfinder.giveNextDirection(roundState, archer, target);
		Position pos = archer.getPosition().addDirection(nextStep, roundState.getWidth(),roundState.getHeight());
		if(field[pos.getX()][pos.getY()].getUnitOnField() != null)
		{
			Unit u = field[pos.getX()][pos.getY()].getUnitOnField();
			if(u.getOwner().equals(archer.getOwner()))
			{
				nextStep = pathfinder.giveNextDirection(roundState, archer, roundState.getBase().getPosition());
			}
		}
        // try to move, and if enemies block the way, fight them
        archer.moveAggressively(nextStep);
	}
	public void getEnemyPositions(ClientRoundState roundState)
	{
		clearEnemyUnits();
		field = roundState.getMap();
		Set<Unit> myUnits = roundState.getOwnUnits();
		for(int x=0;x<field.length;x++)
		{
			for(int y=0;y<field[x].length;y++)
			{
				Unit u = field[x][y].getUnitOnField();
				if(u != null && !myUnits.contains(field[x][y].getUnitOnField()))
				{
					enemyUnitPos.get(u.getUnitType()).add(u);
				}
				if(enemyBase == null)
				{
					if(field[x][y].getType().equals(FieldType.BASE) && !roundState.getBase().getPosition().equals(Position.get(x, y))){
						enemyBase = Position.get(x, y);
					}
				}
			}
		}
	}
	private void clearEnemyUnits()
	{
		enemyUnitPos.clear();
		enemyUnitPos.put(UnitType.COLLECTOR, new LinkedList<>());
		enemyUnitPos.put(UnitType.WARRIOR, new LinkedList<>());
		enemyUnitPos.put(UnitType.ARCHER, new LinkedList<>());
	}
	private Position getCollectorPosition(Unit unit)
	{
		ClientRoundState roundState = MyCodeBattleClient.ROUNDSTATE;
		int distance = Integer.MAX_VALUE;
		Position collectorPos = null;
		for(Unit u : roundState.getOwnUnits())
		{
			if(u.getUnitType().equals(UnitType.COLLECTOR))
			{
				int dist = getDistance(unit.getPosition(), u.getPosition());
				if(dist < distance)
				{
					distance = dist;
					collectorPos = u.getPosition();
				}
			}
		}
		return collectorPos;
	}
	private Position getWarriorTarget(Unit unit)
	{
		if(!UnitFactory.noEnemys())
		{
			Position best = null;
			int minDistance = Integer.MAX_VALUE;
				
			List<Unit> warriors = enemyUnitPos.get(UnitType.WARRIOR);
			for(int i=0;i<warriors.size();i++)
			{
				Position next = warriors.get(i).getPosition();
				int distance = getDistance(unit.getPosition(),next);
				if(minDistance > distance)
				{
					best = next;
					minDistance = distance;
				}
			}
			List<Unit> collector = enemyUnitPos.get(UnitType.COLLECTOR);
			for(int i=0;i<collector.size();i++)
			{
				Position next = collector.get(i).getPosition();
				int distance = getDistance(unit.getPosition(),next);
				if(minDistance > distance)
				{
					best = next;
					minDistance = distance;
				}
			}
			List<Unit> archer = enemyUnitPos.get(UnitType.ARCHER);
			for(int i=0;i<archer.size();i++)
			{
				Position next = archer.get(i).getPosition();
				int distance = getDistance(unit.getPosition(),next);
				if(minDistance > distance)
				{
					best = next;
					minDistance = distance;
				}
			}
			if(best == null)
				return roundState.getBase().getPosition();

			return best;
		}
		else
		{
			if(UnitFactory.myUnits.get(UnitType.COLLECTOR).size() < 4)
			{
				if(isUnitFull(unit))
					return roundState.getBase().getPosition();
				else
					return getResourcePosition(unit.getPosition());
			}
			else
			{
				if(roundState.getRoundNumber() < 10)
					return getResourcePosition(unit.getPosition());
				else
					return roundState.getBase().getPosition();
			}
		}
	}
	private Position getArcherTarget(Unit unit)
	{
		if(!UnitFactory.noEnemys())
		{
			int minDistance = Integer.MAX_VALUE;
			Position bestTarget = enemyBase;
			List<Unit> archer = enemyUnitPos.get(UnitType.ARCHER);
			for(int i=0;i<archer.size();i++)
			{
				Position next = archer.get(i).getPosition();
				int distance = getDistance(next,unit.getPosition());
				if(minDistance > distance)
				{
					minDistance = distance;
					bestTarget = next;
				}
			}
			List<Unit> warrior = enemyUnitPos.get(UnitType.WARRIOR);
			for(int i=0;i<warrior.size();i++)
			{
				Position next = warrior.get(i).getPosition();
				int distance = getDistance(next,unit.getPosition());
				if(minDistance > distance)
				{
					minDistance = distance;
					bestTarget = next;
				}
			}
			List<Unit> collector = enemyUnitPos.get(UnitType.COLLECTOR);
			for(int i=0;i<collector.size();i++)
			{
				Position next = collector.get(i).getPosition();
				int distance = getDistance(next,unit.getPosition());
				if(minDistance > distance)
				{
					minDistance = distance;
					bestTarget = next;
				}
			}				
			return bestTarget;
		}
		else
		{
//			MyCodeBattleClient.log.log("Collectors: "+UnitFactory.myUnits.get(UnitType.COLLECTOR).size());
			if(UnitFactory.myUnits.get(UnitType.COLLECTOR).size() < 2)
			{
				if(isUnitFull(unit)){
					return roundState.getBase().getPosition();
				}
				else{
					return getResourcePosition(unit.getPosition());
				}
			}
			else
			{
//				MyCodeBattleClient.log.log("To Base, cause no enemys and enough collectors");
				return roundState.getBase().getPosition();
			}
		}
	}
    private Position getResourcePosition(Position unitPos) {
    	int distance = Integer.MAX_VALUE;
    	Position bestRes = null;
        for (int x = 0; x < roundState.getWidth(); x++) {
            for (int y = 0; y < roundState.getHeight(); y++) {
                Field field = roundState.getMap()[x][y];
                Position p = Position.get(x, y);
                int dist = getDistance(unitPos,p);
                if (field.getResourceCount() > 0 && distance > dist ) {
                	if(roundState.getWidth() > 25)
                	{
	                	if(!takenResourceTargets.contains(p))
	                	{
	                		 bestRes = p;
	                         distance = dist;
	                	}
                	}
                	else{
                     bestRes = p;
                     distance = dist;
                	}
                }
            }
        }
        takenResourceTargets.add(bestRes);
        return bestRes;
    }
    private Position getResourcePositionSearch(Position unitPos) {

    	Queue<GridNode> queue = new LinkedList<>();
    	Field[][] field = roundState.getMap();
    	Grid grid = new Grid(roundState.getWidth(),roundState.getHeight(),roundState.getMap());
    	
    	GridNode start = grid.getNodeAt(unitPos.getX(), unitPos.getY());
    	start.setClosed(true);
    	queue.add(start);
    	
    	while(!queue.isEmpty())
    	{
    		GridNode next = queue.poll();
    		int x = next.getPos().getX();
    		int y = next.getPos().getY();
    		if(field[x][y].getResourceCount() > 0){
//    			takenResourceTargets.add(bestRes);
    			return next.getPos();
    		}
    		List<GridNode> neighbors = grid.getNeighbors(next);
    		for(GridNode neighbor : neighbors)
    		{
    			if(!neighbor.isClosed())
    			{
	    			neighbor.setClosed(true);
	    			queue.add(neighbor);
    			}
    		}
    	}
    	return unitPos;
    }
    
    /**
     * @return true when the unit cannot collect any more resources. In that case they can be stored in your
     *         {@link Base}.
     */
    private boolean isUnitFull(Unit unit) {
        return unit.getResourceCount() == unitMaxResources.get(unit.getUnitType());
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
