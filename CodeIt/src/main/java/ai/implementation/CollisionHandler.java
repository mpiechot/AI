package ai.implementation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import ai.implementation.util.Grid;
import ai.implementation.util.GridNode;
import ai.implementation.util.MyUnit;
import ai.implementation.util.Pair;
import ai.implementation.util.Util;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.UnitType;

public class CollisionHandler {
	private ClientRoundState roundState;
	public static PriorityQueue<MyUnit> unitList = new PriorityQueue<>(new Comparator<MyUnit>(){

		@Override
		public int compare(MyUnit o1, MyUnit o2) {
			
			if(o1.getUnit().getUnitType().equals(UnitType.ARCHER) && o2.getUnit().getUnitType().equals(UnitType.ARCHER))
				return 0;
			if(o1.getUnit().getUnitType().equals(UnitType.ARCHER))
				return -1;
			if(o2.getUnit().getUnitType().equals(UnitType.ARCHER))
				return 1;
			if(o1.getUnit().getUnitType().equals(UnitType.WARRIOR) && o2.getUnit().getUnitType().equals(UnitType.WARRIOR))
				return 0;
			if(o1.getUnit().getUnitType().equals(UnitType.WARRIOR))
				return -1;
			if(o2.getUnit().getUnitType().equals(UnitType.WARRIOR))
				return 1;				
			if(o1.getUnit().getUnitType().equals(UnitType.COLLECTOR) && o2.getUnit().getUnitType().equals(UnitType.COLLECTOR))
			{
				if(o1.getUnit().getResourceCount() >= o2.getUnit().getResourceCount())
					return 1;
				else
					return -1;
			}
			if(o1.getUnit().getUnitType().equals(UnitType.COLLECTOR))
				return -1;
			if(o2.getUnit().getUnitType().equals(UnitType.COLLECTOR))
				return 1;
			
			return 0;
		}		
	});
	private HashMap<Position,List<MyUnit>> positionToMyUnit = new HashMap<>();
	public void performMoves()
	{
		roundState = MyCodeBattleClient.ROUNDSTATE;
		for(MyUnit u : Util.ALL_MYUNITS){
			if(positionToMyUnit.containsKey(u.getTarget())){
				positionToMyUnit.get(u.getTarget()).add(u);
			}
			else{
				List<MyUnit> list = new LinkedList<>();
				list.add(u);
				positionToMyUnit.put(u.getTarget(), list);
			}
		}
		List<Pair<String,List<MyUnit>>> collidingUnits = new LinkedList<>();
		for(Entry<Position,List<MyUnit>> entry : positionToMyUnit.entrySet()){
			if(entry.getValue().size() == 1){
				MyUnit u = entry.getValue().get(0);
				if(positionToMyUnit.containsKey(u.getUnit().getPosition())){
					List<MyUnit> collide = new LinkedList<>();
					collide.add(u);
					collide.addAll(positionToMyUnit.get(u.getUnit().getPosition()));
					Pair<String,List<MyUnit>> pair = new Pair<>("X",collide);
					collidingUnits.add(pair);
					continue;
				}
				if(u.isAttackTarget()){
					u.getUnit().attack(u.getTarget());
					continue;
				}
				else{
					u.getUnit().moveAggressively(u.getDirection());
					continue;
				}
			}
			else{
				if(entry.getKey().equals(Util.MY_BASE.getPosition())){
					for(MyUnit unit : entry.getValue()){
						unit.getUnit().moveAggressively(unit.getDirection());
					}
					continue;
				}
				List<MyUnit> collide = new LinkedList<>();
				collide.addAll(entry.getValue());
				Pair<String,List<MyUnit>> pair = new Pair<>("SAME",collide);
				collidingUnits.add(pair);
			}
		}
		
		//Print Output
		for(Pair<String,List<MyUnit>> colliders : collidingUnits){
			MyCodeBattleClient.log.log("Colliding because: " + colliders.key);
			MyUnit movingUnit = colliders.value.get(0);
			if(colliders.key.equals("SAME")){
				boolean canMove = true;
				for(MyUnit u : colliders.value){
					MyCodeBattleClient.log.log(u.getUnit().getPosition() +" has target: " + u.getTarget());
					if(canMove){
						u.getUnit().moveAggressively(u.getDirection());
						canMove = false;
					}
					else{
						u.setDirection(giveAlternativeDir(u));
						u.getUnit().moveAggressively(u.getDirection());
					}
				}
			}
			else{
				
				MyCodeBattleClient.log.log("SizeCollider: " + colliders.value.size());
				MyUnit unit1 = colliders.value.get(0);
				MyUnit unit2 = colliders.value.get(1);
				MyCodeBattleClient.log.log(unit1.getUnit().getPosition() +" has target: " + unit1.getTarget());
				MyCodeBattleClient.log.log(unit2.getUnit().getPosition() +" has target: " + unit2.getTarget());
				if(unit1.getPriority() > unit2.getPriority()){
					unit2.setDirection(giveAlternativeDir(unit2));
					unit2.getUnit().moveAggressively(unit2.getDirection());
					unit1.getUnit().moveAggressively(unit1.getDirection());
				}
				else{
					unit1.setDirection(giveAlternativeDir(unit2));
					unit1.getUnit().moveAggressively(unit2.getDirection());
					unit2.getUnit().moveAggressively(unit1.getDirection());					
				}
			}
			int possibleDirs = 15;
			boolean equalPrio = true;
			for(MyUnit u : colliders.value){
				if(movingUnit.getPriority() != u.getPriority()){
					equalPrio = false;
					break;
				}
			}
			if(equalPrio){
				
			}
		}
	}
	private Direction giveAlternativeDir(MyUnit unit){
		int x = unit.getUnit().getPosition().getX();
		int y = unit.getUnit().getPosition().getY();
		unit.getDisableDirs().add(unit.getDirection());
		List<Direction> disabled = unit.getDisableDirs();
		Direction min = Direction.STAY;
		for(Direction dir : Direction.values()){
			if(!disabled.contains(dir) && roundState.getMap()[x][y].isCrossable(unit.getUnit())){
				return dir;
			}
		}
		return min;
	}
}
