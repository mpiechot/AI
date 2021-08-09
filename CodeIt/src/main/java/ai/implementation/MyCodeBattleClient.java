package ai.implementation;

import ai.implementation.util.MyUnit;
import ai.implementation.util.Util;
import de.itdesign.codebattle.api.codeinterface.CodeBattleClientImpl;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Unit;
import de.itdesign.codebattle.api.model.UnitType;

/**
 * The {@link CodeBattleClientImpl} is the API for communicating with the game server.<br>
 * Use the {@link #log(String)} method to log something.
 * 
 * @author <i>Your name here</i>
 */
public class MyCodeBattleClient extends CodeBattleClientImpl {

    private UnitFactory unitFactory;
    private UnitMovement unitMovement;
    private BaseBuilder baseBuilder;
    private CollisionHandler collisionHandler;
    public static ClientRoundState ROUNDSTATE;
    public static Logger log;

    @Override
    protected void processRound(ClientRoundState roundState) {
    	long processStartTime = System.currentTimeMillis();
    	ROUNDSTATE = roundState;
    	init();

        unitFactory.createUnits(roundState);
        
        baseBuilder.checkBaseBuilding(roundState);

        unitMovement.setUnitActions(roundState);

//        collisionHandler.performMoves();
        
        long processEndTime = System.currentTimeMillis();
        log("Processed round " + roundState.getRoundNumber() + " in " + (processEndTime - processStartTime) + " ms");
    }
    private void init()
    {
    	if(log == null)
    		log = new Logger(this);
    	if(unitFactory == null)
    		unitFactory = new UnitFactory(getGameConfiguration().getUnitCost(UnitType.COLLECTOR),
    				getGameConfiguration().getUnitCost(UnitType.WARRIOR),
    				getGameConfiguration().getUnitCost(UnitType.ARCHER),
    				ROUNDSTATE.getWidth());
    	if(unitMovement == null){
    		unitMovement = new UnitMovement(getGameConfiguration().getUnitMaxResources(UnitType.COLLECTOR),
    				getGameConfiguration().getUnitMaxResources(UnitType.WARRIOR),
    				getGameConfiguration().getUnitMaxResources(UnitType.ARCHER),
    				getGameConfiguration().getUnitAttackDistance(UnitType.ARCHER),
    				getGameConfiguration().getUnitAttackDistance(UnitType.WARRIOR));
    	}
//    	if(collisionHandler == null){
//    		collisionHandler = new CollisionHandler();
//    	}
    	if(baseBuilder == null){
    		baseBuilder = new BaseBuilder();
    	}
    	if(Util.UNITTYPE_TO_MAXRESOURCES.isEmpty()){
    		Util.UNITTYPE_TO_MAXRESOURCES.put(UnitType.COLLECTOR, getGameConfiguration().getUnitMaxResources(UnitType.COLLECTOR));
    		Util.UNITTYPE_TO_MAXRESOURCES.put(UnitType.WARRIOR, getGameConfiguration().getUnitMaxResources(UnitType.WARRIOR));
    		Util.UNITTYPE_TO_MAXRESOURCES.put(UnitType.ARCHER, getGameConfiguration().getUnitMaxResources(UnitType.ARCHER));
    	}
    	if(Util.UNITTYPE_TO_MAXDISTANCE.isEmpty()){
    		Util.UNITTYPE_TO_MAXDISTANCE.put(UnitType.COLLECTOR, getGameConfiguration().getUnitAttackDistance(UnitType.COLLECTOR));
    		Util.UNITTYPE_TO_MAXDISTANCE.put(UnitType.WARRIOR, getGameConfiguration().getUnitAttackDistance(UnitType.WARRIOR));
    		Util.UNITTYPE_TO_MAXDISTANCE.put(UnitType.ARCHER, getGameConfiguration().getUnitAttackDistance(UnitType.ARCHER));
    	}
    	if(Util.UNITTYPE_TO_COSTS.isEmpty()){
    		Util.UNITTYPE_TO_COSTS.put(UnitType.COLLECTOR, getGameConfiguration().getUnitCost(UnitType.COLLECTOR));
    		Util.UNITTYPE_TO_COSTS.put(UnitType.WARRIOR, getGameConfiguration().getUnitCost(UnitType.WARRIOR));
    		Util.UNITTYPE_TO_COSTS.put(UnitType.ARCHER, getGameConfiguration().getUnitCost(UnitType.ARCHER));
    	}
    	if(Util.MY_BASE == null)
    		Util.MY_BASE = ROUNDSTATE.getBase();
    	Util.clearMyUnits();
    	for(Unit u : ROUNDSTATE.getOwnUnits())
    	{
    		MyUnit mu = new MyUnit(u);
    		Util.ALL_MYUNITS.add(mu);
    		Util.UNITTYPE_TO_MYUNITLIST.get(u.getUnitType()).add(mu);
    	}
    	Util.getEnemyPositions();
    }
}
