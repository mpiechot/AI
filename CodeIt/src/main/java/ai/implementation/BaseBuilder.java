package ai.implementation;

import ai.implementation.util.Util;
import de.itdesign.codebattle.api.model.Base;
import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Field;
import de.itdesign.codebattle.api.model.FieldType;

public class BaseBuilder 
{
	private Base myBase;

	public void checkBaseBuilding(ClientRoundState roundState)
	{
		myBase = roundState.getBase();
		
		if(myBase.getStoredResources() >= myBase.getUpgradeCosts())
		{
			int ownUnits = roundState.getOwnUnits().size();
			boolean build = true;
			double fieldSize =  roundState.getWidth()*roundState.getHeight();
			double space = fieldSize;
			Field[][] field = roundState.getMap();
			for(int x=0;x<field.length;x++)
			{
				for(int y=0;y<field[x].length;y++)
				{
					if(!field[x][y].getType().equals(FieldType.LAND) || field[x][y].getUnitOnField() != null)
						space--;
				}
			}
			if(space/fieldSize < 0.3)
				return;
			if(Util.ALL_ENEMYUNITS.isEmpty() || myBase.getStoredResources() >= 2*myBase.getUpgradeCosts())
				myBase.upgradeCoinFactory();
		}
		
	}
}
