package ai.implementation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ai.implementation.util.Util;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.FieldType;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

public class UnitFactory{
	private int collectorCost;
	private int warriorCost;
	private int archerCost;
	private final int MAX_UNITS;
	private static final int MAX_COLLECTORS = 5;
	private static final int MAX_WARRIORS = 3;
	private static final int MAX_ARCHER = 5;
	private static int FIELDS = 0;
//	private int collectorCount;
//	private int warriorCount;
//	private int archerCount;
	private int collectedResources;
	private HashMap<UnitType,Integer> unitCosts = new HashMap<>();
	public static HashMap<UnitType, List<Unit>> myUnits = new HashMap<>();
	private UnitType[] unitTypes = new UnitType[3];
	private int ownUnitCount;
	

	public UnitFactory(int collectorCost, int warriorCost, int archerCost, int width) {
		unitCosts.put(UnitType.COLLECTOR, collectorCost);
		unitCosts.put(UnitType.WARRIOR, warriorCost);
		unitCosts.put(UnitType.ARCHER, archerCost);
		this.collectorCost = collectorCost;
		this.warriorCost = warriorCost;
		this.archerCost = archerCost;
		unitTypes[0] = UnitType.COLLECTOR;
		unitTypes[1] = UnitType.ARCHER;
		unitTypes[2] = UnitType.WARRIOR;
		MAX_UNITS = (width >= 35? 25:20);
	}
	
	public void createUnits(ClientRoundState roundState)
	{
		if(FIELDS == 0)
			FIELDS =  roundState.getWidth()*roundState.getHeight();
		collectedResources = roundState.getBase().getStoredResources();
        ownUnitCount = roundState.getOwnUnits().size();
        if(ownUnitCount < MAX_UNITS)
        {
        	updateMyUnits(roundState);	        

//	        MyCodeBattleClient.log.log("Create Units if possible: " + (ownUnitCount < MAX_UNITS) +" && "+ (collectedResources >= collectorCost));
			while(ownUnitCount < MAX_UNITS && collectedResources >= collectorCost)
			{
//				MyCodeBattleClient.log.log("Create Units if possible: " + (ownUnitCount < MAX_UNITS) +" && "+ (collectedResources >= collectorCost));
				boolean created = false;
				for(UnitType type : unitTypes)
				{
					double score = getScore(type, roundState);
					double space = getSpace(roundState);
					if(space/FIELDS < 0.4)
						score -= 100;
					MyCodeBattleClient.log.log(type + " has score: "+ score + " type costs: " + unitCosts.get(type) + " we have: " + collectedResources);
					if(score > 500 && collectedResources >= unitCosts.get(type))
					{
						MyCodeBattleClient.log.log("Create: "+type);
						roundState.getBase().createUnit(type);
				        collectedResources -= unitCosts.get(type);
				        ownUnitCount++;
				        created = true;
					}
				}
				if(!created)
				{
					break;
				}
			}
        }
	}
	private static void clearMyUnits()
	{
		myUnits.clear();
		myUnits.put(UnitType.COLLECTOR, new LinkedList<>());
		myUnits.put(UnitType.WARRIOR, new LinkedList<>());
		myUnits.put(UnitType.ARCHER, new LinkedList<>());
	}
	private double getSpace(ClientRoundState roundState)
	{
		double space = FIELDS;
		Field[][] field = roundState.getMap();
		for(int x=0;x<field.length;x++)
		{
			for(int y=0;y<field[x].length;y++)
			{
				if(!field[x][y].getType().equals(FieldType.LAND) || field[x][y].getUnitOnField() != null)
					space--;
			}
		}
		return space;
	}
	private double getScore(UnitType type, ClientRoundState roundState)
	{
		double score = 0;
		switch(type)
		{
		case WARRIOR:MyCodeBattleClient.log.log("Warrior:");
			if(UnitMovement.enemyUnitPos.size() >0)
			{
				score -= UnitMovement.stayCounter*150;
				MyCodeBattleClient.log.log("Stay: " + score);
				if(myUnits.get(UnitType.WARRIOR).size() < UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size()){
					if(myUnits.get(UnitType.WARRIOR).size()<=4)
						score += 300;
				}
				else{
					score -= 200;
				}
				MyCodeBattleClient.log.log("Warriors count: " + score);
				if(noEnemys())
					score -= 300;
				MyCodeBattleClient.log.log("No Enemys?: " + score);
				if(!UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).isEmpty())
					score += UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).size()*80;
				MyCodeBattleClient.log.log("Collector?: " + score);
				if(!UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty())
					score -= UnitMovement.enemyUnitPos.get(UnitType.ARCHER).size()*170;
				MyCodeBattleClient.log.log("Archer?: " + score);
			}
			break;
		case ARCHER:
			if(UnitMovement.enemyUnitPos.size() >0)
			{
				score += UnitMovement.stayCounter*150;
				if(myUnits.get(UnitType.WARRIOR).size() < UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size())
					score -= 100;
				else
					score += 300;
				if(noEnemys())
					score -= 300;
				if(!UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).isEmpty())
					score += UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).size()*30;
				if(!UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).isEmpty())
					score += UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size()*208;
				if(!UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty())
					score += UnitMovement.enemyUnitPos.get(UnitType.ARCHER).size()*310;
			}
			break;
		case COLLECTOR:MyCodeBattleClient.log.log("Collector:");
			if(noEnemys())
				score += 700;
			else if(ownUnitCount > 12)
				score -= 30;
			MyCodeBattleClient.log.log("No Enemys: " + score);
			if(myUnits.get(UnitType.COLLECTOR).size() < UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).size())
				score += 591;
			MyCodeBattleClient.log.log("Collector count: " + score);
			if(!UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).isEmpty())
				score -= UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size()*90;
			MyCodeBattleClient.log.log("Warriors count: " + score);
			if(!UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty())
				score -= UnitMovement.enemyUnitPos.get(UnitType.ARCHER).size()*100;
			MyCodeBattleClient.log.log("Archer count: " + score);
			if(myUnits.get(UnitType.COLLECTOR).size() < 4)
				score += 591;
			MyCodeBattleClient.log.log("Less than 4: " + score);
			break;
		}
		return score;
	}
	public static void updateMyUnits(ClientRoundState roundState)
	{
		clearMyUnits();
        for(Unit unit : roundState.getOwnUnits())
        {
    		myUnits.get(unit.getUnitType()).add(unit);
        }
	}
	public static boolean noEnemys()
	{
		return UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).isEmpty() && UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).isEmpty() && UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty();
	}
}
