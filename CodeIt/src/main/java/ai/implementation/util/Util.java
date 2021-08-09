package ai.implementation.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ai.implementation.MyCodeBattleClient;
import de.itdesign.codebattle.api.model.Base;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.FieldType;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

public class Util {
	public static HashMap<UnitType,Integer> UNITTYPE_TO_MAXRESOURCES = new HashMap<>();
	public static HashMap<UnitType,Integer> UNITTYPE_TO_MAXDISTANCE= new HashMap<>();
	public static HashMap<UnitType,Integer> UNITTYPE_TO_COSTS= new HashMap<>();
	public static HashMap<UnitType, List<MyUnit>> UNITTYPE_TO_MYUNITLIST = new HashMap<>();
	public static HashMap<UnitType,List<MyUnit>>  UNITTYPE_TO_ENEMYLIST = new HashMap<>();
	public static List<MyUnit> ALL_MYUNITS = new LinkedList<>();
	public static List<MyUnit> ALL_ENEMYUNITS = new LinkedList<>();
	public static Base MY_BASE;
	public static Position ENEMY_BASE;
	
	public static void clearMyUnits()
	{
		UNITTYPE_TO_MYUNITLIST.clear();
		ALL_MYUNITS.clear();
		UNITTYPE_TO_MYUNITLIST.put(UnitType.COLLECTOR, new LinkedList<>());
		UNITTYPE_TO_MYUNITLIST.put(UnitType.WARRIOR, new LinkedList<>());
		UNITTYPE_TO_MYUNITLIST.put(UnitType.ARCHER, new LinkedList<>());
	}
	public static void getEnemyPositions()
	{
		ClientRoundState roundState = MyCodeBattleClient.ROUNDSTATE;
		clearEnemyUnits();
		Field[][] field = roundState.getMap();
		Set<Unit> myUnits = roundState.getOwnUnits();
		for(int x=0;x<field.length;x++)
		{
			for(int y=0;y<field[x].length;y++)
			{
				Unit u = field[x][y].getUnitOnField();
				if(u != null && !myUnits.contains(field[x][y].getUnitOnField()))
				{
					MyUnit mu = new MyUnit(u);
					ALL_ENEMYUNITS.add(mu);
					UNITTYPE_TO_ENEMYLIST.get(u.getUnitType()).add(mu);
				}
				if(ENEMY_BASE == null)
				{
					if(field[x][y].getType().equals(FieldType.BASE) && !MY_BASE.getPosition().equals(Position.get(x, y))){
						ENEMY_BASE = Position.get(x, y);
					}
				}
			}
		}
	}
	private static void clearEnemyUnits()
	{
		UNITTYPE_TO_ENEMYLIST.clear();
		UNITTYPE_TO_ENEMYLIST.put(UnitType.COLLECTOR, new LinkedList<>());
		UNITTYPE_TO_ENEMYLIST.put(UnitType.WARRIOR, new LinkedList<>());
		UNITTYPE_TO_ENEMYLIST.put(UnitType.ARCHER, new LinkedList<>());
		ALL_ENEMYUNITS.clear();
	}
}
